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
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.manager.CertificationResultManager;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.scheduler.job.extra.JobResponse;

public class AddCriteriaToSingleListingJob extends QuartzJob {

    // Default logger
    private Logger logger = LogManager.getLogger("addCriteriaToListingsJobLogger");

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    private CertificationCriterionDAO certificationCriterionDao;

    @Autowired
    private CertificationResultManager certificationResultManager;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {

        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        setLogger(jobContext);
        setSecurityContext();

        Long listing = jobContext.getMergedJobDataMap().getLong("listing");
        ArrayList<String> criteria = new ArrayList<String>(Arrays.asList(jobContext.getMergedJobDataMap()
                .get("criteria").toString().split(";")));

        logger.info("********* Starting the Add Criteria to Single Listing job. [" + listing + "] *********");

        CertifiedProductSearchDetails cpsd = getListing(listing);
        JobResponse response;

        try {
            for (String criterion : criteria) {
                CertificationCriterion crit = new CertificationCriterion(certificationCriterionDao.getByName(criterion));
                CertificationResult toCert = new CertificationResult();
                CertificationResult fromCert = new CertificationResult();
                toCert.setCriterion(crit);
                toCert.setNumber(criterion);
                toCert.setSuccess(false);
                fromCert.setCriterion(crit);
                fromCert.setNumber(criterion);
                fromCert.setSuccess(true);
                certificationResultManager.update(cpsd, cpsd, fromCert, toCert);
            }
            String msg = "Completed Updating certified product {" + cpsd.getId() + "}: "
                    + cpsd.getChplProductNumber() + "-" + criteria.toString();
            response = new JobResponse(cpsd.getChplProductNumber(), true, msg);

        } catch (Exception e) {
            String msg = "Unsuccessful Update certified product {" + cpsd.getId() + "}: " + "-" + criteria.toString()
                    + e.getMessage();
            response =  new JobResponse(cpsd.getChplProductNumber(), false, msg);
        }

        jobContext.setResult(response);

        logger.info(response.toString());
        logger.info("********* Completed the Add Criteria to Single Listing job. [" + listing + "] *********");
    }

    private void setLogger(JobExecutionContext jobContext) {
        if (jobContext.getMergedJobDataMap().containsKey("logger")) {
            if (jobContext.getMergedJobDataMap().get("logger") instanceof Logger) {
                logger = (Logger) jobContext.getMergedJobDataMap().get("logger");
            }
        }
    }

    private CertifiedProductSearchDetails getListing(Long cpId) {
        try {
            CertifiedProductSearchDetails cpsd = certifiedProductDetailsManager.getCertifiedProductDetails(cpId);
            return cpsd;
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
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
