package gov.healthit.chpl.realworldtesting.manager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.activity.history.ListingActivityUtil;
import gov.healthit.chpl.activity.history.explorer.RealWorldTestingEligibilityActivityExplorer;
import gov.healthit.chpl.certifiedproduct.service.CertificationStatusEventsService;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingReport;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.RealWorldTestingEligibility;
import gov.healthit.chpl.service.RealWorldTestingService;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Service
public class RealWorldTestingReportService {

    @Value("${realWorldTestingCriteriaKeys}")
    private String[] eligibleCriteriaKeys;

    @Value("${rwtProgramFirstEligibilityYear}")
    private Integer rwtProgramFirstEligibilityYear;

    @Value("#{T(java.time.LocalDate).parse('${rwtProgramStartDate}')}")
    private LocalDate rwtProgramStartDate;

    private CertifiedProductDAO certifiedProductDAO;
    private ErrorMessageUtil errorMsg;
    private Environment env;
    private CertificationStatusEventsService certificationStatusEventsService;
    private CertificationCriterionService certificationCriterionService;
    private RealWorldTestingEligibilityActivityExplorer realWorldTestingEligibilityActivityExplorer;
    private ListingActivityUtil listingActivityUtil;

    @Autowired
    public RealWorldTestingReportService(CertifiedProductDAO certifiedProductDAO, ErrorMessageUtil errorMsg, Environment env,
            CertificationStatusEventsService certificationStatusEventsService, CertificationCriterionService certificationCriterionService,
            RealWorldTestingEligibilityActivityExplorer realWorldTestingEligibilityActivityExplorer, ListingActivityUtil listingActivityUtil) {

        this.certifiedProductDAO = certifiedProductDAO;
        this.errorMsg = errorMsg;
        this.env = env;
        this.certificationStatusEventsService = certificationStatusEventsService;
        this.certificationCriterionService = certificationCriterionService;
        this.realWorldTestingEligibilityActivityExplorer = realWorldTestingEligibilityActivityExplorer;
        this.listingActivityUtil = listingActivityUtil;
    }

    public List<RealWorldTestingReport> getRealWorldTestingReports(List<Long> acbIds, Logger logger) {
        logger.info("Real World Testing Program Start Date: " + rwtProgramStartDate.toString());
        logger.info("Real World Testing First Eligibility Year: " + rwtProgramFirstEligibilityYear.toString());

        List<RealWorldTestingReport> reports = null;
        try {
            RealWorldTestingService realWorldTestingService =
                    new RealWorldTestingService(certificationCriterionService, realWorldTestingEligibilityActivityExplorer,
                            listingActivityUtil, certifiedProductDAO, eligibleCriteriaKeys, rwtProgramStartDate, rwtProgramFirstEligibilityYear);

            reports = getListingWith2015Edition(logger).stream()
                  .filter(listing -> isInListOfAcbs(listing, acbIds))
                  .map(listing -> getRealWorldTestingReport(listing, realWorldTestingService, logger))
                  .filter(report -> report.getRwtEligibilityYear() != null
                          || report.getRwtPlansCheckDate() != null
                          || report.getRwtPlansUrl() != null
                          || report.getRwtResultsCheckDate() != null
                          || report.getRwtResultsUrl() != null)
                  .collect(Collectors.toList());
        } catch (Exception e) {
            logger.catching(e);
        }
        return reports;
    }

