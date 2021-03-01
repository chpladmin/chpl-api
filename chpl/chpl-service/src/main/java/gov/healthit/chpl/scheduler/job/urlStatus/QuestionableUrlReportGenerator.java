package gov.healthit.chpl.scheduler.job.urlStatus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.scheduler.job.urlStatus.data.UrlCheckerDao;
import gov.healthit.chpl.scheduler.job.urlStatus.data.UrlResult;
import gov.healthit.chpl.scheduler.job.urlStatus.data.UrlType;
import gov.healthit.chpl.scheduler.job.urlStatus.email.FailedUrlCsvFormatter;
import gov.healthit.chpl.scheduler.job.urlStatus.email.FailedUrlResult;
import gov.healthit.chpl.scheduler.job.urlStatus.email.QuestionableUrlLookupDao;
import gov.healthit.chpl.util.EmailBuilder;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "questionableUrlReportGeneratorJobLogger")
public class QuestionableUrlReportGenerator extends QuartzJob {
    @Autowired
    private Environment env;

    @Autowired
    private CertifiedProductDAO cpDao;

    @Autowired
    private UrlCheckerDao urlCheckerDao;

    @Autowired
    private QuestionableUrlLookupDao urlLookupDao;

    private FailedUrlCsvFormatter csvFormatter = new FailedUrlCsvFormatter();

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Questionable URL Report Generator job. *********");

