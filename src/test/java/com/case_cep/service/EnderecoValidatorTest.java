package com.case_cep.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class EnderecoValidatorTest {

    private final EnderecoValidator enderecoValidator = new EnderecoValidator();

    @Test
    void testValidarEnderecoUnicoValido() {
        assertDoesNotThrow(() -> enderecoValidator.validarEnderecoUnico("01001000"));
        assertDoesNotThrow(() -> enderecoValidator.validarEnderecoUnico("12345678"));
    }

    @Test
    void testValidarEnderecoUnicoNulo() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            enderecoValidator.validarEnderecoUnico(null);
        });
        assertTrue(exception.getMessage().contains("não pode ser nulo"));
    }

    @Test
    void testValidarEnderecoUnicoVazio() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            enderecoValidator.validarEnderecoUnico("");
        });
        assertTrue(exception.getMessage().contains("não pode estar vazio"));
    }

    @Test
    void testValidarEnderecoUnicoTamanhoInvalido() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            enderecoValidator.validarEnderecoUnico("123");
        });
        assertTrue(exception.getMessage().contains("exatamente 8 dígitos"));
    }

    @Test
    void testValidarEnderecoUnicoComLetras() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            enderecoValidator.validarEnderecoUnico("0100a000");
        });
        // Após limpar "0100a000" vira "0100000" (7 dígitos), então falha no tamanho
        assertTrue(exception.getMessage().contains("exatamente 8 dígitos"));
    }

    @Test
    void testValidarEnderecoUnicoSomenteLetras() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            enderecoValidator.validarEnderecoUnico("abcdefgh");
        });
        // Após limpar "abcdefgh" vira "" (vazio), então falha na validação de números
        assertTrue(exception.getMessage().contains("apenas números"));
    }

    @Test
    void testValidarListaEnderecosValida() {
        List<String> enderecos = List.of("01001000", "12345678", "87654321");
        assertDoesNotThrow(() -> enderecoValidator.validarListaEnderecos(enderecos));
    }

    @Test
    void testValidarListaEnderecosNula() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            enderecoValidator.validarListaEnderecos(null);
        });
        assertTrue(exception.getMessage().contains("não pode ser nula"));
    }

    @Test
    void testValidarListaEnderecosVazia() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            enderecoValidator.validarListaEnderecos(List.of());
        });
        assertTrue(exception.getMessage().contains("não pode ser nula ou vazia"));
    }

    @Test
    void testValidarListaEnderecosMuitosItens() {
        List<String> enderecosMuitos = new java.util.ArrayList<>();
        for (int i = 0; i < 51; i++) {
            enderecosMuitos.add("0100100" + (i % 10));
        }
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            enderecoValidator.validarListaEnderecos(enderecosMuitos);
        });
        assertTrue(exception.getMessage().contains("Máximo de 50 Endereços"));
    }

    @Test
    void testLimparEndereco() {
        assertEquals("01001000", enderecoValidator.limparEndereco("01001-000"));
        assertEquals("12345678", enderecoValidator.limparEndereco("12.345-678"));
        assertEquals("87654321", enderecoValidator.limparEndereco("87654321"));
        assertEquals("", enderecoValidator.limparEndereco(null));
    }

    @Test
    void testIsEnderecoValido() {
        assertTrue(enderecoValidator.isEnderecoValido("01001000"));
        assertTrue(enderecoValidator.isEnderecoValido("12345678"));
        assertFalse(enderecoValidator.isEnderecoValido(null));
        assertFalse(enderecoValidator.isEnderecoValido(""));
        assertFalse(enderecoValidator.isEnderecoValido("123"));
        assertFalse(enderecoValidator.isEnderecoValido("0100a000"));
    }

    @Test
    void testCenarioToleranciaErros() {
        // Testa CEPs com diferentes tipos de problemas para documentar comportamento
        // Este teste documenta como o endpoint deve se comportar com CEPs problemáticos
        
        // CEPs válidos após limpeza
        assertTrue(enderecoValidator.isEnderecoValido(enderecoValidator.limparEndereco("01001-000")));
        assertTrue(enderecoValidator.isEnderecoValido(enderecoValidator.limparEndereco("12.345-678")));
        
        // CEPs inválidos mesmo após limpeza
        assertFalse(enderecoValidator.isEnderecoValido(enderecoValidator.limparEndereco("123")));
        assertFalse(enderecoValidator.isEnderecoValido(enderecoValidator.limparEndereco("abc12345")));
        assertFalse(enderecoValidator.isEnderecoValido(enderecoValidator.limparEndereco("")));
        assertFalse(enderecoValidator.isEnderecoValido(enderecoValidator.limparEndereco(null)));
    }
}