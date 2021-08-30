package gov.healthit.chpl.caching.listing;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ListingCache {
    private Map<Long, ListingCacheItem> cache = new Hashtable<Long, ListingCacheItem>();
    private Long timeValidInHours;

    public ListingCache(@Value("${listingCache.timeValidInHours}") Long timeValidInHours) {
        this.timeValidInHours = timeValidInHours;
    }

    public Optional<CertifiedProductSearchDetails> get(Long listingId) {
        if (cache.containsKey(listingId)) {
            ListingCacheItem item = cache.get(listingId);
            if (!isCachedListingOld(item)) {
                LOGGER.info("Getting from cache: " + listingId);
                return Optional.of(item.getListing());
            }
        }
        LOGGER.info("Not in cache: " + listingId);
        return Optional.empty();
    }

    public void put(CertifiedProductSearchDetails listing) {
        LOGGER.info("Adding to cache: " + listing.getId());
        ListingCacheItem item = new ListingCacheItem(LocalDateTime.now(), listing);
        cache.put(listing.getId(), item);
    }

    @Scheduled(cron = "0 0/15 * * * *")
    public void cleanUp() {
        LOGGER.info("Cleaning ListingCache");
        Integer initSize = cache.entrySet().size();

        cache = cache.entrySet().stream()
                .filter(entry -> !isCachedListingOld(entry.getValue()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        LOGGER.info("Removed " + (initSize - cache.entrySet().size()) + " from the ListingCache");
    }

    private Boolean isCachedListingOld(ListingCacheItem item) {
        return ChronoUnit.HOURS.between(item.getTimeAdded(), LocalDateTime.now()) > timeValidInHours;
    }
}
