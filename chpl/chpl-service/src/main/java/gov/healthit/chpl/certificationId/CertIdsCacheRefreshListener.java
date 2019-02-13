package gov.healthit.chpl.certificationId;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.caching.CacheRefreshListener;
import gov.healthit.chpl.caching.updater.CertIdsCacheUpdater;
import gov.healthit.chpl.domain.SimpleCertificationId;
import gov.healthit.chpl.dto.CertificationIdDTO;
import gov.healthit.chpl.util.PropertyUtil;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * Listener that determines when to update the cache of certification IDs.
 * @author kekey
 *
 */
@Component
@Aspect
public class CertIdsCacheRefreshListener extends CacheRefreshListener {
    private static final Logger LOGGER = LogManager.getLogger(CertIdsCacheRefreshListener.class);
    @Autowired
    private CertIdsCacheUpdater cacheUpdater;

    /**
     * After a certification ID is created reload the certification ID cache in the background.
     * @param productIds list of ids used to create the certification ID
     * @param year the year of the certification ID
     */
    @AfterReturning(
            pointcut = "execution(* gov.healthit.chpl.manager.impl.CertificationIdManagerImpl.create(..)) && "
            + "args(productIds,year,..)")
    public void afterCertificationIdCreated(final List<Long> productIds, final String year) {
        LOGGER.debug("A certification ID was created. Refreshing certification ID caches. ");
        refreshCache();
    }

    protected void refreshCacheAsync() {
        cacheUpdater.refreshCacheAsync();
    }

    protected void refreshCacheSync() {
        cacheUpdater.refreshCacheSync();
    }
}
