package gov.healthit.chpl.caching;

import javax.annotation.PostConstruct;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import net.sf.ehcache.CacheManager;

@Component
@Aspect
public class ClearAllCachesImpl {
	private CacheManager manager;
	
	@PostConstruct
    public void init() {
		manager = CacheManager.getInstance();
    }
	
	@Before("@annotation(ClearAllCaches)")
    public void beforeClearAllCachesMethod() {
          manager.clearAll();
    }
}
