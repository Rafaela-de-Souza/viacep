package com.case_cep.service;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;

@Service
public class CacheManagementService {

    private final CacheManager cacheManager;

    public CacheManagementService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void limparCache() {
        var cache = cacheManager.getCache("enderecos");
        if (cache != null) {
            cache.clear();
        }
    }

    public void removerDoCache(String cep) {
        var cache = cacheManager.getCache("enderecos");
        if (cache != null) {
            cache.evict(cep);
        }
    }

    public CacheStats obterEstatisticasCache() {
        var cache = cacheManager.getCache("enderecos");
        if (cache != null) {
            var nativeCache = cache.getNativeCache();
            if (nativeCache instanceof Cache) {
                return ((Cache<?, ?>) nativeCache).stats();
            }
        }
        return null;
    }

    public boolean existeNoCache(String cep) {
        var cache = cacheManager.getCache("enderecos");
        if (cache != null) {
            return cache.get(cep) != null;
        }
        return false;
    }

    public long tamanhoCache() {
        var cache = cacheManager.getCache("enderecos");
        if (cache != null) {
            var nativeCache = cache.getNativeCache();
            if (nativeCache instanceof Cache) {
                return ((Cache<?, ?>) nativeCache).estimatedSize();
            }
        }
        return 0;
    }
}