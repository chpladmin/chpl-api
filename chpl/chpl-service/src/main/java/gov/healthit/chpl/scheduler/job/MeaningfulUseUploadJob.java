package gov.healthit.chpl.scheduler.job;

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
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.MeaningfulUseUserDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.MeaningfulUseUserRecord;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.MeaningfulUseUserDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@DisallowConcurrentExecution
@Log4j2(topic = "meaningfulUseUploadJobLogger")
public class MeaningfulUseUploadJob implements Job {
    public static final String JOB_NAME = "meaningfulUseUploadJob";
    public static final String FILE_CONTENTS_KEY = "fileContents";
    public static final String ACCURATE_AS_OF_DATE_KEY = "accurateAsOfDate";
    public static final String USER_KEY = "user";

    @Autowired
    private Environment env;
    @Autowired
    private CertifiedProductDetailsManager cpdManager;
    @Autowired
    private MeaningfulUseUserDAO muuDao;
    @Autowired
    private ActivityManager activityManager;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Meaningful Use Upload job. *********");

        JobDataMap jobDataMap = jobContext.getMergedJobDataMap();
        UserDTO user = (UserDTO) jobDataMap.get(USER_KEY);
        if (user == null) {
            LOGGER.fatal("No user could be found in the job data.");
        } else {
            setSecurityContext(user);
            String muuData = (String) jobDataMap.getString(FILE_CONTENTS_KEY);
            Set<MeaningfulUseUserRecord> muuRecords = parseMeaningfulUseRecords(muuData);
            processMeaningfulUseUpdates(muuRecords);
        }
        LOGGER.info("********* Completed the Meaningful Use Upload job. *********");
    }

    private Set<MeaningfulUseUserRecord> parseMeaningfulUseRecords(String fileContents) {
        Set<MeaningfulUseUserRecord> muusToUpdate = new LinkedHashSet<MeaningfulUseUserRecord>();
        Set<String> uniqueMuusFromFile = new LinkedHashSet<String>();
        String[] muuDateCsvSplit = fileContents.split(";");
        String muuDateMillis = muuDateCsvSplit[0];
        Date muuDate = new Date(Long.parseLong(muuDateMillis));
        String muuCsv = muuDateCsvSplit[1];

        try (BufferedReader reader = new BufferedReader(new StringReader(muuCsv));
                CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL)) {

            List<CSVRecord> records = parser.getRecords();
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
                    } catch (NumberFormatException e) {
                        muu.setProductNumber(chplProductNumber);
                        muu.setCsvLineNumber(i);
                        String error = "Line " + muu.getCsvLineNumber()
                                + ": Field \"num_meaningful_use\" with value \"" + currRecord.get(1).trim()
                                + "\" is invalid. " + "Value in field \"num_meaningful_use\" must be an integer.";
                        muu.setError(error);
                        muusToUpdate.add(muu);
                        uniqueMuusFromFile.add(muu.getProductNumber());
                    } catch (IOException e) {
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
        } catch (IOException ex) {
            LOGGER.error("Could not read file as CSV: " + ex.getMessage());
        }
        return muusToUpdate;
    }

    private void processMeaningfulUseUpdates(Set<MeaningfulUseUserRecord> muuRecords) {
        for (MeaningfulUseUserRecord muu : muuRecords) {
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
                                    "User " + AuthUtil.getUsername() + " updated MUU count via upload file.");
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
    }

    private void setSecurityContext(UserDTO user) {
        JWTAuthenticatedUser jobUser = new JWTAuthenticatedUser();
        jobUser.setFullName(user.getFullName());
        jobUser.setId(user.getId());
        jobUser.setFriendlyName(user.getFriendlyName());
        jobUser.setSubjectName(user.getUsername());
        jobUser.getPermissions().add(user.getPermission().getGrantedPermission());

        SecurityContextHolder.getContext().setAuthentication(jobUser);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }
}
