package gov.healthit.chpl.scheduler.job;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingReport;
import gov.healthit.chpl.util.EmailBuilder;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2()
public class RealWorldTestingReportEmailJob implements Job {

    @Autowired
    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    private CertificationBodyDAO certificationBodyDAO;

    @Autowired
    private ErrorMessageUtil errorMsg;

    @Autowired
    private Environment env;

    private List<Long> acbIds = new ArrayList<Long>();

    @SuppressWarnings("checkstyle:linelength")
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Real World Report Email job for " + context.getMergedJobDataMap().getString("email") + " *********");
        try {
            setAcbIds(context);

            List<RealWorldTestingReport> reportRows =
                    certifiedProductDAO.findByEdition(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear()).stream()
                    .filter(listing -> isListingRwtEligible(listing.getRwtEligibilityYear()))
                    .filter(listing -> isInListOfAcbs(listing))
                    .map(listing -> getRealWorldTestingReport(listing))
                    .collect(Collectors.toList());

            sendEmail(context, reportRows);

        } catch (Exception e) {
            LOGGER.catching(e);
        } finally {
            LOGGER.info("********* Completed the Real World Report Email job. *********");
        }
    }

    private boolean isListingRwtEligible(Integer rwtEligYear) {
        return rwtEligYear != null;
    }

    private boolean isRwtPlansEmpty(RealWorldTestingReport report) {
        return StringUtils.isEmpty(report.getRwtPlansUrl());
    }

    private boolean isRwtResultsEmpty(RealWorldTestingReport report) {
        return StringUtils.isEmpty(report.getRwtResultsUrl());
    }

    private boolean isInListOfAcbs(CertifiedProductDetailsDTO listing) {
        return acbIds.stream()
                .filter(acbId -> acbId.equals(listing.getCertificationBodyId()))
                .findAny()
                .isPresent();
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
                .rwtEligibilityYear(listing.getRwtEligibilityYear())
                .build();

        return addMessages(report);
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

    private void sendEmail(JobExecutionContext context, List<RealWorldTestingReport> rows) throws MessagingException {
        EmailBuilder emailBuilder = new EmailBuilder(env);
        emailBuilder.recipient(context.getMergedJobDataMap().getString("email"))
                .subject(env.getProperty("rwt.report.subject"))
                .htmlMessage(String.format(env.getProperty("rwt.report.body"), getAcbNamesAsCommaSeparatedList(context)))
                .fileAttachments(new ArrayList<File>(Arrays.asList(generateCsvFile(context, rows))))
                .sendEmail();
    }

    private File generateCsvFile(JobExecutionContext context, List<RealWorldTestingReport> rows) {
        File outputFile = getOutputFile(env.getProperty("rwt.report.filename") + LocalDate.now().toString());
        outputFile = writeToFile(rows, outputFile);
        return outputFile;
    }

    private File getOutputFile(String reportFilename) {
        File temp = null;
        try {
            temp = File.createTempFile(reportFilename, ".csv");
            temp.deleteOnExit();
        } catch (IOException ex) {
            LOGGER.error("Could not create temporary file " + ex.getMessage(), ex);
        }

        return temp;
    }

    private File writeToFile(List<RealWorldTestingReport> rows, File outputFile) {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputFile),
                Charset.forName("UTF-8").newEncoder());
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL)) {
            writer.write('\ufeff');
            csvPrinter.printRecord(RealWorldTestingReport.getHeaders());
            rows.stream()
                    .forEach(row -> {
                        try {
                            csvPrinter.printRecord(row.toListOfStrings());
                        } catch (Exception e) {
                            LOGGER.error(e);
                        }

                    });
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return outputFile;
    }

    private String getAcbNamesAsCommaSeparatedList(JobExecutionContext jobContext) {
        if (Objects.nonNull(jobContext.getMergedJobDataMap().getString("acb"))) {
            return Arrays.asList(
                    jobContext.getMergedJobDataMap().getString("acb").split(SchedulerManager.DATA_DELIMITER)).stream()
                    .map(acbId -> getAcbName(Long.valueOf(acbId)))
                    .collect(Collectors.joining(", "));
        } else {
            return "";
        }
    }

    private String getAcbName(Long acbId) {
        try {
            return certificationBodyDAO.getById(acbId).getName();
        } catch (NumberFormatException | EntityRetrievalException e) {
            LOGGER.error("Could not retreive ACB name based on value: " + acbId, e);
            return "";
        }
    }
}
