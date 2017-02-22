package gov.healthit.chpl.caching;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

@Component
public class CacheReplacer {
	
	/** Removes all keys from the oldCache and puts all keys from the newCache into the oldCache
	 * 
	 * @param oldCache - the cache whose values will be replaced
	 * @param newCache - the cache whose keys will replace the values in the old cache
	 */
	public static void replaceCache(Cache oldCache, Cache newCache){
		List<Integer> keys = newCache.getKeys();
		Map<Object, Element> objects = newCache.getAll(keys);
		oldCache.removeAll();
		oldCache.putAll(objects.values());
	}
}
