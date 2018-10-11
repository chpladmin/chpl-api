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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.MeaningfulUseUserDAO;
import gov.healthit.chpl.domain.MeaningfulUseUserRecord;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.MeaningfulUseUserDTO;
import gov.healthit.chpl.dto.job.JobDTO;
import gov.healthit.chpl.entity.job.JobStatusType;
import gov.healthit.chpl.manager.CertifiedProductManager;

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
    private CertifiedProductManager cpManager;

    @Autowired
    private CertifiedProductDAO cpDao;

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

    @Transactional
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
        Date muuDate = new Date(new Long(muuDateMillis));
        String muuCsv = muuDateCsvSplit[1];

        BufferedReader reader = null;
        CSVParser parser = null;
        try {
            reader = new BufferedReader(new StringReader(muuCsv));
            parser = new CSVParser(reader, CSVFormat.EXCEL);

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
            try {
                parser.close();
            } catch (Exception ignore) {
            }
            try {
                reader.close();
            } catch (Exception ignore) {
            }
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
                        addJobMessage("Line " + muu.getCsvLineNumber() +
                                ": Field \"num_meaningful_users\" is missing.");
                    } else {
                        MeaningfulUseUserDTO muuDto = new MeaningfulUseUserDTO();
                        muuDto.setMuuDate(muuDate);
                        muuDto.setMuuCount(muu.getNumberOfUsers());
                        CertifiedProductDTO cpOldStyle = null;
                        try {
                            cpOldStyle = cpDao.getByChplNumber(muu.getProductNumber());
                        } catch(Exception ex) {
                            LOGGER.warn("Searching for CHPL ID " + muu.getProductNumber() + " as old style ID and got exception: " + ex.getMessage());
                        }
                        CertifiedProductDetailsDTO cpNewStyle = null;
                        try {
                            cpNewStyle = cpDao.getByChplUniqueId(muu.getProductNumber());
                        } catch(Exception ex) {
                            LOGGER.warn("Searching for CHPL ID " + muu.getProductNumber() + " as new style ID and got exception: " + ex.getMessage());
                        }
                        if (cpOldStyle != null) {
                            muuDto.setCertifiedProductId(cpOldStyle.getId());
                        } else if (cpNewStyle != null) {
                            muuDto.setCertifiedProductId(cpNewStyle.getId());
                        }

                        if (muuDto.getCertifiedProductId() != null && muuDto.getCertifiedProductId() > 0) {
                            muuDao.create(muuDto);
                        } else {
                            addJobMessage("Line " + muu.getCsvLineNumber()
                                + ": Field \"chpl_product_number\" with value \"" + muu.getProductNumber()
                                + "\" is invalid. " + "The provided \"chpl_product_number\" does not exist.");
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

    public CertifiedProductManager getCpManager() {
        return cpManager;
    }

    public void setCpManager(final CertifiedProductManager cpManager) {
        this.cpManager = cpManager;
    }

    public CertifiedProductDAO getCpDao() {
        return cpDao;
    }

    public void setCpDao(final CertifiedProductDAO cpDao) {
        this.cpDao = cpDao;
    }
}
