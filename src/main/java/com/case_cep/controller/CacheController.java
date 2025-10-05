package com.case_cep.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.case_cep.service.CacheManagementService;
import com.github.benmanes.caffeine.cache.stats.CacheStats;

@RestController
@RequestMapping("/api/cache")
public class CacheController {

    private final CacheManagementService cacheManagementService;

    public CacheController(CacheManagementService cacheManagementService) {
        this.cacheManagementService = cacheManagementService;
    }

    @DeleteMapping("/limpar")
    public ResponseEntity<Map<String, String>> limparCache() {
        cacheManagementService.limparCache();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Cache limpo com sucesso");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/remover/{cep}")
    public ResponseEntity<Map<String, String>> removerDoCache(@PathVariable String cep) {
        cacheManagementService.removerDoCache(cep);
        Map<String, String> response = new HashMap<>();
        response.put("message", "CEP " + cep + " removido do cache");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> obterEstatisticas() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("tamanho", cacheManagementService.tamanhoCache());
        
        CacheStats cacheStats = cacheManagementService.obterEstatisticasCache();
        if (cacheStats != null) {
            stats.put("hitCount", cacheStats.hitCount());
            stats.put("missCount", cacheStats.missCount());
            stats.put("hitRate", String.format("%.2f%%", cacheStats.hitRate() * 100));
            stats.put("evictionCount", cacheStats.evictionCount());
        }
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/exists/{cep}")
    public ResponseEntity<Map<String, Object>> verificarCache(@PathVariable String cep) {
        boolean exists = cacheManagementService.existeNoCache(cep);
        Map<String, Object> response = new HashMap<>();
        response.put("cep", cep);
        response.put("emCache", exists);
        return ResponseEntity.ok(response);
    }
}