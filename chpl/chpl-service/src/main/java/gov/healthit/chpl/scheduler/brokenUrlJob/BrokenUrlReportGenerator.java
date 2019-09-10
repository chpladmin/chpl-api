package gov.healthit.chpl.scheduler.brokenUrlJob;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.mail.MessagingException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.util.EmailBuilder;

/**
 * Quartz job to compile the results of the saved broken url data into a report.
 * @author kekey
 *
 */
public class BrokenUrlReportGenerator extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("brokenUrlReportGeneratorJobLogger");
    private static final String[] CSV_HEADER = {
            "ONC-ATL", "ONC-ACB", "Developer", "Developer Contact Name", "Developer Contact Email",
            "Developer Contact Phone Number", "Product", "Version",
            "CHPL Product Number", "URL Type", "URL", "Status Code", "Status Name", "Error Message",
            "Date Last Checked"};

    @Autowired
    private Environment env;

    @Autowired
    private UrlCheckerDao urlCheckerDao;

    @Autowired
    private TestingLabDAO atlDao;

    @Autowired
    private CertificationBodyDAO acbDao;

    @Autowired
    private DeveloperDAO devDao;

    @Autowired
    private CertifiedProductDAO cpDao;

    @Override
    @Transactional
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Broken URL Report Generator job. *********");

        try {
            List<UrlResultWithErrorDTO> badUrlsToWrite = new ArrayList<UrlResultWithErrorDTO>();
            List<UrlResultDTO> badUrls = urlCheckerDao.getUrlResultsWithError();
            LOGGER.info("Found " + badUrls.size() + " urls with errors.");
            int i = 0;
            for (UrlResultDTO urlResult : badUrls) {
                switch (urlResult.getUrlType()) {
                case ACB:
                    LOGGER.info("[" + i + "]: Getting ACBs with bad website " + urlResult.getUrl());
                    List<CertificationBodyDTO> acbsWithBadUrl = acbDao.getByWebsite(urlResult.getUrl());
                    for (CertificationBodyDTO acb : acbsWithBadUrl) {
                        UrlResultWithErrorDTO urlResultWithError = new UrlResultWithErrorDTO(urlResult);
                        urlResultWithError.setAcbName(acb.getName());
                        badUrlsToWrite.add(urlResultWithError);
                    }
                    break;
                case ATL:
                    LOGGER.info("[" + i + "] Getting ATLs with bad website " + urlResult.getUrl());
                    List<TestingLabDTO> atlsWithBadUrl = atlDao.getByWebsite(urlResult.getUrl());
                    for (TestingLabDTO atl : atlsWithBadUrl) {
                        UrlResultWithErrorDTO urlResultWithError = new UrlResultWithErrorDTO(urlResult);
                        urlResultWithError.setAtlName(atl.getName());
                        badUrlsToWrite.add(urlResultWithError);
                    }
                    break;
                case DEVELOPER:
                    LOGGER.info("[ " + i + "] Getting Developers with bad website " + urlResult.getUrl());
                    List<DeveloperDTO> devsWithBadUrl = devDao.getByWebsite(urlResult.getUrl());
                    for (DeveloperDTO dev : devsWithBadUrl) {
                        UrlResultWithErrorDTO urlResultWithError = new UrlResultWithErrorDTO(urlResult);
                        urlResultWithError.setDeveloper(dev);
                        badUrlsToWrite.add(urlResultWithError);
                    }
                    break;
                case FULL_USABILITY_REPORT:
                case MANDATORY_DISCLOSURE_URL:
                case TEST_RESULTS_SUMMARY:
                    LOGGER.info("[" + i + "] Getting Listings with bad " + urlResult.getUrlType().getName() + " website " + urlResult.getUrl());
                    List<CertifiedProductDetailsDTO> listingsWithBadUrl =
                        cpDao.getDetailsByUrl(urlResult.getUrl(), urlResult.getUrlType());
                    for (CertifiedProductDetailsDTO listing : listingsWithBadUrl) {
                        UrlResultWithErrorDTO urlResultWithError = new UrlResultWithErrorDTO(urlResult);
                        urlResultWithError.setListing(listing);
                        badUrlsToWrite.add(urlResultWithError);
                    }
                    break;
                default:
                    break;
                }
                i++;
            }

            LOGGER.info("Creating email subject and body.");
            String to = jobContext.getMergedJobDataMap().getString("email");
            String subject = env.getProperty("job.badUrlChecker.emailSubject");
            String htmlMessage = env.getProperty("job.badUrlChecker.emailBodyBegin");
            htmlMessage += createHtmlEmailBody(badUrlsToWrite, env.getProperty("job.badUrlChecker.emailBodyNoContent"));
            File output = null;
            List<File> files = new ArrayList<File>();
            if (badUrlsToWrite.size() > 0) {
                output = getOutputFile(badUrlsToWrite, env.getProperty("job.badUrlChecker.emailAttachmentName"));
                files.add(output);
            }

            LOGGER.info("Sending email to {} with contents {} and a total of {} broken URLs.",
                    to, htmlMessage, badUrlsToWrite.size());
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
        } catch (Exception ex) {
            LOGGER.error("Unable to complete job: " + ex.getMessage(), ex);
        }
        LOGGER.info("********* Completed the Broken URL Report Generator job. *********");
    }

    /**
     * Generates a CSV output file with all bad url data.
     * @param urlResultsToWrite
     * @param reportFilename
     * @return
     */
    private File getOutputFile(final List<UrlResultWithErrorDTO> urlResultsToWrite, final String reportFilename) {
        File temp = null;
        try {
            temp = File.createTempFile(reportFilename, ".csv");
            temp.deleteOnExit();
        } catch (IOException ex) {
            LOGGER.error("Could not create temporary file " + ex.getMessage(), ex);
        }

        if (temp != null) {
            try (OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(temp), Charset.forName("UTF-8").newEncoder());
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL)) {
                csvPrinter.printRecord(getHeaderRow());
                for (UrlResultWithErrorDTO urlResult : urlResultsToWrite) {
                    List<String> rowValue = generateRowValue(urlResult);
                    csvPrinter.printRecord(rowValue);
                }
            } catch (IOException e) {
                LOGGER.error(e);
            }
        }
        return temp;
    }

    /**
     * Create an array of strings representing the header of a CSV.
     * @return
     */
    private List<String> getHeaderRow() {
        return Arrays.asList(CSV_HEADER);
    }

    /**
     * Create an array of strings representing one row of data in a CSV.
     * @param urlResult
     * @return
     */
    private List<String> generateRowValue(final UrlResultWithErrorDTO urlResult) {
        List<String> result = new ArrayList<String>();
        if (urlResult.getAtlName() != null) {
            result.add(urlResult.getAtlName());
        } else {
            result.add("");
        }

        if (urlResult.getAcbName() != null) {
            result.add(urlResult.getAcbName());
        } else {
            result.add("");
        }

        if (urlResult.getDeveloper() != null) {
            result.add(urlResult.getDeveloper().getName());
        } else {
            result.add("");
        }

        if (urlResult.getDeveloper() != null && urlResult.getDeveloper().getContact() != null) {
            ContactDTO contact = urlResult.getDeveloper().getContact();
            if (contact.getFullName() != null) {
                result.add(contact.getFullName());
            } else {
                result.add("");
            }
            if (contact.getEmail() != null) {
                result.add(contact.getEmail());
            } else {
                result.add("");
            }
            if (contact.getPhoneNumber() != null) {
                result.add(contact.getPhoneNumber());
            } else {
                result.add("");
            }
        } else {
            result.add("");
            result.add("");
            result.add("");
        }

        if (urlResult.getListing() != null) {
            CertifiedProductDetailsDTO listing = urlResult.getListing();
            if (listing.getProduct() != null) {
                result.add(listing.getProduct().getName());
            } else {
                result.add("");
            }
            if (listing.getVersion() != null) {
                result.add(listing.getVersion().getVersion());
            } else {
                result.add("");
            }
            if (listing.getChplProductNumber() != null) {
                result.add(listing.getChplProductNumber());
            } else {
                result.add("");
            }
        } else {
            result.add("");
            result.add("");
            result.add("");
        }

        result.add(urlResult.getUrlType().getName());
        result.add(urlResult.getUrl());

        if (urlResult.getResponseCode() != null) {
            result.add(urlResult.getResponseCode().toString());
            try {
                HttpStatus httpStatus = HttpStatus.valueOf(urlResult.getResponseCode());
                if (httpStatus != null) {
                    result.add(httpStatus.getReasonPhrase());
                } else {
                    result.add("");
                }
            } catch (IllegalArgumentException ex) {
                LOGGER.warn("No HttpStatus object could be found for response code " + urlResult.getResponseCode());
                result.add("");
            }
        } else {
            result.add("");
            result.add("");
        }

        if (urlResult.getResponseMessage() != null) {
            result.add(urlResult.getResponseMessage());
        } else {
            result.add("");
        }

        if (urlResult.getLastChecked() != null) {
            result.add(getDateFormatter().format(urlResult.getLastChecked()));
        } else {
            result.add("");
        }
        return result;
    }

    /**
     * Create the HTML body of the email to be sent.
     * @param urlResults
     * @param noContentMsg
     * @return
     */
    private String createHtmlEmailBody(final List<UrlResultWithErrorDTO> urlResults, final String noContentMsg) {
        String htmlMessage = "";
        if (urlResults.size() == 0) {
            htmlMessage = noContentMsg;
        } else {
            int brokenAcbUrls = getCountOfBrokenUrlsOfType(urlResults, UrlType.ACB);
            int brokenAtlUrls = getCountOfBrokenUrlsOfType(urlResults, UrlType.ATL);
            int brokenDeveloperUrls = getCountOfBrokenUrlsOfType(urlResults, UrlType.DEVELOPER);
            int brokenFullUsabilityReportUrls = getCountOfBrokenUrlsOfType(urlResults, UrlType.FULL_USABILITY_REPORT);
            int brokenMandatoryDisclosureUrls = getCountOfBrokenUrlsOfType(urlResults, UrlType.MANDATORY_DISCLOSURE_URL);
            int brokenTestResultsSummaryUrls = getCountOfBrokenUrlsOfType(urlResults, UrlType.TEST_RESULTS_SUMMARY);

            htmlMessage += "<ul>";
            htmlMessage += "<li>" + UrlType.ACB.getName() + ": " + brokenAcbUrls + "</li>";
            htmlMessage += "<li>" + UrlType.ATL.getName() + ": " + brokenAtlUrls + "</li>";
            htmlMessage += "<li>" + UrlType.DEVELOPER.getName() + ": " + brokenDeveloperUrls + "</li>";
            htmlMessage += "<li>" + UrlType.FULL_USABILITY_REPORT.getName() + ": " + brokenFullUsabilityReportUrls + "</li>";
            htmlMessage += "<li>" + UrlType.MANDATORY_DISCLOSURE_URL.getName() + ": " + brokenMandatoryDisclosureUrls + "</li>";
            htmlMessage += "<li>" + UrlType.TEST_RESULTS_SUMMARY.getName() + ": " + brokenTestResultsSummaryUrls + "</li>";
            htmlMessage += "</ul>";
        }

        return htmlMessage;
    }

    private int getCountOfBrokenUrlsOfType(final List<UrlResultWithErrorDTO> urlResults, final UrlType urlType) {
        int count = 0;
        for (UrlResultWithErrorDTO urlResult : urlResults) {
            if (urlResult.getUrlType().equals(urlType)) {
                count++;
            }
        }
        return count;
    }

    private DateFormat getDateFormatter() {
        return DateFormat.getDateTimeInstance(
                DateFormat.LONG,
                DateFormat.LONG,
                 Locale.US);
    }
}
