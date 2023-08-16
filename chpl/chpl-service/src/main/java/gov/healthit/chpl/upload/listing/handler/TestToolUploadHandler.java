package gov.healthit.chpl.upload.listing.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.criteriaattribute.testtool.CertificationResultTestTool;
import gov.healthit.chpl.criteriaattribute.testtool.TestTool;
import gov.healthit.chpl.upload.listing.Headings;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;

@Component("testToolUploadHandler")
public class TestToolUploadHandler {
    private ListingUploadHandlerUtil uploadUtil;

    @Autowired
    public TestToolUploadHandler(ListingUploadHandlerUtil uploadUtil) {
        this.uploadUtil = uploadUtil;
    }

    public List<CertificationResultTestTool> handle(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        List<CertificationResultTestTool> testTools = new ArrayList<CertificationResultTestTool>();
        List<String> testToolNames = parseTestToolNames(certHeadingRecord, certResultRecords);
        List<String> testToolVersions = parseTestToolVersions(certHeadingRecord, certResultRecords);
        if (uploadUtil.areCollectionsEmpty(testToolNames, testToolVersions)) {
            return testTools;
        }

        int max = 0;
        if (CollectionUtils.isNotEmpty(testToolNames)) {
            max = Math.max(max, testToolNames.size());
        }
        if (CollectionUtils.isNotEmpty(testToolVersions)) {
            max = Math.max(max, testToolVersions.size());
        }
        //I think everything remains ordered using these data structures so this should be okay.
        testTools = IntStream.range(0, max)
            .mapToObj(index -> buildTestTool(index, testToolNames, testToolVersions))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        return testTools;
    }

    private CertificationResultTestTool buildTestTool(int index, List<String> testToolNames, List<String> testToolVersions) {
        String ttName = (testToolNames != null && testToolNames.size() > index) ? testToolNames.get(index) : null;
        String ttVersion = (testToolVersions != null && testToolVersions.size() > index)
                ? testToolVersions.get(index) : null;

        if (StringUtils.isAllEmpty(ttName, ttVersion)) {
            return null;
        }

        return CertificationResultTestTool.builder()
            .testTool(TestTool.builder()
                    .value(ttName)
                    .build())
            .version(ttVersion)
        .build();
    }

    private List<String> parseTestToolNames(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.TEST_TOOL_NAME, certHeadingRecord, certResultRecords);
        return values;
    }

    private List<String> parseTestToolVersions(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.TEST_TOOL_VERSION, certHeadingRecord, certResultRecords);
        return values;
    }
}
