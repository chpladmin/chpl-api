package gov.healthit.chpl.realworldtesting.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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

import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.manager.auth.UserManager;
import gov.healthit.chpl.realworldtesting.dao.RealWorldTestingByDeveloperDao;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingType;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingUpload;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingUploadResponse;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingUrlByDeveloper;
import gov.healthit.chpl.scheduler.job.RealWorldTestingUploadJob;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class RealWorldTestingManager {
    private static final int CHPL_PRODUCT_NUMBER_COLUMN_IDX = 0;
    private static final int RWT_TYPE_COLUMN_IDX = 1;
    private static final int LAST_CHECKED_COLUMN_IDX = 2;
    private static final int URL_COLUMN_IDX = 3;
    private static final int DELAY_BEFORE_JOB_START = 5000;

    private RealWorldTestingByDeveloperDao rwtByDeveloperDao;
    private SchedulerManager schedulerManager;
    private UserManager userManager;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public RealWorldTestingManager(RealWorldTestingByDeveloperDao rwtByDeveloperDao,
            SchedulerManager schedulerManager, UserManager userManager,
            ErrorMessageUtil errorMessageUtil) {

        this.rwtByDeveloperDao = rwtByDeveloperDao;
        this.schedulerManager = schedulerManager;
        this.userManager = userManager;
        this.errorMessageUtil = errorMessageUtil;
    }

    public List<RealWorldTestingUrlByDeveloper> getPlansUrls(Long developerId) {
        return rwtByDeveloperDao.getPlansUrls(developerId);
    }

    public List<RealWorldTestingUrlByDeveloper> getResultsUrls(Long developerId) {
        return rwtByDeveloperDao.getResultsUrls(developerId);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).REAL_WORLD_TESTING, "
            + "T(gov.healthit.chpl.permissions.domains.RealWorldTestingDomainPermissions).UPLOAD)")
    public RealWorldTestingUploadResponse uploadRealWorldTestingCsv(MultipartFile file)
            throws ValidationException, SchedulerException, UserRetrievalException {

        checkBasicFileProperties(file);
        List<RealWorldTestingUpload> rwts = parseCsvFile(file);
        startRwtUploadJob(rwts);

        JWTAuthenticatedUser currentUser = AuthUtil.getCurrentUser();
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
        jobDataMap.put(RealWorldTestingUploadJob.USER_KEY, AuthUtil.getCurrentUser());
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
                throw new ValidationException(errorMessageUtil.getMessage("realWorldTesting.upload.headerOnly"));
            }

            records = removeHeaderRow(records);
            records = removeEmptyRows(records);

            if (records.size() == 0) {
                throw new ValidationException(errorMessageUtil.getMessage("realWorldTesting.upload.emptyFile"));
            }

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
        rwtUpload = setChplProductNumber(record, rwtUpload);
        rwtUpload = setRwtType(record, rwtUpload);
        rwtUpload = setLastCheckedDate(record, rwtUpload);
        rwtUpload = setUrl(record, rwtUpload);
        rwtUpload.setOrder(record.getRecordNumber());
        return rwtUpload;
    }

    private RealWorldTestingUpload setChplProductNumber(CSVRecord record, RealWorldTestingUpload rwtUpload) {
        rwtUpload.getOriginalData().setChplProductNumber(record.get(CHPL_PRODUCT_NUMBER_COLUMN_IDX));
        if (!StringUtils.isEmpty(record.get(CHPL_PRODUCT_NUMBER_COLUMN_IDX))) {
            rwtUpload.setChplProductNumber(record.get(CHPL_PRODUCT_NUMBER_COLUMN_IDX));
        } else {
            rwtUpload.getValidationErrors()
                    .add(errorMessageUtil.getMessage("realWorldTesting.upload.chplProductNumberInvalid"));
        }
        return rwtUpload;
    }

    private RealWorldTestingUpload setRwtType(CSVRecord record, RealWorldTestingUpload rwtUpload) {
        rwtUpload.getOriginalData().setType(record.get(RWT_TYPE_COLUMN_IDX));
        if (!StringUtils.isEmpty(record.get(RWT_TYPE_COLUMN_IDX))) {
            try {
                rwtUpload.setType(getType(record.get(RWT_TYPE_COLUMN_IDX)));
            } catch (ParseException e) {
                rwtUpload.getValidationErrors()
                        .add(errorMessageUtil.getMessage("realWorldTesting.upload.realWorldTestingTypeInvalid"));
            }
        } else {
            rwtUpload.getValidationErrors()
                    .add(errorMessageUtil.getMessage("realWorldTesting.upload.realWorldTestingTypeInvalid"));
        }
        return rwtUpload;
    }

    private RealWorldTestingUpload setLastCheckedDate(CSVRecord record, RealWorldTestingUpload rwtUpload) {
        rwtUpload.getOriginalData().setLastChecked(record.get(LAST_CHECKED_COLUMN_IDX));
        if (!StringUtils.isEmpty(record.get(LAST_CHECKED_COLUMN_IDX))) {
            try {
                rwtUpload.setLastChecked(getLastCheckedDate(record.get(LAST_CHECKED_COLUMN_IDX)));
            } catch (DateTimeParseException e) {
                rwtUpload.getValidationErrors().add(errorMessageUtil.getMessage("realWorldTesting.upload.lastCheckedDateInvalid"));
            }
        } else {
            rwtUpload.setLastChecked(null);
        }
        return rwtUpload;
    }

    private RealWorldTestingUpload setUrl(CSVRecord record, RealWorldTestingUpload rwtUpload) {
        rwtUpload.getOriginalData().setUrl(record.get(URL_COLUMN_IDX));
        if (!StringUtils.isEmpty(record.get(URL_COLUMN_IDX))) {
            rwtUpload.setUrl(record.get(URL_COLUMN_IDX));
        } else {
            rwtUpload.setUrl(null);
        }
        return rwtUpload;
    }

    private LocalDate getLastCheckedDate(String unformatted) throws DateTimeParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyMMdd");
        return LocalDate.parse(unformatted, formatter);
    }

    private RealWorldTestingType getType(String rwtType) throws ParseException {
        if (rwtType.toUpperCase().equals(RealWorldTestingType.PLANS.getName())) {
            return RealWorldTestingType.PLANS;
        } else if (rwtType.toUpperCase().equals(RealWorldTestingType.RESULTS.getName())) {
            return RealWorldTestingType.RESULTS;
        } else {
            throw new ParseException("Could not parse Real World Testing Type: " + rwtType, 0);
        }
    }

    private List<CSVRecord> removeHeaderRow(List<CSVRecord> records) {
        if (doesHeaderRowExist(records)) {
            records.remove(0);
        }
        return records;
    }

    private List<CSVRecord> removeEmptyRows(List<CSVRecord> records) {
        return records.stream().filter(rec -> !isRowEmpty(rec)).collect(Collectors.toList());
    }

    private boolean isRowEmpty(CSVRecord rec) {
        return rec.get(CHPL_PRODUCT_NUMBER_COLUMN_IDX).trim().equals("")
                && rec.get(RWT_TYPE_COLUMN_IDX).trim().equals("") && rec.get(LAST_CHECKED_COLUMN_IDX).trim().equals("")
                && rec.get(URL_COLUMN_IDX).trim().equals("");
    }

    private boolean doesHeaderRowExist(List<CSVRecord> records) {
        // If the CHPL Product Number is UNIQUE_CHPL_ID__C assume there is a header
        return records.get(0).get(CHPL_PRODUCT_NUMBER_COLUMN_IDX).toUpperCase().contains("UNIQUE_CHPL_ID__C");
    }

    private void checkBasicFileProperties(MultipartFile file) throws ValidationException {
        if (file.isEmpty()) {
            throw new ValidationException(errorMessageUtil.getMessage("realWorldTesting.upload.emptyFile"));
        }

        if (!file.getContentType().equalsIgnoreCase("text/csv")
                && !file.getContentType().equalsIgnoreCase("application/vnd.ms-excel")) {
            throw new ValidationException("File must be a CSV document.");
        }
    }
}
