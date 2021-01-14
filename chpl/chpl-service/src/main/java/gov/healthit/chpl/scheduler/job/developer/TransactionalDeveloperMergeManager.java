package gov.healthit.chpl.scheduler.job.developer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductOwnerDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.scheduler.SchedulerCertifiedProductSearchDetailsAsync;
import gov.healthit.chpl.service.DirectReviewUpdateEmailService;
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
    private SchedulerCertifiedProductSearchDetailsAsync schedulerCertifiedProductSearchDetailsAsync;

    @Autowired
    private ActivityManager activityManager;

    @Autowired
    private DeveloperDAO devDao;

    @Autowired
    private DirectReviewUpdateEmailService directReviewEmailService;

    @Transactional
    public DeveloperDTO merge(List<DeveloperDTO> beforeDevelopers, DeveloperDTO developerToCreate)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException, Exception {
        List<Long> developerIdsToMerge = beforeDevelopers.stream()
                .map(DeveloperDTO::getId)
                .collect(Collectors.toList());
        LOGGER.info("Creating new developer " + developerToCreate.getName());
        DeveloperDTO createdDeveloper = devManager.create(developerToCreate);

        Map<Long, CertifiedProductSearchDetails> preMergeListingDetails = new HashMap<Long, CertifiedProductSearchDetails>();
        Map<Long, CertifiedProductSearchDetails> postMergeListingDetails = new HashMap<Long, CertifiedProductSearchDetails>();
        // search for any products assigned to the list of developers passed in
        List<ProductDTO> developerProducts = productManager.getByDevelopers(developerIdsToMerge);
        for (ProductDTO product : developerProducts) {
            List<CertifiedProductDetailsDTO> affectedListings = cpManager.getByProduct(product.getId());
            LOGGER.info("Found " + affectedListings.size() + " affected listings under product " + product.getName());
            // need to get details for affected listings now before the product is re-assigned
            // so that any listings with a generated new-style CHPL ID have the old developer code
            List<Future<CertifiedProductSearchDetails>> beforeListingFutures
                = getCertifiedProductSearchDetailsFutures(affectedListings);
            for (Future<CertifiedProductSearchDetails> future : beforeListingFutures) {
                CertifiedProductSearchDetails details = future.get();
                LOGGER.info("Complete retrieving details for id: " + details.getId());
                preMergeListingDetails.put(details.getId(), details);
            }

            // add an item to the ownership history of each product
            ProductOwnerDTO historyToAdd = new ProductOwnerDTO();
            historyToAdd.setProductId(product.getId());
            DeveloperDTO prevOwner = new DeveloperDTO();
            prevOwner.setId(product.getOwner().getId());
            historyToAdd.setDeveloper(prevOwner);
            historyToAdd.setTransferDate(System.currentTimeMillis());
            product.getOwnerHistory().add(historyToAdd);
            // reassign those products to the new developer
            product.getOwner().setId(createdDeveloper.getId());
            productManager.update(product);

            // get the listing details again - this time they will have the new developer code
            List<Future<CertifiedProductSearchDetails>> afterListingFutures
                = getCertifiedProductSearchDetailsFutures(affectedListings);
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

        directReviewEmailService.sendEmail(beforeDevelopers, Arrays.asList(createdDeveloper),
                preMergeListingDetails, postMergeListingDetails);

        LOGGER.info("Logging developer merge activity.");
        DeveloperDTO afterDeveloper = devManager.getById(createdDeveloper.getId());
        String beforeDevNames = String.join(",",
                beforeDevelopers.stream().map(DeveloperDTO::getName).collect(Collectors.toList()));
        activityManager.addActivity(ActivityConcept.DEVELOPER, afterDeveloper.getId(),
                "Merged developers " + beforeDevNames + " into " + afterDeveloper.getName(),
                beforeDevelopers, afterDeveloper);

        return createdDeveloper;
    }

    private List<Future<CertifiedProductSearchDetails>> getCertifiedProductSearchDetailsFutures(
            List<CertifiedProductDetailsDTO> listings) throws Exception {

        List<Future<CertifiedProductSearchDetails>> futures = new ArrayList<Future<CertifiedProductSearchDetails>>();
        for (CertifiedProductDetailsDTO currListing : listings) {
            try {
                LOGGER.info("Getting details for affected listing " + currListing.getChplProductNumber());
                futures.add(schedulerCertifiedProductSearchDetailsAsync.getCertifiedProductDetail(
                        currListing.getId(), cpdManager));
            } catch (EntityRetrievalException e) {
                LOGGER.error("Could not retrieve certified product details for id: " + currListing.getId(), e);
            }
        }
        return futures;
    }

}
