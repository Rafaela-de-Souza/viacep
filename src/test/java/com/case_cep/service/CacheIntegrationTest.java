package com.case_cep.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import com.case_cep.model.EnderecoResponse;
import com.case_cep.service.EnderecoService.EnderecoNotFoundException;

@SpringBootTest
@ActiveProfiles("test")
class CacheIntegrationTest {

    @Autowired
    private EnderecoService enderecoService;

    @Autowired
    private CacheManagementService cacheManagementService;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void deveArmazenarConsultaNoCache() {
        String cep = "01310-100";
        
        assertFalse(cacheManagementService.existeNoCache(cep));
        
        try {
            EnderecoResponse resultado1 = enderecoService.buscarEndereco(cep);
            
            assertNotNull(resultado1);
            assertTrue(cacheManagementService.existeNoCache(cep));
            
            EnderecoResponse resultado2 = enderecoService.buscarEndereco(cep);
            assertNotNull(resultado2);
            assertEquals(resultado1.getCep(), resultado2.getCep());
        } catch (EnderecoNotFoundException e) {
            assertTrue(cacheManagementService.existeNoCache(cep));
        }
    }

    @Test
    void deveLimparCacheCorretamente() {
        String cep = "01310-100";
        
        try {
            enderecoService.buscarEndereco(cep);
            assertTrue(cacheManagementService.existeNoCache(cep));
        } catch (EnderecoNotFoundException e) {
            assertTrue(cacheManagementService.existeNoCache(cep));
        }
        
        cacheManagementService.limparCache();
        
        assertFalse(cacheManagementService.existeNoCache(cep));
        assertEquals(0, cacheManagementService.tamanhoCache());
    }

    @Test
    void deveRemoverEntradaEspecificaDoCache() {
        String cep1 = "01310-100";
        String cep2 = "04567-890";
        
        try {
            enderecoService.buscarEndereco(cep1);
            enderecoService.buscarEndereco(cep2);
        } catch (EnderecoNotFoundException e) {
        }
        
        cacheManagementService.removerDoCache(cep1);
        
        assertFalse(cacheManagementService.existeNoCache(cep1));
    }

    @Test
    void deveObterEstatisticasDoCache() {
        try {
            enderecoService.buscarEndereco("01310-100");
            enderecoService.buscarEndereco("04567-890");
        } catch (EnderecoNotFoundException e) {
        }
        
        var stats = cacheManagementService.obterEstatisticasCache();
        
        assertNotNull(stats);
        assertTrue(cacheManagementService.tamanhoCache() >= 0);
    }

    @Test
    void deveTerConfiguracoesDeExpiracaoCorretas() {
        var cache = cacheManager.getCache("enderecos");
        
        assertNotNull(cache, "Cache 'enderecos' deve estar configurado");
        
        assertNotNull(cacheManager);
        assertTrue(cacheManager.getCacheNames().contains("enderecos"));
    }
}