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

import gov.healthit.chpl.domain.CertifiedProductUcdProcess;
import gov.healthit.chpl.upload.listing.Headings;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;

@Component("ucdProcessUploadHandler")
public class UcdProcessUploadHandler {
    private ListingUploadHandlerUtil uploadUtil;

    @Autowired
    public UcdProcessUploadHandler(ListingUploadHandlerUtil uploadUtil) {
        this.uploadUtil = uploadUtil;
    }

    public List<CertifiedProductUcdProcess> handle(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        List<CertifiedProductUcdProcess> ucdProcesses = new ArrayList<CertifiedProductUcdProcess>();
        List<String> ucdNames = parseUcdNames(certHeadingRecord, certResultRecords);
        List<String> ucdDetails = parseUcdDetails(certHeadingRecord, certResultRecords);
        if (uploadUtil.areCollectionsEmpty(ucdNames, ucdDetails)) {
            return ucdProcesses;
        }

        int max = 0;
        if (CollectionUtils.isNotEmpty(ucdNames)) {
            max = Math.max(max, ucdNames.size());
        }
        if (CollectionUtils.isNotEmpty(ucdDetails)) {
            max = Math.max(max, ucdDetails.size());
        }
        //I think everything remains ordered using these data structures so this should be okay.
        ucdProcesses = IntStream.range(0, max)
            .mapToObj(index -> buildUcdProcess(index, ucdNames, ucdDetails))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        return ucdProcesses;
    }

    private CertifiedProductUcdProcess buildUcdProcess(int index, List<String> ucdNames, List<String> ucdDetails) {
        String ucdName = (ucdNames != null && ucdNames.size() > index) ? ucdNames.get(index) : null;
        String ucdDetail = (ucdDetails != null && ucdDetails.size() > index)
                ? ucdDetails.get(index) : null;

        if (StringUtils.isAllEmpty(ucdName, ucdDetail)) {
            return null;
        }

        return CertifiedProductUcdProcess.builder()
                .name(ucdName)
                .details(ucdDetail)
        .build();
    }

    private List<String> parseUcdNames(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.UCD_PROCESS, certHeadingRecord, certResultRecords);
        return values;
    }

    private List<String> parseUcdDetails(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.UCD_PROCESS_DETAILS, certHeadingRecord, certResultRecords);
        return values;
    }
}
