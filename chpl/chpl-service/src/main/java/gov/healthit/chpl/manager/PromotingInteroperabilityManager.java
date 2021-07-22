package gov.healthit.chpl.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.auth.UserManager;
import gov.healthit.chpl.scheduler.job.promotingInteroperability.PromotingInteroperabilityUploadJob;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.FileUtils;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class PromotingInteroperabilityManager {
    private SchedulerManager schedulerManager;
    private UserManager userManager;
    private FileUtils fileUtils;

    @Autowired
    public PromotingInteroperabilityManager(SchedulerManager schedulerManager, UserManager userManager,
            FileUtils fileUtils) {
        this.schedulerManager = schedulerManager;
        this.userManager = userManager;
        this.fileUtils = fileUtils;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.CertifiedProductDomainPermissions).UPLOAD_PIU)")
    public ChplOneTimeTrigger processUploadAsJob(MultipartFile file, Long accurateAsOfDate)
            throws EntityCreationException, EntityRetrievalException, ValidationException,
            IOException, SchedulerException {
        if (file.isEmpty()) {
            throw new ValidationException("You cannot upload an empty file!");
        }

        if (!file.getContentType().equalsIgnoreCase("text/csv")
                && !file.getContentType().equalsIgnoreCase("application/vnd.ms-excel")) {
            throw new ValidationException("File must be a CSV document.");
        }

        String data = fileUtils.readFileAsString(file);
        checkFileCanBeReadAndMultipleRowsExist(data);

        UserDTO jobUser = null;
        try {
            jobUser = userManager.getById(AuthUtil.getCurrentUser().getId());
        } catch (UserRetrievalException ex) {
            LOGGER.error("Could not find user to execute job.");
        }

        ChplOneTimeTrigger uploadPiuTrigger = new ChplOneTimeTrigger();
        ChplJob uploadPiuJob = new ChplJob();
        uploadPiuJob.setName(PromotingInteroperabilityUploadJob.JOB_NAME);
        uploadPiuJob.setGroup(SchedulerManager.CHPL_BACKGROUND_JOBS_KEY);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(PromotingInteroperabilityUploadJob.FILE_CONTENTS_KEY, data);
        jobDataMap.put(PromotingInteroperabilityUploadJob.ACCURATE_AS_OF_DATE_KEY, accurateAsOfDate);
        jobDataMap.put(PromotingInteroperabilityUploadJob.USER_KEY, jobUser);
        uploadPiuJob.setJobDataMap(jobDataMap);
        uploadPiuTrigger.setJob(uploadPiuJob);
        uploadPiuTrigger.setRunDateMillis(System.currentTimeMillis() + SchedulerManager.DELAY_BEFORE_BACKGROUND_JOB_START);
        uploadPiuTrigger = schedulerManager.createBackgroundJobTrigger(uploadPiuTrigger);
        return uploadPiuTrigger;
    }

    private void checkFileCanBeReadAndMultipleRowsExist(String fileContents) throws IOException, ValidationException  {
        try (BufferedReader reader = new BufferedReader(new StringReader(fileContents));
                CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL)) {

            List<CSVRecord> records = parser.getRecords();
            if (records.size() <= 1) {
                String msg = "The file appears to have a header line with no other information. "
                        + "Please make sure there are at least two rows in the CSV file.";
                throw new ValidationException(msg);
            }
        } catch (IOException ex) {
            LOGGER.error("Cannot read file as CSV: " + ex.getMessage());
            throw ex;
        }
    }
}
