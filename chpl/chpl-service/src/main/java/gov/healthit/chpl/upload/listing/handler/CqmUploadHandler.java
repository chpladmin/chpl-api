package gov.healthit.chpl.upload.listing.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.upload.listing.Headings;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;

@Component("cqmUploadHandler")
public class CqmUploadHandler {
    private ListingUploadHandlerUtil uploadUtil;

    @Autowired
    public CqmUploadHandler(ListingUploadHandlerUtil uploadUtil) {
        this.uploadUtil = uploadUtil;
    }

    public List<CQMResultDetails> handle(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<CQMResultDetails> cqms = new ArrayList<CQMResultDetails>();
        List<String> cqmNumbers = parseCqmNumbers(headingRecord, listingRecords);
        List<String> cqmVersions = parseCqmVersions(headingRecord, listingRecords);
        List<String> cqmCriteria = parseCqmCriteria(headingRecord, listingRecords);
        if (CollectionUtils.isEmpty(cqmNumbers)
                && CollectionUtils.isEmpty(cqmVersions)
                && CollectionUtils.isEmpty(cqmCriteria)) {
            return cqms;
        }

        int max = 0;
        if (CollectionUtils.isNotEmpty(cqmNumbers)) {
            max = Math.max(max, cqmNumbers.size());
        }
        if (CollectionUtils.isNotEmpty(cqmVersions)) {
            max = Math.max(max, cqmVersions.size());
        }
        if (CollectionUtils.isNotEmpty(cqmCriteria)) {
            max = Math.max(max, cqmCriteria.size());
        }

        cqms = IntStream.range(0, max)
                .mapToObj(index -> buildCqmDetails(index, cqmNumbers, cqmVersions, cqmCriteria))
                .collect(Collectors.toList());
        return cqms;
    }

    private CQMResultDetails buildCqmDetails(int index, List<String> cqmNumbers, List<String> cqmVersions,
            List<String> cqmCriteria) {
        String cqmNumber = (cqmNumbers != null && cqmNumbers.size() > index) ? cqmNumbers.get(index) : null;
        String cqmVersionDelimited = (cqmVersions != null && cqmVersions.size() > index)
                ? cqmVersions.get(index) : null;
        String cqmCriteriaDelimited = (cqmCriteria != null && cqmCriteria.size() > index)
                ? cqmCriteria.get(index) : null;

        Set<String> versions = new HashSet<String>();
        if (!StringUtils.isEmpty(cqmVersionDelimited) && !"0".equals(cqmVersionDelimited)) {
            String[] splitVersions = cqmVersionDelimited.split(";");
            if (splitVersions.length == 1) {
                splitVersions = cqmVersionDelimited.split(",");
            }
            List<String> splitTrimmedVersions = Arrays.stream(splitVersions)
                    .map(String::trim)
                    .collect(Collectors.toList());
            versions.addAll(splitTrimmedVersions);
        }

        List<CQMResultCertification> criteria = new ArrayList<CQMResultCertification>();
        if (!StringUtils.isEmpty(cqmCriteriaDelimited)
                && !"0".equals(cqmCriteriaDelimited)) {
            String[] splitCriteria = cqmCriteriaDelimited.split(";");
            if (splitCriteria.length == 1) {
                splitCriteria = cqmCriteriaDelimited.split(",");
            }
            Arrays.stream(splitCriteria)
                .forEach(currCriteria -> {
                    CQMResultCertification currCqmCriteria = CQMResultCertification.builder()
                            .certificationNumber(currCriteria.trim())
                            .build();
                    criteria.add(currCqmCriteria);
                });
        }

        CQMResultDetails cqm = CQMResultDetails.builder()
                .success(Boolean.TRUE)
                .number(cqmNumber)
                .successVersions(versions)
                .criteria(criteria)
        .build();
        return cqm;
    }


    private List<String> parseCqmNumbers(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.CQM_NUMBER, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseCqmVersions(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.CQM_VERSION, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseCqmCriteria(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.CQM_CRITERIA, headingRecord, listingRecords);
        return values;
    }
}
