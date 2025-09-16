package com.case_cep.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.case_cep.model.ConsultaCep;

class ExcelExportServiceTest {

    private final ExcelExportService excelExportService = new ExcelExportService();

    @Test
    void testExportarConsultasParaExcel() throws IOException {
        ConsultaCep consulta = new ConsultaCep();
        consulta.setId(1L);
        consulta.setCep("01001-000");
        consulta.setLogradouro("praça da sé");
        consulta.setLocalidade("são paulo");
        consulta.setUf("sp");
        consulta.setDataConsulta(LocalDateTime.now());

        List<ConsultaCep> consultas = List.of(consulta);
        byte[] arquivo = excelExportService.exportarConsultasParaExcel(consultas);
        assertNotNull(arquivo);
        assertTrue(arquivo.length > 0);
    }
}
