package gov.healthit.chpl.scheduler.job.updatedlistingstatusreport;

import java.util.Optional;
import java.util.Set;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.SearchRequest;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class UpdatedListingStatusReportCreatorJob  extends QuartzJob {


    @Autowired
    private ListingSearchService listingSearchService;

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    private UpdatedListingStatusReportDAO updatedListingStatusReportDAO;

    @Autowired
    private JpaTransactionManager txManager;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        try {
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
                        xxxx();
                    } catch (ValidationException e) {
                        LOGGER.error(e);
                    }
                }
            });
        } catch (Exception e) {
            LOGGER.error(e);
        }

    }


    private void xxxx() throws ValidationException {
        SearchRequest request = SearchRequest.builder()
                .certificationStatuses(Set.of("Active", "Suspended by ONC", "Suspended by ONC-ACB"))
                .build();

        listingSearchService.getAllPagesOfSearchResults(request).stream()
                .map(result -> getCertifiedProductDetails(result.getId()))
                .filter(listing -> listing.isPresent())
                .map(certifiedProductDetails -> UpdatedListingStatusReport.builder()
                        .certifiedProductId(certifiedProductDetails.get().getId())
                        .criteriaRequireUpdateCount(getCriteriaRequireUpdateCount(certifiedProductDetails.get()))
                        .daysUpdatedEarly(getDaysUpdatedEarly(certifiedProductDetails.get()))
                        .build())
                .forEach(updatedListingStatusReport -> updatedListingStatusReportDAO.create(updatedListingStatusReport));
    }

    private Integer getCriteriaRequireUpdateCount(CertifiedProductSearchDetails certifiedProductDetails) {
        return 0;
    }

    private Integer getDaysUpdatedEarly(CertifiedProductSearchDetails certifiedProductDetails) {
        return 0;
    }

    private Optional<CertifiedProductSearchDetails> getCertifiedProductDetails(Long id) {
        try {
            return Optional.of(certifiedProductDetailsManager.getCertifiedProductDetails(id));
        } catch (EntityRetrievalException e) {
            LOGGER.error("Could not retrieve listing with id: {}", id, e);
            return Optional.empty();
        }
    }
}