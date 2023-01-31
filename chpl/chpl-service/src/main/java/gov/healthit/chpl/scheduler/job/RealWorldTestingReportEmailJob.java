package gov.healthit.chpl.scheduler.job;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingReport;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingReportSummary;
import gov.healthit.chpl.realworldtesting.manager.RealWorldTestingReportService;
import gov.healthit.chpl.scheduler.job.realworldtesting.RealWorldTestingReportSummaryCalculator;
import gov.healthit.chpl.util.NullSafeEvaluator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "realWorldTestingReportEmailJobLogger")
public class RealWorldTestingReportEmailJob implements Job {

    @Autowired
    private RealWorldTestingReportService rwtReportService;

    @Autowired
    private CertificationBodyDAO certificationBodyDAO;

    @Autowired
    private Environment env;

    @Autowired
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    private List<Long> acbIds = new ArrayList<Long>();

    @SuppressWarnings("checkstyle:linelength")
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Real World Report Email job for " + context.getMergedJobDataMap().getString("email") + " *********");
        try {
            setAcbIds(context);
            List<RealWorldTestingReport> reportRows = rwtReportService.getRealWorldTestingReports(acbIds, LOGGER);
            sendEmail(context, reportRows);
        } catch (Exception e) {
            LOGGER.catching(e);
        } finally {
            LOGGER.info("********* Completed the Real World Report Email job. *********");
        }
    }

    private void setAcbIds(JobExecutionContext context) {
        acbIds = Arrays.asList(context.getMergedJobDataMap().getString("acb").split(SchedulerManager.DATA_DELIMITER)).stream()
                .map(acb -> Long.parseLong(acb))
                .collect(Collectors.toList());
    }

    private void sendEmail(JobExecutionContext context, List<RealWorldTestingReport> rows) throws EmailNotSentException {
        LOGGER.info("Sending email to: " + context.getMergedJobDataMap().getString("email"));
        chplEmailFactory.emailBuilder()
                .recipient(context.getMergedJobDataMap().getString("email"))
                .subject(env.getProperty("rwt.report.subject"))
                .htmlMessage(createHtmlMessage(context, rows))
                .fileAttachments(new ArrayList<File>(Arrays.asList(generateCsvFile(context, rows))))
                .sendEmail();
        LOGGER.info("Completed Sending email to: " + context.getMergedJobDataMap().getString("email"));
    }

    private String createHtmlMessage(JobExecutionContext context, List<RealWorldTestingReport> rows) {
        return chplHtmlEmailBuilder.initialize()
                .heading(env.getProperty("rwt.report.subject"))
                .paragraph(String.format(env.getProperty("rwt.report.body")), getAcbNamesAsBrSeparatedList(context))
                .paragraph("Real World Testing Report Summary", getEmailSummaryParagraph(rows))
                .footer(true)
                .build();
    }

    private File generateCsvFile(JobExecutionContext context, List<RealWorldTestingReport> rows) {
        LOGGER.info("Generating CSV attachment");
        File outputFile = getOutputFile(env.getProperty("rwt.report.filename") + LocalDate.now().toString());
        outputFile = writeToFile(rows, outputFile);
        LOGGER.info("Completed Generating CSV attachment");
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

    private String getAcbNamesAsBrSeparatedList(JobExecutionContext jobContext) {
        if (Objects.nonNull(jobContext.getMergedJobDataMap().getString("acb"))) {
            return Arrays.asList(
                    jobContext.getMergedJobDataMap().getString("acb").split(SchedulerManager.DATA_DELIMITER)).stream()
                    .map(acbId -> getAcbName(Long.valueOf(acbId)))
                    .collect(Collectors.joining("<br />"));
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

    private String getEmailSummaryParagraph(List<RealWorldTestingReport> rows) {
        Integer currentRwtYear = getCurrentRwtYear();
        List<Integer> rwtYears = List.of(currentRwtYear, currentRwtYear - 1);
        StringBuffer paragraph = new StringBuffer();
        rwtYears.forEach(year -> {
            RealWorldTestingReportSummary summary = RealWorldTestingReportSummaryCalculator.calculateSummariesByEligibityYear(rows, year);
            paragraph.append("Real World Testing for ").append(summary.getRwtEligibilityYear()).append(":<br/>")
                    .append("<ul>")
                    .append("<li>").append(NullSafeEvaluator.eval(() -> summary.getTotalListings(), 0)).append(" Total listing for this period</li>")
                    .append("<li>").append(NullSafeEvaluator.eval(() -> summary.getTotalWithdrawn(), 0)).append(" Withdrawn, no longer eligible</li>")
                    .append("<li>").append(NullSafeEvaluator.eval(() -> summary.getTotalActive(), 0)).append(" Active</li>")
                    .append("<li>").append(NullSafeEvaluator.eval(() -> summary.getTotalEligibleViaIcs(), 0)).append(" Eligible via ICS</li>")
                    .append("<li>").append(NullSafeEvaluator.eval(() -> summary.getTotalWithPlansUrl(), 0)).append(" Have RWT plans URL</li>")
                    .append("<li>").append(NullSafeEvaluator.eval(() -> summary.getTotalWithPlansUrlValidated(), 0)).append(" RWT plans validated</li>")
                    .append("<li>").append(NullSafeEvaluator.eval(() -> summary.getTotalWithResultsUrl(), 0)).append(" Have RWT results URL</li>")
                    .append("<li>").append(NullSafeEvaluator.eval(() -> summary.getTotalWithResultsUrlValidated(), 0)).append(" RWT results validated</li>")
                    .append("</ul>").append("<br />");
        });
        return paragraph.toString();
    }

    private Integer getCurrentRwtYear() {
        DayAndMonth rwtPlanStartDayAndMonth = getRwtPlanStartDayAndMonth();
        LocalDate rwtPlanStartDate = LocalDate.of(LocalDate.now().getYear(), rwtPlanStartDayAndMonth.getMonth(), rwtPlanStartDayAndMonth.getDay());
        if (rwtPlanStartDate.equals(LocalDate.now()) || rwtPlanStartDate.isBefore(LocalDate.now())) {
            return LocalDate.now().getYear() + 1;
        } else {
            return LocalDate.now().getYear();
        }
    }

    private DayAndMonth getRwtPlanStartDayAndMonth() {
        String[] dateParts = env.getProperty("rwtPlanStartDayOfYear").split("/");
        return new DayAndMonth(Integer.valueOf(dateParts[1]), Integer.valueOf(dateParts[0]));
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class DayAndMonth {
        private Integer day;
        private Integer month;
    }
}
