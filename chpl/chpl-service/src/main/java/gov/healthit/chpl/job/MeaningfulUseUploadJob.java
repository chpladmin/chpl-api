package gov.healthit.chpl.job;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.dao.MeaningfulUseUserDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.MeaningfulUseUserRecord;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.MeaningfulUseUserDTO;
import gov.healthit.chpl.dto.job.JobDTO;
import gov.healthit.chpl.entity.job.JobStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ActivityManager;

/**
 * Upload a potentially large amount of MUU data in the background.
 * @author kekey
 *
 */
@Component
@Scope("prototype") // tells spring to make a new instance of this class every
                    // time it is needed
public class MeaningfulUseUploadJob extends RunnableJob {
    private static final Logger LOGGER = LogManager.getLogger(MeaningfulUseUploadJob.class);
    private static final Integer COMPLETE_PERCENT = 100;
    private static final Integer FILE_PARSED_PERCENT = 10;

    @Autowired
    private CertifiedProductDetailsManager cpdManager;

    @Autowired
    private ActivityManager activityManager;

    @Autowired
    private MeaningfulUseUserDAO muuDao;

    /**
     * Default constructor.
     */
    public MeaningfulUseUploadJob() {
        LOGGER.debug("Created new MUUJob");
    }

    /**
     * Constructor that takes a job parameter.
     * @param job
     */
    public MeaningfulUseUploadJob(final JobDTO job) {
        LOGGER.debug("Created new MUUJob");
        this.job = job;
    }

    /**
     * Parse the job data, in this case the contents of a csv file that was
     * saved to the job object in the database.
     * Get the details of each listing and add the MUU count provided.
     */
    @Override
    public void run() {
        super.run();

        //MUU job data is formatted like "date;all,csv,data"
        String[] muuDateCsvSplit = job.getData().split(";");
        if (muuDateCsvSplit == null || muuDateCsvSplit.length != 2) {
            String msg = "Could not split ;" + job.getData() + "; with ';' "
                    + "into an array of length 2. Cannot process MUU job.";
            LOGGER.error(msg);
            addJobMessage(msg);
            updateStatus(COMPLETE_PERCENT, JobStatusType.Error);
            return;
        }

        double jobPercentComplete = 0;
        Set<MeaningfulUseUserRecord> muusToUpdate = new LinkedHashSet<MeaningfulUseUserRecord>();
        Set<String> uniqueMuusFromFile = new LinkedHashSet<String>();
        String muuDateMillis = muuDateCsvSplit[0];
        Date muuDate = new Date(Long.parseLong(muuDateMillis));
        String muuCsv = muuDateCsvSplit[1];

        try (BufferedReader reader = new BufferedReader(new StringReader(muuCsv));
                CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL)) {

            List<CSVRecord> records = parser.getRecords();
            if (records.size() <= 1) {
                String msg = "The file appears to have a header line with no other information. "
                        + "Please make sure there are at least two rows in the CSV file.";
                LOGGER.error(msg);
                addJobMessage(msg);
                updateStatus(COMPLETE_PERCENT, JobStatusType.Error);
                try {
                    parser.close();
                } catch (Exception ignore) {
                }
                try {
                    reader.close();
                } catch (Exception ignore) {
                }
            } else {
                CSVRecord heading = null;
                for (int i = 1; i <= records.size(); i++) {
                    CSVRecord currRecord = records.get(i - 1);
                    MeaningfulUseUserRecord muu = new MeaningfulUseUserRecord();

                    // add header if something similar to "chpl_product_number"
                    // and "num_meaningful_use" exists
                    if (heading == null && i == 1 && !StringUtils.isEmpty(currRecord.get(0).trim())
                            && currRecord.get(0).trim().contains("product")
                            && !StringUtils.isEmpty(currRecord.get(1).trim())
                            && currRecord.get(1).trim().contains("meaning")) {
                        heading = currRecord;
                    } else {
                        // populate MeaningfulUseUserResults
                        String chplProductNumber = currRecord.get(0).trim();
                        Long numMeaningfulUseUsers = null;
                        try {
                            numMeaningfulUseUsers = Long.parseLong(currRecord.get(1).trim());
                            muu.setProductNumber(chplProductNumber);
                            muu.setNumberOfUsers(numMeaningfulUseUsers);
                            muu.setCsvLineNumber(i);
                            // check if product number has already been updated
                            if (uniqueMuusFromFile.contains(muu.getProductNumber())) {
                                throw new IOException();
                            }
                            muusToUpdate.add(muu);
                            uniqueMuusFromFile.add(muu.getProductNumber());
                        } catch (final NumberFormatException e) {
                            muu.setProductNumber(chplProductNumber);
                            muu.setCsvLineNumber(i);
                            String error = "Line " + muu.getCsvLineNumber()
                                    + ": Field \"num_meaningful_use\" with value \"" + currRecord.get(1).trim()
                                    + "\" is invalid. " + "Value in field \"num_meaningful_use\" must be an integer.";
                            muu.setError(error);
                            muusToUpdate.add(muu);
                            uniqueMuusFromFile.add(muu.getProductNumber());
                        } catch (final IOException e) {
                            muu.setProductNumber(chplProductNumber);
                            muu.setCsvLineNumber(i);
                            Integer dupLineNumber = null;
                            // get line number with duplicate
                            // chpl_product_number
                            for (MeaningfulUseUserRecord entry : muusToUpdate) {
                                if (entry.getProductNumber().equals(muu.getProductNumber())) {
                                    dupLineNumber = entry.getCsvLineNumber();
                                }
                            }

                            String error = "Line " + muu.getCsvLineNumber()
                                    + ": Field \"chpl_product_number\" with value \"" + muu.getProductNumber()
                                    + "\" is invalid. " + "Duplicate \"chpl_product_number\" at line " + dupLineNumber;
                            muu.setError(error);
                            muusToUpdate.add(muu);
                        }
                    }
                }
            }

            // finished parsing the file which is pretty quick, say that's 10%
            // of the job done
            jobPercentComplete = FILE_PARSED_PERCENT;
            updateStatus(jobPercentComplete, JobStatusType.In_Progress);
        } catch (final IOException ioEx) {
            String msg = "Could not get input stream for job data string for job with ID " + job.getId();
            LOGGER.error(msg);
            addJobMessage(msg);
            updateStatus(COMPLETE_PERCENT, JobStatusType.Error);
        }

