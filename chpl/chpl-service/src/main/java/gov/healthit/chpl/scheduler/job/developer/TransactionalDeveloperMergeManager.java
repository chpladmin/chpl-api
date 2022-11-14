package gov.healthit.chpl.scheduler.job.developer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductOwner;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.service.DirectReviewUpdateEmailService;
import gov.healthit.chpl.sharedstore.listing.ListingStoreRemove;
import gov.healthit.chpl.sharedstore.listing.RemoveBy;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component("transactionalDeveloperMergeManager")
@NoArgsConstructor
@Log4j2(topic = "mergeDeveloperJobLogger")
public class TransactionalDeveloperMergeManager {

    @Autowired
    private DeveloperManager devManager;

    @Autowired
    private ProductManager productManager;

    @Autowired
    private CertifiedProductManager cpManager;

    @Autowired
    private CertifiedProductDetailsManager cpdManager;

    @Autowired
    private ActivityManager activityManager;

    @Autowired
    private DeveloperDAO devDao;

    @Autowired
    private DirectReviewUpdateEmailService directReviewEmailService;

    @Transactional
    @ListingStoreRemove(removeBy = RemoveBy.DEVELOPER_ID, id = "#developerToCreate.id")
    public Developer merge(List<Developer> beforeDevelopers, Developer developerToCreate)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException, Exception {
        List<Long> developerIdsToMerge = beforeDevelopers.stream()
                .map(Developer::getId)
                .collect(Collectors.toList());
        LOGGER.info("Creating new developer " + developerToCreate.getName());
        Long createdDeveloperId = devManager.create(developerToCreate);

        Map<Long, CertifiedProductSearchDetails> preMergeListingDetails = new HashMap<Long, CertifiedProductSearchDetails>();
        Map<Long, CertifiedProductSearchDetails> postMergeListingDetails = new HashMap<Long, CertifiedProductSearchDetails>();
        // search for any products assigned to the list of developers passed in
        List<Product> developerProducts = productManager.getByDevelopers(developerIdsToMerge);
        for (Product product : developerProducts) {
            List<CertifiedProductDetailsDTO> affectedListings = cpManager.getByProduct(product.getId());
            LOGGER.info("Found " + affectedListings.size() + " affected listings under product " + product.getName());
            // need to get details for affected listings now before the product is re-assigned
            // so that any listings with a generated new-style CHPL ID have the old developer code
            List<Future<CertifiedProductSearchDetails>> beforeListingFutures
                = getCertifiedProductSearchDetailsFutures(affectedListings, true);
            for (Future<CertifiedProductSearchDetails> future : beforeListingFutures) {
                CertifiedProductSearchDetails details = future.get();
                LOGGER.info("Complete retrieving details for id: " + details.getId());
                preMergeListingDetails.put(details.getId(), details);
            }

            // add an item to the ownership history of each product
            ProductOwner historyToAdd = new ProductOwner();
            Developer prevOwner = new Developer();
            prevOwner.setId(product.getOwner().getId());
            historyToAdd.setDeveloper(prevOwner);
            historyToAdd.setTransferDay(LocalDate.now());
            product.getOwnerHistory().add(historyToAdd);
            // reassign those products to the new developer
            product.setOwner(Developer.builder()
                    .id(createdDeveloperId)
                    .build());
            productManager.update(product);

            // get the listing details again - this time they will have the new developer code
            List<Future<CertifiedProductSearchDetails>> afterListingFutures
                = getCertifiedProductSearchDetailsFutures(affectedListings, false);
            for (Future<CertifiedProductSearchDetails> future : afterListingFutures) {
                CertifiedProductSearchDetails details = future.get();
                LOGGER.info("Complete retrieving details for id: " + details.getId());
                postMergeListingDetails.put(details.getId(), details);
            }
        }

        // - mark the passed in developers as deleted
        for (Long developerId : developerIdsToMerge) {
            devDao.delete(developerId);
        }

        LOGGER.info("Logging listing activity for developer merge.");
        for (Long id : postMergeListingDetails.keySet()) {
            CertifiedProductSearchDetails preMergeListing = preMergeListingDetails.get(id);
            CertifiedProductSearchDetails postMergeListing = postMergeListingDetails.get(id);
            activityManager.addActivity(ActivityConcept.CERTIFIED_PRODUCT, preMergeListing.getId(),
                    "Updated certified product " + postMergeListing.getChplProductNumber() + ".", preMergeListing,
                    postMergeListing);
        }

        Developer createdDeveloper = devManager.getById(createdDeveloperId);
        directReviewEmailService.sendEmail(beforeDevelopers, Arrays.asList(createdDeveloper),
                preMergeListingDetails, postMergeListingDetails, LOGGER);

        LOGGER.info("Logging developer merge activity.");
        Developer afterDeveloper = devManager.getById(createdDeveloperId);
        String beforeDevNames = String.join(",",
                beforeDevelopers.stream().map(Developer::getName).collect(Collectors.toList()));
        activityManager.addActivity(ActivityConcept.DEVELOPER, afterDeveloper.getId(),
                "Merged developers " + beforeDevNames + " into " + afterDeveloper.getName(),
                beforeDevelopers, afterDeveloper);

        return createdDeveloper;
    }

    private List<Future<CertifiedProductSearchDetails>> getCertifiedProductSearchDetailsFutures(
            List<CertifiedProductDetailsDTO> listings, boolean fromCache) throws Exception {

        List<Future<CertifiedProductSearchDetails>> futures = new ArrayList<Future<CertifiedProductSearchDetails>>();
        for (CertifiedProductDetailsDTO currListing : listings) {
            try {
                LOGGER.info("Getting details for affected listing " + currListing.getChplProductNumber());
                futures.add(new AsyncResult<CertifiedProductSearchDetails>(
                        getDetails(currListing.getId(), fromCache)));
            } catch (EntityRetrievalException e) {
                LOGGER.error("Could not retrieve certified product details for id: " + currListing.getId(), e);
            }
        }
        return futures;
    }

    private CertifiedProductSearchDetails getDetails(Long listingId, boolean fromCache) throws EntityRetrievalException {
        if (fromCache) {
            return cpdManager.getCertifiedProductDetails(listingId);
        } else {
            return cpdManager.getCertifiedProductDetailsNoCache(listingId);
        }
    }
}
