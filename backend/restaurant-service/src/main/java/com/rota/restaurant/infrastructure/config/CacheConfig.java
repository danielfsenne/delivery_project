package com.rota.restaurant.infrastructure.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rota.restaurant.application.cache.CachedPage;
import com.rota.restaurant.application.cache.CatalogCaches;
import com.rota.restaurant.interfaces.rest.dto.RestaurantDetailResponse;
import com.rota.restaurant.interfaces.rest.dto.RestaurantSummaryResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.BatchStrategies;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

import java.time.Duration;
import java.util.Map;

/**
 * Cache do catálogo no Redis. Cada cache tem um serializador JSON tipado (sem nomes de classe
 * gravados no valor) e o gerenciador é transacional: put e evict só acontecem após o commit,
 * evitando que uma leitura concorrente grave no cache um dado que ainda não foi confirmado.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper,
                                   @Value("${rota.cache.catalog-ttl:60s}") Duration ttl) {
        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .prefixCacheNameWith("rota:")
                .disableCachingNullValues();

        JavaType searchType = objectMapper.getTypeFactory()
                .constructParametricType(CachedPage.class, RestaurantSummaryResponse.class);
        JavaType detailType = objectMapper.getTypeFactory().constructType(RestaurantDetailResponse.class);

        return RedisCacheManager.builder(
                        RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory, BatchStrategies.scan(200)))
                .cacheDefaults(base)
                .withInitialCacheConfigurations(Map.of(
                        CatalogCaches.SEARCH, base.serializeValuesWith(json(objectMapper, searchType)),
                        CatalogCaches.DETAIL, base.serializeValuesWith(json(objectMapper, detailType))))
                .transactionAware()
                .enableStatistics()
                .build();
    }

    private static SerializationPair<Object> json(ObjectMapper objectMapper, JavaType type) {
        return SerializationPair.fromSerializer(new Jackson2JsonRedisSerializer<>(objectMapper, type));
    }
}
