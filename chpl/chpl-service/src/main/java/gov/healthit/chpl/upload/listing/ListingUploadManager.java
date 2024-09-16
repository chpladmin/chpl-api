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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
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
import gov.healthit.chpl.domain.ConfirmListingRequest;
import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.ActivityException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ObjectMissingValidationException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.scheduler.job.ListingUploadValidationJob;
import gov.healthit.chpl.upload.listing.ListingUploadHeadingUtil.Heading;
import gov.healthit.chpl.upload.listing.handler.CertificationDateHandler;
import gov.healthit.chpl.upload.listing.handler.ListingDetailsUploadHandler;
import gov.healthit.chpl.upload.listing.normalizer.BaselineStandardAsOfCertificationDayNormalizer;
import gov.healthit.chpl.upload.listing.normalizer.ListingDetailsNormalizer;
import gov.healthit.chpl.upload.listing.validation.ListingUploadValidator;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ListingUploadManager {
    private ListingDetailsUploadHandler listingDetailsHandler;
    private CertificationDateHandler certDateHandler;
    private ListingDetailsNormalizer listingNormalizer;
    private ListingUploadHandlerUtil uploadUtil;
    private ChplProductNumberUtil chplProductNumberUtil;
    private ListingUploadDao listingUploadDao;
    private ListingUploadValidator listingUploadValidator;
    private CertificationBodyDAO acbDao;
    private UserDAO userDao;
    private ListingConfirmationManager listingConfirmationManager;
    private SchedulerManager schedulerManager;
    private ActivityManager activityManager;
    private ErrorMessageUtil msgUtil;
    private BaselineStandardAsOfCertificationDayNormalizer baselineStandardNormalizer;

    @Autowired
    @SuppressWarnings("checkstyle:parameternumber")
    public ListingUploadManager(ListingDetailsUploadHandler listingDetailsHandler,
            CertificationDateHandler certDateHandler, ListingDetailsNormalizer listingNormalizer,
            ListingUploadValidator listingUploadValidator, ListingUploadHandlerUtil uploadUtil,
            ChplProductNumberUtil chplProductNumberUtil, ListingUploadDao listingUploadDao,
            CertificationBodyDAO acbDao, UserDAO userDao,
            ListingConfirmationManager listingConfirmationManager, SchedulerManager schedulerManager,
            ActivityManager activityManager, ErrorMessageUtil msgUtil,
            BaselineStandardAsOfCertificationDayNormalizer baselineStandardNormalizer) {
        this.listingDetailsHandler = listingDetailsHandler;
        this.certDateHandler = certDateHandler;
        this.listingNormalizer = listingNormalizer;
        this.listingUploadValidator = listingUploadValidator;
        this.uploadUtil = uploadUtil;
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.listingUploadDao = listingUploadDao;
        this.acbDao = acbDao;
        this.userDao = userDao;
        this.listingConfirmationManager = listingConfirmationManager;
        this.schedulerManager = schedulerManager;
        this.activityManager = activityManager;
        this.msgUtil = msgUtil;
        this.baselineStandardNormalizer = baselineStandardNormalizer;
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
    public ListingUpload createOrReplaceListingUpload(ListingUpload uploadMetadata) throws ValidationException, ActivityException {
        if (StringUtils.isEmpty(uploadMetadata.getChplProductNumber())) {
            throw new ValidationException(msgUtil.getMessage("listing.upload.missingChplProductNumber"));
        } else if (uploadMetadata.getAcb() == null || uploadMetadata.getAcb().getId() == null) {
            throw new ValidationException(msgUtil.getMessage("listing.upload.missingAcb"));
        }

        ListingUpload existingListing = listingUploadDao.getByChplProductNumber(uploadMetadata.getChplProductNumber());
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
    public List<ListingUpload> getAllProcessingAndAvailable() {
        return listingUploadDao.getAllProcessingAndAvailable();
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
        CertifiedProductSearchDetails listing = listingDetailsHandler.parseAsListing(headingRecord, allListingRecords);
        listing.setId(id);
        LOGGER.debug("Converted listing upload with ID " + id + " into CertifiedProductSearchDetails object");
        listingNormalizer.normalize(listing, List.of(baselineStandardNormalizer));
        LOGGER.debug("Normalized listing upload with ID " + id);
        listingUploadValidator.review(listingUpload, listing);
        LOGGER.debug("Validated listing upload with ID " + id);
        return listing;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).LISTING_UPLOAD, "
            + "T(gov.healthit.chpl.permissions.domains.ListingUploadDomainPerissions).GET_BY_ID, #id)")
    public CertifiedProductSearchDetails getSubmittedListing(Long id) throws ValidationException, EntityRetrievalException {
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
        CertifiedProductSearchDetails listing = listingDetailsHandler.parseAsListing(headingRecord, allListingRecords);
        copyUserEnteredDeveloperDataToRegularDeveloperData(listing);
        return listing;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).LISTING_UPLOAD, "
            + "T(gov.healthit.chpl.permissions.domains.ListingUploadDomainPerissions).GET_UPLOADED_CSV)")
    public List<List<String>> getUploadedCsvRecords(Long confirmedListingId) throws EntityRetrievalException {
        ListingUpload listingUpload = listingUploadDao.getByConfirmedListingIdIncludingRecords(confirmedListingId);
        LOGGER.debug("Confirmed listing " + confirmedListingId + " has uploaded listing ID " + listingUpload.getId());
        List<CSVRecord> allCsvRecords = listingUpload.getRecords();
        if (allCsvRecords == null) {
            LOGGER.warn("Confirmed listing " + confirmedListingId + " has no CSV records associated with it.");
            return null;
        }
        LOGGER.debug("Confirmed listing " + confirmedListingId + " has " + allCsvRecords.size() + " CSV records associated with it.");
        List<List<String>> records = new ArrayList<List<String>>();
        allCsvRecords.stream()
                .forEach(csvRecord -> records.add(csvRecord.stream().collect(Collectors.toList())));
        return records;
    }

    private void copyUserEnteredDeveloperDataToRegularDeveloperData(CertifiedProductSearchDetails listing) {
        listing.getDeveloper().setName(listing.getDeveloper().getUserEnteredName());
        listing.getDeveloper().setAddress(listing.getDeveloper().getUserEnteredAddress());
        listing.getDeveloper().setContact(listing.getDeveloper().getUserEnteredPointOfContact());
        Boolean selfDeveloper = null;
        try {
            selfDeveloper = uploadUtil.parseBoolean(listing.getDeveloper().getUserEnteredSelfDeveloper());
        } catch (Exception ex) {
        }
        listing.getDeveloper().setSelfDeveloper(selfDeveloper);
        listing.getDeveloper().setWebsite(listing.getDeveloper().getUserEnteredWebsite());
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).LISTING_UPLOAD, "
            + "T(gov.healthit.chpl.permissions.domains.ListingUploadDomainPerissions).VALIDATE_BY_IDS)")
    public void calculateErrorAndWarningCounts(List<Long> listingUploadIds)
            throws ValidationException, SchedulerException {

        ChplOneTimeTrigger validateListingUploadTrigger = new ChplOneTimeTrigger();
        ChplJob validateListingUploadJob = new ChplJob();
        validateListingUploadJob.setName(ListingUploadValidationJob.JOB_NAME);
        validateListingUploadJob.setGroup(SchedulerManager.CHPL_BACKGROUND_JOBS_KEY);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(ListingUploadValidationJob.LISTING_UPLOAD_IDS, listingUploadIds);
        jobDataMap.put(ListingUploadValidationJob.USER_KEY, AuthUtil.getCurrentUser());
        validateListingUploadJob.setJobDataMap(jobDataMap);
        validateListingUploadTrigger.setJob(validateListingUploadJob);
        validateListingUploadTrigger.setRunDateMillis(System.currentTimeMillis() + SchedulerManager.FIVE_SECONDS_IN_MILLIS);
        validateListingUploadTrigger = schedulerManager.createBackgroundJobTrigger(validateListingUploadTrigger);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).LISTING_UPLOAD, "
            + "T(gov.healthit.chpl.permissions.domains.ListingUploadDomainPerissions).CONFIRM, #id)")
    public CertifiedProductSearchDetails confirm(Long id, ConfirmListingRequest confirmListingRequest)
            throws InvalidArgumentsException, JsonProcessingException, EntityRetrievalException, EntityCreationException,
            ValidationException, ActivityException {
        // Is listing already processing?
        if (!listingUploadDao.isAvailableForProcessing(id)) {
            throw new InvalidArgumentsException(msgUtil.getMessage("pendingListing.alreadyProcessing"));
        } else {
            CertifiedProductSearchDetails confirmedListing = null;
            listingUploadDao.updateStatus(id, ListingUploadStatus.CONFIRMATION_PROCESSING);
            try {
                CertifiedProductSearchDetails listing = confirmListingRequest.getListing();
                checkForErrorsOrUnacknowledgedWarnings(listing, confirmListingRequest.isAcknowledgeWarnings());
                confirmedListing = listingConfirmationManager.create(listing);
            } catch (ValidationException ex) {
                listingUploadDao.updateStatus(id, ListingUploadStatus.UPLOAD_SUCCESS);
                LOGGER.error("Could not confirm pending listing " + id + " due to validation error.");
                throw ex;
            } catch (ActivityException ex) {
                listingUploadDao.updateStatus(id, ListingUploadStatus.UPLOAD_SUCCESS);
                LOGGER.error("Could not generate activity.");
                throw ex;
            } catch (Exception ex) {
                listingUploadDao.updateStatus(id, ListingUploadStatus.UPLOAD_SUCCESS);
                LOGGER.error("Could not confirm pending listing " + id, ex);
                throw ex;
            }
            listingUploadDao.updateStatus(id, ListingUploadStatus.CONFIRMED);
            listingUploadDao.updateConfirmedListingId(id, confirmedListing.getId());
            return confirmedListing;
        }
    }

    private void checkForErrorsOrUnacknowledgedWarnings(CertifiedProductSearchDetails listing,
            boolean acknowledgeWarnings) throws ValidationException {
        listingNormalizer.normalize(listing, List.of(baselineStandardNormalizer));
        LOGGER.debug("Normalized listing for confirmation: " + listing.getChplProductNumber());
        listingUploadValidator.review(listing);
        LOGGER.debug("Validated listing for confirmation: " + listing.getChplProductNumber());

        if (listing.getErrorMessages() != null && listing.getErrorMessages().size() > 0
                || (listing.getWarningMessages() != null && listing.getWarningMessages().size() > 0
                        && !acknowledgeWarnings)) {
            throw new ValidationException(listing.getErrorMessages(), listing.getBusinessErrorMessages(), listing.getDataErrorMessages(), listing.getWarningMessages());
        }
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).LISTING_UPLOAD, "
            + "T(gov.healthit.chpl.permissions.domains.ListingUploadDomainPerissions).DELETE, #id)")
    public void reject(Long id) throws ObjectMissingValidationException, EntityRetrievalException, ActivityException {
        if (isListingUploadAvailableForRejection(id)) {
            ListingUpload listingUploadBeforeDelete = listingUploadDao.getById(id);
            listingUploadDao.updateStatus(id, ListingUploadStatus.REJECTED);
            String activityMsg = "Listing upload " + listingUploadBeforeDelete.getChplProductNumber() + " has been rejected.";
            activityManager.addActivity(ActivityConcept.LISTING_UPLOAD, listingUploadBeforeDelete.getId(),
                    activityMsg, listingUploadBeforeDelete, null);
        }
    }

    private boolean isListingUploadAvailableForRejection(Long id)
            throws EntityRetrievalException, ObjectMissingValidationException {

        ListingUploadEntity entity = listingUploadDao.getEntityByIdIncludingDeleted(id);
        if (entity.getStatus().equals(ListingUploadStatus.CONFIRMED)
                || entity.getStatus().equals(ListingUploadStatus.REJECTED)
                || BooleanUtils.isTrue(entity.getDeleted())) {
            ObjectMissingValidationException alreadyHandledEx =
                    new ObjectMissingValidationException(
                            "This pending certified product has already been confirmed or rejected by another user.",
                            null,
                            entity.getChplProductNumber());
            try {
                UserDTO lastModifiedUserDto = userDao.getById(entity.getLastModifiedUser());
                if (lastModifiedUserDto != null) {
                    User lastModifiedUser = lastModifiedUserDto.toDomain();
                    alreadyHandledEx.setUser(lastModifiedUser);
                } else {
                    alreadyHandledEx.setUser(null);
                }
            } catch (UserRetrievalException ex) {
                alreadyHandledEx.setUser(null);
            }
            throw alreadyHandledEx;
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
        List<String> missingRequiredHeadings = ListingUploadHeadingUtil.getRequiredHeadings().stream()
                .filter(heading -> !uploadUtil.hasHeading(heading, headingRecord))
                .map(headingVal -> headingVal.getNamesAsString())
                .collect(Collectors.toList());
        if (missingRequiredHeadings.size() > 0) {
            throw new ValidationException(msgUtil.getMessage("listing.upload.missingRequiredHeadings",
                    String.join("; ", missingRequiredHeadings)));
        }
    }

    private void checkRequiredFields(CSVRecord headingRecord, List<CSVRecord> listingRecords) throws ValidationException {
        List<String> headingsWithMissingData = ListingUploadHeadingUtil.getRequiredHeadings().stream()
                .filter(heading -> StringUtils.isEmpty(uploadUtil.parseSingleRowField(heading, headingRecord, listingRecords)))
                .map(headingVal -> headingVal.getNamesAsString())
                .collect(Collectors.toList());
        if (headingsWithMissingData.size() > 0) {
            throw new ValidationException(msgUtil.getMessage("listing.upload.missingRequiredData",
                    String.join("; ", headingsWithMissingData)));
        }
    }

    private String parseRequiredField(Heading heading, CSVRecord headingRecord, List<CSVRecord> listingRecords) {
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
        // first look for an ACB name in the file
        String acbName = uploadUtil.parseSingleRowField(Heading.CERTIFICATION_BODY_NAME, headingRecord, listingRecords);
        if (!StringUtils.isEmpty(acbName)) {
            CertificationBody acbByName = acbDao.getByName(acbName);
            if (acbByName != null) {
                acb = acbByName;
            }
        }
        // if it's not there use the ACB code from the CHPL product number
        String acbCode = chplProductNumberUtil.getAcbCode(chplProductNumber);
        if (!StringUtils.isEmpty(acbCode)) {
            CertificationBody acbByCode = acbDao.getByCode(acbCode);
            if (acbByCode != null) {
                acb = acbByCode;
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
                    Heading.UNIQUE_ID, headingRecord, record);
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
        listingUploadMetadata.setChplProductNumber(parseRequiredField(Heading.UNIQUE_ID, headingRecord, listingCsvRecords));
        listingUploadMetadata.setDeveloper(parseRequiredField(Heading.DEVELOPER, headingRecord, listingCsvRecords));
        listingUploadMetadata.setProduct(parseRequiredField(Heading.PRODUCT, headingRecord, listingCsvRecords));
        listingUploadMetadata.setVersion(parseRequiredField(Heading.VERSION, headingRecord, listingCsvRecords));
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
