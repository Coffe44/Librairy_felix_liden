package com.example.Library_Felix_liden.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = true)
public class CacheConfig {

    @Bean
    RedisCacheManager cacheManager(
            RedisConnectionFactory redisConnectionFactory,
            @Value("${app.cache.read-ttl:5m}") Duration readTtl,
            @Value("${app.cache.book-by-id-ttl:10m}") Duration bookByIdTtl) {
        RedisCacheConfiguration defaultConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(readTtl)
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        GenericJacksonJsonRedisSerializer.builder().build()))
                .disableCachingNullValues();

        RedisCacheConfiguration bookByIdConfiguration = defaultConfiguration.entryTtl(bookByIdTtl);

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfiguration)
                .withCacheConfiguration("booksById", bookByIdConfiguration)
                .transactionAware()
                .build();
    }
}
