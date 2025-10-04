package com.case_cep.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.case_cep.model.EnderecoResponse;

@Service
public class EnderecoService {

    private final WebClient webClient = WebClient.create("https://viacep.com.br/ws");
    private final EnderecoValidator enderecoValidator;

    public EnderecoService(EnderecoValidator enderecoValidator) {
        this.enderecoValidator = enderecoValidator;
    }

    public EnderecoResponse buscarEndereco(String endereco) {
            enderecoValidator.validarEnderecoUnico(endereco);
            String enderecoLimpo = enderecoValidator.limparEndereco(endereco);

            try {
                EnderecoResponse response = webClient.get()
                        .uri("/{cep}/json/", enderecoLimpo)
                        .retrieve()
                        .bodyToMono(EnderecoResponse.class)
                        .block();

                if (response == null || response.getCep() == null || response.getLogradouro() == null) {
                    throw new EnderecoNotFoundException("Endereço não encontrado.");
                }

                response.setLogradouro(response.getLogradouro().toLowerCase());
                return response;
            } catch (EnderecoNotFoundException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("Erro ao consultar o serviço ViaCEP.");
            }
    }

        public static class EnderecoNotFoundException extends RuntimeException {
            public EnderecoNotFoundException(String message) {
                super(message);
            }
        }
}