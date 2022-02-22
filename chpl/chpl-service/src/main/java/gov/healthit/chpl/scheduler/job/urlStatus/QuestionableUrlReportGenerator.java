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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.scheduler.job.urlStatus.data.UrlCheckerDao;
import gov.healthit.chpl.scheduler.job.urlStatus.data.UrlResult;
import gov.healthit.chpl.scheduler.job.urlStatus.data.UrlType;
import gov.healthit.chpl.scheduler.job.urlStatus.email.FailedUrlCsvFormatter;
import gov.healthit.chpl.scheduler.job.urlStatus.email.FailedUrlResult;
import gov.healthit.chpl.scheduler.job.urlStatus.email.QuestionableUrlLookupDao;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "questionableUrlReportGeneratorJobLogger")
public class QuestionableUrlReportGenerator extends QuartzJob {

    @Autowired
    private Environment env;

    @Autowired
    private ChplHtmlEmailBuilder htmlEmailBuilder;

    @Autowired
    private CertifiedProductDAO cpDao;

    @Autowired
    private CertificationBodyDAO acbDao;

    @Autowired
    private UrlCheckerDao urlCheckerDao;

    @Autowired
    private QuestionableUrlLookupDao urlLookupDao;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    @Value("${job.questionableUrlReport.emailSubject}")
    private String emailSubject;

    @Value("${job.questionableUrlReport.emailBodyNoContent}")
    private String emailBodyNoContent;

    @Value("${job.questionableUrlReport.emailBodyTitle}")
    private String emailBodyTitle;

    @Value("${job.questionableUrlReport.acbSpecific.emailBodyAcbNames}")
    private String emailBodyAcbNames;

    @Value("${job.questionableUrlReport.emailAttachmentName}")
    private String emailAttachmentName;

    private FailedUrlCsvFormatter csvFormatter = new FailedUrlCsvFormatter();
    private List<CertificationStatusType> activeStatuses = new ArrayList<CertificationStatusType>();

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Questionable URL Report Generator job. *********");
        activeStatuses.add(CertificationStatusType.Active);
        activeStatuses.add(CertificationStatusType.SuspendedByAcb);
        activeStatuses.add(CertificationStatusType.SuspendedByOnc);

