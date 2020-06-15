package upload.listing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ListingUploadManager {

    private ListingUploadHandler listingUploadHandler;
    private ListingUploadDao listingUploadDao;
    private ErrorMessageUtil msgUtil;

    public ListingUploadManager(ListingUploadHandler listingUploadHandler,
            ListingUploadDao listingUploadDao, ErrorMessageUtil msgUtil) {
        this.listingUploadHandler = listingUploadHandler;
        this.listingUploadDao = listingUploadDao;
        this.msgUtil = msgUtil;
    }

    //TODO: Security - admin, onc, or ANY acb can do this
    public List<ListingUpload> parseUploadFile(MultipartFile file) throws ValidationException {
        List<CSVRecord> allCsvRecords = getFileAsCsvRecords(file);
        List<ListingUpload> uploadMetadatas = new ArrayList<ListingUpload>();

        int headingIndex = listingUploadHandler.getHeadingRecordIndex(allCsvRecords);
        if (headingIndex < 0) {
            LOGGER.warn("Cannot continue parsing upload file " + file.getName() + " without heading.");
            //TODO: move to errors.properties
            throw new ValidationException("No records with allowed heading values were found in the file.");
        }

        int currIndex = headingIndex+1;
        while (currIndex < allCsvRecords.size()) {
            List<CSVRecord> singleListingCsvRecords = getNextListingRecords(allCsvRecords, currIndex);
            currIndex += singleListingCsvRecords.size();
            ListingUpload uploadMetadata = null;
            //TODO: parse the csv record list to fill in metadata we need for the upload object
            uploadMetadatas.add(uploadMetadata);
        }
        return uploadMetadatas;
    }

    //TODO security - admin, onc, or OWNING acb can do this
    @Transactional
    public void createListingUpload(ListingUpload uploadMetadata, String fileContents) throws ValidationException {
        listingUploadDao.create(uploadMetadata, fileContents);
    }

    private List<CSVRecord> getNextListingRecords(List<CSVRecord> allCsvRecords, int startIndex) {
        if (startIndex < 0 || startIndex >= allCsvRecords.size()) {
            LOGGER.error("Cannot look for listing CSV records starting at "
                    + startIndex + ". There are " + allCsvRecords.size() + " records.");
            return null;
        }
        CSVRecord heading = listingUploadHandler.getHeadingRecord(allCsvRecords);
        List<CSVRecord> listingCsvRecords = new ArrayList<CSVRecord>();
        Iterator<CSVRecord> remainingRecords = allCsvRecords.stream().skip(startIndex).iterator();
        while (remainingRecords.hasNext()) {
            CSVRecord record = remainingRecords.next();
            //TODO: call handler getUniqueId, getStatus methods
            String recordUniqueId = listingUploadHandler.parseChplProductNumber(heading, record);
            String recordStatus = listingUploadHandler.parseStatus(heading, record);
            if (!StringUtils.isEmpty(recordUniqueId)) {
                if (recordStatus.equalsIgnoreCase("NEW") && listingCsvRecords.size() > 0) {
                    break;
                }
            }
            listingCsvRecords.add(record);
        }
        return listingCsvRecords;
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
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"));
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
