package gov.healthit.chpl.upload.listing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ObjectMissingValidationException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.scheduler.job.ListingUploadValidationJob;
import gov.healthit.chpl.upload.listing.handler.CertificationDateHandler;
import gov.healthit.chpl.upload.listing.handler.ListingDetailsUploadHandler;
import gov.healthit.chpl.upload.listing.normalizer.ListingDetailsNormalizer;
import gov.healthit.chpl.upload.listing.validation.ListingUploadValidator;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ListingUploadManager {
    public static final String NEW_DEVELOPER_CODE = "XXXX";

    private ListingDetailsUploadHandler listingDetailsHandler;
    private CertificationDateHandler certDateHandler;
    private ListingDetailsNormalizer listingNormalizer;
    private ListingUploadHandlerUtil uploadUtil;
    private ChplProductNumberUtil chplProductNumberUtil;
    private ListingUploadDao listingUploadDao;
    private ListingUploadValidator listingUploadValidator;
    private CertificationBodyDAO acbDao;
    private UserDAO userDao;
    private SchedulerManager schedulerManager;
    private ActivityManager activityManager;
    private ErrorMessageUtil msgUtil;

    @Autowired
    @SuppressWarnings("checkstyle:parameternumber")
    public ListingUploadManager(ListingDetailsUploadHandler listingDetailsHandler,
            CertificationDateHandler certDateHandler,
            ListingDetailsNormalizer listingNormalizer,
            ListingUploadValidator listingUploadValidator,
            ListingUploadHandlerUtil uploadUtil, ChplProductNumberUtil chplProductNumberUtil,
            ListingUploadDao listingUploadDao, CertificationBodyDAO acbDao, UserDAO userDao,
            SchedulerManager schedulerManager, ActivityManager activityManager, ErrorMessageUtil msgUtil) {
        this.listingDetailsHandler = listingDetailsHandler;
        this.certDateHandler = certDateHandler;
        this.listingNormalizer = listingNormalizer;
        this.listingUploadValidator = listingUploadValidator;
        this.uploadUtil = uploadUtil;
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.listingUploadDao = listingUploadDao;
        this.acbDao = acbDao;
        this.userDao = userDao;
        this.schedulerManager = schedulerManager;
        this.activityManager = activityManager;
        this.msgUtil = msgUtil;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).LISTING_UPLOAD, "
            + "T(gov.healthit.chpl.permissions.domains.ListingUploadDomainPerissions).CREATE)")
    public List<ListingUpload> parseUploadFile(MultipartFile file) throws ValidationException {
        List<CSVRecord> allCsvRecords = getFileAsCsvRecords(file);
        int headingRowIndex = uploadUtil.getHeadingRecordIndex(allCsvRecords);
        if (headingRowIndex < 0) {
            throw new ValidationException(msgUtil.getMessage("listing.upload.noHeadingFound"));
        }
        CSVRecord headingRecord = uploadUtil.getHeadingRecord(allCsvRecords);
        List<CSVRecord> allListingRecords = allCsvRecords.subList(headingRowIndex + 1, allCsvRecords.size());
        if (headingRecord == null) {
            LOGGER.warn("Cannot continue parsing upload file " + file.getName() + " without heading.");
            throw new ValidationException(msgUtil.getMessage("listing.upload.noHeadingFound"));
        }
        checkRequiredHeadings(headingRecord);
        List<List<CSVRecord>> recordsGroupedByListing = groupRecordsByListing(headingRecord, allListingRecords);
        for (List<CSVRecord> listingRecords : recordsGroupedByListing) {
            checkRequiredFields(headingRecord, listingRecords);
        }

        List<ListingUpload> uploadedListings = recordsGroupedByListing.stream()
                .map(listingRecords -> createListingUploadMetadata(headingRecord, listingRecords))
                .collect(Collectors.toList());
        return uploadedListings;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).LISTING_UPLOAD, "
            + "T(gov.healthit.chpl.permissions.domains.ListingUploadDomainPerissions).CREATE, #uploadMetadata)")
    public ListingUpload createOrReplaceListingUpload(ListingUpload uploadMetadata) throws ValidationException,
        JsonProcessingException, EntityRetrievalException, EntityCreationException {
        if (StringUtils.isEmpty(uploadMetadata.getChplProductNumber())) {
            throw new ValidationException(msgUtil.getMessage("listing.upload.missingChplProductNumber"));
        } else if (uploadMetadata.getAcb() == null || uploadMetadata.getAcb().getId() == null) {
            throw new ValidationException(msgUtil.getMessage("listing.upload.missingAcb"));
        }

        //Should we actually only allow unique CHPL IDs here?
        //It would keep functionality the same, but a file with new developer codes can have multiple
        //listings with the same pending CHPL ID and the latest one would overwrite the earlier one.
        ListingUpload existingListing =
                listingUploadDao.getByChplProductNumber(uploadMetadata.getChplProductNumber());
        if (existingListing != null) {
            listingUploadDao.delete(existingListing.getId());
        }
        ListingUpload created = listingUploadDao.create(uploadMetadata);
        String activityMsg = "Listing upload " + created.getChplProductNumber() + " has been created.";
        activityManager.addActivity(ActivityConcept.LISTING_UPLOAD, created.getId(),
                activityMsg, null, created);
        return created;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).LISTING_UPLOAD, "
            + "T(gov.healthit.chpl.permissions.domains.ListingUploadDomainPerissions).GET_ALL)")
    @PostFilter("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).LISTING_UPLOAD, "
            + "T(gov.healthit.chpl.permissions.domains.ListingUploadDomainPerissions).GET_ALL, filterObject)")
    public List<ListingUpload> getAll() {
        return listingUploadDao.getAll();
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).LISTING_UPLOAD, "
            + "T(gov.healthit.chpl.permissions.domains.ListingUploadDomainPerissions).GET_BY_ID, #id)")
    public ListingUpload getById(Long id) throws EntityRetrievalException {
        return listingUploadDao.getById(id);
    }

    @Transactional
    @Cacheable(CacheNames.UPLOADED_LISTING_DETAILS)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).LISTING_UPLOAD, "
            + "T(gov.healthit.chpl.permissions.domains.ListingUploadDomainPerissions).GET_BY_ID, #id)")
    public CertifiedProductSearchDetails getDetailsById(Long id) throws ValidationException, EntityRetrievalException {
        ListingUpload listingUpload = listingUploadDao.getByIdIncludingRecords(id);
        LOGGER.debug("Got listing upload with ID " + id);
        List<CSVRecord> allCsvRecords = listingUpload.getRecords();
        if (allCsvRecords == null) {
            LOGGER.debug("Listing upload with ID " + id + " has no CSV records associated with it.");
            return null;
        }
        LOGGER.debug("Listing upload with ID " + id + " has " + allCsvRecords.size() + " CSV records associated with it.");
        int headingRowIndex = uploadUtil.getHeadingRecordIndex(allCsvRecords);
        CSVRecord headingRecord = uploadUtil.getHeadingRecord(allCsvRecords);
        List<CSVRecord> allListingRecords = allCsvRecords.subList(headingRowIndex + 1, allCsvRecords.size());
        LOGGER.debug("Converting listing upload with ID " + id + " into CertifiedProductSearchDetails object");
        CertifiedProductSearchDetails listing =
                listingDetailsHandler.parseAsListing(headingRecord, allListingRecords);
        listing.setId(id);
        LOGGER.debug("Converted listing upload with ID " + id + " into CertifiedProductSearchDetails object");
        listingNormalizer.normalize(listing);
        LOGGER.debug("Normalized listing upload with ID " + id);
        listingUploadValidator.review(listingUpload, listing);
        LOGGER.debug("Validated listing upload with ID " + id);
        return listing;
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).LISTING_UPLOAD, "
            + "T(gov.healthit.chpl.permissions.domains.ListingUploadDomainPerissions).VALIDATE_BY_IDS)")
    public void calculateErrorAndWarningCounts(List<Long> listingUploadIds)
            throws ValidationException, SchedulerException {
        UserDTO jobUser = null;
        try {
            jobUser = userDao.getById(AuthUtil.getCurrentUser().getId());
        } catch (UserRetrievalException ex) {
            LOGGER.error("Could not find user to execute job.");
        }

        ChplOneTimeTrigger validateListingUploadTrigger = new ChplOneTimeTrigger();
        ChplJob validateListingUploadJob = new ChplJob();
        validateListingUploadJob.setName(ListingUploadValidationJob.JOB_NAME);
        validateListingUploadJob.setGroup(SchedulerManager.CHPL_BACKGROUND_JOBS_KEY);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(ListingUploadValidationJob.LISTING_UPLOAD_IDS, listingUploadIds);
        jobDataMap.put(ListingUploadValidationJob.USER_KEY, jobUser);
        validateListingUploadJob.setJobDataMap(jobDataMap);
        validateListingUploadTrigger.setJob(validateListingUploadJob);
        validateListingUploadTrigger.setRunDateMillis(System.currentTimeMillis() + SchedulerManager.DELAY_BEFORE_BACKGROUND_JOB_START);
        validateListingUploadTrigger = schedulerManager.createBackgroundJobTrigger(validateListingUploadTrigger);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).LISTING_UPLOAD, "
            + "T(gov.healthit.chpl.permissions.domains.ListingUploadDomainPerissions).DELETE, #id)")
    public void delete(Long id)
            throws EntityRetrievalException, EntityNotFoundException, EntityCreationException, AccessDeniedException,
            JsonProcessingException, ObjectMissingValidationException {
        if (isListingUploadAvailableForDelete(id)) {
            ListingUpload listingUploadBeforeDelete = listingUploadDao.getById(id);
            listingUploadDao.delete(id);
            String activityMsg = "Listing upload " + listingUploadBeforeDelete.getChplProductNumber() + " has been rejected.";
            activityManager.addActivity(ActivityConcept.LISTING_UPLOAD, listingUploadBeforeDelete.getId(),
                    activityMsg, listingUploadBeforeDelete, null);
        }
    }

    private boolean isListingUploadAvailableForDelete(Long id)
            throws EntityRetrievalException, ObjectMissingValidationException {

        ListingUploadEntity entity = listingUploadDao.getEntityByIdIncludingDeleted(id);
        if (entity.getDeleted()) {
            ObjectMissingValidationException alreadyDeletedEx = new ObjectMissingValidationException();
            alreadyDeletedEx.getErrorMessages()
                    .add("This pending certified product has already been confirmed or rejected by another user.");
            alreadyDeletedEx.setObjectId(entity.getChplProductNumber());
            try {
                UserDTO lastModifiedUserDto = userDao.getById(entity.getLastModifiedUser());
                if (lastModifiedUserDto != null) {
                    User lastModifiedUser = new User(lastModifiedUserDto);
                    alreadyDeletedEx.setUser(lastModifiedUser);
                } else {
                    alreadyDeletedEx.setUser(null);
                }
            } catch (final UserRetrievalException ex) {
                alreadyDeletedEx.setUser(null);
            }
            throw alreadyDeletedEx;
        } else {
            return true;
        }

    }

    private List<List<CSVRecord>> groupRecordsByListing(CSVRecord headingRecord, List<CSVRecord> allRecords) {
        List<List<CSVRecord>> recordsGroupedByListing = new ArrayList<List<CSVRecord>>();
        int fileStartIndex = 0;
        while (fileStartIndex < allRecords.size()) {
            List<CSVRecord> currListingRecords = getNextListingRecordGroup(fileStartIndex, headingRecord, allRecords);
            recordsGroupedByListing.add(currListingRecords);
            fileStartIndex += currListingRecords.size();
        }
        return recordsGroupedByListing;
    }

    private void checkRequiredHeadings(CSVRecord headingRecord) throws ValidationException {
        List<String> missingRequiredHeadings = Headings.getRequiredHeadings().stream()
            .filter(heading -> !uploadUtil.hasHeading(heading, headingRecord))
            .map(headingVal -> headingVal.getNamesAsString())
            .collect(Collectors.toList());
        if (missingRequiredHeadings.size() > 0) {
            throw new ValidationException(msgUtil.getMessage("listing.upload.missingRequiredHeadings",
                    String.join("; ", missingRequiredHeadings)));
        }
    }

    private void checkRequiredFields(CSVRecord headingRecord, List<CSVRecord> listingRecords) throws ValidationException {
        List<String> headingsWithMissingData = Headings.getRequiredHeadings().stream()
            .filter(heading -> StringUtils.isEmpty(uploadUtil.parseSingleRowField(heading, headingRecord, listingRecords)))
            .map(headingVal -> headingVal.getNamesAsString())
            .collect(Collectors.toList());
        if (headingsWithMissingData.size() > 0) {
            throw new ValidationException(msgUtil.getMessage("listing.upload.missingRequiredData",
                    String.join("; ", headingsWithMissingData)));
        }
    }

    private String parseRequiredField(Headings heading, CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        String value = null;
        try {
            value = uploadUtil.parseRequiredSingleRowField(
                heading, headingRecord, listingRecords);
        } catch (Exception ex) {
            LOGGER.error("Could not parse required field " + heading.name() + ": " + ex.getMessage());
        }
        return value;
    }

    private CertificationBody determineAcb(CSVRecord headingRecord, List<CSVRecord> listingRecords, String chplProductNumber) {
        CertificationBody acb = null;
        //first look for an ACB name in the file
       String acbName = uploadUtil.parseSingleRowField(Headings.CERTIFICATION_BODY_NAME, headingRecord, listingRecords);
       if (!StringUtils.isEmpty(acbName)) {
           CertificationBodyDTO acbByName = acbDao.getByName(acbName);
           if (acbByName != null) {
               acb = new CertificationBody(acbByName);
           }
       }
        //if it's not there use the ACB code from the CHPL product number
       String acbCode = chplProductNumberUtil.getAcbCode(chplProductNumber);
       if (!StringUtils.isEmpty(acbCode)) {
           CertificationBodyDTO acbByCode = acbDao.getByCode(acbCode);
           if (acbByCode != null) {
               acb = new CertificationBody(acbByCode);
           }
       }
       return acb;
    }

    private List<CSVRecord> getNextListingRecordGroup(int startRow, CSVRecord headingRecord,
            List<CSVRecord> allListingRecords) {
        List<CSVRecord> listingCsvRecords = new ArrayList<CSVRecord>();
        Iterator<CSVRecord> listingCsvRecordsIter = allListingRecords.stream().skip(startRow).iterator();
        String chplProductNumber = null;
        while (listingCsvRecordsIter.hasNext()) {
            CSVRecord record = listingCsvRecordsIter.next();
            String recordUniqueId = uploadUtil.parseRequiredSingleRowField(
                    Headings.UNIQUE_ID, headingRecord, record);
            if (chplProductNumber == null) {
                chplProductNumber = new String(recordUniqueId);
                listingCsvRecords.add(record);
            } else if (StringUtils.isEmpty(recordUniqueId) || recordUniqueId.equals(chplProductNumber)) {
                listingCsvRecords.add(record);
            }
        }
        return listingCsvRecords;
    }

    private ListingUpload createListingUploadMetadata(CSVRecord headingRecord, List<CSVRecord> listingCsvRecords) {
        ListingUpload listingUploadMetadata = new ListingUpload();
        listingUploadMetadata.getRecords().add(headingRecord);
        listingUploadMetadata.getRecords().addAll(listingCsvRecords);
        listingUploadMetadata.setChplProductNumber(parseRequiredField(Headings.UNIQUE_ID, headingRecord, listingCsvRecords));
        listingUploadMetadata.setDeveloper(parseRequiredField(Headings.DEVELOPER, headingRecord, listingCsvRecords));
        listingUploadMetadata.setProduct(parseRequiredField(Headings.PRODUCT, headingRecord, listingCsvRecords));
        listingUploadMetadata.setVersion(parseRequiredField(Headings.VERSION, headingRecord, listingCsvRecords));
        CertificationBody acb = null;
        try {
            acb = determineAcb(headingRecord, listingCsvRecords, listingUploadMetadata.getChplProductNumber());
        } catch (Exception ex) {
            LOGGER.error("Could not determine ACB.", ex);
        }
        listingUploadMetadata.setAcb(acb);
        LocalDate certificationDate = null;
        try {
            certificationDate = certDateHandler.handle(headingRecord, listingCsvRecords);
        } catch (Exception ex) {
            LOGGER.error("Could not determine certification date.", ex);
        }
        listingUploadMetadata.setCertificationDate(certificationDate);
        return listingUploadMetadata;
    }

    private List<CSVRecord> getFileAsCsvRecords(MultipartFile file) throws ValidationException {
        if (file.isEmpty()) {
            LOGGER.warn("User uploaded an empty file.");
            throw new ValidationException(msgUtil.getMessage("upload.emptyFile"));
        }

        if (!file.getContentType().equalsIgnoreCase("text/csv")
                && !file.getContentType().equalsIgnoreCase("application/vnd.ms-excel")) {
            LOGGER.warn("User uploaded a file that is not csv and not excel. Content Type was " + file.getContentType());
            throw new ValidationException(msgUtil.getMessage("upload.notCSV"));
        }

        List<CSVRecord> records = null;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new BOMInputStream(file.getInputStream()), StandardCharsets.UTF_8.name()));
                CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL
                        .withIgnoreEmptyLines()
                        .withIgnoreSurroundingSpaces())) {
            records = parser.getRecords();
            trimTailingEmptyLines(records);
            if (records.size() <= 1) {
                throw new ValidationException(
                        msgUtil.getMessage("listing.upload.emptyRows"));
            }
        } catch (IOException ioEx) {
            LOGGER.error("Could not get input stream for uploaded file " + file.getName());
            throw new ValidationException(
                    msgUtil.getMessage("listing.upload.couldNotParse", file.getName()));
        }

        return records;
    }

    private void trimTailingEmptyLines(List<CSVRecord> records) {
        ListIterator<CSVRecord> recordsIter = records.listIterator();
        while (recordsIter.hasNext()) {
            CSVRecord currRecord = recordsIter.next();
            List<String> recordValues = Util.getListFromIterator(currRecord.iterator());
            if (StringUtils.isAllBlank(recordValues.toArray(new String[0]))) {
                recordsIter.remove();
            }
        }
    }
}
