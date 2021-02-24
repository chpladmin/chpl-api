package gov.healthit.chpl.scheduler.job.measurefix;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class MeasureFixJob implements Job {

    @Autowired
    private JpaTransactionManager txManager;

    @Autowired
    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    private CertifiedProductManager certifiedProductManager;

    @Value("${executorThreadCountForQuartzJobs}")
    private Integer threadCount;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the G1/G2 Measure Data Fix job. *********");
        setSecurityContext();
        try {
            // This will control how many threads are used by the parallelStream.  By default parallelStream
            // will use the # of processors - 1 threads.  We want to be able to limit this.
            ForkJoinPool pool = new ForkJoinPool(4);
            pool.submit(() -> {
               getAll2015CertifiedProducts().stream()
                       .forEach(cp -> resaveListing(cp));
            });
        } catch (Exception e) {
            LOGGER.catching(e);
        }
    }

    private List<CertifiedProductDetailsDTO> getAll2015CertifiedProducts() {
        LOGGER.info("Retrieving all 2015 listings");
        List<CertifiedProductDetailsDTO> listings = certifiedProductDAO.findByEdition(
                CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
        LOGGER.info("Completed retreiving all 2015 listings");
        return listings;
    }

    private void resaveListing(CertifiedProductDetailsDTO cp) {
     // We need to manually create a transaction in this case because of how AOP works. When a method is
        // annotated with @Transactional, the transaction wrapper is only added if the object's proxy is called.
        // The object's proxy is not called when the method is called from within this class. The object's proxy
        // is called when the method is public and is called from a different object.
        // https://stackoverflow.com/questions/3037006/starting-new-transaction-in-spring-bean
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    // This will control how many threads are used by the parallelStream.  By default parallelStream
                    // will use the # of processors - 1 threads.  We want to be able to limit this.
                    CertifiedProductSearchDetails listing = certifiedProductDetailsManager.getCertifiedProductDetails(cp.getId());
                    ListingUpdateRequest request = ListingUpdateRequest.builder()
                            .listing(listing)
                            .acknowledgeWarnings(true)
                            .reason(null)
                            .build();

                    certifiedProductManager.update(request);
                    LOGGER.info("Successfully updated listing: " + cp.getId());
                } catch (ValidationException e) {
                    LOGGER.info("Could not update listing: " + cp.getId());
                    e.getErrorMessages().stream()
                        .forEach(message -> LOGGER.info(message));
                } catch (Exception e) {
                    LOGGER.info("Could not update listing: " + cp.getId());
                    LOGGER.catching(e);
                }
            }
        });
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
