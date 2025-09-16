package com.case_cep.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.case_cep.model.CepResponse;

@Service
public class CepService {

    private final WebClient webClient = WebClient.create("https://viacep.com.br/ws");

    public CepResponse buscarCep(String cep) {
            if (cep == null || !cep.matches("\\d{8}")) {
                throw new IllegalArgumentException("CEP inválido. Informe 8 dígitos numéricos.");
            }

            try {
                CepResponse response = webClient.get()
                        .uri("/{cep}/json/", cep)
                        .retrieve()
                        .bodyToMono(CepResponse.class)
                        .block();

                if (response == null || response.getCep() == null || response.getLogradouro() == null) {
                    throw new CepNotFoundException("CEP não encontrado.");
                }

                response.setLogradouro(response.getLogradouro().toLowerCase());
                return response;
            } catch (CepNotFoundException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("Erro ao consultar o serviço ViaCEP.");
            }
    }

        public static class CepNotFoundException extends RuntimeException {
            public CepNotFoundException(String message) {
                super(message);
            }
        }
}
