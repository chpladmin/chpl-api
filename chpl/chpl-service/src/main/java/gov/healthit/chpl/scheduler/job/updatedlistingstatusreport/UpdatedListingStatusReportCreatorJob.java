package gov.healthit.chpl.scheduler.job.updatedlistingstatusreport;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.attribute.AttributeUpToDate;
import gov.healthit.chpl.attribute.AttributeUpToDateService;
import gov.healthit.chpl.attribute.CodeSetUpToDate;
import gov.healthit.chpl.attribute.FunctionalityTestedUpToDate;
import gov.healthit.chpl.attribute.StandardUpToDate;
import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.util.CertificationStatusUtil;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "updatedListingStatusReportCreatorJobLogger")
public class UpdatedListingStatusReportCreatorJob extends QuartzJob {

    @Autowired
    private ListingSearchService listingSearchService;

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    private UpdatedListingStatusReportDao updatedListingStatusReportDao;

    @Autowired
    private ListingNotUpToDateReasonDao listingNotUpToDateReasonDao;

    @Autowired
    private AttributeUpToDateService attributeUpToDateService;

    @Autowired
    private JpaTransactionManager txManager;

    private List<ListingNotUpToDateReason> reasons;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Updated Listing Status Report Creator job. *********");

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
                        if (doStatisticsExistForDate(LocalDate.now())) {
                            deleteStatisticsForDate(LocalDate.now());
                        }
                        calculateStatisticsForActiveListings();
                    } catch (ValidationException e) {
                        LOGGER.error(e);
                    }
                }
            });
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            LOGGER.info("********* Completed the Updated Listing Status Report Creator job. *********");
        }
    }

    private void calculateStatisticsForActiveListings() throws ValidationException {
        reasons = listingNotUpToDateReasonDao.getAll();

        SearchRequest request = SearchRequest.builder()
                .certificationStatuses(CertificationStatusUtil.getActiveStatusNames().stream().collect(Collectors.toSet()))
                .pageSize(SearchRequest.MAX_PAGE_SIZE)
                .build();

        listingSearchService.getAllPagesOfSearchResults(request).stream()
                .map(result -> getCertifiedProductDetails(result.getId()))
                .filter(listing -> listing.isPresent())
                .peek(x -> LOGGER.info("Calculating all criteria updates needed for {}", x.get().getChplProductNumber()))
                .flatMap(certifiedProductDetails -> calculateUpdatedListingStatusReports(certifiedProductDetails.get()).stream())
                .forEach(updatedListingStatusReport -> updatedListingStatusReportDao.create(updatedListingStatusReport));
    }

    private List<UpdatedListingStatusReport> calculateUpdatedListingStatusReports(CertifiedProductSearchDetails listing) {
        List<UpdatedListingStatusReport> updatedListingStatusReports = new ArrayList<UpdatedListingStatusReport>();
        if (!CollectionUtils.isEmpty(listing.getCertificationResults())) {
            listing.getCertificationResults().stream()
                .filter(certResult -> BooleanUtils.isTrue(certResult.getSuccess()))
                .flatMap(certResult -> calculateUpdatedCertificationResultStatusReports(listing, certResult).stream())
                .filter(criterionStatusReport -> criterionStatusReport != null)
                .forEach(criterionStatusReport -> updatedListingStatusReports.add(criterionStatusReport));
        }
        return updatedListingStatusReports;
    }

    private List<UpdatedListingStatusReport> calculateUpdatedCertificationResultStatusReports(
            CertifiedProductSearchDetails listing, CertificationResult certResult) {
        List<UpdatedListingStatusReport> updatesRequiredForCriterion = getUpdatesRequiredForCriterion(listing, certResult);
        if (!CollectionUtils.isEmpty(updatesRequiredForCriterion)) {
            LOGGER.info("Criterion {} is NOT up-to-date. It requires {} updates.",
                    Util.formatCriteriaNumber(certResult.getCriterion()),
                    updatesRequiredForCriterion.size());
        } else {
            LOGGER.info("Criterion {} is up-to-date.", Util.formatCriteriaNumber(certResult.getCriterion()));
        }
        return updatesRequiredForCriterion;
    }

    private List<UpdatedListingStatusReport> getUpdatesRequiredForCriterion(CertifiedProductSearchDetails listing,
            CertificationResult certResult) {
        List<StandardUpToDate> standardsUpToDate = attributeUpToDateService.getStandardsUpToDate(certResult, LOGGER);
        List<FunctionalityTestedUpToDate> functionalityTestedUpToDate = attributeUpToDateService.getFunctionalitiesTestedUpToDate(certResult, LOGGER);
        List<CodeSetUpToDate> codeSetsUpToDate = attributeUpToDateService.getCodeSetsUpToDate(certResult, LOGGER);

        List<UpdatedListingStatusReport> updatesRequiredForCriterion = new ArrayList<UpdatedListingStatusReport>();
        if (!CollectionUtils.isEmpty(standardsUpToDate)) {
            standardsUpToDate.stream()
                .map(stdReport -> buildReportFromStandardRequiringUpdate(listing, stdReport))
                .forEach(report -> updatesRequiredForCriterion.add(report));
        }
        if (!CollectionUtils.isEmpty(functionalityTestedUpToDate)) {
            functionalityTestedUpToDate.stream()
                .map(ftReport -> buildReportFromFunctionalityTestedRequiringUpdate(listing, ftReport))
                .forEach(report -> updatesRequiredForCriterion.add(report));
        }
        if (!CollectionUtils.isEmpty(codeSetsUpToDate)) {
            codeSetsUpToDate.stream()
                .map(codeSetReport -> buildReportFromCodeSetRequiringUpdate(listing, codeSetReport))
                .forEach(report -> updatesRequiredForCriterion.add(report));
        }
        return updatesRequiredForCriterion;
    }

    private UpdatedListingStatusReport buildReportFromStandardRequiringUpdate(CertifiedProductSearchDetails listing,
            StandardUpToDate standardReport) {
        return UpdatedListingStatusReport.builder()
            .certifiedProductId(listing.getId())
            .chplProductNumber(listing.getChplProductNumber())
            .product(listing.getProduct().getName())
            .version(listing.getVersion().getVersion())
            .developer(listing.getDeveloper().getName())
            .certificationBody(listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY).toString())
            .certificationStatus(listing.getCurrentStatus().getStatus().getName())
            .developerId(listing.getDeveloper().getId())
            .certificationBodyId(Long.valueOf(listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_ID_KEY).toString()))
            .certificationStatusId(listing.getCurrentStatus().getStatus().getId())
            .certificationCriterion(standardReport.getCriterion())
            .standard(standardReport.getStandard())
            .functionalityTested(null)
            .codeSet(null)
            .certificationResultId(getCertificationResultId(listing, standardReport.getCriterion()))
            .listingNotUpToDateReason(getListingNotUpToDateReason(standardReport))
        .build();
    }

    private UpdatedListingStatusReport buildReportFromFunctionalityTestedRequiringUpdate(
            CertifiedProductSearchDetails listing,
            FunctionalityTestedUpToDate ftReport) {
        return UpdatedListingStatusReport.builder()
            .certifiedProductId(listing.getId())
            .chplProductNumber(listing.getChplProductNumber())
            .product(listing.getProduct().getName())
            .version(listing.getVersion().getVersion())
            .developer(listing.getDeveloper().getName())
            .certificationBody(listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY).toString())
            .certificationStatus(listing.getCurrentStatus().getStatus().getName())
            .developerId(listing.getDeveloper().getId())
            .certificationBodyId(Long.valueOf(listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_ID_KEY).toString()))
            .certificationStatusId(listing.getCurrentStatus().getStatus().getId())
            .certificationCriterion(ftReport.getCriterion())
            .standard(null)
            .functionalityTested(ftReport.getFunctionalityTested())
            .codeSet(null)
            .certificationResultId(getCertificationResultId(listing, ftReport.getCriterion()))
            .listingNotUpToDateReason(getListingNotUpToDateReason(ftReport))
        .build();
    }

    private UpdatedListingStatusReport buildReportFromCodeSetRequiringUpdate(CertifiedProductSearchDetails listing,
            CodeSetUpToDate codeSetReport) {
        return UpdatedListingStatusReport.builder()
            .certifiedProductId(listing.getId())
            .chplProductNumber(listing.getChplProductNumber())
            .product(listing.getProduct().getName())
            .version(listing.getVersion().getVersion())
            .developer(listing.getDeveloper().getName())
            .certificationBody(listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_NAME_KEY).toString())
            .certificationStatus(listing.getCurrentStatus().getStatus().getName())
            .developerId(listing.getDeveloper().getId())
            .certificationBodyId(Long.valueOf(listing.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_ID_KEY).toString()))
            .certificationStatusId(listing.getCurrentStatus().getStatus().getId())
            .certificationCriterion(codeSetReport.getCriterion())
            .standard(null)
            .functionalityTested(null)
            .codeSet(codeSetReport.getCodeSet())
            .certificationResultId(getCertificationResultId(listing, codeSetReport.getCriterion()))
            .listingNotUpToDateReason(getListingNotUpToDateReason(codeSetReport))
        .build();
    }

    private ListingNotUpToDateReason getListingNotUpToDateReason(AttributeUpToDate attributeReport) {
        ListingNotUpToDateReasonEnum reasonEnum = ListingNotUpToDateReasonEnum.calculateReason(attributeReport, LOGGER);
        return getReason(reasonEnum);
    }

    private ListingNotUpToDateReason getReason(ListingNotUpToDateReasonEnum reasonEnum) {
        return reasons.stream()
                .filter(reason -> reason.getName().equals(reasonEnum.getName()))
                .findAny()
                .orElse(null);
    }

    private Long getCertificationResultId(CertifiedProductSearchDetails listing, CertificationCriterion criterion) {
        return listing.getCertificationResults().stream()
                .filter(certResult -> certResult.getCriterion() != null && certResult.getCriterion().getId().equals(criterion.getId()))
                .map(certResult -> certResult.getId())
                .findAny()
                .orElse(null);
    }

    private Optional<CertifiedProductSearchDetails> getCertifiedProductDetails(Long id) {
        try {
            return Optional.of(certifiedProductDetailsManager.getCertifiedProductDetails(id));
        } catch (EntityRetrievalException e) {
            LOGGER.error("Could not retrieve listing with id: {}", id, e);
            return Optional.empty();
        }
    }

    private Boolean doStatisticsExistForDate(LocalDate dateToCheck) {
        return updatedListingStatusReportDao.getUpdatedListingStatusReportsByDate(dateToCheck).size() > 0;
    }

    private void deleteStatisticsForDate(LocalDate dateToCheck) {
        updatedListingStatusReportDao.deleteUpdatedListingStatusReportsByDay(dateToCheck);
    }
}