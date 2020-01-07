package gov.healthit.chpl.scheduler.job;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.PendingCertifiedProductDAO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.manager.PendingCertifiedProductManager;
import gov.healthit.chpl.scheduler.job.extra.JobResponse;

public class AddCriteriaToSinglePendingListingJob extends QuartzJob {

    // Default logger
    private Logger logger = LogManager.getLogger("addCriteriaToListingsJobLogger");

    @Autowired
    private PendingCertifiedProductManager pendingCertifiedProductManager;

    @Autowired
    private PendingCertifiedProductDAO pendingCertifiedProductDAO;

    @Autowired
    private CertificationCriterionDAO certCritDAO;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {

        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        setLogger(jobContext);
        setSecurityContext();

        Long listing = jobContext.getMergedJobDataMap().getLong("listing");
        ArrayList<String> criteria = new ArrayList<String>(Arrays.asList(jobContext.getMergedJobDataMap()
                .get("criteria").toString().split(";")));

        logger.info("********* Starting the Add Criteria to Single Pending Listing job. [" + listing + "] *********");

        PendingCertifiedProductDTO pcp = getListing(listing);
        JobResponse response;

        try {
            for (String criterion : criteria) {
                pcp = addCertToPendingListing(pcp, criterion);
            }
            updatePendingListing(pcp);
            //TODO: figure out what a chpl product number is for a pending listing
            String msg = "Completed Updating certified product {" + pcp.getId() + "}: "
                    + /*pcp.getChplProductNumber() +*/ "-" + criteria.toString();
            response = new JobResponse(pcp.getId().toString()/*pcp.getChplProductNumber()*/, true, msg);

        } catch (Exception e) {
            String msg = "Unsuccessful Update certified product {" + pcp.getId() + "} " + criteria.toString()
            + ':' + e.getMessage();
            response =  new JobResponse(pcp.getId().toString()/*pcp.getChplProductNumber()*/, false, msg);
        }

        jobContext.setResult(response);

        logger.info(response.toString());
        logger.info("********* Completed the Add Criteria to Single Pending Listing job. [" + listing + "] *********");
    }

    private void setLogger(JobExecutionContext jobContext) {
        if (jobContext.getMergedJobDataMap().containsKey("logger")) {
            if (jobContext.getMergedJobDataMap().get("logger") instanceof Logger) {
                logger = (Logger) jobContext.getMergedJobDataMap().get("logger");
            }
        }
    }

    private PendingCertifiedProductDTO getListing(Long cpId) {
        //TODO: figure out what object getListing should return
        // maybe PendingCertifiedProductDTO
        // maybe PendingCertifiedProduct
        PendingCertifiedProductDTO pcp;
        try {
            pcp = pendingCertifiedProductDAO.findById(cpId, false);
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
        return pcp; 
    }

    private PendingCertifiedProductDTO addCertToPendingListing(PendingCertifiedProductDTO pcp, String criterionNumber) {
        //TODO: add a new certification criterion to the passed in listing, return that new listing
        /*
        CertificationCriterionDTO criterion = certCritDAO.getByName(criterionNumber);
        if (criterion == null || criterion.getId() == null) {
            throw new EntityCreationException(
                    "Cannot create certification result mapping for unknown criteria " + criterionNumber);
        }
        List<CertificationResult> criteria = pcp.getCertificationResults();
        for (CertificationResult crit : criteria) {
            if (crit.getNumber().equalsIgnoreCase(criterionNumber)) {
                throw new EntityCreationException(
                        "Cannot create duplicate certification result mapping for criteria " + criterionNumber);
            }
        }
        PendingCertificationResultDTO toCreate = new PendingCertificationResultDTO();
        toCreate.setPendingCertifiedProductId(pcp.getId());
        toCreate.setCriterion(criterion);
        toCreate.setMeetsCriteria(false);
        */
        return pcp;
    }

    private void updatePendingListing (PendingCertifiedProductDTO pcp) {
        //TODO: take "createOrReplace" logic from PendingCertifiedProductManagerImpl class and do that here, minus the activity

    }

    private void setSecurityContext() {
        JWTAuthenticatedUser adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(-2L);
        adminUser.setFriendlyName("Admin");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));

        SecurityContextHolder.getContext().setAuthentication(adminUser);
    }
}
