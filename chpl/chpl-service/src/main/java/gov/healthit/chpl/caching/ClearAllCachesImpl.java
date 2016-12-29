package gov.healthit.chpl.caching;

import javax.annotation.PostConstruct;

import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
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
	
	@Around("@annotation(ClearAllCaches)")
    public void aroundClearAllCachesMethod() {
          manager.clearAll();
    }
}
