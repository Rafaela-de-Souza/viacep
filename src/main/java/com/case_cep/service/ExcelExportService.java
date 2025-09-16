package com.case_cep.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.case_cep.model.ConsultaCep;

@Service
public class ExcelExportService {

    public byte[] exportarConsultasParaExcel(List<ConsultaCep> consultas) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Consultas CEP");

            Row header = sheet.createRow(0);
            String[] colunas = {"ID", "CEP", "Logradouro", "Localidade", "UF", "Data Consulta"};

            for (int i = 0; i < colunas.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(colunas[i]);
            }

            int rowNum = 1;
            for (ConsultaCep c : consultas) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(c.getId());
                row.createCell(1).setCellValue(c.getCep());
                row.createCell(2).setCellValue(c.getLogradouro());
                row.createCell(3).setCellValue(c.getLocalidade());
                row.createCell(4).setCellValue(c.getUf());
                row.createCell(5).setCellValue(c.getDataConsulta().toString());
            }

            for (int i = 0; i < colunas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}
