
package com.case_cep.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
    public ResponseEntity<?> consultarEnderecos(@RequestParam(required = false) String endereco, 
                                                @RequestParam(required = false) List<String> enderecos) {
        validarParametros(endereco, enderecos);
        
        if (endereco != null && !endereco.isEmpty()) {
            return consultarEnderecoUnico(endereco);
        }
        
        return consultarMultiplosEnderecos(enderecos);
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> exportarExcel(@RequestParam(required = false) String endereco, 
                                               @RequestParam(required = false) List<String> enderecos) throws IOException {
        validarParametros(endereco, enderecos);
        
        List<ConsultaEndereco> consultas = coletarConsultas(endereco, enderecos);
        
        if (consultas.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Nenhum CEP válido encontrado. O arquivo não foi gerado."
            );
        }
        
        byte[] arquivo = excelExportService.exportarConsultasParaExcel(consultas);
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=enderecos.xlsx")
            .body(arquivo);
    }
    
    private void validarParametros(String endereco, List<String> enderecos) {
        if ((endereco == null || endereco.trim().isEmpty()) && (enderecos == null || enderecos.isEmpty())) {
            throw new IllegalArgumentException(
                "É necessário fornecer 'endereco' para consulta única ou 'enderecos' para consulta múltipla"
            );
        }
        
        if (endereco != null && !endereco.trim().isEmpty()) {
            enderecoValidator.validarEnderecoUnico(endereco);
        }
        
        if (enderecos != null && !enderecos.isEmpty()) {
            enderecoValidator.validarListaEnderecos(enderecos);
        }
    }
    
    private ResponseEntity<EnderecoResponse> consultarEnderecoUnico(String endereco) {
        EnderecoResponse response = enderecoService.buscarEndereco(endereco);
        return ResponseEntity.ok(response);
    }
    
    private ResponseEntity<Map<String, Object>> consultarMultiplosEnderecos(List<String> enderecos) {
        List<EnderecoResponse> encontrados = new ArrayList<>();
        List<String> naoEncontrados = new ArrayList<>();
        
        for (String enderecoItem : enderecos) {
            try {
                if (!enderecoValidator.isEnderecoValido(enderecoItem)) {
                    naoEncontrados.add(enderecoItem);
                    continue;
                }
                
                EnderecoResponse resp = enderecoService.buscarEndereco(enderecoItem);
                if (resp != null) {
                    encontrados.add(resp);
                } else {
                    naoEncontrados.add(enderecoItem);
                }
            } catch (Exception e) {
                naoEncontrados.add(enderecoItem);
            }
        }
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("encontrados", encontrados);
        resultado.put("naoEncontrados", naoEncontrados);
        return ResponseEntity.ok(resultado);
    }
    
    private List<ConsultaEndereco> coletarConsultas(String endereco, List<String> enderecos) {
        List<ConsultaEndereco> consultas = new ArrayList<>();
        AtomicLong contador = new AtomicLong(1);
        
        if (endereco != null && !endereco.isEmpty()) {
            adicionarConsultaSeValida(endereco, consultas, contador);
        }
        
        if (enderecos != null && !enderecos.isEmpty()) {
            for (String enderecoItem : enderecos) {
                adicionarConsultaSeValida(enderecoItem, consultas, contador);
            }
        }
        
        return consultas;
    }
    
    private void adicionarConsultaSeValida(String endereco, List<ConsultaEndereco> consultas, AtomicLong contador) {
        try {
            // Validação antes de tentar buscar
            if (!enderecoValidator.isEnderecoValido(endereco)) {
                return; // Endereço inválido - pula
            }
            
            EnderecoResponse response = enderecoService.buscarEndereco(endereco);
            if (response != null) {
                ConsultaEndereco consultaEndereco = criarConsultaEndereco(response, contador.getAndIncrement());
                consultas.add(consultaEndereco);
            }
        } catch (Exception e) {}
    }
    
    private ConsultaEndereco criarConsultaEndereco(EnderecoResponse response, long id) {
        ConsultaEndereco consultaEndereco = new ConsultaEndereco();
        consultaEndereco.setId(id);
        consultaEndereco.setCep(response.getCep());
        consultaEndereco.setLogradouro(response.getLogradouro());
        consultaEndereco.setLocalidade(response.getLocalidade());
        consultaEndereco.setUf(response.getUf());
        consultaEndereco.setDataConsulta(LocalDateTime.now());
        return consultaEndereco;
    }
}