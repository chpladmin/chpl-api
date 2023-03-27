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
import gov.healthit.chpl.compliance.directreview.DirectReviewUpdateEmailService;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductOwner;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.sharedstore.listing.ListingStoreRemove;
import gov.healthit.chpl.sharedstore.listing.RemoveBy;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component("transactionalJoinDeveloperManager")
@NoArgsConstructor
@Log4j2(topic = "joinDeveloperJobLogger")
public class TransactionalJoinDeveloperManager {

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

    @Transactional(rollbackFor = Exception.class)
    @ListingStoreRemove(removeBy = RemoveBy.DEVELOPER_ID, id = "#developerToJoin.id")
    public void join(List<Developer> developersJoining, Developer developerToJoin)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException, ValidationException, Exception {
        List<Long> developerIdsJoining = developersJoining.stream()
                .map(Developer::getId)
                .collect(Collectors.toList());

        Map<Long, CertifiedProductSearchDetails> preJoinListingDetails = new HashMap<Long, CertifiedProductSearchDetails>();
        Map<Long, CertifiedProductSearchDetails> postJoinListingDetails = new HashMap<Long, CertifiedProductSearchDetails>();
        // search for any products assigned to the list of developers passed in
        List<Product> developerProducts = productManager.getByDevelopers(developerIdsJoining);
        for (Product product : developerProducts) {
            List<CertifiedProductDetailsDTO> affectedListings = cpManager.getByProduct(product.getId());
            LOGGER.info("Found " + affectedListings.size() + " affected listings under product " + product.getName());
            // need to get details for affected listings now before the product is re-assigned
            // so that any listings with a generated new-style CHPL ID have the old developer code
            List<Future<CertifiedProductSearchDetails>> beforeListingFutures = getCertifiedProductSearchDetailsFutures(affectedListings, true);
            for (Future<CertifiedProductSearchDetails> future : beforeListingFutures) {
                CertifiedProductSearchDetails details = future.get();
                LOGGER.info("Complete retrieving details for id: " + details.getId());
                preJoinListingDetails.put(details.getId(), details);
            }

            // add an item to the ownership history of each product
            ProductOwner historyToAdd = new ProductOwner();
            Developer prevOwner = new Developer();
            prevOwner.setId(product.getOwner().getId());
            historyToAdd.setDeveloper(prevOwner);
            historyToAdd.setTransferDay(LocalDate.now());
            product.getOwnerHistory().add(historyToAdd);
            // reassign those products to the new developer
            product.setOwner(developerToJoin);
            productManager.updateProductForOwnerJoin(product);

            // get the listing details again - this time they will have the new developer code
            List<Future<CertifiedProductSearchDetails>> afterListingFutures = getCertifiedProductSearchDetailsFutures(affectedListings, false);
            for (Future<CertifiedProductSearchDetails> future : afterListingFutures) {
                CertifiedProductSearchDetails details = future.get();
                LOGGER.info("Complete retrieving details for id: " + details.getId());
                postJoinListingDetails.put(details.getId(), details);
            }
        }

        // mark the passed in developers as deleted
        for (Long developerId : developerIdsJoining) {
            devDao.delete(developerId);
        }

        logListingActivities(preJoinListingDetails, postJoinListingDetails);

        directReviewEmailService.sendEmail(developersJoining, Arrays.asList(developerToJoin),
                preJoinListingDetails, postJoinListingDetails, LOGGER);

        logDeveloperJoinActivities(developersJoining, developerToJoin);
    }

    private void logListingActivities(Map<Long, CertifiedProductSearchDetails> preJoinListingDetails,
            Map<Long, CertifiedProductSearchDetails> postJoinListingDetails) {
        LOGGER.info("Logging listing activity for developer join.");
        postJoinListingDetails.keySet().stream()
            .forEach(postJoinListingId -> {
                CertifiedProductSearchDetails preJoinListing = preJoinListingDetails.get(postJoinListingId);
                CertifiedProductSearchDetails postJoinListing = postJoinListingDetails.get(postJoinListingId);
                try {
                    activityManager.addActivity(ActivityConcept.CERTIFIED_PRODUCT, preJoinListing.getId(),
                        "Updated certified product " + postJoinListing.getChplProductNumber() + ".", preJoinListing,
                        postJoinListing);
                } catch (JsonProcessingException | EntityRetrievalException | EntityCreationException ex) {
                    LOGGER.warn("Unable to log listing activity for listing " + postJoinListingId + " during developer join.", ex);
                }  catch (Exception ex) {
                    LOGGER.warn("Unexpected error logging listing activity for listing " + postJoinListingId + " during developer join.", ex);
                }
            });
    }

    private void logDeveloperJoinActivities(List<Developer> developersJoining, Developer developerToJoin) {
        developersJoining.stream()
            .forEach(developerJoined -> {
                LOGGER.info("Logging activity that developer " + developerJoined.getName() + " joined " + developerToJoin.getName());
                try {
                    activityManager.addActivity(ActivityConcept.DEVELOPER, developerJoined.getId(),
                        "Developer " + developerJoined.getName() + " joined " + developerToJoin.getName(),
                        developerJoined, developerToJoin);
                } catch (JsonProcessingException | EntityRetrievalException | EntityCreationException ex) {
                    LOGGER.warn("Unable to log developer join activity.", ex);
                }  catch (Exception ex) {
                    LOGGER.warn("Unexpected error logging developer join activity.", ex);
                }
            });
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
