package gov.healthit.chpl.upload.listing;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public final class ListingUploadTestUtil {
    private ListingUploadTestUtil() {}

    public static List<CSVRecord> getRecordsFromString(String str) {
        List<CSVRecord> records = null;
        try {
            StringReader in = new StringReader(str);
            CSVParser csvParser = CSVFormat.EXCEL.parse(in);
            records = csvParser.getRecords();
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
        return records;
    }
}
