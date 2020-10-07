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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingType;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingUpload;

public class RealWorldTestingManager {
    private static final String PLANS = "PLANS";
    private static final String RESULTS = "RESULTS";

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).REAL_WORLD_TESTING, "
            + "T(gov.healthit.chpl.permissions.domains.RealWorldTestingDomainPermissions).UPLOAD)")
    public void uploadPendingSurveillance(MultipartFile file) throws ValidationException {
        checkBasicFileProperties(file);
        List<RealWorldTestingUpload> rwts = parseCsvFile(file);
    }

    private List<RealWorldTestingUpload> parseCsvFile(MultipartFile file) throws ValidationException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
                CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL)) {

            List<CSVRecord> records = parser.getRecords();
            if (records.size() <= 1) {
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
        rwtUpload.setChplProductNumber(record.get(0).toString());
        rwtUpload.setType(getType(record.get(0).toString()));
        rwtUpload.setLastChecked(getLastCheckedDate(record.get(0).toString()));
        rwtUpload.setUrl(record.get(0).toString());
        return rwtUpload;
    }

    private LocalDate getLastCheckedDate(String unformatted) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYYMMdd");
        return LocalDate.parse(unformatted, formatter);
    }

    private RealWorldTestingType getType(String rwtType) {
        if (rwtType.toUpperCase().equals(PLANS)) {
            return RealWorldTestingType.PLANS;
        } else if (rwtType.toUpperCase().equals(RESULTS)) {
            return RealWorldTestingType.RESULTS;
        } else {
            //TODO: Determine what to do here
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
        //TODO: Figure this out
        return true;
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
