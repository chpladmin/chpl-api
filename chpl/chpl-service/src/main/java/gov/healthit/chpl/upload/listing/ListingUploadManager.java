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
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ListingUploadManager {
    public static final String NEW_DEVELOPER_CODE = "XXXX";

    private ListingDetailsUploadHandler uploadHandler;
    private ListingUploadHandlerUtil uploadUtil;
    private ListingUploadDao listingUploadDao;
    private CertificationBodyDAO acbDao;
    private ErrorMessageUtil msgUtil;

    public ListingUploadManager(ListingDetailsUploadHandler uploadHandler, ListingUploadHandlerUtil uploadUtil,
            ListingUploadDao listingUploadDao, CertificationBodyDAO acbDao, ErrorMessageUtil msgUtil) {
        this.uploadHandler = uploadHandler;
        this.uploadUtil = uploadUtil;
        this.listingUploadDao = listingUploadDao;
        this.acbDao = acbDao;
        this.msgUtil = msgUtil;
    }

    //TODO: Security - admin, onc, or ANY acb can do this
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

        long currIndex = heading.getRecordNumber() + 1;
        while (currIndex < allCsvRecords.size()) {
            List<CSVRecord> nextListingCsvRecords = getNextListingRecords(allCsvRecords, currIndex);
            currIndex += nextListingCsvRecords.size();
            CertifiedProductSearchDetails parsedListing = uploadHandler.parseAsListing(heading, nextListingCsvRecords);
            ListingUpload listing = new ListingUpload();
            listing.setChplProductNumber(parsedListing.getChplProductNumber());
            //TODO: parse the csv record list to fill in metadata we need for the upload object
            //acb
            //error count
            //warning count
            //for above we need the entire listing to be parsed and run through the validator
            uploadedListings.add(listing);
        }

        //check for duplicate chpl ids in the file and throw ValidationException
        Set<ListingUpload> duplicateListings = getDuplicateListings(uploadedListings);
        if (duplicateListings != null && duplicateListings.size() > 0) {
            throw new ValidationException(msgUtil.getMessage("upload.duplicateUniqueIds",
                    String.join(",", duplicateListings.stream()
                            .map(ListingUpload::getChplProductNumber)
                            .collect(Collectors.toList()))));
        }
        return uploadedListings;
    }

    //TODO security - admin, onc, or OWNING acb can do this
    @Transactional
    public void createListingUpload(ListingUpload uploadMetadata, String fileContents) throws ValidationException {
        listingUploadDao.create(uploadMetadata, fileContents);
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
            String recordUniqueId = uploadUtil.parseSingleValueField(
                    Headings.UNIQUE_ID, heading, record);
            String recordStatus = uploadUtil.parseSingleValueField(
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

    private Set<ListingUpload> getDuplicateListings(List<ListingUpload> uploadedListings) {
        List<ListingUpload> uploadedListingsExcludingNewDevelopers =
                uploadedListings.stream()
                    .filter(uploadMetadata -> !uploadMetadata.getChplProductNumber().contains(NEW_DEVELOPER_CODE))
                    .collect(Collectors.toList());

        Set<ListingUpload> distinctUploadedListings = new LinkedHashSet<ListingUpload>();
        Set<ListingUpload> duplicates = new LinkedHashSet<ListingUpload>();
        uploadedListingsExcludingNewDevelopers.stream()
            .forEach(uploadedListing -> {
                if (!distinctUploadedListings.add(uploadedListing)) {
                    duplicates.add(uploadedListing);
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
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8.name()));
                CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL)) {
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
