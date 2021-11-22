package gov.healthit.chpl.service.realworldtesting;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.service.RealWorldTestingEligibility;
import lombok.extern.log4j.Log4j2;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

@Component("rwtEligibilityCachingService")
@Log4j2
public class RealWorldTestingEligiblityCachingService {
    private CertifiedProductDAO certifiedProductDao;
    private RealWorldTestingEligiblityServiceFactory rwtEligibilityServiceFactory;

    @Autowired
    public RealWorldTestingEligiblityCachingService(CertifiedProductDAO certifiedProductDao,
            RealWorldTestingEligiblityServiceFactory rwtEligibilityServiceFactory) {
        this.certifiedProductDao = certifiedProductDao;
        this.rwtEligibilityServiceFactory = rwtEligibilityServiceFactory;
    }

    @Cacheable(CacheNames.RWT_ELIGIBILITY)
    public RealWorldTestingEligibility getRwtEligibility(Long listingId) {
        RealWorldTestingEligiblityService rwtService = rwtEligibilityServiceFactory.getInstance();
        return getRwtEligibility(listingId, rwtService);
    }

    @CachePut(CacheNames.RWT_ELIGIBILITY)
    public RealWorldTestingEligibility calculateRwtEligibility(Long listingId) {
        RealWorldTestingEligiblityService rwtService = rwtEligibilityServiceFactory.getInstance();
        return getRwtEligibility(listingId, rwtService);
    }

    @CacheEvict(value = { CacheNames.COLLECTIONS_LISTINGS, CacheNames.COLLECTIONS_SEARCH }, allEntries = true)
    public void populateRwtEligibilityCache() {
        Ehcache rwtCache = getRwtEligibilityCache();
        LOGGER.info("Clearing the RWT Eligibility cache.");
        rwtCache.removeAll();

        LOGGER.info("Getting all 2015 listings");
        List<Long> listingIds =
                certifiedProductDao.findIdsByEdition(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
        LOGGER.info("Got " + listingIds.size() + " 2015 listings");

        RealWorldTestingEligiblityService rwtService = rwtEligibilityServiceFactory.getInstance();
        LOGGER.info("Fetching all RWT Eligibility data.");
        listingIds.parallelStream()
            .forEach(listingId -> getRwtEligibilityAndAddToCache(listingId, rwtService, rwtCache));
    }

    private void getRwtEligibilityAndAddToCache(Long listingId, RealWorldTestingEligiblityService rwtService,
            Ehcache rwtCache) {
        RealWorldTestingEligibility rwtEligibility = getRwtEligibility(listingId, rwtService);
        rwtCache.put(new Element(listingId, rwtEligibility));
    }

    private RealWorldTestingEligibility getRwtEligibility(Long listingId, RealWorldTestingEligiblityService rwtService) {
        Date start = new Date();
        RealWorldTestingEligibility rwtEligibility = rwtService.getRwtEligibilityYearForListing(listingId, LOGGER);
        Date end = new Date();
        LOGGER.info(String.format("ListingId: %s, Elig Year %s, %s [ %s ms ]",
                listingId,
                rwtEligibility.getEligibilityYear() != null ? rwtEligibility.getEligibilityYear().toString() : "N/A",
                        rwtEligibility.getReason().getReason(),
                (end.getTime() - start.getTime())));
        return rwtEligibility;
    }

    private Ehcache getRwtEligibilityCache() {
        CacheManager manager = CacheManager.getInstance();
        Ehcache cache = manager.getEhcache(CacheNames.RWT_ELIGIBILITY);
        return cache;
    }
}
