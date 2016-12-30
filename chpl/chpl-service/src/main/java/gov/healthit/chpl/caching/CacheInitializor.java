package gov.healthit.chpl.caching;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.EntityRetrievalException;
import net.sf.ehcache.CacheManager;

@Component
@Aspect
public class CacheInitializor {
	private static final Logger logger = LogManager.getLogger(CacheInitializor.class);
	private static final String DEFAULT_PROPERTIES_FILE = "environment.properties";
	private CacheManager manager;
	
	//@Autowired private AsynchronousCacheInitialization asynchronousCacheInitialization;

	  @PostConstruct
	  @Async
	  public void initialize() throws IOException, EntityRetrievalException, InterruptedException {
		  Properties props = new Properties();
		  InputStream in = CacheInitializor.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE);
		  
		  if (in == null) {
				props = null;
				throw new FileNotFoundException("Environment Properties File not found in class path.");
			} else {
				props = new Properties();
				props.load(in);
				in.close();
			}
		  
		  String enableCacheInitializationValue = props.getProperty("enableCacheInitialization").toString();
		  
		  try {
			if(enableCacheInitializationValue != null && enableCacheInitializationValue.equalsIgnoreCase("true")){
//				  asynchronousCacheInitialization.initializeSearchOptions();
//				  asynchronousCacheInitialization.initializePending();
//				  asynchronousCacheInitialization.initializeSearch();
//				  asynchronousCacheInitialization.initializeCertificationIdsGetAll();
//				  asynchronousCacheInitialization.initializeCertificationIdsGetAllWithProducts();
			  }
		} catch (Exception e) {
			System.out.println("Caching failed to initialize");
			e.printStackTrace();
		}
	  }
	  
	@Before("@annotation(ClearAllCaches)")
    public void beforeClearAllCachesMethod() {
		logger.info("Clearing all caches before @ClearAllCaches method execution.");
          manager.clearAll();
    }
		
	@After("@annotation(ClearAllCaches)")
	public void afterClearAllCachesMethod() throws IOException, EntityRetrievalException, InterruptedException {
		logger.info("Initializing all caches after @ClearAllCaches method execution.");
		//initialize();
	}
}
