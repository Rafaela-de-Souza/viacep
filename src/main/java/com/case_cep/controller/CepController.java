
package com.case_cep.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.case_cep.model.CepResponse;
import com.case_cep.model.ConsultaCep;
import com.case_cep.service.CepService;
import com.case_cep.service.ExcelExportService;

@RestController
@RequestMapping("/cep")
public class CepController {

    private final CepService cepService;
    private final ExcelExportService excelExportService;

    public CepController(CepService cepService, ExcelExportService excelExportService) {
        this.cepService = cepService;
        this.excelExportService = excelExportService;
    }

    @GetMapping("/{cep}")
        public CepResponse buscarCep(@PathVariable String cep) {
            try {
                return cepService.buscarCep(cep);
            } catch (IllegalArgumentException e) {
                throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, e.getMessage()
                );
            } catch (CepService.CepNotFoundException e) {
                throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, e.getMessage()
                );
            } catch (Exception e) {
                throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "Erro ao consultar serviço externo. Tente novamente mais tarde."
                );
            }
    }
    @GetMapping("/consultas/excel")
    public ResponseEntity<byte[]> exportarConsultasExcel() throws IOException {
        List<ConsultaCep> consultas = List.of();
        byte[] arquivo = excelExportService.exportarConsultasParaExcel(consultas);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=consultas-ceps.xlsx")
                .body(arquivo);
    }

    @GetMapping("/lista")
    public ResponseEntity<?> consultarLista(@RequestParam List<String> ceps) {
        List<CepResponse> encontrados = new java.util.ArrayList<>();
        List<String> naoEncontrados = new java.util.ArrayList<>();
        for (String cep : ceps) {
            try {
                CepResponse resp = cepService.buscarCep(cep);
                if (resp != null) {
                    encontrados.add(resp);
                } else {
                    naoEncontrados.add(cep);
                }
            } catch (Exception e) {
                naoEncontrados.add(cep);
            }
        }
        java.util.Map<String, Object> resultado = new java.util.HashMap<>();
        resultado.put("encontrados", encontrados);
        resultado.put("naoEncontrados", naoEncontrados);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/excel/lista")
    public ResponseEntity<byte[]> exportarListaExcel(@RequestParam List<String> ceps) throws IOException {
        List<CepResponse> resultados = ceps.stream()
            .map(cepService::buscarCep)
            .collect(Collectors.toList());
        List<ConsultaCep> consultas = new java.util.ArrayList<>();
        java.util.concurrent.atomic.AtomicLong contador = new java.util.concurrent.atomic.AtomicLong(1);
        for (CepResponse r : resultados) {
            ConsultaCep c = new ConsultaCep();
            c.setId(contador.getAndIncrement()); 
            c.setCep(r.getCep());
            c.setLogradouro(r.getLogradouro());
            c.setLocalidade(r.getLocalidade());
            c.setUf(r.getUf());
            c.setDataConsulta(java.time.LocalDateTime.now());
            consultas.add(c);
        }
        if (consultas.isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "Nenhum CEP válido encontrado. O arquivo não foi gerado."
            );
        }
        byte[] arquivo = excelExportService.exportarConsultasParaExcel(consultas);
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=ceps.xlsx")
            .body(arquivo);
    }
}
