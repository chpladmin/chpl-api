package gov.healthit.chpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import lombok.extern.log4j.Log4j2;

@Configuration
@EnableCaching
@Log4j2
public class ChplCacheConfig {

    @Autowired
    private Environment env;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        LOGGER.info("Creating LettuceConnectionFactory");
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(env.getProperty("spring.redis.host"));
        redisStandaloneConfiguration.setPort(Integer.valueOf(env.getProperty("spring.redis.port")));
        redisStandaloneConfiguration.setPassword(RedisPassword.of(env.getProperty("spring.redis.password")));
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }


    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        LOGGER.info("Creating RedisCacheManager");

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .prefixCacheNameWith("chpl.")
                .disableCachingNullValues();

        RedisCacheManager manager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();

        return manager;
    }
}
