package gov.healthit.chpl;

import java.util.HashMap;
import java.util.Map;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.CompositeCodec;
import org.redisson.codec.Kryo5Codec;
import org.redisson.config.Config;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import lombok.extern.log4j.Log4j2;

@Configuration
@EnableCaching
@Log4j2
public class ChplCacheConfig {
    public static final String CACHE_NAME_PREFIX = "chpl.";
    private static final Integer NETTY_THREADS = 64;

    @Autowired
    private Environment env;

    @Bean(destroyMethod = "shutdown")
    RedissonClient redisson() {
        Config config = new Config();
        config.setNettyThreads(NETTY_THREADS)
                .useSingleServer()
                .setAddress("redis://" + env.getProperty("spring.redis.host") + ":" + env.getProperty("spring.redis.port"))
                .setPassword(env.getProperty("spring.redis.password"));
        return Redisson.create(config);
    }

    @Bean
    CacheManager cacheManager(RedissonClient redissonClient) {
        Map<String, CacheConfig> config = new HashMap<>();
        CompositeCodec compositeCodec = new CompositeCodec(new StringCodec(), new Kryo5Codec());
        return new RedissonSpringCacheManager(redissonClient, config, compositeCodec);
    }
}
