package gov.healthit.chpl.upload.listing.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResultTestData;
import gov.healthit.chpl.domain.TestData;
import gov.healthit.chpl.upload.listing.Headings;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;

@Component("testDataUploadHandler")
public class TestDataUploadHandler {
    private ListingUploadHandlerUtil uploadUtil;

    @Autowired
    public TestDataUploadHandler(ListingUploadHandlerUtil uploadUtil) {
        this.uploadUtil = uploadUtil;
    }

    public List<CertificationResultTestData> handle(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        List<CertificationResultTestData> testData = new ArrayList<CertificationResultTestData>();
        List<String> testDataNames = parseTestDataNames(certHeadingRecord, certResultRecords);
        List<String> testDataVersions = parseTestDataVersions(certHeadingRecord, certResultRecords);
        List<String> testDataAlterations = parseTestDataAlterations(certHeadingRecord, certResultRecords);
        if (CollectionUtils.isEmpty(testDataNames)
                && CollectionUtils.isEmpty(testDataVersions)
                && CollectionUtils.isEmpty(testDataAlterations)) {
            return testData;
        }

        int max = 0;
        if (CollectionUtils.isNotEmpty(testDataNames)) {
            max = Math.max(max, testDataNames.size());
        }
        if (CollectionUtils.isNotEmpty(testDataVersions)) {
            max = Math.max(max, testDataVersions.size());
        }
        if (CollectionUtils.isNotEmpty(testDataAlterations)) {
            max = Math.max(max, testDataAlterations.size());
        }
        //I think everything remains ordered using these data structures so this should be okay.
        testData = IntStream.range(0, max)
            .mapToObj(index -> buildTestTool(index, testDataNames, testDataVersions, testDataAlterations))
            .collect(Collectors.toList());
        return testData;
    }

    private CertificationResultTestData buildTestTool(int index, List<String> testDataNames,
            List<String> testDataVersions, List<String> testDataAlterations) {
        String tdName = (testDataNames != null && testDataNames.size() > index) ? testDataNames.get(index) : null;
        String tdVersion = (testDataVersions != null && testDataVersions.size() > index)
                ? testDataVersions.get(index) : null;
        String tdAlteration = (testDataAlterations != null && testDataAlterations.size() > index)
                ? testDataAlterations.get(index) : null;

        CertificationResultTestData testData = CertificationResultTestData.builder()
                .testData(tdName == null ? null : TestData.builder().name(tdName).build())
                .version(tdVersion)
                .alteration(tdAlteration)
            .build();
        return testData;
    }

    private List<String> parseTestDataNames(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.TEST_DATA, certHeadingRecord, certResultRecords);
        return values;
    }

    private List<String> parseTestDataVersions(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.TEST_DATA_VERSION, certHeadingRecord, certResultRecords);
        return values;
    }

    private List<String> parseTestDataAlterations(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.TEST_DATA_ALTERATION_DESC, certHeadingRecord, certResultRecords);
        return values;
    }
}
