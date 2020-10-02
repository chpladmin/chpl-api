package gov.healthit.chpl.scheduler.job;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.scheduler.BrokenSurveillanceRulesDAO;
import gov.healthit.chpl.domain.surveillance.SurveillanceOversightRule;
import gov.healthit.chpl.dto.scheduler.BrokenSurveillanceRulesDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.util.EmailBuilder;

public class BrokenSurveillanceRulesEmailJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("brokenSurveillanceRulesEmailJobLogger");
    private DateTimeFormatter dateFormatter;
    private Map<SurveillanceOversightRule, Integer> allBrokenRulesCounts;

    @Autowired
    private BrokenSurveillanceRulesDAO brokenSurveillanceRulesDAO;

    @Autowired
    private CertificationBodyDAO certificationBodyDAO;

    @Autowired
    private Environment env;

    private static final String TRIGGER_DESCRIPTIONS = "<h4>Description of Surveillance Rules</h4>" + "<ol>" + "<li>"
            + SurveillanceOversightRule.LONG_SUSPENSION.getTitle() + ": "
            + SurveillanceOversightRule.LONG_SUSPENSION.getDescription() + "</li>" + "<li>"
            + SurveillanceOversightRule.CAP_NOT_APPROVED.getTitle() + ": "
            + SurveillanceOversightRule.CAP_NOT_APPROVED.getDescription() + "</li>" + "<li>"
            + SurveillanceOversightRule.CAP_NOT_STARTED.getTitle() + ": "
            + SurveillanceOversightRule.CAP_NOT_STARTED.getDescription() + "</li>" + "<li>"
            + SurveillanceOversightRule.CAP_NOT_COMPLETED.getTitle() + ": "
            + SurveillanceOversightRule.CAP_NOT_COMPLETED.getDescription() + "</li>" + "<li>"
            + SurveillanceOversightRule.CAP_NOT_CLOSED.getTitle() + ": "
            + SurveillanceOversightRule.CAP_NOT_CLOSED.getDescription() + "</li>" + "<li>"
            + SurveillanceOversightRule.NONCONFORMITY_OPEN_CAP_COMPLETE.getTitle() + ": "
            + SurveillanceOversightRule.NONCONFORMITY_OPEN_CAP_COMPLETE.getDescription() + "</li>" + "</ol>";

    public BrokenSurveillanceRulesEmailJob() throws Exception {
        super();
        allBrokenRulesCounts = new HashMap<SurveillanceOversightRule, Integer>();
        allBrokenRulesCounts.put(SurveillanceOversightRule.LONG_SUSPENSION, 0);
        allBrokenRulesCounts.put(SurveillanceOversightRule.CAP_NOT_APPROVED, 0);
        allBrokenRulesCounts.put(SurveillanceOversightRule.CAP_NOT_STARTED, 0);
        allBrokenRulesCounts.put(SurveillanceOversightRule.CAP_NOT_COMPLETED, 0);
        allBrokenRulesCounts.put(SurveillanceOversightRule.CAP_NOT_CLOSED, 0);
        allBrokenRulesCounts.put(SurveillanceOversightRule.NONCONFORMITY_OPEN_CAP_COMPLETE, 0);
        dateFormatter = DateTimeFormatter.ofPattern("uuuu/MM/dd");
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Broken Surveillance Rules Email job. *********");
        LOGGER.info("Sending email to: " + jobContext.getMergedJobDataMap().getString("email"));

        List<BrokenSurveillanceRulesDTO> errors = getAppropriateErrors(jobContext);
        String filename = getFileName(jobContext);
        File output = null;
        List<File> files = new ArrayList<File>();
        if (errors.size() > 0) {
            output = getOutputFile(errors, filename);
            files.add(output);
        }
        String to = jobContext.getMergedJobDataMap().getString("email");
        String subject = null;
        String htmlMessage = null;
        if (jobContext.getMergedJobDataMap().getString("type").equalsIgnoreCase("All")) {
            subject = env.getProperty("oversightEmailAcbWeeklySubjectSuffix");
            htmlMessage = String.format(env.getProperty("oversightEmailAcbWeeklyHtmlMessage"), getAcbNamesAsCommaSeparatedList(jobContext));
            htmlMessage += createHtmlEmailBody(errors.size(), env.getProperty("oversightEmailWeeklyNoContent"));
        } else {
            subject = env.getProperty("oversightEmailAcbDailySubjectSuffix");
            htmlMessage = String.format(env.getProperty("oversightEmailAcbDailyHtmlMessage"), getAcbNamesAsCommaSeparatedList(jobContext));
            htmlMessage += createHtmlEmailBody(errors.size(), env.getProperty("oversightEmailDailyNoContent"));
        }
        LOGGER.info("Sending email to {} with contents {} and a total of {} broken rules",
                to, htmlMessage, errors.size());
        try {
            List<String> addresses = new ArrayList<String>();
            addresses.add(to);

            EmailBuilder emailBuilder = new EmailBuilder(env);
            emailBuilder.recipients(addresses)
            .subject(subject)
            .htmlMessage(htmlMessage)
            .fileAttachments(files)
            .sendEmail();
        } catch (MessagingException e) {
            LOGGER.error(e);
        }
        LOGGER.info("********* Completed the Broken Surveillance Rules Email job. *********");
    }

    private List<BrokenSurveillanceRulesDTO> getAppropriateErrors(JobExecutionContext jobContext) {
        List<BrokenSurveillanceRulesDTO> allErrors = brokenSurveillanceRulesDAO.findAll();
        List<BrokenSurveillanceRulesDTO> filteredErrors = new ArrayList<BrokenSurveillanceRulesDTO>();
        List<BrokenSurveillanceRulesDTO> errors = new ArrayList<BrokenSurveillanceRulesDTO>();
        if (jobContext.getMergedJobDataMap().getString("type").equalsIgnoreCase("Overnight")) {
            Date today = new Date();
            LocalDateTime brokenToday = LocalDateTime.ofInstant(Instant.ofEpochMilli(today.getTime()),
                    ZoneId.systemDefault());
            String formattedToday = dateFormatter.format(brokenToday);
            for (BrokenSurveillanceRulesDTO error : allErrors) {
                if (happenedOvernight(error, formattedToday)) {
                    filteredErrors.add(error);
                }
            }
        } else {
            filteredErrors.addAll(allErrors);
        }
        List<Long> acbIds = Arrays.asList(
                jobContext.getMergedJobDataMap().getString("acb").split(SchedulerManager.DATA_DELIMITER)).stream()
                .map(acb -> Long.parseLong(acb))
                .collect(Collectors.toList());

        errors = filteredErrors.stream()
                .filter(error -> acbIds.contains(error.getCertificationBody().getId()))
                .collect(Collectors.toList());

        return errors;
    }

    private boolean happenedOvernight(BrokenSurveillanceRulesDTO error, String formattedToday) {
        if (formattedToday.equalsIgnoreCase(error.getLengthySuspensionRule())) {
            return true;
        }
        if (formattedToday.equalsIgnoreCase(error.getCapNotApprovedRule())) {
            return true;
        }
        if (formattedToday.equalsIgnoreCase(error.getCapNotStartedRule())) {
            return true;
        }
        if (formattedToday.equalsIgnoreCase(error.getCapNotCompletedRule())) {
            return true;
        }
        if (formattedToday.equalsIgnoreCase(error.getCapNotClosedRule())) {
            return true;
        }
        if (formattedToday.equalsIgnoreCase(error.getClosedCapWithOpenNonconformityRule())) {
            return true;
        }
        return false;
    }

    private File getOutputFile(List<BrokenSurveillanceRulesDTO> errors, String reportFilename) {
        File temp = null;
        try {
            temp = File.createTempFile(reportFilename, ".csv");
            temp.deleteOnExit();
        } catch (IOException ex) {
            LOGGER.error("Could not create temporary file " + ex.getMessage(), ex);
        }

        if (temp != null) {
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(temp),
                    Charset.forName("UTF-8").newEncoder());
                    CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL)) {
                writer.write('\ufeff');
                csvPrinter.printRecord(getHeaderRow());
                for (BrokenSurveillanceRulesDTO error : errors) {
                    List<String> rowValue = generateRowValue(error);
                    csvPrinter.printRecord(rowValue);
                    updateSummary(error);
                }
            } catch (IOException e) {
                LOGGER.error(e);
            }
        }
        return temp;
    }

    private List<String> getHeaderRow() {
        List<String> result = new ArrayList<String>();
        result.add("Developer");
        result.add("Product");
        result.add("Version");
        result.add("CHPL ID");
        result.add("URL");
        result.add("ONC-ACB");
        result.add("Certification Status");
        result.add("Date of Last Status Change");
        result.add("Surveillance ID");
        result.add("Date Surveillance Began");
        result.add("Date Surveillance Ended");
        result.add("Surveillance Type");
        result.add("Lengthy Suspension Rule");
        result.add("CAP Not Approved Rule");
        result.add("CAP Not Started Rule");
        result.add("CAP Not Completed Rule");
        result.add("CAP Not Closed Rule");
        result.add("Closed CAP with Open Nonconformity Rule");
        result.add("Non-conformity (Y/N)");
        result.add("Nonconformity Status");
        result.add("Non-conformity Criteria");
        result.add("Date of Determination of Non-Conformity");
        result.add("Corrective Action Plan Approved Date");
        result.add("Date Corrective Action Began");
        result.add("Date Corrective Action Must Be Completed");
        result.add("Date Corrective Action Was Completed");
        result.add("Number of Days from Determination to CAP Approval");
        result.add("Number of Days from Determination to Present");
        result.add("Number of Days from CAP Approval to CAP Began");
        result.add("Number of Days from CAP Approval to Present");
        result.add("Number of Days from CAP Began to CAP Completed");
        result.add("Number of Days from CAP Began to Present");
        result.add("Difference from CAP Completed and CAP Must Be Completed");
        return result;
    }

    private List<String> generateRowValue(BrokenSurveillanceRulesDTO data) {
        List<String> result = new ArrayList<String>();
        result.add(data.getDeveloper());
        result.add(data.getProduct());
        result.add(data.getVersion());
        result.add(data.getChplProductNumber());
        result.add(data.getUrl());
        result.add(data.getCertificationBody().getName());
        result.add(data.getCertificationStatus());
        result.add(data.getDateOfLastStatusChange());
        result.add(data.getSurveillanceId());
        result.add(data.getDateSurveillanceBegan());
        result.add(data.getDateSurveillanceEnded());
        result.add(data.getSurveillanceType());
        result.add(data.getLengthySuspensionRule());
        result.add(data.getCapNotApprovedRule());
        result.add(data.getCapNotStartedRule());
        result.add(data.getCapNotCompletedRule());
        result.add(data.getCapNotClosedRule());
        result.add(data.getClosedCapWithOpenNonconformityRule());
        result.add(data.getNonconformity() ? "Y" : "N");
        result.add(data.getNonconformityStatus());
        result.add(data.getNonconformityCriteria());
        result.add(data.getDateOfDeterminationOfNonconformity());
        result.add(data.getCorrectiveActionPlanApprovedDate());
        result.add(data.getDateCorrectiveActionBegan());
        result.add(data.getDateCorrectiveActionMustBeCompleted());
        result.add(data.getDateCorrectiveActionWasCompleted());
        result.add(data.getNumberOfDaysFromDeterminationToCapApproval() > Long.MIN_VALUE
                ? "" + data.getNumberOfDaysFromDeterminationToCapApproval()
                : "");
        result.add(data.getNumberOfDaysFromDeterminationToPresent() > Long.MIN_VALUE
                ? "" + data.getNumberOfDaysFromDeterminationToPresent()
                : "");
        result.add(data.getNumberOfDaysFromCapApprovalToCapBegan() > Long.MIN_VALUE
                ? "" + data.getNumberOfDaysFromCapApprovalToCapBegan()
                : "");
        result.add(data.getNumberOfDaysFromCapApprovalToPresent() > Long.MIN_VALUE
                ? "" + data.getNumberOfDaysFromCapApprovalToPresent()
                : "");
        result.add(data.getNumberOfDaysFromCapBeganToCapCompleted() > Long.MIN_VALUE
                ? "" + data.getNumberOfDaysFromCapBeganToCapCompleted()
                : "");
        result.add(data.getNumberOfDaysFromCapBeganToPresent() > Long.MIN_VALUE
                ? "" + data.getNumberOfDaysFromCapBeganToPresent()
                : "");
        result.add(data.getDifferenceFromCapCompletedAndCapMustBeCompleted() > Long.MIN_VALUE
                ? "" + data.getDifferenceFromCapCompletedAndCapMustBeCompleted()
                : "N/A");

        return result;
    }

    private void updateSummary(BrokenSurveillanceRulesDTO data) {
        if (!StringUtils.isEmpty(data.getLengthySuspensionRule())) {
            allBrokenRulesCounts.put(SurveillanceOversightRule.LONG_SUSPENSION,
                    allBrokenRulesCounts.get(SurveillanceOversightRule.LONG_SUSPENSION) + 1);
        }
        if (!StringUtils.isEmpty(data.getCapNotApprovedRule())) {
            allBrokenRulesCounts.put(SurveillanceOversightRule.CAP_NOT_APPROVED,
                    allBrokenRulesCounts.get(SurveillanceOversightRule.CAP_NOT_APPROVED) + 1);
        }
        if (!StringUtils.isEmpty(data.getCapNotStartedRule())) {
            allBrokenRulesCounts.put(SurveillanceOversightRule.CAP_NOT_STARTED,
                    allBrokenRulesCounts.get(SurveillanceOversightRule.CAP_NOT_STARTED) + 1);
        }
        if (!StringUtils.isEmpty(data.getCapNotCompletedRule())) {
            allBrokenRulesCounts.put(SurveillanceOversightRule.CAP_NOT_COMPLETED,
                    allBrokenRulesCounts.get(SurveillanceOversightRule.CAP_NOT_COMPLETED) + 1);
        }
        if (!StringUtils.isEmpty(data.getCapNotClosedRule())) {
            allBrokenRulesCounts.put(SurveillanceOversightRule.CAP_NOT_CLOSED,
                    allBrokenRulesCounts.get(SurveillanceOversightRule.CAP_NOT_CLOSED) + 1);
        }
        if (!StringUtils.isEmpty(data.getClosedCapWithOpenNonconformityRule())) {
            allBrokenRulesCounts.put(SurveillanceOversightRule.NONCONFORMITY_OPEN_CAP_COMPLETE,
                    allBrokenRulesCounts.get(SurveillanceOversightRule.NONCONFORMITY_OPEN_CAP_COMPLETE) + 1);
        }
    }

    private String createHtmlEmailBody(int numRecords, String noContentMsg) {
        String htmlMessage = "";
        if (numRecords == 0) {
            htmlMessage = noContentMsg;
        } else {
            htmlMessage += "<ul>";
            htmlMessage += "<li>" + SurveillanceOversightRule.LONG_SUSPENSION.getTitle() + ": "
                    + allBrokenRulesCounts.get(SurveillanceOversightRule.LONG_SUSPENSION) + "</li>";
            htmlMessage += "<li>" + SurveillanceOversightRule.CAP_NOT_APPROVED.getTitle() + ": "
                    + allBrokenRulesCounts.get(SurveillanceOversightRule.CAP_NOT_APPROVED) + "</li>";
            htmlMessage += "<li>" + SurveillanceOversightRule.CAP_NOT_STARTED.getTitle() + ": "
                    + allBrokenRulesCounts.get(SurveillanceOversightRule.CAP_NOT_STARTED) + "</li>";
            htmlMessage += "<li>" + SurveillanceOversightRule.CAP_NOT_COMPLETED.getTitle() + ": "
                    + allBrokenRulesCounts.get(SurveillanceOversightRule.CAP_NOT_COMPLETED) + "</li>";
            htmlMessage += "<li>" + SurveillanceOversightRule.CAP_NOT_CLOSED.getTitle() + ": "
                    + allBrokenRulesCounts.get(SurveillanceOversightRule.CAP_NOT_CLOSED) + "</li>";
            htmlMessage += "<li>" + SurveillanceOversightRule.NONCONFORMITY_OPEN_CAP_COMPLETE.getTitle() + ": "
                    + allBrokenRulesCounts.get(SurveillanceOversightRule.NONCONFORMITY_OPEN_CAP_COMPLETE) + "</li>";
            htmlMessage += "</ul>";
        }

        htmlMessage += TRIGGER_DESCRIPTIONS;
        return htmlMessage;
    }

    private String getAcbNamesAsCommaSeparatedList(JobExecutionContext jobContext) {
        if (Objects.nonNull(jobContext.getMergedJobDataMap().getString("acb"))) {
            return Arrays.asList(
                    jobContext.getMergedJobDataMap().getString("acb").split(SchedulerManager.DATA_DELIMITER)).stream()
                    .map(acbId -> {
                        try {
                            return certificationBodyDAO.getById(Long.parseLong(acbId)).getName();
                        } catch (NumberFormatException | EntityRetrievalException e) {
                            LOGGER.error("Could not retreive ACB name based on value: " + acbId, e);
                            return "";
                        }
                    })
                    .collect(Collectors.joining(", "));
        } else {
            return "";
        }
    }

    private String getFileName(JobExecutionContext jobContext) {
        if (jobContext.getMergedJobDataMap().getString("type").equalsIgnoreCase("All")) {
            return env.getProperty("oversightEmailWeeklyFileName");
        } else {
            return  env.getProperty("oversightEmailDailyFileName");
        }
    }
}
