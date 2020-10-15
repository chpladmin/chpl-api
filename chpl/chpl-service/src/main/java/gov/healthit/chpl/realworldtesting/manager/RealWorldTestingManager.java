package gov.healthit.chpl.realworldtesting.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
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
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.manager.auth.UserManager;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingType;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingUpload;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingUploadResponse;
import gov.healthit.chpl.scheduler.job.RealWorldTestingUploadJob;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class RealWorldTestingManager {
    private static final String PLANS = "PLANS";
    private static final String RESULTS = "RESULTS";
    private static final int CHPL_PRODUCT_NUMBER = 0;
    private static final int RWT_TYPE = 1;
    private static final int LAST_CHECKED = 2;
    private static final int URL = 3;
    private static final int DELAY_BEFORE_JOB_START = 5000;

    private SchedulerManager schedulerManager;
    private UserManager userManager;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public RealWorldTestingManager(SchedulerManager schedulerManager, UserManager userManager,
            ErrorMessageUtil errorMessageUtil) {

        this.schedulerManager = schedulerManager;
        this.userManager = userManager;
        this.errorMessageUtil = errorMessageUtil;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).REAL_WORLD_TESTING, "
            + "T(gov.healthit.chpl.permissions.domains.RealWorldTestingDomainPermissions).UPLOAD)")
    public RealWorldTestingUploadResponse uploadRealWorldTestingCsv(MultipartFile file)
            throws ValidationException, SchedulerException, UserRetrievalException {

        checkBasicFileProperties(file);
        List<RealWorldTestingUpload> rwts = parseCsvFile(file);
        startRwtUploadJob(rwts);

        UserDTO currentUser = userManager.getById(AuthUtil.getCurrentUser().getId());
        RealWorldTestingUploadResponse response = new RealWorldTestingUploadResponse();
        response.setEmail(currentUser.getEmail());
        response.setFileName(file.getName());
        response.setRecordsToBeProcessed(rwts.size());
        return response;
    }

    private ChplOneTimeTrigger startRwtUploadJob(List<RealWorldTestingUpload> rwts)
            throws SchedulerException, ValidationException, UserRetrievalException {

        ChplOneTimeTrigger rwtUploadTrigger = new ChplOneTimeTrigger();
        ChplJob rwtUploadJob = new ChplJob();
        rwtUploadJob.setName(RealWorldTestingUploadJob.JOB_NAME);
        rwtUploadJob.setGroup(SchedulerManager.CHPL_BACKGROUND_JOBS_KEY);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(RealWorldTestingUploadJob.RWT_UPLOAD_ITEMS, rwts);
        jobDataMap.put(RealWorldTestingUploadJob.USER_KEY, userManager.getById(AuthUtil.getCurrentUser().getId()));
        rwtUploadJob.setJobDataMap(jobDataMap);
        rwtUploadTrigger.setJob(rwtUploadJob);
        rwtUploadTrigger.setRunDateMillis(System.currentTimeMillis() + DELAY_BEFORE_JOB_START);
        rwtUploadTrigger = schedulerManager.createBackgroundJobTrigger(rwtUploadTrigger);
        return rwtUploadTrigger;
    }

    private List<RealWorldTestingUpload> parseCsvFile(MultipartFile file) throws ValidationException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
                CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL)) {

            List<CSVRecord> records = parser.getRecords();
            if (records.size() <= 1 && doesHeaderRowExist(records)) {
                throw new ValidationException(
                        "The file appears to have a header line with no other information. "
                        + "Please make sure there are at least two rows in the CSV file.");
            }

            //Remove the header row before processing, if it exists
            records = removeHeaderRow(records);

            List<RealWorldTestingUpload> rwts = records.stream()
                    .map(rec -> createRwtUploadFromCsvRecord(rec))
                    .collect(Collectors.toList());

            return rwts;
        } catch (final IOException ioEx) {
            throw new ValidationException("Could not get input stream for uploaded file " + file.getName());
        }
    }

    private RealWorldTestingUpload createRwtUploadFromCsvRecord(CSVRecord record) {
        RealWorldTestingUpload rwtUpload = new RealWorldTestingUpload();
        if (!StringUtils.isEmpty(record.get(CHPL_PRODUCT_NUMBER))) {
            rwtUpload.setChplProductNumber(record.get(CHPL_PRODUCT_NUMBER));
        } else {
            rwtUpload.getErrors().add(errorMessageUtil.getMessage("realWorldTesting.upload.chplProductNumberInvalid"));
        }
        if (!StringUtils.isEmpty(record.get(RWT_TYPE))) {
            rwtUpload.setType(getType(record.get(RWT_TYPE)));
        } else {
            rwtUpload.getErrors().add(errorMessageUtil.getMessage("realWorldTesting.upload.realWorldTestingTypeInvalid"));
        }
        if (!StringUtils.isEmpty(record.get(LAST_CHECKED))) {
            rwtUpload.setLastChecked(getLastCheckedDate(record.get(LAST_CHECKED)));
        } else {
            rwtUpload.getErrors().add(errorMessageUtil.getMessage("realWorldTesting.upload.lastCheckedDateInvalid"));
        }
        if (!StringUtils.isEmpty(record.get(URL))) {
            rwtUpload.setUrl(record.get(URL));
        } else {
            rwtUpload.getErrors().add(errorMessageUtil.getMessage("realWorldTesting.upload.url"));
        }
        return rwtUpload;
    }

    private LocalDate getLastCheckedDate(String unformatted) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyMMdd");
        return LocalDate.parse(unformatted, formatter);
    }

    private RealWorldTestingType getType(String rwtType) {
        if (rwtType.toUpperCase().equals(PLANS)) {
            return RealWorldTestingType.PLANS;
        } else if (rwtType.toUpperCase().equals(RESULTS)) {
            return RealWorldTestingType.RESULTS;
        } else {
            return null;
        }
    }

    private List<CSVRecord> removeHeaderRow(List<CSVRecord> records) {
        if (doesHeaderRowExist(records)) {
            records.remove(0);
        }
        return records;
    }

    private boolean doesHeaderRowExist(List<CSVRecord> records) {
        //If the CHPL Product Number is UNIQUE_CHPL_ID__C assume there is a header
        return records.get(0).get(CHPL_PRODUCT_NUMBER).toUpperCase().contains("UNIQUE_CHPL_ID__C");
    }

    private void checkBasicFileProperties(MultipartFile file) throws ValidationException {
        if (file.isEmpty()) {
            throw new ValidationException("You cannot upload an empty file!");
        }

        if (!file.getContentType().equalsIgnoreCase("text/csv")
                && !file.getContentType().equalsIgnoreCase("application/vnd.ms-excel")) {
            throw new ValidationException("File must be a CSV document.");
        }
    }
}
