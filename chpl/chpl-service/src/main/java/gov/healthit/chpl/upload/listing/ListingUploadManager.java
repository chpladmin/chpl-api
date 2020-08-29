package gov.healthit.chpl.upload.listing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ListingUploadManager {
    public static final String NEW_DEVELOPER_CODE = "XXXX";

    private ListingUploadHandlerUtil uploadUtil;
    private ListingUploadDao listingUploadDao;
    private CertificationBodyDAO acbDao;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public ListingUploadManager(ListingUploadHandlerUtil uploadUtil,
            ListingUploadDao listingUploadDao, CertificationBodyDAO acbDao, ErrorMessageUtil msgUtil) {
        this.uploadUtil = uploadUtil;
        this.listingUploadDao = listingUploadDao;
        this.acbDao = acbDao;
        this.msgUtil = msgUtil;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.CertifiedProductDomainPermissions).LISTING_UPLOAD)")
    public List<ListingUpload> parseUploadFile(MultipartFile file) throws ValidationException {
        List<CSVRecord> allCsvRecords = getFileAsCsvRecords(file);
        List<ListingUpload> uploadedListings = new ArrayList<ListingUpload>();

        CSVRecord heading = uploadUtil.getHeadingRecord(allCsvRecords);
        if (heading == null) {
            LOGGER.warn("Cannot continue parsing upload file " + file.getName() + " without heading.");
            throw new ValidationException(msgUtil.getMessage("listingUpload.noHeadingFound"));
        }

        //are there any heading values we don't recognize? Give error for that
        //since that might mean we miss data that the user wants
        List<String> unrecognizedHeadings = getUnrecognizedHeadings(heading);
        if ((unrecognizedHeadings != null && unrecognizedHeadings.size() > 0)) {
            LOGGER.warn("User uploaded file with unrecognized headings: " + String.join(",", unrecognizedHeadings));
            throw new ValidationException(msgUtil.getMessage("listingUpload.unrecognizedHeadings",
                    String.join(",", unrecognizedHeadings)));
        }

        //check for duplicate chpl ids in the file
        Set<String> duplicateChplIds = getDuplicateChplIds(heading, allCsvRecords);
        if (duplicateChplIds != null && duplicateChplIds.size() > 0) {
            throw new ValidationException(msgUtil.getMessage("upload.duplicateUniqueIds",
                    String.join(",", duplicateChplIds)));
        }

        long currIndex = heading.getRecordNumber();
        while (currIndex < allCsvRecords.size()) {
            List<CSVRecord> nextListingCsvRecords = getNextListingRecords(allCsvRecords, currIndex);
            currIndex += nextListingCsvRecords.size();
            //CHPL ID and ACB are all that is needed for initial display and for security.
            //After this is inserted into the database, a quartz job will be kicked off
            //to populate the error and warning counts -
            ListingUpload listingUploadMetadata = new ListingUpload();
            listingUploadMetadata.getRecords().add(heading);
            listingUploadMetadata.getRecords().addAll(nextListingCsvRecords);
            listingUploadMetadata.setChplProductNumber(parseChplId(heading, nextListingCsvRecords));
            CertificationBodyDTO acb = acbDao.getByName(parseAcb(heading, nextListingCsvRecords));
            if (acb != null) {
                listingUploadMetadata.setAcb(new CertificationBody(acb));
            }
            uploadedListings.add(listingUploadMetadata);
        }
        return uploadedListings;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.CertifiedProductDomainPermissions).LISTING_UPLOAD, #uploadMetadata)")
    public void createOrReplaceListingUpload(ListingUpload uploadMetadata) throws ValidationException {
        //TODO: should we actually only allow unique CHPL IDs here?
        //It would keep functionality the same, but a file with new developer codes can have multiple
        //listings with the same pending CHPL ID and the latest one would overwrite the earlier one.
        ListingUpload existingListing =
                listingUploadDao.getByChplProductNumber(uploadMetadata.getChplProductNumber());
        if (existingListing != null) {
            listingUploadDao.delete(existingListing.getId());
        }
        listingUploadDao.create(uploadMetadata);
    }

    private String parseChplId(CSVRecord headingRecord, List<CSVRecord> listingRecords)
        throws ValidationException {
        String chplId = null;
        try {
            chplId = uploadUtil.parseRequiredSingleValueField(
                Headings.UNIQUE_ID, headingRecord, listingRecords);
        } catch (Exception ex) {
            throw new ValidationException("Could not parse CHPL Unique ID: " + ex.getMessage());
        }
        return chplId;
    }

    private String parseAcb(CSVRecord headingRecord, List<CSVRecord> listingRecords)
            throws ValidationException {
            String acbName = null;
            try {
                acbName = uploadUtil.parseRequiredSingleValueField(
                    Headings.CERTIFICATION_BODY_NAME, headingRecord, listingRecords);
            } catch (Exception ex) {
                throw new ValidationException("Could not parse Certification Body name: " + ex.getMessage());
            }
            return acbName;
        }

    private List<CSVRecord> getNextListingRecords(List<CSVRecord> allCsvRecords, long startIndex) {
        if (startIndex < 0 || startIndex >= allCsvRecords.size()) {
            LOGGER.error("Cannot look for listing CSV records starting at "
                    + startIndex + ". There are " + allCsvRecords.size() + " records.");
            return null;
        }
        CSVRecord heading = uploadUtil.getHeadingRecord(allCsvRecords);
        List<CSVRecord> listingCsvRecords = new ArrayList<CSVRecord>();
        Iterator<CSVRecord> remainingRecords = allCsvRecords.stream().skip(startIndex).iterator();
        while (remainingRecords.hasNext()) {
            CSVRecord record = remainingRecords.next();
            String recordUniqueId = uploadUtil.parseRequiredSingleValueField(
                    Headings.UNIQUE_ID, heading, record);
            String recordStatus = uploadUtil.parseRequiredSingleValueField(
                    Headings.RECORD_STATUS, heading, record);
            if (!StringUtils.isEmpty(recordUniqueId)) {
                if (recordStatus.equalsIgnoreCase("NEW") && listingCsvRecords.size() > 0) {
                    break;
                }
            }
            listingCsvRecords.add(record);
        }
        return listingCsvRecords;
    }

    private List<String> getUnrecognizedHeadings(CSVRecord record) {
        List<String> unrecognizedHeadings = new ArrayList<String>();
        Iterator<String> recordIter = record.iterator();
        while (recordIter.hasNext()) {
            String val = recordIter.next();
            if (Headings.getHeading(val) == null) {
                unrecognizedHeadings.add(val);
            }
        }
        return unrecognizedHeadings;
    }

    private Set<String> getDuplicateChplIds(CSVRecord heading, List<CSVRecord> allCsvRecords) {
        List<String> uploadedChplIdsExcludingNewDevelopers = new ArrayList<String>();
        long currIndex = heading.getRecordNumber();
        while (currIndex < allCsvRecords.size()) {
            List<CSVRecord> nextListingCsvRecords = getNextListingRecords(allCsvRecords, currIndex);
            currIndex += nextListingCsvRecords.size();
            String chplId = uploadUtil.parseSingleValueField(Headings.UNIQUE_ID, heading, nextListingCsvRecords);
            if (!StringUtils.isEmpty(chplId) && !chplId.contains(NEW_DEVELOPER_CODE)) {
                uploadedChplIdsExcludingNewDevelopers.add(chplId);
            }
        }

        Set<String> distinctUploadedIds = new LinkedHashSet<String>();
        Set<String> duplicates = new LinkedHashSet<String>();
        uploadedChplIdsExcludingNewDevelopers.stream()
            .forEach(uploadedChplId -> {
                if (!distinctUploadedIds.add(uploadedChplId)) {
                    duplicates.add(uploadedChplId);
                }
            });
        return duplicates;
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
