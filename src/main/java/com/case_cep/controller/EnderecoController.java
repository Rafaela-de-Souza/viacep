
package com.case_cep.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.case_cep.model.ConsultaEndereco;
import com.case_cep.model.EnderecoResponse;
import com.case_cep.service.EnderecoService;
import com.case_cep.service.EnderecoValidator;
import com.case_cep.service.ExcelExportService;

@RestController
@RequestMapping("/enderecos")
public class EnderecoController {

    private final EnderecoService enderecoService;
    private final ExcelExportService excelExportService;
    private final EnderecoValidator enderecoValidator;

    public EnderecoController(EnderecoService enderecoService, ExcelExportService excelExportService, EnderecoValidator enderecoValidator) {
        this.enderecoService = enderecoService;
        this.excelExportService = excelExportService;
        this.enderecoValidator = enderecoValidator;
    }

    @GetMapping
    public ResponseEntity<?> consultarEnderecos(@RequestParam(value = "cep") List<String> ceps) {
        if (ceps == null || ceps.isEmpty()) {
            throw new IllegalArgumentException(
                "É necessário fornecer pelo menos um CEP usando o parâmetro 'cep'"
            );
        }
        
        if (ceps.size() > 50) {
            throw new IllegalArgumentException("Máximo de 50 CEPs por consulta");
        }
        
        return processarConsultaCeps(ceps);
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> exportarExcel(@RequestParam(value = "cep") List<String> ceps) throws IOException {
        if (ceps == null || ceps.isEmpty()) {
            throw new IllegalArgumentException(
                "É necessário fornecer pelo menos um CEP usando o parâmetro 'cep'"
            );
        }
        
        if (ceps.size() > 50) {
            throw new IllegalArgumentException("Máximo de 50 CEPs por consulta");
        }
        
        List<ConsultaEndereco> consultas = coletarConsultasComNovosParametros(ceps);
        
        byte[] arquivo = excelExportService.exportarConsultasParaExcel(consultas);
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=enderecos.xlsx")
            .body(arquivo);
    }
    
    private ResponseEntity<?> processarConsultaCeps(List<String> ceps) {
        List<EnderecoResponse> encontrados = new ArrayList<>();
        List<Map<String, String>> erros = new ArrayList<>();
        
        for (String cep : ceps) {
            try {
                String cepLimpo = enderecoValidator.limparEndereco(cep);
                
                if (!enderecoValidator.isEnderecoValido(cepLimpo)) {
                    Map<String, String> erro = new HashMap<>();
                    erro.put("cep", cep);
                    erro.put("motivo", determinarMotivoErro(cepLimpo));
                    erros.add(erro);
                    continue;
                }
                
                EnderecoResponse response = enderecoService.buscarEndereco(cepLimpo);
                encontrados.add(response);
                
            } catch (EnderecoService.EnderecoNotFoundException e) {
                Map<String, String> erro = new HashMap<>();
                erro.put("cep", cep);
                erro.put("motivo", "CEP não encontrado");
                erros.add(erro);
            } catch (Exception e) {
                Map<String, String> erro = new HashMap<>();
                erro.put("cep", cep);
                erro.put("motivo", "Erro interno: " + e.getMessage());
                erros.add(erro);
            }
        }

        if (ceps.size() == 1 && encontrados.size() == 1 && erros.isEmpty()) {
            return ResponseEntity.ok(encontrados.get(0));
        }
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("encontrados", encontrados);
        resultado.put("erros", erros);
        resultado.put("total", ceps.size());
        resultado.put("sucessos", encontrados.size());
        resultado.put("falhas", erros.size());
        
        return ResponseEntity.ok(resultado);
    }
    
    private String determinarMotivoErro(String cep) {
        if (cep == null || cep.trim().isEmpty()) {
            return "CEP vazio ou contém apenas caracteres não numéricos";
        }
        if (cep.length() != 8) {
            return "CEP deve ter exatamente 8 dígitos";
        }
        return "CEP com formato inválido";
    }
    
    private List<ConsultaEndereco> coletarConsultasComNovosParametros(List<String> ceps) {
        List<ConsultaEndereco> consultas = new ArrayList<>();
        AtomicLong contador = new AtomicLong(1);
        
        for (String cep : ceps) {
            adicionarConsultaSeValida(cep, consultas, contador);
        }
        
        return consultas;
    }
    
    private void adicionarConsultaSeValida(String endereco, List<ConsultaEndereco> consultas, AtomicLong contador) {
        ConsultaEndereco consultaEndereco = new ConsultaEndereco();
        consultaEndereco.setId(contador.getAndIncrement());
        consultaEndereco.setCep(endereco); 
        consultaEndereco.setDataConsulta(LocalDateTime.now());
        
        try {
            String cepLimpo = enderecoValidator.limparEndereco(endereco);
            
            if (enderecoValidator.isEnderecoValido(cepLimpo)) {
                EnderecoResponse response = enderecoService.buscarEndereco(cepLimpo);
                if (response != null) {
                    consultaEndereco.setCep(response.getCep());
                    consultaEndereco.setLogradouro(response.getLogradouro());
                    consultaEndereco.setLocalidade(response.getLocalidade());
                    consultaEndereco.setUf(response.getUf());
                } 
            }
        } catch (Exception e) {}
        
        
        consultas.add(consultaEndereco);
    }
    
}