package gov.healthit.chpl.certifiedproduct.csv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.FileUtils;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ListingCsvWriter {

    private ListingCsvHeadingWriter headingWriter;
    private ListingCsvDataWriter dataWriter;
    private FileUtils fileUtils;

    @Autowired
    public ListingCsvWriter(ListingCsvHeadingWriter headingWriter, ListingCsvDataWriter dataWriter,
            FileUtils fileUtils) {
        this.headingWriter = headingWriter;
        this.dataWriter = dataWriter;
        this.fileUtils = fileUtils;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.CertifiedProductDomainPermissions).CONVERT_TO_CSV, #listing)")
    public File getAsCsv(CertifiedProductSearchDetails listing) throws IOException {
        File csvFile = fileUtils.createTempFile("listing-" + listing.getId() + "-details", ".csv");
        OutputStreamWriter osWriter = new OutputStreamWriter(new FileOutputStream(csvFile), StandardCharsets.UTF_8);
        osWriter.write('\ufeff');
        CSVPrinter csvPrinter = new CSVPrinter(osWriter, CSVFormat.EXCEL);
        csvPrinter.flush();

        List<String> headings = headingWriter.getCsvHeadings(listing);
        csvPrinter.printRecord(headings);
        csvPrinter.flush();
        List<List<String>> allData = dataWriter.getCsvData(listing, headings.size());
        csvPrinter.printRecords(allData);
        csvPrinter.flush();

        if (csvPrinter != null) {
            csvPrinter.close();
        }
        if (osWriter != null) {
            osWriter.close();
        }
        return csvFile;
    }
}
