package gov.healthit.chpl.scheduler.job.ics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.util.Util;

public class IcsErrorsReportEmailJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("icsErrorsReportEmailJobLogger");

    @Autowired
    private IcsErrorsReportDao icsErrorsReportDao;

    @Autowired
    private CertificationBodyDAO certificationBodyDAO;

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    private Environment env;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    @Autowired
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;

    private String listingUrlBegin;

    public IcsErrorsReportEmailJob() throws Exception {
        super();
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the ICS Errors Report Email job. *********");
        LOGGER.info("Sending email to: " + jobContext.getMergedJobDataMap().getString("email"));
        listingUrlBegin = env.getProperty("chplUrlBegin").trim() + env.getProperty("listingDetailsUrl");

        List<IcsErrorsReportItem> icsErrorItems = getAppropriateErrors(jobContext);

        ExecutorService executorService = null;
        try {
            Integer threadPoolSize = getThreadCountForJob();
            executorService = Executors.newFixedThreadPool(threadPoolSize);
            List<CompletableFuture<Void>> futures = new ArrayList<CompletableFuture<Void>>();
            for (IcsErrorsReportItem icsErrorItem : icsErrorItems) {
                futures.add(CompletableFuture
                        .supplyAsync(() -> getCertifiedProductSearchDetails(icsErrorItem.getListingId()), executorService)
                        .thenAccept(listingDetails -> fillInIcsErrorItem(icsErrorItem, listingDetails)));
            }
            CompletableFuture<Void> combinedFutures = CompletableFuture
                    .allOf(futures.toArray(new CompletableFuture[futures.size()]));

            // This is not blocking - presumably because the job executes using it's own ExecutorService
            // This is necessary so that the system can indicate that the job and it's threads are still running
            combinedFutures.get();
            LOGGER.info("All processes have completed");
        } catch (Exception ex) {

        } finally {
            executorService.shutdown();
        }

        File output = null;
        List<File> files = new ArrayList<File>();
        if (icsErrorItems.size() > 0) {
            output = getOutputFile(icsErrorItems);
            files.add(output);
        }
        String to = jobContext.getMergedJobDataMap().getString("email");
        String subject = env.getProperty("icsErrorsReportEmailSubject");
        try {
            String htmlMessage = createHtmlEmailBody(icsErrorItems, jobContext);
            LOGGER.info("Message to be sent: " + htmlMessage);

            List<String> addresses = new ArrayList<String>();
            addresses.add(to);

            chplEmailFactory.emailBuilder().recipients(addresses)
                    .subject(subject)
                    .htmlMessage(htmlMessage)
                    .fileAttachments(files)
                    .sendEmail();
        } catch (IOException | EmailNotSentException e) {
            LOGGER.error(e);
        }
        LOGGER.info("********* Completed the ICS Errors Report Email job. *********");
    }

    private void fillInIcsErrorItem(IcsErrorsReportItem icsErrorItem, CertifiedProductSearchDetails listing) {
        icsErrorItem.setChplProductNumber(listing.getChplProductNumber());
        icsErrorItem.setDeveloper(listing.getDeveloper() != null ? listing.getDeveloper().getName() : "");
        icsErrorItem.setProduct(listing.getProduct() != null ? listing.getProduct().getName() : "");
        icsErrorItem.setVersion(listing.getVersion() != null ? listing.getVersion().getVersion() : "");
        icsErrorItem.setCertificationStatus(listing.getCurrentStatus() != null
                && listing.getCurrentStatus().getStatus() != null ? listing.getCurrentStatus().getStatus().getName() : "");
    }

    private List<IcsErrorsReportItem> getAppropriateErrors(JobExecutionContext jobContext) {
        List<IcsErrorsReportItem> allErrors = icsErrorsReportDao.findAll();
        LOGGER.info("Found " + allErrors.size() + " total ICS Errors");

        List<IcsErrorsReportItem> errorsForJobAcbs = new ArrayList<IcsErrorsReportItem>();
        List<Long> acbIds =
                Arrays.asList(
                        jobContext.getMergedJobDataMap().getString(QuartzJob.JOB_DATA_KEY_ACB).split(SchedulerManager.DATA_DELIMITER)).stream()
                .map(acb -> Long.parseLong(acb))
                .collect(Collectors.toList());

        errorsForJobAcbs = allErrors.stream()
                .filter(error -> acbIds.contains(error.getCertificationBody().getId()))
                .collect(Collectors.toList());
        LOGGER.info("Filtered to " + errorsForJobAcbs.size() + " errors for ACBs: "
                + Util.joinListGrammatically(acbIds.stream().map(acbId -> acbId.toString()).toList()));
        return errorsForJobAcbs;
    }

    private CertifiedProductSearchDetails getCertifiedProductSearchDetails(Long id) {
        CertifiedProductSearchDetails cp = null;
        try {
            cp = certifiedProductDetailsManager.getCertifiedProductDetails(id);
            LOGGER.info("Completed retrieval of listing [" + cp.getChplProductNumber() + "]");
        } catch (Exception e) {
            LOGGER.error("Could not retrieve listing [" + id + "] - " + e.getMessage(), e);
        }
        return cp;
    }

    private File getOutputFile(List<IcsErrorsReportItem> errors) {
        String reportFilename = env.getProperty("icsErrorsReportEmailFileName");
        File temp = null;
        try {
            temp = File.createTempFile(reportFilename, ".csv");
            temp.deleteOnExit();
        } catch (IOException ex) {
            LOGGER.error("Error creating temporary file: " + ex.getMessage(), ex);
        }

        if (temp != null) {
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(temp),
                    Charset.forName("UTF-8").newEncoder());
                    CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL)) {
                writer.write('\ufeff');
                csvPrinter.printRecord(getHeaderRow());
                for (IcsErrorsReportItem error : errors) {
                    List<String> rowValue = generateRowValue(error);
                    csvPrinter.printRecord(rowValue);
                }
            } catch (IOException e) {
                LOGGER.error(e);
            }
        }
        return temp;
    }

    private List<String> getHeaderRow() {
        List<String> result = new ArrayList<String>();
        result.add("CHPL ID");
        result.add("Developer");
        result.add("Product");
        result.add("Version");
        result.add("ONC-ACB");
        result.add("Certification Status");
        result.add("URL");
        result.add("Reason for Inclusion");
        return result;
    }

    private List<String> generateRowValue(IcsErrorsReportItem icsErrorItem) {
        List<String> result = new ArrayList<String>();
        result.add(icsErrorItem.getChplProductNumber());
        result.add(icsErrorItem.getDeveloper());
        result.add(icsErrorItem.getProduct());
        result.add(icsErrorItem.getVersion());
        result.add(icsErrorItem.getCertificationBody().getName());
        result.add(icsErrorItem.getCertificationStatus());
        result.add(listingUrlBegin + icsErrorItem.getListingId());
        result.add(icsErrorItem.getReason());
        return result;
    }

    private String createHtmlEmailBody(List<IcsErrorsReportItem> icsErrors, JobExecutionContext jobContext) throws IOException {
        String htmlMessage = "";
        if (CollectionUtils.isEmpty(icsErrors)) {
            htmlMessage = chplHtmlEmailBuilder.initialize()
                    .heading(env.getProperty("icsErrorsReportEmailHeading"))
                    .paragraph(null, String.format(env.getProperty("icsErrorsReportEmailParagraph1"), getAcbNamesAsCommaSeparatedList(jobContext)))
                    .paragraph(null, env.getProperty("icsErrorsReportEmailNoContent"))
                    .build();
        } else {
            htmlMessage = chplHtmlEmailBuilder.initialize()
                .heading(env.getProperty("icsErrorsReportEmailHeading"))
                .paragraph(null, String.format(env.getProperty("icsErrorsReportEmailParagraph1"), getAcbNamesAsCommaSeparatedList(jobContext)))
                .paragraph(null, String.format(env.getProperty("icsErrorsReportEmailParagraph2"), icsErrors.size(),
                            (icsErrors.size() == 1 ? "" : "s"),
                            (icsErrors.size() == 1 ? "was" : "were")))
                .build();
        }
        return htmlMessage;
    }

    private String getAcbNamesAsCommaSeparatedList(JobExecutionContext jobContext) {
        if (Objects.nonNull(jobContext.getMergedJobDataMap().getString(QuartzJob.JOB_DATA_KEY_ACB))) {
            List<String> acbNames =
                    Arrays.asList(jobContext.getMergedJobDataMap().getString(QuartzJob.JOB_DATA_KEY_ACB).split(SchedulerManager.DATA_DELIMITER)).stream()
                    .map(acbId -> {
                        try {
                            return certificationBodyDAO.getById(Long.parseLong(acbId)).getName();
                        } catch (NumberFormatException | EntityRetrievalException e) {
                            LOGGER.error("Could not retreive ACB name based on value: " + acbId, e);
                            return "";
                        }
                    })
                    .collect(Collectors.toList());
            return Util.joinListGrammatically(acbNames);
        } else {
            return "";
        }
    }

    private Integer getThreadCountForJob() throws NumberFormatException {
        return Integer.parseInt(env.getProperty("executorThreadCountForQuartzJobs"));
    }
}
