package com.blueant_crm_erp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.data.redis.url:}")
    private String redisUrl;

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RedisConfig.class);

    /**
     * Redis connection using Lettuce (non-blocking, thread-safe)
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        boolean useSsl = false;

        if (redisUrl != null && !redisUrl.trim().isEmpty()) {
            log.info("Configuring LettuceConnectionFactory using Redis URL from environment");
            try {
                java.net.URI uri = new java.net.URI(redisUrl);
                config.setHostName(uri.getHost());
                config.setPort(uri.getPort() >= 0 ? uri.getPort() : 6379);
                String userInfo = uri.getUserInfo();
                if (userInfo != null) {
                    int colon = userInfo.indexOf(':');
                    String pwd = colon >= 0 ? userInfo.substring(colon + 1) : userInfo;
                    config.setPassword(org.springframework.data.redis.connection.RedisPassword.of(pwd));
                }
                if (redisUrl.startsWith("rediss://")) {
                    useSsl = true;
                }
            } catch (Exception e) {
                log.error("Failed to parse Redis URL: {}", redisUrl, e);
                throw new IllegalArgumentException("Invalid Redis URL: " + redisUrl, e);
            }
        } else {
            log.info("Configuring LettuceConnectionFactory using individual host and port configurations");
            config.setHostName(redisHost);
            config.setPort(redisPort);
            if (redisPassword != null && !redisPassword.trim().isEmpty()) {
                config.setPassword(org.springframework.data.redis.connection.RedisPassword.of(redisPassword));
            }
        }

        if (useSsl) {
            org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration clientConfig =
                    org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration.builder()
                            .useSsl()
                            .disablePeerVerification()
                            .build();
            return new LettuceConnectionFactory(config, clientConfig);
        } else {
            return new LettuceConnectionFactory(config);
        }
    }

    /**
     * RedisTemplate — used for manual Redis operations
     *
     * Used by:
     * - AuthModule: JWT blacklist after logout
     * - AuthModule: OTP storage with 5-min expiry
     * - MappingModule: SM → Leader cache
     * - AuthModule: Failed login attempt counter
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        template.afterPropertiesSet();

        return template;
    }

    /**
     * CacheManager — used with @Cacheable, @CacheEvict, @CachePut annotations
     *
     * Cache names and their TTLs:
     * - "sm-leader-mapping"  → 24 hours  (SM to Leader lookup)
     * - "user-details"       → 1 hour    (Spring Security UserDetails)
     * - "hierarchy"          → 24 hours  (org tree)
     * - "roles"              → 24 hours  (role-permission matrix)
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofHours(24))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("sm-leader-mapping",
                defaultConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigs.put("user-details",
                defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigs.put("hierarchy",
                defaultConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigs.put("roles",
                defaultConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigs.put("dashboards",
                defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("analytics",
                defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}