        try {
            List<FailedUrlResult> badUrlsToWrite = new ArrayList<FailedUrlResult>();
            List<UrlResult> badUrls = urlCheckerDao.getUrlResultsWithError();
            LOGGER.info("Found " + badUrls.size() + " urls with errors.");
            int i = 0;
            for (UrlResult urlResult : badUrls) {
                switch (urlResult.getUrlType()) {
                case ACB:
                    LOGGER.info("[" + i + "]: Getting ACBs with bad website " + urlResult.getUrl());
                    badUrlsToWrite.addAll(urlLookupDao.getAcbsWithUrl(urlResult));
                    break;
                case ATL:
                    LOGGER.info("[" + i + "] Getting ATLs with bad website " + urlResult.getUrl());
                    badUrlsToWrite.addAll(urlLookupDao.getAtlsWithUrl(urlResult));
                    break;
                case DEVELOPER:
                    LOGGER.info("[" + i + "] Getting Developers with bad website " + urlResult.getUrl());
                    badUrlsToWrite.addAll(urlLookupDao.getDevelopersWithUrl(urlResult));
                    break;
                case FULL_USABILITY_REPORT:
                case MANDATORY_DISCLOSURE:
                case TEST_RESULTS_SUMMARY:
                case REAL_WORLD_TESTING_PLANS:
                case REAL_WORLD_TESTING_RESULTS:
                case STANDARDS_VERSION_ADVANCEMENT_PROCESS_NOTICE:
                    LOGGER.info("[" + i + "] Getting Listings with bad " + urlResult.getUrlType().getName()
                            + " website " + urlResult.getUrl());
                    badUrlsToWrite.addAll(urlLookupDao.getListingsWithUrl(urlResult));
                    break;
                case API_DOCUMENTATION:
                case EXPORT_DOCUMENTATION:
                case DOCUMENTATION:
                case USE_CASES:
                    LOGGER.info("[" + i + "] Getting criteria with bad " + urlResult.getUrlType().getName()
                            + " website " + urlResult.getUrl());
                    badUrlsToWrite.addAll(urlLookupDao.getCertificationResultsWithUrl(urlResult));
                    break;
                default:
                    break;
                }
                i++;
            }

            badUrlsToWrite = filterUrls(badUrlsToWrite, jobContext);

            // sort the bad urls first by url and then by type
            Collections.sort(badUrlsToWrite, new Comparator<FailedUrlResult>() {
                @Override
                public int compare(final FailedUrlResult o1, final FailedUrlResult o2) {
                    if (o1.getUrl().equals(o2.getUrl())) {
                        return o1.getUrlType().ordinal() - o2.getUrlType().ordinal();
                    }
                    return o1.getUrl().compareTo(o2.getUrl());
                }
            });

            LOGGER.info("Creating email subject and body.");
            String to = jobContext.getMergedJobDataMap().getString("email");
            String subject = env.getProperty("job.questionableUrlReport.emailSubject");
            String htmlMessage = env.getProperty("job.questionableUrlReport.emailBodyBegin");
            htmlMessage += createHtmlEmailBody(badUrlsToWrite,
                    env.getProperty("job.questionableUrlReport.emailBodyNoContent"));
            File output = null;
            List<File> files = new ArrayList<File>();
            if (badUrlsToWrite.size() > 0) {
                output = getOutputFile(badUrlsToWrite,
                        env.getProperty("job.questionableUrlReport.emailAttachmentName"));
                files.add(output);
            }

            LOGGER.info("Sending email to {} with contents {} and a total of {} questionable URLs.", to,
                    htmlMessage, badUrlsToWrite.size());
            try {
                List<String> addresses = new ArrayList<String>();
                addresses.add(to);

                EmailBuilder emailBuilder = new EmailBuilder(env);
                emailBuilder.recipients(addresses).subject(subject).htmlMessage(htmlMessage)
                        .fileAttachments(files).sendEmail();
            } catch (MessagingException e) {
                LOGGER.error(e);
            }
        } catch (Exception ex) {
            LOGGER.error("Unable to complete job: " + ex.getMessage(), ex);
        }
        LOGGER.info("********* Completed the Questionable URL Report Generator job. *********");
    }

    private List<FailedUrlResult> filterUrls(List<FailedUrlResult> badUrls, JobExecutionContext jobContext) {
        String acbSpecific = jobContext.getMergedJobDataMap().getString("acbSpecific");
        if (!StringUtils.isEmpty(acbSpecific) && BooleanUtils.toBoolean(acbSpecific)) {
            List<Long> acbIds = getSelectedAcbIds(jobContext);
            return badUrls.stream()
                .filter(badUrl -> isUrlRelatedToAcbs(badUrl, acbIds))
                .filter(badUrl -> doesUrlResultMatchAllowedStatusCodes(badUrl, jobContext))
                .collect(Collectors.toList());
        }
        return badUrls;
    }

    private boolean isUrlRelatedToAcbs(FailedUrlResult urlResult, List<Long> acbIds) {
        if (urlResult.getAcb() != null) {
            return acbIds.contains(urlResult.getAcb().getId());
        }
        if (urlResult.getListing() != null && urlResult.getListing().getAcb() != null) {
            return acbIds.contains(urlResult.getListing().getAcb().getId());
        }
        if (urlResult.getDeveloper() != null) {
            List<CertificationStatusType> activeStatuses = new ArrayList<CertificationStatusType>();
            activeStatuses.add(CertificationStatusType.Active);
            activeStatuses.add(CertificationStatusType.SuspendedByAcb);
            activeStatuses.add(CertificationStatusType.SuspendedByOnc);

            List<CertifiedProductDetailsDTO> filteredListings
                = cpDao.getListingsByStatusForDeveloperAndAcb(urlResult.getDeveloper().getDeveloperId(), activeStatuses, acbIds);
            return filteredListings != null && filteredListings.size() > 0;
        }
        return false;
    }

    private boolean doesUrlResultMatchAllowedStatusCodes(FailedUrlResult urlResult, JobExecutionContext jobContext) {
        String httpStatusIncludeRegex = jobContext.getMergedJobDataMap().getString("httpStatusIncludeRegex");
        if (StringUtils.isEmpty(httpStatusIncludeRegex)) {
            return true;
        }
        if (urlResult.getResponseCode() == null) {
            return true;
        }
        try {
            Pattern httpStatusPattern = Pattern.compile(httpStatusIncludeRegex);
            Matcher httpStatusMatcher = httpStatusPattern.matcher(urlResult.getResponseCode().toString());
            return httpStatusMatcher.matches();
        } catch (PatternSyntaxException ex) {
            LOGGER.error("Invalid pattern: " + httpStatusIncludeRegex, ex);
        }
        return false;
    }

    private List<Long> getSelectedAcbIds(JobExecutionContext jobContext) {
        return Arrays.asList(jobContext.getMergedJobDataMap().getString("acb")
                .split(SchedulerManager.DATA_DELIMITER)).stream()
                .map(str -> Long.parseLong(str))
                .collect(Collectors.toList());
    }

    private File getOutputFile(List<FailedUrlResult> urlResultsToWrite, String reportFilename) {
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
                csvPrinter.printRecord(csvFormatter.getHeaderRow());
                for (int i = 0; i < urlResultsToWrite.size(); i++) {
                    FailedUrlResult currUrlResult = urlResultsToWrite.get(i);
                    List<String> rowValue = csvFormatter.getRow(currUrlResult);
                    if (rowValue != null) {
                        csvPrinter.printRecord(rowValue);
                    }
                }
            } catch (IOException e) {
                LOGGER.error(e);
            }
        }
        return temp;
    }

    private String createHtmlEmailBody(List<FailedUrlResult> urlResults, String noContentMsg) {
        String htmlMessage = "";
        if (urlResults.size() == 0) {
            htmlMessage = noContentMsg;
        } else {
            int brokenAcbUrls = getCountOfBrokenUrlsOfType(urlResults, UrlType.ACB);
            int brokenAtlUrls = getCountOfBrokenUrlsOfType(urlResults, UrlType.ATL);
            int brokenDeveloperUrls = getCountOfBrokenUrlsOfType(urlResults, UrlType.DEVELOPER);
            int brokenMandatoryDisclosureUrls = getCountOfBrokenUrlsOfType(urlResults, UrlType.MANDATORY_DISCLOSURE);
            int brokenTestResultsSummaryUrls = getCountOfBrokenUrlsOfType(urlResults, UrlType.TEST_RESULTS_SUMMARY);
            int brokenFullUsabilityReportUrls = getCountOfBrokenUrlsOfType(urlResults, UrlType.FULL_USABILITY_REPORT);
            int brokenApiDocumentationUrls = getCountOfBrokenUrlsOfType(urlResults, UrlType.API_DOCUMENTATION);
            int brokenExportDocumentationUrls = getCountOfBrokenUrlsOfType(urlResults, UrlType.EXPORT_DOCUMENTATION);
            int brokenDocumentationUrlUrls = getCountOfBrokenUrlsOfType(urlResults, UrlType.DOCUMENTATION);
            int brokenUseCasesUrls = getCountOfBrokenUrlsOfType(urlResults, UrlType.USE_CASES);
            int brokenRwtPlansUrls = getCountOfBrokenUrlsOfType(urlResults, UrlType.REAL_WORLD_TESTING_PLANS);
            int brokenRwtResultsUrls = getCountOfBrokenUrlsOfType(urlResults, UrlType.REAL_WORLD_TESTING_RESULTS);
            int brokenSvapNoticeUrls = getCountOfBrokenUrlsOfType(urlResults, UrlType.STANDARDS_VERSION_ADVANCEMENT_PROCESS_NOTICE);

            htmlMessage += "<ul>";
            htmlMessage += "<li>" + UrlType.ATL.getName() + ": " + brokenAtlUrls + "</li>";
            htmlMessage += "<li>" + UrlType.ACB.getName() + ": " + brokenAcbUrls + "</li>";
            htmlMessage += "<li>" + UrlType.DEVELOPER.getName() + ": " + brokenDeveloperUrls + "</li>";
            htmlMessage += "<li>" + UrlType.FULL_USABILITY_REPORT.getName() + ": " + brokenFullUsabilityReportUrls + "</li>";
            htmlMessage += "<li>" + UrlType.MANDATORY_DISCLOSURE.getName() + ": " + brokenMandatoryDisclosureUrls + "</li>";
            htmlMessage += "<li>" + UrlType.TEST_RESULTS_SUMMARY.getName() + ": " + brokenTestResultsSummaryUrls + "</li>";
            htmlMessage += "<li>" + UrlType.API_DOCUMENTATION.getName() + ": " + brokenApiDocumentationUrls + "</li>";
            htmlMessage += "<li>" + UrlType.EXPORT_DOCUMENTATION.getName() + ": " + brokenExportDocumentationUrls + "</li>";
            htmlMessage += "<li>" + UrlType.DOCUMENTATION.getName() + ": " + brokenDocumentationUrlUrls + "</li>";
            htmlMessage += "<li>" + UrlType.USE_CASES.getName() + ": " + brokenUseCasesUrls + "</li>";
            htmlMessage += "<li>" + UrlType.REAL_WORLD_TESTING_PLANS.getName() + ": " + brokenRwtPlansUrls + "</li>";
            htmlMessage += "<li>" + UrlType.REAL_WORLD_TESTING_RESULTS.getName() + ": " + brokenRwtResultsUrls + "</li>";
            htmlMessage += "<li>" + UrlType.STANDARDS_VERSION_ADVANCEMENT_PROCESS_NOTICE.getName() + ": " + brokenSvapNoticeUrls + "</li>";
            htmlMessage += "</ul>";
        }

        return htmlMessage;
    }

    private int getCountOfBrokenUrlsOfType(List<FailedUrlResult> urlResults, UrlType urlType) {
        int count = 0;
        for (FailedUrlResult urlResult : urlResults) {
            if (urlResult.getUrlType().equals(urlType)) {
                count++;
            }
        }
        return count;
    }
}
