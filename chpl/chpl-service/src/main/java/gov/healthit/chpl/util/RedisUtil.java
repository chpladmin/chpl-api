package gov.healthit.chpl.util;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.ChplCacheConfig;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class RedisUtil {
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public RedisUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public List<Long> getAllKeysForCacheAsLong(Cache cache) {
        return redisTemplate.keys(ChplCacheConfig.CACHE_NAME_PREFIX + cache.getName() + "*").stream()
                .map(redisKey -> Long.valueOf(redisKey.split("::")[1]))
                .toList();
        //List<String> keys = redisTemplate.keys(ChplCacheConfig.CACHE_NAME_PREFIX + cache.getName() + "*").stream().toList();
        //LOGGER.info("Found {} keys in cache {}", keys.size(), cache.getName());
        //return keys;
    }
}
