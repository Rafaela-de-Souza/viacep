package com.case_cep.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.case_cep.model.EnderecoResponse;

@SpringBootTest
class EnderecoServiceTest {

    @Autowired
    private EnderecoService enderecoService;

    @Test
    void testBuscarEnderecoValido() {
        EnderecoResponse response = enderecoService.buscarEndereco("01001000");
        assertNotNull(response);
        assertEquals("01001-000", response.getCep());
        assertEquals("praça da sé", response.getLogradouro());
        assertEquals("Sé", response.getBairro());
        assertEquals("São Paulo", response.getLocalidade());
        assertEquals("SP", response.getUf());
    }

    @Test
    void testBuscarEnderecoInvalido() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            enderecoService.buscarEndereco("123");
        });
        assertTrue(exception.getMessage().contains("8 dígitos"));
    }

    @Test
    void testBuscarEnderecoNaoEncontrado() {
        EnderecoService.EnderecoNotFoundException exception = assertThrows(EnderecoService.EnderecoNotFoundException.class, () -> {
            enderecoService.buscarEndereco("00000000");
        });
        assertTrue(exception.getMessage().contains("não encontrado"));
    }
}