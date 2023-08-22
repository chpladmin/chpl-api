package gov.healthit.chpl.util;

import java.util.List;

import org.redisson.RedissonMap;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class RedisUtil {

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