        // now load everything that was parsed
        for (MeaningfulUseUserRecord muu : muusToUpdate) {
            if (StringUtils.isEmpty(muu.getError())) {
                try {
                    // If bad input, add error for this MeaningfulUseUser and continue
                    if (org.apache.commons.lang.StringUtils.isEmpty(muu.getProductNumber())) {
                        addJobMessage("Line " + muu.getCsvLineNumber()
                                + ": Field \"chpl_product_number\" is missing.");
                    } else if (muu.getNumberOfUsers() == null) {
                        addJobMessage("Line " + muu.getCsvLineNumber()
                            + ": Field \"num_meaningful_users\" is missing.");
                    } else {
                        //make sure the listing is valid and get the details
                        //object so that it can be updated
                        CertifiedProductSearchDetails existingListing = null;
                        try {
                            existingListing =
                                    cpdManager.getCertifiedProductDetailsByChplProductNumber(muu.getProductNumber());
                        } catch (EntityRetrievalException ex) {
                            LOGGER.warn("Searching for CHPL ID " + muu.getProductNumber()
                                + " encountered exception: " + ex.getMessage());
                            addJobMessage("Line " + muu.getCsvLineNumber()
                                + ": Field \"chpl_product_number\" with value \"" + muu.getProductNumber()
                                + "\" is invalid. " + "The provided \"chpl_product_number\" does not exist.");
                        }

                        if (existingListing != null) {
                            //add a meaningful use entry
                            MeaningfulUseUserDTO muuDto = new MeaningfulUseUserDTO();
                            muuDto.setCertifiedProductId(existingListing.getId());
                            muuDto.setMuuCount(muu.getNumberOfUsers());
                            muuDto.setMuuDate(muuDate);
                            muuDao.create(muuDto);

                            //write activity for the listing update
                            CertifiedProductSearchDetails updatedListing =
                                    cpdManager.getCertifiedProductDetails(existingListing.getId());
                            activityManager.addActivity(ActivityConcept.CERTIFIED_PRODUCT, existingListing.getId(),
                                    "Updated certified product " + updatedListing.getChplProductNumber() + ".", existingListing,
                                    updatedListing,
                                    "User " + getUser().getUsername() + " updated MUU count via upload file.");
                        }
                    }
                } catch (Exception ex) {
                    String msg = "Line " + muu.getCsvLineNumber() + ": An unexpected error occurred. "
                            + ex.getMessage();
                    LOGGER.error(msg, ex);
                    addJobMessage(msg);

                }
            } else {
                addJobMessage(muu.getError());
            }

            // update the status
            jobPercentComplete += (COMPLETE_PERCENT - FILE_PARSED_PERCENT) / (double) muusToUpdate.size();
            updateStatus(jobPercentComplete, JobStatusType.In_Progress);
        }

        this.complete();
    }
}