        try {
            List<FailedUrlResult> questionableUrls = new ArrayList<FailedUrlResult>();
            List<UrlResult> allQuestionableUrlResults = urlCheckerDao.getUrlResultsWithError();
            LOGGER.info("Found " + allQuestionableUrlResults.size() + " urls with errors.");
            int i = 0;
            for (UrlResult questionableUrlResult : allQuestionableUrlResults) {
                switch (questionableUrlResult.getUrlType()) {
                case ACB:
                    LOGGER.info("[" + i + "]: Getting ACBs with bad website " + questionableUrlResult.getUrl());
                    questionableUrls.addAll(urlLookupDao.getAcbsWithUrl(questionableUrlResult));
                    break;
                case ATL:
                    LOGGER.info("[" + i + "] Getting ATLs with bad website " + questionableUrlResult.getUrl());
                    questionableUrls.addAll(urlLookupDao.getAtlsWithUrl(questionableUrlResult));
                    break;
                case DEVELOPER:
                    LOGGER.info("[" + i + "] Getting Developers with bad website " + questionableUrlResult.getUrl());
                    questionableUrls.addAll(urlLookupDao.getDevelopersWithUrl(questionableUrlResult));
                    break;
                case FULL_USABILITY_REPORT:
                case MANDATORY_DISCLOSURE:
                case TEST_RESULTS_SUMMARY:
                case REAL_WORLD_TESTING_PLANS:
                case REAL_WORLD_TESTING_RESULTS:
                case STANDARDS_VERSION_ADVANCEMENT_PROCESS_NOTICE:
                    LOGGER.info("[" + i + "] Getting Listings with bad " + questionableUrlResult.getUrlType().getName()
                            + " website " + questionableUrlResult.getUrl());
                    questionableUrls.addAll(urlLookupDao.getListingsWithUrl(questionableUrlResult));
                    break;
                case API_DOCUMENTATION:
                case EXPORT_DOCUMENTATION:
                case DOCUMENTATION:
                case USE_CASES:
                case SERVICE_BASE_URL_LIST:
                    LOGGER.info("[" + i + "] Getting criteria with bad " + questionableUrlResult.getUrlType().getName()
                            + " website " + questionableUrlResult.getUrl());
                    questionableUrls.addAll(urlLookupDao.getCertificationResultsWithUrl(questionableUrlResult));
                    break;
                default:
                    break;
                }
                i++;
            }

            questionableUrls = filterUrls(questionableUrls, jobContext);

            // sort the questionable urls first by url and then by type
            Collections.sort(questionableUrls, new Comparator<FailedUrlResult>() {
                @Override
                public int compare(final FailedUrlResult o1, final FailedUrlResult o2) {
                    if (o1.getUrl().equals(o2.getUrl())) {
                        return o1.getUrlType().ordinal() - o2.getUrlType().ordinal();
                    }
                    return o1.getUrl().compareTo(o2.getUrl());
                }
            });

            sendEmail(questionableUrls, jobContext);
        } catch (Exception ex) {
            LOGGER.error("Unable to complete job: " + ex.getMessage(), ex);
        }
        LOGGER.info("********* Completed the Questionable URL Report Generator job. *********");
    }

    private List<FailedUrlResult> filterUrls(List<FailedUrlResult> badUrls, JobExecutionContext jobContext) {
        if (isAcbSpecific(jobContext)) {
            List<Long> acbIds = getSelectedAcbIds(jobContext);
            return badUrls.stream()
                .filter(badUrl -> isUrlRelatedToAcbs(badUrl, acbIds))
                .filter(badUrl -> isNotListingUrl(badUrl) || (isUrlRelatedTo2015Edition(badUrl) && isUrlRelatedToActiveListing(badUrl)))
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
        if (urlResult.getDeveloper() != null && isActive(urlResult.getDeveloper())) {
            List<CertifiedProductDetailsDTO> filteredListings
                = cpDao.getListingsByStatusForDeveloperAndAcb(urlResult.getDeveloper().getDeveloperId(), activeStatuses, acbIds);
            return filteredListings != null && filteredListings.size() > 0;
        }
        return false;
    }

    private boolean isActive(Developer developer) {
        return developer.getStatus() != null
                && !StringUtils.isEmpty(developer.getStatus().getStatus())
                && developer.getStatus().getStatus().equals(DeveloperStatusType.Active.getName());
    }

    private boolean isNotListingUrl(FailedUrlResult urlResult) {
        return urlResult.getListing() == null;
    }

    private boolean isUrlRelatedTo2015Edition(FailedUrlResult urlResult) {
        if (urlResult.getListing() != null && !StringUtils.isEmpty(urlResult.getListing().getYear())) {
            return urlResult.getListing().getYear()
                    .equals(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
        }
        return false;
    }

    private boolean isUrlRelatedToActiveListing(FailedUrlResult urlResult) {
        if (urlResult.getListing() != null && !StringUtils.isEmpty(urlResult.getListing().getCertificationStatus())) {
            return activeStatuses.stream()
                    .map(status -> status.getName())
                    .collect(Collectors.toList())
                    .contains(urlResult.getListing().getCertificationStatus());
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

    private void sendEmail(List<FailedUrlResult> questionableUrls, JobExecutionContext jobContext) {
        LOGGER.info("Creating email subject and body.");
        String to = jobContext.getMergedJobDataMap().getString("email");
        String htmlMessage = createHtmlEmailBody(questionableUrls, jobContext);

        File output = null;
        List<File> files = new ArrayList<File>();
        if (questionableUrls.size() > 0) {
            output = getOutputFile(questionableUrls);
            files.add(output);
        }

        LOGGER.info("Sending email to {} with contents {} and a total of {} questionable URLs.", to,
                htmlMessage, questionableUrls.size());
        try {
            List<String> addresses = new ArrayList<String>();
            addresses.add(to);

            chplEmailFactory.emailBuilder()
                .recipients(addresses)
                .subject(emailSubject)
                .htmlMessage(htmlMessage)
                .fileAttachments(files)
                .sendEmail();
        } catch (EmailNotSentException e) {
            LOGGER.error(e);
        }
    }

    private File getOutputFile(List<FailedUrlResult> urlResultsToWrite) {
        File temp = null;
        try {
            temp = File.createTempFile(emailAttachmentName, ".csv");
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

    private String createHtmlEmailBody(List<FailedUrlResult> urlResults, JobExecutionContext jobContext) {
        String jobAcbHtml = "";
        if (isAcbSpecific(jobContext)) {
            List<Long> jobAcbIds = getSelectedAcbIds(jobContext);
            if (jobAcbIds != null && jobAcbIds.size() > 0) {
                jobAcbHtml += String.format(emailBodyAcbNames, getAcbNamesAsCommaSeparatedString(jobAcbIds));
            }
      }

        String htmlMessage = "";
        if (CollectionUtils.isEmpty(urlResults)) {
            htmlMessage = htmlEmailBuilder.initialize()
                    .heading(emailBodyTitle)
                    .paragraph(null, emailBodyNoContent)
                    .paragraph(null, jobAcbHtml)
                    .footer(false)
                    .build();
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
            int brokenServiceBaseUrlLists = getCountOfBrokenUrlsOfType(urlResults, UrlType.SERVICE_BASE_URL_LIST);
            int brokenRwtPlansUrls = getCountOfBrokenUrlsOfType(urlResults, UrlType.REAL_WORLD_TESTING_PLANS);
            int brokenRwtResultsUrls = getCountOfBrokenUrlsOfType(urlResults, UrlType.REAL_WORLD_TESTING_RESULTS);
            int brokenSvapNoticeUrls = getCountOfBrokenUrlsOfType(urlResults, UrlType.STANDARDS_VERSION_ADVANCEMENT_PROCESS_NOTICE);

            String brokenUrlSummaryHtml = "<ul>";
            brokenUrlSummaryHtml += "<li>" + UrlType.ATL.getName() + ": " + brokenAtlUrls + "</li>";
            brokenUrlSummaryHtml += "<li>" + UrlType.ACB.getName() + ": " + brokenAcbUrls + "</li>";
            brokenUrlSummaryHtml += "<li>" + UrlType.DEVELOPER.getName() + ": " + brokenDeveloperUrls + "</li>";
            brokenUrlSummaryHtml += "<li>" + UrlType.FULL_USABILITY_REPORT.getName() + ": " + brokenFullUsabilityReportUrls + "</li>";
            brokenUrlSummaryHtml += "<li>" + UrlType.MANDATORY_DISCLOSURE.getName() + ": " + brokenMandatoryDisclosureUrls + "</li>";
            brokenUrlSummaryHtml += "<li>" + UrlType.TEST_RESULTS_SUMMARY.getName() + ": " + brokenTestResultsSummaryUrls + "</li>";
            brokenUrlSummaryHtml += "<li>" + UrlType.API_DOCUMENTATION.getName() + ": " + brokenApiDocumentationUrls + "</li>";
            brokenUrlSummaryHtml += "<li>" + UrlType.EXPORT_DOCUMENTATION.getName() + ": " + brokenExportDocumentationUrls + "</li>";
            brokenUrlSummaryHtml += "<li>" + UrlType.DOCUMENTATION.getName() + ": " + brokenDocumentationUrlUrls + "</li>";
            brokenUrlSummaryHtml += "<li>" + UrlType.USE_CASES.getName() + ": " + brokenUseCasesUrls + "</li>";
            brokenUrlSummaryHtml += "<li>" + UrlType.SERVICE_BASE_URL_LIST.getName() + ": " + brokenServiceBaseUrlLists + "</li>";
            brokenUrlSummaryHtml += "<li>" + UrlType.REAL_WORLD_TESTING_PLANS.getName() + ": " + brokenRwtPlansUrls + "</li>";
            brokenUrlSummaryHtml += "<li>" + UrlType.REAL_WORLD_TESTING_RESULTS.getName() + ": " + brokenRwtResultsUrls + "</li>";
            brokenUrlSummaryHtml += "<li>" + UrlType.STANDARDS_VERSION_ADVANCEMENT_PROCESS_NOTICE.getName() + ": " + brokenSvapNoticeUrls + "</li>";
            brokenUrlSummaryHtml += "</ul>";

            htmlMessage = htmlEmailBuilder.initialize()
                    .heading(emailBodyTitle)
                    .paragraph(null, brokenUrlSummaryHtml)
                    .paragraph(null, jobAcbHtml)
                    .footer(false)
                    .build();
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

    private String getAcbNamesAsCommaSeparatedString(List<Long> acbIds) {
        return acbIds.stream()
            .map(acbId -> {
                try {
                    return acbDao.getById(acbId);
                } catch (EntityRetrievalException ex) {
                    LOGGER.error("Could not find acb " + acbId, ex);
                    return null;
                }
            })
            .filter(acb -> acb != null)
            .map(acb -> acb.getName())
            .collect(Collectors.joining(", "));
    }

    private boolean isAcbSpecific(JobExecutionContext jobContext) {
        String acbSpecific = jobContext.getMergedJobDataMap().getString("acbSpecific");
        return !StringUtils.isEmpty(acbSpecific) && BooleanUtils.toBoolean(acbSpecific);
    }
}
