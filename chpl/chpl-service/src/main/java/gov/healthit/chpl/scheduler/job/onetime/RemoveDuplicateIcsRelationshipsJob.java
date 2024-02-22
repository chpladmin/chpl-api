package gov.healthit.chpl.scheduler.job.onetime;

import javax.persistence.Query;
import javax.transaction.Transactional;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.sharedstore.listing.SharedListingStoreProvider;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "removeDuplicateIcsRelationshipsJobLogger")
public class RemoveDuplicateIcsRelationshipsJob extends QuartzJob {
    private static final String ACTIVITY_REASON = "System job removing duplicate ICS relationship.";

    @Autowired
    private UpdateIcsRelationshipDao updateIcsRelationshipDao;

    @Autowired
    private ActivityManager activityManager;

    @Autowired
    private CertifiedProductDetailsManager cpdManager;

    @Autowired
    private SharedListingStoreProvider sharedListingStoreProvider;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Remove Duplicate ICS Relationships job. *********");
        setSecurityContext();
        try {
            Long listingId = 10099L;
            LOGGER.info("Updating listing " + listingId);
            CertifiedProductSearchDetails listing = cpdManager.getCertifiedProductDetailsNoCache(listingId);
            updateIcsRelationshipDao.removeListingToListingMap(1135L);
            logCertifiedProductUpdateActivity(listing, ACTIVITY_REASON);
            sharedListingStoreProvider.remove(listingId); //parent id
            sharedListingStoreProvider.remove(11050L); //child id that was duplicate
            LOGGER.info("Updated listing " + listingId);
        } catch (Exception ex) {
            LOGGER.error("Could not update listing 10099", ex);
        }

        try {
            Long listingId = 10875L;
            LOGGER.info("Updating listing " + listingId);
            CertifiedProductSearchDetails listing = cpdManager.getCertifiedProductDetailsNoCache(listingId);
            updateIcsRelationshipDao.removeListingToListingMap(1123L);
            logCertifiedProductUpdateActivity(listing, ACTIVITY_REASON);
            sharedListingStoreProvider.remove(listingId); //parent id
            sharedListingStoreProvider.remove(11032L); //child id that was duplicate
            LOGGER.info("Updated listing " + listingId);
        } catch (Exception ex) {
            LOGGER.error("Could not update listing 10875", ex);
        }

        try {
            Long listingId = 10553L;
            LOGGER.info("Updating listing " + listingId);
            CertifiedProductSearchDetails listing = cpdManager.getCertifiedProductDetailsNoCache(listingId);
            updateIcsRelationshipDao.removeListingToListingMap(1132L);
            logCertifiedProductUpdateActivity(listing, ACTIVITY_REASON);
            sharedListingStoreProvider.remove(listingId); //parent id
            sharedListingStoreProvider.remove(11040L); //child id that was duplicate
            LOGGER.info("Updated listing " + listingId);
        } catch (Exception ex) {
            LOGGER.error("Could not update listing 10553", ex);
        }

        try {
            Long listingId = 9298L;
            LOGGER.info("Updating listing " + listingId);
            CertifiedProductSearchDetails listing = cpdManager.getCertifiedProductDetailsNoCache(listingId);
            updateIcsRelationshipDao.removeListingToListingMap(1111L);
            logCertifiedProductUpdateActivity(listing, ACTIVITY_REASON);
            sharedListingStoreProvider.remove(listingId); //parent id
            sharedListingStoreProvider.remove(11037L); //child id that was duplicate
            LOGGER.info("Updated listing " + listingId);
        } catch (Exception ex) {
            LOGGER.error("Could not update listing 9298", ex);
        }

        LOGGER.info("********* Completed the Remove Duplicate ICS Relationships job. *********");
    }

    private void logCertifiedProductUpdateActivity(CertifiedProductSearchDetails existingListing,
            String reason) throws JsonProcessingException, EntityCreationException, EntityRetrievalException {

        CertifiedProductSearchDetails updatedListing = cpdManager.getCertifiedProductDetailsNoCache(existingListing.getId());

        activityManager.addActivity(ActivityConcept.CERTIFIED_PRODUCT, existingListing.getId(),
                "Updated certified product " + updatedListing.getChplProductNumber() + ".", existingListing,
                updatedListing, reason);
    }

    private void setSecurityContext() {
        JWTAuthenticatedUser adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(-2L);
        adminUser.setFriendlyName("Admin");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));

        SecurityContextHolder.getContext().setAuthentication(adminUser);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    @Component("updateIcsRelationshipDao")
    private static class UpdateIcsRelationshipDao extends BaseDAOImpl {

        UpdateIcsRelationshipDao() {
        }

        @Transactional
        public void removeListingToListingMap(Long listingToListingMapId) throws JsonProcessingException {
            Query query = entityManager.createQuery("UPDATE ListingToListingMapEntity "
                    + "SET deleted = true "
                    + "WHERE id = :listingToListingMapId");
            query.setParameter("listingToListingMapId", listingToListingMapId);
            query.executeUpdate();
            entityManager.flush();
        }
    }
}
