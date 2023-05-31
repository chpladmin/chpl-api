package gov.healthit.chpl.util;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
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
        ScanOptions opts = ScanOptions.scanOptions()
                .match(ChplCacheConfig.CACHE_NAME_PREFIX + cache.getName() + "*")
                .build();
        Cursor cursor = redisTemplate.scan(opts);
        return cursor.stream()
                .map(redisKey -> Long.valueOf(((String)redisKey).split("::")[1]))
                .toList();

//        return redisTemplate.keys(ChplCacheConfig.CACHE_NAME_PREFIX + cache.getName() + "*").parallelStream()
//                .map(redisKey -> Long.valueOf(redisKey.split("::")[1]))
//                .toList();
    }
}
