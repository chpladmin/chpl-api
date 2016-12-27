package gov.healthit.chpl.caching;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.EntityRetrievalException;

@Component
public class CacheInitializor {
	private static final String DEFAULT_PROPERTIES_FILE = "environment.properties";
	@Autowired private CacheInitializorImpl cacheInitializorImpl;

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
				  cacheInitializorImpl.initializeCaches();
			  }
		} catch (Exception e) {
			System.out.println("Caching failed to initialize");
			e.printStackTrace();
		}
	  }
}