    private List<CertifiedProductDetailsDTO> getListingWith2015Edition(Logger logger) {
        logger.info("Retrieving 2015 Listings");
        List<CertifiedProductDetailsDTO> listings =
                certifiedProductDAO.findByEdition(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
        logger.info("Completed Retreiving 2015 Listings");
        return listings;
    }

    private boolean isRwtPlansEmpty(RealWorldTestingReport report) {
        return StringUtils.isEmpty(report.getRwtPlansUrl());
    }

    private boolean isRwtResultsEmpty(RealWorldTestingReport report) {
        return StringUtils.isEmpty(report.getRwtResultsUrl());
    }

    private boolean isInListOfAcbs(CertifiedProductDetailsDTO listing, List<Long> acbIds) {
        return acbIds.stream()
                .filter(acbId -> acbId.equals(listing.getCertificationBodyId()))
                .findAny()
                .isPresent();
    }

    private RealWorldTestingReport getRealWorldTestingReport(CertifiedProductDetailsDTO listing, RealWorldTestingService realWorldTestingService, Logger logger) {
        RealWorldTestingEligibility rwtElig = realWorldTestingService.getRwtEligibilityYearForListing(listing.getId(), logger);

        logger.info(String.format("ListingId: %s, Elig Year %s, %s",
                listing.getId(),
                rwtElig.getEligibilityYear().isPresent() ? rwtElig.getEligibilityYear().get().toString() : " N/A",
                rwtElig.getReason().getReason()));

        CertificationStatusEvent currentStatus;
        try {
            currentStatus = certificationStatusEventsService.getCurrentCertificationStatusEvent(listing.getId());
        } catch (EntityRetrievalException e) {
            currentStatus = null;
        }

        RealWorldTestingReport report = RealWorldTestingReport.builder()
                .acbName(listing.getCertificationBodyName())
                .chplProductNumber(listing.getChplProductNumber())
                .currentStatus(currentStatus != null ? currentStatus.getStatus().getName() : "")
                .productName(listing.getProduct().getName())
                .productId(listing.getProduct().getId())
                .developerName(listing.getDeveloper().getName())
                .developerId(listing.getDeveloper().getId())
                .rwtEligibilityYear(rwtElig.getEligibilityYear().isPresent() ? rwtElig.getEligibilityYear().get() : null)
                .reason(rwtElig.getReason().getReason())
                .rwtPlansUrl(listing.getRwtPlansUrl())
                .rwtPlansCheckDate(listing.getRwtPlansCheckDate())
                .rwtResultsUrl(listing.getRwtResultsUrl())
                .rwtResultsCheckDate(listing.getRwtResultsCheckDate())
                .build();

        if (rwtElig.getEligibilityYear().isPresent()) {
            return addMessages(report);
        } else {
            return report;
        }
    }

    @SuppressWarnings("checkstyle:linelength")
    private RealWorldTestingReport addMessages(RealWorldTestingReport report) {
        if (isRwtPlansEmpty(report)) {
            if (arePlansLateWarning(report.getRwtEligibilityYear())) {
                report.setRwtPlansMessage(errorMsg.getMessage("realWorldTesting.report.missingPlansWarning",
                        report.getRwtEligibilityYear().toString(),
                        getPlansLateDate(report.getRwtEligibilityYear()).toString()));
            } else if (arePlansLateError(report.getRwtEligibilityYear())) {
                report.setRwtPlansMessage(errorMsg.getMessage("realWorldTesting.report.missingPlansError",
                        report.getRwtEligibilityYear().toString(),
                        getPlansLateDate(report.getRwtEligibilityYear()).toString()));
            }
        }
        if (isRwtResultsEmpty(report)) {
            if (areResultsLateWarning(report.getRwtEligibilityYear())) {
                report.setRwtResultsMessage(errorMsg.getMessage("realWorldTesting.report.missingResultsWarning",
                        report.getRwtEligibilityYear().toString(),
                        getResultsLateDate(report.getRwtEligibilityYear()).toString()));
            } else if (areResultsLateError(report.getRwtEligibilityYear())) {
                report.setRwtResultsMessage(errorMsg.getMessage("realWorldTesting.report.missingResultsError",
                        report.getRwtEligibilityYear().toString(),
                        getResultsLateDate(report.getRwtEligibilityYear()).toString()));
            }
        }
        return report;
    }

    private boolean arePlansLateWarning(Integer rwtEligYear) {
        return isLocalDateEqualOrAfter(LocalDate.now(), getPlansStartDate(rwtEligYear))
                && LocalDate.now().isBefore(getPlansLateDate(rwtEligYear));
    }

    private boolean arePlansLateError(Integer rwtEligYear) {
        return isLocalDateEqualOrAfter(LocalDate.now(), getPlansLateDate(rwtEligYear));
    }

    private boolean areResultsLateWarning(Integer rwtEligYear) {
        return isLocalDateEqualOrAfter(LocalDate.now(), getResultsStartDate(rwtEligYear))
                && LocalDate.now().isBefore(getResultsLateDate(rwtEligYear));
    }

    private boolean areResultsLateError(Integer rwtEligYear) {
        return isLocalDateEqualOrAfter(LocalDate.now(), getResultsLateDate(rwtEligYear));
    }

    private boolean isLocalDateEqualOrAfter(LocalDate date1, LocalDate date2) {
        return date1.isEqual(date2) || date1.isAfter(date2);
    }

    private LocalDate getPlansStartDate(Integer rwtEligYear) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        String mmdd = env.getProperty("rwtPlanStartDayOfYear");
        String mmddyyyy = mmdd + "/" + String.valueOf(rwtEligYear - 1);
        return LocalDate.parse(mmddyyyy, formatter);
    }

    private LocalDate getPlansLateDate(Integer rwtEligYear) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        String mmdd = env.getProperty("rwtPlanDueDate");
        String mmddyyyy = mmdd + "/" + String.valueOf(rwtEligYear - 1);
        return LocalDate.parse(mmddyyyy, formatter);
    }

    private LocalDate getResultsStartDate(Integer rwtEligYear) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        String mmdd = env.getProperty("rwtResultsStartDayOfYear");
        String mmddyyyy = mmdd + "/" + String.valueOf(rwtEligYear + 1);
        return LocalDate.parse(mmddyyyy, formatter);
    }

    private LocalDate getResultsLateDate(Integer rwtEligYear) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        String mmdd = env.getProperty("rwtResultsDueDate");
        String mmddyyyy = mmdd + "/" + String.valueOf(rwtEligYear + 1);
        return LocalDate.parse(mmddyyyy, formatter);
    }

}
