package gov.healthit.chpl.upload.listing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ListingUploadManager {
    public static final String NEW_DEVELOPER_CODE = "XXXX";

    private ListingUploadHandlerUtil uploadUtil;
    private ChplProductNumberUtil chplProductNumberUtil;
    private ListingUploadDao listingUploadDao;
    private CertificationBodyDAO acbDao;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public ListingUploadManager(ListingUploadHandlerUtil uploadUtil, ChplProductNumberUtil chplProductNumberUtil,
            ListingUploadDao listingUploadDao, CertificationBodyDAO acbDao, ErrorMessageUtil msgUtil) {
        this.uploadUtil = uploadUtil;
        this.chplProductNumberUtil = chplProductNumberUtil;
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

        int headingRowIndex = uploadUtil.getHeadingRecordIndex(allCsvRecords);
        CSVRecord headingRecord = uploadUtil.getHeadingRecord(allCsvRecords);
        List<CSVRecord> allListingRecords = allCsvRecords.subList(headingRowIndex + 1, allCsvRecords.size());
        if (headingRecord == null) {
            LOGGER.warn("Cannot continue parsing upload file " + file.getName() + " without heading.");
            throw new ValidationException(msgUtil.getMessage("listing.upload.noHeadingFound"));
        }
        checkRequiredHeadings(headingRecord);

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

    private void checkRequiredHeadings(CSVRecord headingRecord) throws ValidationException {
        List<String> missingRequiredHeadings = Headings.getRequiredHeadings().stream()
            .filter(heading -> !uploadUtil.hasHeading(heading, headingRecord))
            .map(headingVal -> headingVal.getNamesAsString())
            .collect(Collectors.toList());
        if (missingRequiredHeadings != null && missingRequiredHeadings.size() > 0) {
            throw new ValidationException(msgUtil.getMessage("listing.upload.missingRequiredHeadings",
                    String.join(";", missingRequiredHeadings)));
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
        //After this is inserted into the database, a quartz job will be kicked off
        //to populate the error and warning counts -
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
            LOGGER.error("Could not parse ACB.", ex);
        }
        listingUploadMetadata.setAcb(acb);
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
