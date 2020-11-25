package gov.healthit.chpl.upload.listing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ObjectMissingValidationException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.upload.listing.handler.ListingDetailsUploadHandler;
import gov.healthit.chpl.upload.listing.normalizer.ListingDetailsNormalizer;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ListingUploadManager {
    public static final String NEW_DEVELOPER_CODE = "XXXX";
    private static final String CERT_DATE_CODE = "yyMMdd";
    private DateFormat dateFormat;

    private ListingDetailsUploadHandler listingDetailsHandler;
    private ListingDetailsNormalizer listingNormalizer;
    private ListingUploadHandlerUtil uploadUtil;
    private ChplProductNumberUtil chplProductNumberUtil;
    private ListingUploadDao listingUploadDao;
    private CertificationBodyDAO acbDao;
    private UserDAO userDao;
    private ActivityManager activityManager;
    private ErrorMessageUtil msgUtil;

    @Autowired
    @SuppressWarnings("checkstyle:parameternumber")
    public ListingUploadManager(ListingDetailsUploadHandler listingDetailsHandler,
            ListingDetailsNormalizer listingNormalizer,
            ListingUploadHandlerUtil uploadUtil, ChplProductNumberUtil chplProductNumberUtil,
            ListingUploadDao listingUploadDao, CertificationBodyDAO acbDao, UserDAO userDao,
            ActivityManager activityManager, ErrorMessageUtil msgUtil) {
        this.dateFormat = new SimpleDateFormat(CERT_DATE_CODE);
        this.listingDetailsHandler = listingDetailsHandler;
        this.listingNormalizer = listingNormalizer;
        this.uploadUtil = uploadUtil;
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.listingUploadDao = listingUploadDao;
        this.acbDao = acbDao;
        this.userDao = userDao;
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

        List<ListingUpload> uploadedListings = new ArrayList<ListingUpload>();
        Set<String> distinctChplProductNumbers = getDistinctChplProductNumbers(headingRecord, allListingRecords);
        distinctChplProductNumbers.stream()
                .map(chplProductNumber -> getListingRecords(chplProductNumber, headingRecord, allListingRecords))
                .map(listingRecords -> createListingUploadMetadata(headingRecord, listingRecords))
                .forEach(listingUploadMetadata -> {
                    uploadedListings.add(listingUploadMetadata);
                });
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
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).LISTING_UPLOAD, "
            + "T(gov.healthit.chpl.permissions.domains.ListingUploadDomainPerissions).GET_BY_ID, #id)")
    public CertifiedProductSearchDetails getDetailsById(Long id) throws ValidationException, EntityRetrievalException {
        ListingUpload listingUpload = listingUploadDao.getByIdIncludingRecords(id);
        List<CSVRecord> allCsvRecords = listingUpload.getRecords();
        if (allCsvRecords == null) {
            return null;
        }
        int headingRowIndex = uploadUtil.getHeadingRecordIndex(allCsvRecords);
        CSVRecord headingRecord = uploadUtil.getHeadingRecord(allCsvRecords);
        List<CSVRecord> allListingRecords = allCsvRecords.subList(headingRowIndex + 1, allCsvRecords.size());
        CertifiedProductSearchDetails listing =
                listingDetailsHandler.parseAsListing(headingRecord, allListingRecords);
        listingNormalizer.normalize(listing);
        return listing;
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

    private void checkRequiredHeadings(CSVRecord headingRecord) throws ValidationException {
        List<String> missingRequiredHeadings = Headings.getRequiredHeadings().stream()
            .filter(heading -> !uploadUtil.hasHeading(heading, headingRecord))
            .map(headingVal -> headingVal.getNamesAsString())
            .collect(Collectors.toList());
        if (missingRequiredHeadings != null && missingRequiredHeadings.size() > 0) {
            throw new ValidationException(msgUtil.getMessage("listing.upload.missingRequiredHeadings",
                    String.join("; ", missingRequiredHeadings)));
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

    private LocalDate determineCertificationDate(CSVRecord headingRecord,
            List<CSVRecord> listingRecords, String chplProductNumber) throws ParseException, ValidationException {
        LocalDate certificationDate = null;
        //first look for a certification date column in the file
       Date certDateFromFile = uploadUtil.parseSingleRowFieldAsDate(Headings.CERTIFICATION_DATE, headingRecord, listingRecords);
       if (certDateFromFile != null) {
           certificationDate = certDateFromFile.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
       }
        //if it's not there use the cert date code from the CHPL product number
       String certDateCode = chplProductNumberUtil.getCertificationDateCode(chplProductNumber);
       if (!StringUtils.isEmpty(certDateCode)) {
           Date certDateFromChplNumber = dateFormat.parse(certDateCode);
           certificationDate = certDateFromChplNumber.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
       }
       return certificationDate;
    }

    private List<CSVRecord> getListingRecords(String chplProductNumber, CSVRecord headingRecord,
            List<CSVRecord> allListingRecords) {
        List<CSVRecord> listingCsvRecords = new ArrayList<CSVRecord>();
        Iterator<CSVRecord> listingCsvRecordsIter = allListingRecords.stream().iterator();
        while (listingCsvRecordsIter.hasNext()) {
            CSVRecord record = listingCsvRecordsIter.next();
            String recordUniqueId = uploadUtil.parseRequiredSingleRowField(
                    Headings.UNIQUE_ID, headingRecord, record);
            if (!StringUtils.isEmpty(recordUniqueId) && recordUniqueId.equals(chplProductNumber)) {
                listingCsvRecords.add(record);
            }
        }
        return listingCsvRecords;
    }

    private Set<String> getDistinctChplProductNumbers(CSVRecord headingRecord, List<CSVRecord> allCsvRecords) {
        List<String> distinctChplProductNumbers = uploadUtil.parseMultiRowField(Headings.UNIQUE_ID, headingRecord, allCsvRecords);
        return distinctChplProductNumbers.stream().collect(Collectors.toSet());
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
            certificationDate = determineCertificationDate(headingRecord, listingCsvRecords,
                    listingUploadMetadata.getChplProductNumber());
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
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8.name()));
                CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL
                        .withIgnoreEmptyLines()
                        .withIgnoreSurroundingSpaces())) {
            records = parser.getRecords();
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
}
