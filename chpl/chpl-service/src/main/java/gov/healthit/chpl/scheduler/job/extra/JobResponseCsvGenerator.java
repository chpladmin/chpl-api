package gov.healthit.chpl.scheduler.job.extra;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class JobResponseCsvGenerator {

    public File getCsvFile(final List<JobResponse> responses, final String fileName)
            throws IOException {
        return writeDataToFile(responses, createFile(fileName));
    }

    private File createFile(final String fileName) throws IOException {
        File temp = File.createTempFile(fileName + " - ", ".csv");
        temp.deleteOnExit();
        return temp;
    }

    private File writeDataToFile(final List<JobResponse> responses, final File file) throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file),
                Charset.forName("UTF-8").newEncoder());
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL)) {

            csvPrinter.printRecord(getHeaderRow());
            for (JobResponse response : responses) {
                csvPrinter.printRecord(getDataRow(response));
            }

            return file;
        }

    }

    private List<String> getHeaderRow() {
        return new ArrayList<String>(Arrays.asList("Identifier", "Success", "Message"));
    }

    private List<String> getDataRow(JobResponse response) {
        return new ArrayList<String>(Arrays.asList(response.getIdentifier(),
                String.valueOf(response.isCompletedSuccessfully()), response.getMessage()));
    }

}
