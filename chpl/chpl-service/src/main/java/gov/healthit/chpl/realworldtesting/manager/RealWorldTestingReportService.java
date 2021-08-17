package gov.healthit.chpl.realworldtesting.manager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.certifiedproduct.service.CertificationStatusEventsService;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingReport;
import gov.healthit.chpl.service.RealWorldTestingService;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class RealWorldTestingReportService {

    private CertifiedProductDAO certifiedProductDAO;
    private ErrorMessageUtil errorMsg;
    private Environment env;
    private RealWorldTestingService realWorldTestingService;
    private CertificationStatusEventsService certificationStatusEventsService;


    @Autowired
    public RealWorldTestingReportService(CertifiedProductDAO certifiedProductDAO, ErrorMessageUtil errorMsg, Environment env,
            RealWorldTestingService realWorldTestingService, CertificationStatusEventsService certificationStatusEventsService) {

        this.certifiedProductDAO = certifiedProductDAO;
        this.errorMsg = errorMsg;
        this.env = env;
        this.realWorldTestingService = realWorldTestingService;
        this.certificationStatusEventsService = certificationStatusEventsService;
    }

    public List<RealWorldTestingReport> getRealWorldTestingReports(List<Long> acbIds, Logger logger) {
        List<RealWorldTestingReport> reports = null;
        try {
            reports = getListingWith2015Edition(LOGGER).stream()
                    .filter(listing -> isInListOfAcbs(listing, acbIds))
                    .map(listing -> getRealWorldTestingReport(listing, logger))
                    .filter(report -> report.getRwtEligibilityYear() != null
                            || report.getRwtPlansCheckDate() != null
                            || report.getRwtPlansUrl() != null
                            || report.getRwtResultsCheckDate() != null
                            || report.getRwtResultsUrl() != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.catching(e);
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

    private RealWorldTestingReport getRealWorldTestingReport(CertifiedProductDetailsDTO listing, Logger logger) {
        Optional<Integer> rwtEligYear = realWorldTestingService.getRwtEligibilityYearForListing(listing.getId());

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
                .rwtEligibilityYear(rwtEligYear.isPresent() ? rwtEligYear.get() : null)
                .rwtPlansUrl(listing.getRwtPlansUrl())
                .rwtPlansCheckDate(listing.getRwtPlansCheckDate())
                .rwtResultsUrl(listing.getRwtResultsUrl())
                .rwtResultsCheckDate(listing.getRwtResultsCheckDate())
                .build();

        if (rwtEligYear.isPresent()) {
            return addMessages(report, logger);
        } else {
            return report;
        }
    }

    @SuppressWarnings("checkstyle:linelength")
    private RealWorldTestingReport addMessages(RealWorldTestingReport report, Logger logger) {
        logger.info("Checking/Adding messages for listing: " + report.getChplProductNumber());
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
        logger.info("Completed Checking/Adding messages for listing: " + report.getChplProductNumber());
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
