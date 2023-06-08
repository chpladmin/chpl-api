package gov.healthit.chpl.compliance.directreview;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.jfree.data.time.DateRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.comparator.CertificationStatusEventComparator;
import gov.healthit.chpl.domain.compliance.DirectReview;
import gov.healthit.chpl.domain.compliance.DirectReviewContainer;
import gov.healthit.chpl.domain.compliance.DirectReviewNonConformity;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.util.RedisUtil;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import one.util.streamex.StreamEx;

@Component("directReviewSearchService")
@NoArgsConstructor
@Log4j2
public class DirectReviewSearchService {
    private DirectReviewCachingService drCacheService;
    private CacheManager cacheManager;
    private RedisUtil redisUtil;

    @Autowired
    public DirectReviewSearchService(DirectReviewCachingService drCacheService, CacheManager cacheManager, RedisUtil redisUtil) {
        this.drCacheService = drCacheService;
        this.cacheManager = cacheManager;
        this.redisUtil = redisUtil;
    }

    public boolean doesCacheHaveAnyOkData() {
        //DRs are available if there is at least one developer with a 2xx http status code for it's dr list
        return drCacheService.doesCacheHaveAnyOkData();
    }

    public boolean areDirectReviewsLoading() {
        //if there are any entries in the cache with "null" http status then it's still loading
        List<DirectReviewContainer> drContainers = getAll();
        return CollectionUtils.isEmpty(drContainers)
                || drContainers.stream()
                    .filter(drResponse -> drResponse.getHttpStatus() == null)
                    .findAny().isPresent();
    }

    public List<DirectReviewContainer> getAll() {
        Cache drCache = getDirectReviewsCache();

        return  redisUtil.getAllKeysForCacheAsLong(drCache).stream()
                .map(key -> drCache.get(key).get())
                .filter(objValue -> objValue != null && (objValue instanceof DirectReviewContainer))
                .map(objValue -> (DirectReviewContainer) objValue)
                .toList();
    }

    public List<DirectReview> getDeveloperDirectReviews(Long developerId, Logger logger) {
        DirectReviewContainer drContainer = drCacheService.getDeveloperDirectReviewsFromCache(developerId, logger);
        if (CollectionUtils.isEmpty(drContainer.getDirectReviews())) {
            return new ArrayList<DirectReview>();
        }
        return drContainer.getDirectReviews();
    }

    /**
     * The set of direct reviews related to the listing includes those that have a
     * developer-associated listing with the same listing ID +
     * those that are for the listing's developer during a time when a 2015 listing was active
     * but do not have any developer-associated listings.
     */
    public List<DirectReview> getDirectReviewsRelatedToListing(Long listingId, Long developerId, String editionYear,
            List<CertificationStatusEvent> statusEvents, Logger logger) {
        List<DirectReview> drs = new ArrayList<DirectReview>();
        drs.addAll(getDirectReviewsWithDeveloperAssociatedListingId(listingId, developerId));

        if (!StringUtils.isEmpty(editionYear) && editionYear.equals(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear())) {
            drs.addAll(getDeveloperDirectReviewsWithoutAssociatedListings(developerId, statusEvents, logger));
        }

        drs = StreamEx.of(drs)
            .distinct(DirectReview::getJiraKey)
            .collect(Collectors.toList());

        return drs;
    }

    private List<DirectReview> getDeveloperDirectReviewsWithoutAssociatedListings(Long developerId,
            List<CertificationStatusEvent> statusEvents, Logger logger) {
        List<DirectReview> allDeveloperDirectReviews = getDeveloperDirectReviews(developerId, logger);
        List<DirectReview> drsWithoutAssociatedListings = Stream.of(
                allDeveloperDirectReviews.stream()
                .filter(dr -> dr.getNonConformities() == null || dr.getNonConformities().size() == 0)
                .collect(Collectors.toList()),
                allDeveloperDirectReviews.stream()
                .filter(dr -> hasNoDeveloperAssociatedListings(dr.getNonConformities()))
                .collect(Collectors.toList()))
          .flatMap(List::stream)
          .collect(Collectors.toList());
        return drsWithoutAssociatedListings.stream()
            .filter(dr -> isOpenWhileListingIsActive(dr, statusEvents))
            .collect(Collectors.toList());
    }

    private boolean hasNoDeveloperAssociatedListings(List<DirectReviewNonConformity> ncs) {
        return ncs.stream()
            .filter(nc -> nc.getDeveloperAssociatedListings() == null || nc.getDeveloperAssociatedListings().size() == 0)
            .findAny()
            .isPresent();
    }

    private List<DirectReview> getDirectReviewsWithDeveloperAssociatedListingId(Long listingId, Long developerId) {
        DirectReviewContainer directReviewContainerForDeveloper =
                (DirectReviewContainer) cacheManager.getCache(CacheNames.DIRECT_REVIEWS).get(developerId).get();

        return directReviewContainerForDeveloper.getDirectReviews().stream()
                .filter(dr -> isAssociatedWithListing(dr, listingId))
                .collect(Collectors.toList());
    }

    private boolean isAssociatedWithListing(DirectReview dr, Long listingId) {
        if (dr.getNonConformities() == null || dr.getNonConformities().size() == 0) {
            return false;
        }

        return dr.getNonConformities().stream()
            .filter(nc -> nc.getDeveloperAssociatedListings() != null && nc.getDeveloperAssociatedListings().size() > 0)
            .flatMap(nc -> nc.getDeveloperAssociatedListings().stream())
            .filter(devAssocListing -> devAssocListing.getId().equals(listingId))
            .findAny().isPresent();
    }

    private boolean isOpenWhileListingIsActive(DirectReview dr, List<CertificationStatusEvent> statusEvents) {
        if (dr.getStartDate() == null) {
            return false;
        }
        Date drStartDate = dr.getStartDate();
        Date drEndDate = dr.getEndDate() != null ? dr.getEndDate() : new Date();

        List<DateRange> activeDateRanges = getDateRangesWithActiveStatus(statusEvents);
        return activeDateRanges.stream()
            .filter(activeDates -> drStartDate.getTime() <= activeDates.getUpperMillis()
                    && drEndDate.getTime() >= activeDates.getLowerMillis())
            .findAny().isPresent();
    }

    private List<DateRange> getDateRangesWithActiveStatus(List<CertificationStatusEvent> listingStatusEvents) {
        List<String> activeStatuses = Stream.of(CertificationStatusType.Active.getName(),
                CertificationStatusType.SuspendedByAcb.getName(),
                CertificationStatusType.SuspendedByOnc.getName())
                .collect(Collectors.toList());
        listingStatusEvents.sort(new CertificationStatusEventComparator());
        return IntStream.range(0, listingStatusEvents.size())
            .filter(i -> listingStatusEvents.get(i) != null && listingStatusEvents.get(i).getStatus() != null
                && !StringUtils.isEmpty(listingStatusEvents.get(i).getStatus().getName()))
            .filter(i -> activeStatuses.contains(listingStatusEvents.get(i).getStatus().getName()))
            .mapToObj(i -> new DateRange(new Date(listingStatusEvents.get(i).getEventDate()),
                    i < (listingStatusEvents.size() - 1) ? new Date(listingStatusEvents.get(i + 1).getEventDate())
                            //Math.max here to handle the case where status is a future date
                            : new Date(Math.max(System.currentTimeMillis(), listingStatusEvents.get(i).getEventDate()))))
            .collect(Collectors.toList());
    }

    private Cache getDirectReviewsCache() {
        return cacheManager.getCache(CacheNames.DIRECT_REVIEWS);
    }
}
