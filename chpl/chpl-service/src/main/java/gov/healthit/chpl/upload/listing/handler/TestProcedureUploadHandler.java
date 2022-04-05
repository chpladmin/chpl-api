package gov.healthit.chpl.upload.listing.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import gov.healthit.chpl.domain.TestProcedure;
import gov.healthit.chpl.upload.listing.Headings;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;

@Component("testProcedureUploadHandler")
public class TestProcedureUploadHandler {
    private ListingUploadHandlerUtil uploadUtil;
    private FF4j ff4j;

    @Autowired
    public TestProcedureUploadHandler(ListingUploadHandlerUtil uploadUtil, FF4j ff4j) {
        this.uploadUtil = uploadUtil;
        this.ff4j = ff4j;
    }

    public List<CertificationResultTestProcedure> handle(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        if (ff4j.check(FeatureList.CONFORMANCE_METHOD)) {
            return new ArrayList<CertificationResultTestProcedure>();
        }

        List<CertificationResultTestProcedure> testProcedures = new ArrayList<CertificationResultTestProcedure>();
        List<String> testProcedureNames = parseTestProceduredNames(certHeadingRecord, certResultRecords);
        List<String> testProcedureVersions = parseTestProcedureVersions(certHeadingRecord, certResultRecords);
        if (uploadUtil.areCollectionsEmpty(testProcedureNames, testProcedureVersions)) {
            return testProcedures;
        }

        int max = 0;
        if (CollectionUtils.isNotEmpty(testProcedureNames)) {
            max = Math.max(max, testProcedureNames.size());
        }
        if (CollectionUtils.isNotEmpty(testProcedureVersions)) {
            max = Math.max(max, testProcedureVersions.size());
        }
        //I think everything remains ordered using these data structures so this should be okay.
        testProcedures = IntStream.range(0, max)
            .mapToObj(index -> buildTestProcedure(index, testProcedureNames, testProcedureVersions))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        return testProcedures;
    }

    private CertificationResultTestProcedure buildTestProcedure(int index, List<String> testProcedureNames,
            List<String> testProcedureVersions) {
        String tpName = (testProcedureNames != null && testProcedureNames.size() > index) ? testProcedureNames.get(index) : null;
        String tpVersion = (testProcedureVersions != null && testProcedureVersions.size() > index)
                ? testProcedureVersions.get(index) : null;

        if (StringUtils.isAllEmpty(tpName, tpVersion)) {
            return null;
        }

        return CertificationResultTestProcedure.builder()
            .testProcedure(tpName == null ? null : TestProcedure.builder().name(tpName).build())
            .testProcedureVersion(tpVersion)
            .build();
    }

    private List<String> parseTestProceduredNames(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.CONFORMANCE_METHOD, certHeadingRecord, certResultRecords);
        return values;
    }

    private List<String> parseTestProcedureVersions(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.CONFORMANCE_METHOD_VERSION, certHeadingRecord, certResultRecords);
        return values;
    }
}
