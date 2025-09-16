package com.case_cep.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.case_cep.model.CepResponse;

@SpringBootTest
class CepServiceTest {

    @Autowired
    private CepService cepService;

    @Test
    void testBuscarCepValido() {
        CepResponse response = cepService.buscarCep("01001000");
        assertNotNull(response);
        assertEquals("01001-000", response.getCep());
        assertEquals("praça da sé", response.getLogradouro());
        assertEquals("Sé", response.getBairro());
        assertEquals("São Paulo", response.getLocalidade());
        assertEquals("SP", response.getUf());
    }

    @Test
    void testBuscarCepInvalido() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            cepService.buscarCep("123");
        });
        assertTrue(exception.getMessage().contains("CEP inválido"));
    }

    @Test
    void testBuscarCepNaoEncontrado() {
        Exception exception = assertThrows(CepService.CepNotFoundException.class, () -> {
            cepService.buscarCep("00000000");
        });
        assertTrue(exception.getMessage().contains("CEP não encontrado"));
    }
}
