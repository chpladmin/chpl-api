package gov.healthit.chpl.caching;

import org.junit.rules.ExternalResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.sf.ehcache.CacheManager;

@Service
public class CacheInvalidationRule extends ExternalResource {
	private final CacheManager cacheManager;
	
	@Autowired
	public CacheInvalidationRule(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
	
	@Override
	protected void before() {
		System.out.println("Clearing all caches before running @Test");
		cacheManager.clearAll();
	}
}
