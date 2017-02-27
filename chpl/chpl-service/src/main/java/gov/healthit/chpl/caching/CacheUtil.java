package gov.healthit.chpl.caching;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import net.sf.ehcache.CacheManager;

@Component
public class CacheUtil {
	  public static CacheManager myCacheManager;
	 
	  public CacheManager getMyCacheManager() {
	    if(myCacheManager == null) {
	    for (CacheManager cacheManager : CacheManager.ALL_CACHE_MANAGERS) {
	       if(Pattern.compile(Pattern.quote("default"), Pattern.CASE_INSENSITIVE).matcher(cacheManager.getName()).find()) {
	          myCacheManager = cacheManager;
	       }
	    }
	    return myCacheManager;
	  }
	    else{
	    	return myCacheManager;
	    }
	}
}
