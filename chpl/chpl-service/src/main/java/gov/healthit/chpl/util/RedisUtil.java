package gov.healthit.chpl.util;

import java.util.List;

import org.redisson.RedissonMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class RedisUtil {
    private RedissonClient redissonClient;

    @Autowired
    public RedisUtil(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public List<Long> getAllKeysForCacheAsLong(Cache cache) {
        RedissonMap<String, Object> map = (RedissonMap) cache.getNativeCache();
        return map.readAllEntrySet().stream()
                .map(entry -> Long.valueOf(entry.getKey()))
                .toList();
    }

    public List<Object> getAllValuesForCache(Cache cache) {
        RedissonMap<String, Object> map = (RedissonMap) cache.getNativeCache();
        return map.readAllEntrySet().stream()
                .map(entry -> entry.getValue())
                .toList();
    }
}
