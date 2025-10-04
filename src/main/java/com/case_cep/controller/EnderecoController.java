
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

import com.case_cep.model.CepResponse;
import com.case_cep.model.ConsultaCep;
import com.case_cep.service.CepService;
import com.case_cep.service.ExcelExportService;

@RestController
@RequestMapping("/enderecos")
public class EnderecoController {

    private final CepService cepService;
    private final ExcelExportService excelExportService;

    public EnderecoController(CepService cepService, ExcelExportService excelExportService) {
        this.cepService = cepService;
        this.excelExportService = excelExportService;
    }

    @GetMapping
    public ResponseEntity<?> consultarEnderecos(@RequestParam(required = false) String cep, 
                                                @RequestParam(required = false) List<String> ceps) {
        validarParametros(cep, ceps);
        
        if (cep != null && !cep.isEmpty()) {
            return consultarCepUnico(cep);
        }
        
        return consultarMultiplosCeps(ceps);
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> exportarExcel(@RequestParam(required = false) String cep, 
                                               @RequestParam(required = false) List<String> ceps) throws IOException {
        validarParametros(cep, ceps);
        
        List<ConsultaCep> consultas = coletarConsultas(cep, ceps);
        
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
    
    private void validarParametros(String cep, List<String> ceps) {
        if ((cep == null || cep.isEmpty()) && (ceps == null || ceps.isEmpty())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "É necessário fornecer 'cep' para consulta única ou 'ceps' para consulta múltipla"
            );
        }
    }
    
    private ResponseEntity<CepResponse> consultarCepUnico(String cep) {
        try {
            CepResponse response = cepService.buscarCep(cep);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (CepService.CepNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE, 
                "Erro ao consultar serviço externo. Tente novamente mais tarde."
            );
        }
    }
    
    private ResponseEntity<Map<String, Object>> consultarMultiplosCeps(List<String> ceps) {
        List<CepResponse> encontrados = new ArrayList<>();
        List<String> naoEncontrados = new ArrayList<>();
        
        for (String cepItem : ceps) {
            try {
                CepResponse resp = cepService.buscarCep(cepItem);
                if (resp != null) {
                    encontrados.add(resp);
                } else {
                    naoEncontrados.add(cepItem);
                }
            } catch (Exception e) {
                naoEncontrados.add(cepItem);
            }
        }
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("encontrados", encontrados);
        resultado.put("naoEncontrados", naoEncontrados);
        return ResponseEntity.ok(resultado);
    }
    
    private List<ConsultaCep> coletarConsultas(String cep, List<String> ceps) {
        List<ConsultaCep> consultas = new ArrayList<>();
        AtomicLong contador = new AtomicLong(1);
        
        if (cep != null && !cep.isEmpty()) {
            adicionarConsultaSeValida(cep, consultas, contador);
        }
        
        if (ceps != null && !ceps.isEmpty()) {
            for (String cepItem : ceps) {
                adicionarConsultaSeValida(cepItem, consultas, contador);
            }
        }
        
        return consultas;
    }
    
    private void adicionarConsultaSeValida(String cep, List<ConsultaCep> consultas, AtomicLong contador) {
        try {
            CepResponse response = cepService.buscarCep(cep);
            if (response != null) {
                ConsultaCep consultaCep = criarConsultaCep(response, contador.getAndIncrement());
                consultas.add(consultaCep);
            }
        } catch (Exception e) {
            // CEP inválido ou erro na consulta - continua sem adicionar
        }
    }
    
    private ConsultaCep criarConsultaCep(CepResponse response, long id) {
        ConsultaCep consultaCep = new ConsultaCep();
        consultaCep.setId(id);
        consultaCep.setCep(response.getCep());
        consultaCep.setLogradouro(response.getLogradouro());
        consultaCep.setLocalidade(response.getLocalidade());
        consultaCep.setUf(response.getUf());
        consultaCep.setDataConsulta(LocalDateTime.now());
        return consultaCep;
    }
}