package gov.healthit.chpl.scheduler.job;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingReport;
import lombok.extern.log4j.Log4j2;

@Log4j2()
public class RealWorldTestingReportEmailJob implements Job {

    @Autowired
    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    private Environment env;

    private LocalDate planStartDate;

    private LocalDate planLateDate;

    private List<Long> acbIds = new ArrayList<Long>();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Real World Report Email job for " + context.getMergedJobDataMap().getString("email") + " *********");
        try {
            setPlanStartDate();
            setPlanLateDate();
            setAcbIds(context);

            List<RealWorldTestingReport> reportRows =
                    certifiedProductDAO.findByEdition(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear()).stream()
                    .filter(listing -> isListingRwtEligible(listing.getRwtEligibilityYear()))
                    .filter(listing -> isInListOfAcbs(listing))
                    .map(listing -> getRealWorldTestingReport(listing))
                    .collect(Collectors.toList());

            LOGGER.info(reportRows);

        } catch (Exception e) {
            LOGGER.catching(e);
        } finally {
            LOGGER.info("********* Completed the Real World Report Email job. *********");
        }
    }

    private boolean isListingRwtEligible(Integer rwtEligYear) {
        return rwtEligYear != null;
    }

    private boolean isRwtPlanEmpty(RealWorldTestingReport report) {
        return StringUtils.isEmpty(report.getRwtPlansUrl());
    }

    private boolean isInListOfAcbs(CertifiedProductDetailsDTO listing) {
        return acbIds.stream()
                .filter(acbId -> acbId.equals(listing.getCertificationBodyId()))
                .findAny()
                .isPresent();
    }

    private void setPlanStartDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        String mmdd = env.getProperty("rwtPlanStartDayOfYear");
        String mmddyyyy = mmdd + "/" + String.valueOf(LocalDate.now().getYear());
        planStartDate = LocalDate.parse(mmddyyyy, formatter);
    }

    private void setPlanLateDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        String mmdd = env.getProperty("rwtPlanDueDate");
        String mmddyyyy = mmdd + "/" + String.valueOf(LocalDate.now().getYear());
        planLateDate = LocalDate.parse(mmddyyyy, formatter);
    }

    private void setAcbIds(JobExecutionContext context) {
        acbIds = Arrays.asList(context.getMergedJobDataMap().getString("acb").split(SchedulerManager.DATA_DELIMITER)).stream()
                .map(acb -> Long.parseLong(acb))
                .collect(Collectors.toList());
    }

    private RealWorldTestingReport getRealWorldTestingReport(CertifiedProductDetailsDTO listing) {
        RealWorldTestingReport report = RealWorldTestingReport.builder()
                .acbName(listing.getCertificationBodyName())
                .chplProductNumber(listing.getChplProductNumber())
                .productName(listing.getProduct().getName())
                .productId(listing.getProduct().getId())
                .developerName(listing.getDeveloper().getName())
                .developerId(listing.getDeveloper().getId())
                .rwtPlansUrl(listing.getRwtPlansUrl())
                .rwtPlansCheckDate(listing.getRwtPlansCheckDate())
                .rwtResultsUrl(listing.getRwtResultsUrl())
                .rwtResultsCheckDate(listing.getRwtResultsCheckDate())
                .build();

        return addMessages(report);
    }

    private RealWorldTestingReport addMessages(RealWorldTestingReport report) {
        if (isRwtPlanEmpty(report)) {
            if (arePlansLateWarning()) {
                report.setRwtPlansMessage("WARNING: Listing requires RWT Plans by " + planLateDate.toString());
            } else if (arePlansLateError()) {
                report.setRwtPlansMessage("Listing requires RWT Plans by " + planLateDate.toString());
            }
        }
        return report;
    }

    private boolean arePlansLateWarning() {
        return isLocalDateEqualOrAfter(LocalDate.now(), planStartDate)
                && LocalDate.now().isBefore(planLateDate);
    }

    private boolean arePlansLateError() {
        return isLocalDateEqualOrAfter(LocalDate.now(), planLateDate);
    }

    private boolean isLocalDateEqualOrAfter(LocalDate date1, LocalDate date2) {
        return date1.isEqual(date2) || date1.isAfter(date2);
    }

}
