package com.case_cep.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class EnderecoValidator {

    private static final int ENDERECO_LENGTH = 8;

    public void validarEnderecoUnico(String endereco) {
        List<String> erros = new ArrayList<>();
        
        if (endereco == null) {
            erros.add("Endereço não pode ser nulo");
        } else if (endereco.trim().isEmpty()) {
            erros.add("Endereço não pode estar vazio");
        } else {
            String enderecoLimpo = limparEndereco(endereco);
            if (!enderecoLimpo.matches("\\d+")) {
                erros.add("Endereço deve conter apenas números");
            } else if (enderecoLimpo.length() != ENDERECO_LENGTH) {
                erros.add("Endereço deve conter exatamente 8 dígitos");
            }
        }
        
        if (!erros.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", erros));
        }
    }

    public void validarListaEnderecos(List<String> enderecos) {
        if (enderecos == null || enderecos.isEmpty()) {
            throw new IllegalArgumentException("Lista de Endereços não pode ser nula ou vazia");
        }
        
        if (enderecos.size() > 50) {
            throw new IllegalArgumentException("Máximo de 50 Endereços permitido por consulta");
        }
        
        List<String> erros = new ArrayList<>();
        for (int i = 0; i < enderecos.size(); i++) {
            String endereco = enderecos.get(i);
            try {
                validarEnderecoUnico(endereco);
            } catch (IllegalArgumentException e) {
                erros.add("Endereço " + (i + 1) + ": " + e.getMessage());
            }
        }
        
        if (!erros.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", erros));
        }
    }

    public String limparEndereco(String endereco) {
        if (endereco == null) {
            return "";
        }
        return endereco.replaceAll("[^0-9]", "");
    }

    public boolean isEnderecoValido(String endereco) {
        try {
            validarEnderecoUnico(endereco);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}