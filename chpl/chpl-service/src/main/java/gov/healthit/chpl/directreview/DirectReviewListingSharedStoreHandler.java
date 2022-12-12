package gov.healthit.chpl.directreview;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.compliance.DirectReview;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.sharedstore.listing.SharedListingStoreProvider;
import gov.healthit.chpl.util.NullSafeEvaluator;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class DirectReviewListingSharedStoreHandler {
    private SharedListingStoreProvider sharedListingStoreProvider;
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    public DirectReviewListingSharedStoreHandler(@Lazy SharedListingStoreProvider sharedListingStoreProvider,
            @Lazy CertifiedProductDetailsManager certifiedProductDetailsManager) {
        this.sharedListingStoreProvider = sharedListingStoreProvider;
        this.certifiedProductDetailsManager = certifiedProductDetailsManager;
    }

    public void handler(List<DirectReview> allDirectReviews) {
        getUniqueListings(allDirectReviews).forEach(listing -> {
            removeListingFromSharedStoreIfDirectReviewUpdated(listing, allDirectReviews);
        });
    }

    private void removeListingFromSharedStoreIfDirectReviewUpdated(CertifiedProductSearchDetails listing, List<DirectReview> allDirectReviews) {
        listing.getDirectReviews().forEach(dr -> {
            if (hasDirectReviewBeenUpdated(findDirectReview(allDirectReviews, dr.getJiraKey()), dr)) {
                LOGGER.info("Removing Listing Id {} from the Shared Store", listing.getId());
                sharedListingStoreProvider.remove(listing.getId());
            } else {
                LOGGER.info("Not Removing Listing Id {} from the Shared Store - Direct Review Last Updated Date not changed", listing.getId());
            }
        });
    }

    private Boolean hasDirectReviewBeenUpdated(DirectReview newVersion, DirectReview origVersion) {
        return NullSafeEvaluator.eval(() -> newVersion.getLastUpdated(), new Date(Long.MIN_VALUE)).equals(
                NullSafeEvaluator.eval(() -> newVersion.getLastUpdated(), new Date(Long.MIN_VALUE)));

    }

    private DirectReview findDirectReview(List<DirectReview> directReviews, String jiraKey) {
        return directReviews.stream()
                .filter(dr -> dr.getJiraKey().equals(jiraKey))
                .findAny()
                .orElse(null);
    }

    private List<CertifiedProductSearchDetails> getUniqueListings(List<DirectReview> allDirectReviews) {
        return  allDirectReviews.stream()
                .flatMap(dr -> dr.getNonConformities().stream())
                .flatMap(nc -> nc.getDeveloperAssociatedListings().stream())
                .map(dal -> dal.getChplProductNumber())
                .distinct()
                .map(chplProdNbr -> getListing(chplProdNbr))
                .filter(listing -> listing != null)
                .toList();

    }

    private CertifiedProductSearchDetails getListing(String chplProductNumber) {
        try {
            return  certifiedProductDetailsManager.getCertifiedProductDetailsByChplProductNumber(chplProductNumber);
        } catch (EntityRetrievalException e) {
            LOGGER.error("Could not lookup ChplProductNumber: {}", chplProductNumber, e);
            return null;
        }
    }
 }
