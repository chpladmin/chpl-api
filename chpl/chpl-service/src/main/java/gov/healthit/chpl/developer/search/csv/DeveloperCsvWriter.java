package gov.healthit.chpl.developer.search.csv;

import static gov.healthit.chpl.util.LambdaExceptionUtil.rethrowConsumer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.developer.search.DeveloperSearchRequest;
import gov.healthit.chpl.developer.search.DeveloperSearchResult;
import gov.healthit.chpl.developer.search.DeveloperSearchService;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.util.FileUtils;

@Component
public class DeveloperCsvWriter {
    private OutputStreamWriter osWriter = null;
    private CSVPrinter csvPrinter = null;
    private DeveloperCsvHeadingService csvHeadingService;
    private DeveloperCsvRecordService csvRecordService;
    private DeveloperSearchService devSearchService;
    private FileUtils fileUtils;
    private  ResourcePermissionsFactory resourcePermissionsFactory;

    @Autowired
    public DeveloperCsvWriter(DeveloperCsvHeadingService csvHeadingService,
            DeveloperCsvRecordService csvRecordService,
            DeveloperSearchService devSearchService,
            FileUtils fileUtils,
            ResourcePermissionsFactory resourcePermissionsFactory) {
        this.csvHeadingService = csvHeadingService;
        this.csvRecordService = csvRecordService;
        this.devSearchService = devSearchService;
        this.fileUtils = fileUtils;
        this.resourcePermissionsFactory = resourcePermissionsFactory;
    }

    public File getAsCsv(DeveloperSearchRequest searchRequest, Logger logger) throws IOException {
        List<DeveloperSearchResult> allSearchResults = devSearchService.getAllPagesOfSearchResults(searchRequest, logger);
        File csvFile = fileUtils.createTempFile("developer-search-results", ".csv");
        openDataFile(csvFile);
        csvPrinter.printRecord(getHeadingRecord());
        csvPrinter.flush();
        if (!CollectionUtils.isEmpty(allSearchResults)) {
            allSearchResults.stream()
                .forEach(rethrowConsumer(searchResult -> csvPrinter.printRecord(getDeveloperRecord(searchResult))));
        }
        csvPrinter.flush();
        close();
        return csvFile;
    }

    private List<String> getHeadingRecord() {
        if (isAuthorizedToSeeUserData()) {
            return csvHeadingService.getCsvHeadingsWithUsers();
        }
        return csvHeadingService.getCsvHeadings();
    }

    private List<String> getDeveloperRecord(DeveloperSearchResult dev) {
        if (isAuthorizedToSeeUserData()) {
            return csvRecordService.getRecordWithUsers(dev);
        }
        return csvRecordService.getRecord(dev);
    }

    private boolean isAuthorizedToSeeUserData() {
        return resourcePermissionsFactory.get().isUserRoleAdmin()
                || resourcePermissionsFactory.get().isUserRoleOnc()
                || resourcePermissionsFactory.get().isUserRoleAcbAdmin();
    }

    private void openDataFile(File csvFile) throws IOException {
        osWriter = new OutputStreamWriter(new FileOutputStream(csvFile), StandardCharsets.UTF_8);
        osWriter.write('\ufeff');
        csvPrinter = new CSVPrinter(osWriter, CSVFormat.EXCEL);
        csvPrinter.flush();
    }

    private void close() throws IOException {
        if (csvPrinter != null) {
            csvPrinter.close();
        }
        if (osWriter != null) {
            osWriter.close();
        }
    }
}
