package gov.healthit.chpl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.rules.ExternalResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import net.sf.ehcache.CacheManager;

@Service
public class UnitTestRules extends ExternalResource {
	private static final Logger logger = LogManager.getLogger(UnitTestRules.class);
	private final CacheManager cacheManager;
	
	@Autowired
	public UnitTestRules(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
	
	@Override
	protected void before() {
		logger.info("Clearing all caches before running @Test");
		cacheManager.clearAll();
		logger.info("Setting security context authentication to null before running @Test");
		SecurityContextHolder.getContext().setAuthentication(null);
	}
}
