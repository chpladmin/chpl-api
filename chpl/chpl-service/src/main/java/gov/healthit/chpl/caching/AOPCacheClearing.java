package gov.healthit.chpl.caching;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.EntityRetrievalException;
import net.sf.ehcache.CacheManager;

//@Component
//@Aspect
public class AOPCacheClearing {
//	private CacheManager manager;
//	
//	@Autowired private CacheInitializor cacheInitializor;
//	
//	@PostConstruct
//    public void init() {
//		manager = CacheManager.getInstance();
//    }
//	
//	@Before("@annotation(ClearAllCaches)")
//    public void beforeClearAllCachesMethod() {
//          manager.clearAll();
//    }
//	
//	@After("@annotation(ClearAllCaches)")
//	public void afterClearAllCachesMethod() throws IOException, EntityRetrievalException, InterruptedException {
//		cacheInitializor.initialize();
//	}
}
