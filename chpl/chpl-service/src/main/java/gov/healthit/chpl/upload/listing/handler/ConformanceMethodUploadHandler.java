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

import gov.healthit.chpl.conformanceMethod.domain.CertificationResultConformanceMethod;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethod;
import gov.healthit.chpl.upload.listing.Headings;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;

@Component("conformanceMethodUploadHandler")
public class ConformanceMethodUploadHandler {
    private ListingUploadHandlerUtil uploadUtil;

    @Autowired
    public ConformanceMethodUploadHandler(ListingUploadHandlerUtil uploadUtil) {
        this.uploadUtil = uploadUtil;
    }

    public List<CertificationResultConformanceMethod> handle(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        List<CertificationResultConformanceMethod> conformanceMethods = new ArrayList<CertificationResultConformanceMethod>();
        List<String> conformanceMethodNames = parseConformanceMethodNames(certHeadingRecord, certResultRecords);
        List<String> conformanceMethodVersions = parseConformanceMethodVersions(certHeadingRecord, certResultRecords);
        if (uploadUtil.areCollectionsEmpty(conformanceMethodNames, conformanceMethodVersions)) {
            return conformanceMethods;
        }

        int max = 0;
        if (CollectionUtils.isNotEmpty(conformanceMethodNames)) {
            max = Math.max(max, conformanceMethodNames.size());
        }
        if (CollectionUtils.isNotEmpty(conformanceMethodVersions)) {
            max = Math.max(max, conformanceMethodVersions.size());
        }
        //I think everything remains ordered using these data structures so this should be okay.
        conformanceMethods = IntStream.range(0, max)
            .mapToObj(index -> buildConformanceMethod(index, conformanceMethodNames, conformanceMethodVersions))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        return conformanceMethods;
    }

    private CertificationResultConformanceMethod buildConformanceMethod(int index, List<String> conformanceMethodNames,
            List<String> conformanceMethodVersions) {
        String cmName = (conformanceMethodNames != null && conformanceMethodNames.size() > index) ? conformanceMethodNames.get(index) : null;
        String cmVersion = (conformanceMethodVersions != null && conformanceMethodVersions.size() > index)
                ? conformanceMethodVersions.get(index) : null;

        if (StringUtils.isAllEmpty(cmName, cmVersion)) {
            return null;
        }

        return CertificationResultConformanceMethod.builder()
                .conformanceMethod(cmName == null ? null : ConformanceMethod.builder().name(cmName).build())
                .conformanceMethodVersion(cmVersion)
                .build();
    }

    private List<String> parseConformanceMethodNames(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.CONFORMANCE_METHOD, certHeadingRecord, certResultRecords);
        return values;
    }

    private List<String> parseConformanceMethodVersions(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.CONFORMANCE_METHOD_VERSION, certHeadingRecord, certResultRecords);
        return values;
    }
}
