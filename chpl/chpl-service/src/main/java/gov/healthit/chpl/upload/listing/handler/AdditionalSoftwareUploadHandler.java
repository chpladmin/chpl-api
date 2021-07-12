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

import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.upload.listing.Headings;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;

@Component("additionalSoftwareUploadHandler")
public class AdditionalSoftwareUploadHandler {
    private ListingUploadHandlerUtil uploadUtil;

    @Autowired
    public AdditionalSoftwareUploadHandler(ListingUploadHandlerUtil uploadUtil) {
        this.uploadUtil = uploadUtil;
    }

    public List<CertificationResultAdditionalSoftware> handle(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        List<CertificationResultAdditionalSoftware> additionalSoftware = new ArrayList<CertificationResultAdditionalSoftware>();
        String hasAdditionalSoftwareString = parseHasAdditionalSoftwareStr(certHeadingRecord, certResultRecords);
        Boolean hasAdditionalSoftware = parseHasAdditionalSoftware(certHeadingRecord, certResultRecords);
        List<String> listingSources = parseListingSources(certHeadingRecord, certResultRecords);
        List<String> listingGroupings = parseListingGroupings(certHeadingRecord, certResultRecords);
        List<String> nonlistingSources = parseNonListingSources(certHeadingRecord, certResultRecords);
        List<String> nonlistingVersions = parseNonListingVersions(certHeadingRecord, certResultRecords);
        List<String> nonlistingGroupings = parseNonListingGroupings(certHeadingRecord, certResultRecords);

        if (StringUtils.isEmpty(hasAdditionalSoftwareString)
                && uploadUtil.areCollectionsEmpty(listingSources, listingGroupings, nonlistingSources,
                nonlistingVersions, nonlistingGroupings)) {
            return additionalSoftware;
        }

        int max = 0;
        if (CollectionUtils.isNotEmpty(listingSources)) {
            max = Math.max(max, listingSources.size());
        }
        if (CollectionUtils.isNotEmpty(listingGroupings)) {
            max = Math.max(max, listingGroupings.size());
        }
        if (CollectionUtils.isNotEmpty(nonlistingSources)) {
            max = Math.max(max, nonlistingSources.size());
        }
        if (CollectionUtils.isNotEmpty(nonlistingVersions)) {
            max = Math.max(max, nonlistingVersions.size());
        }
        if (CollectionUtils.isNotEmpty(nonlistingGroupings)) {
            max = Math.max(max, nonlistingGroupings.size());
        }

        additionalSoftware = IntStream.range(0, max)
                .mapToObj(index -> buildAdditionalSoftware(index, listingSources, listingGroupings, nonlistingSources,
                        nonlistingVersions, nonlistingGroupings))
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        return additionalSoftware;
    }


    private List<CertificationResultAdditionalSoftware> buildAdditionalSoftware(int index, List<String> listingSources,
            List<String> listingGroupings, List<String> nonlistingSources, List<String> nonlistingVersions,
            List<String> nonlistingGroupings) {
        String listingSource = (listingSources != null && listingSources.size() > index) ? listingSources.get(index) : null;
        String listingGrouping = (listingGroupings != null && listingGroupings.size() > index)
                ? listingGroupings.get(index) : null;
        String nonlistingSource = (nonlistingSources != null && nonlistingSources.size() > index)
                ? nonlistingSources.get(index) : null;
        String nonlistingVersion = (nonlistingVersions != null && nonlistingVersions.size() > index)
                ? nonlistingVersions.get(index) : null;
        String nonlistingGrouping = (nonlistingGroupings != null && nonlistingGroupings.size() > index)
                ? nonlistingGroupings.get(index) : null;

        if (StringUtils.isAllEmpty(listingSource, listingGrouping, nonlistingSource,
                nonlistingVersion, nonlistingGrouping)) {
            return null;
        }

        List<CertificationResultAdditionalSoftware> parsedAdditionalSoftware
            = new ArrayList<CertificationResultAdditionalSoftware>();
        if (!StringUtils.isEmpty(listingSource) || !StringUtils.isEmpty(listingGrouping)) {
                parsedAdditionalSoftware.add(CertificationResultAdditionalSoftware.builder()
                .certifiedProductNumber(listingSource)
                .name(null)
                .version(null)
                .grouping(listingGrouping)
            .build());
        }
        if (!StringUtils.isEmpty(nonlistingSource) || !StringUtils.isEmpty(nonlistingVersion)
                || !StringUtils.isEmpty(nonlistingGrouping)) {
            parsedAdditionalSoftware.add(CertificationResultAdditionalSoftware.builder()
                    .certifiedProductNumber(null)
                    .name(nonlistingSource)
                    .version(nonlistingVersion)
                    .grouping(nonlistingGrouping)
                .build());
        }
        return parsedAdditionalSoftware;
    }

    private Boolean parseHasAdditionalSoftware(CSVRecord certResultHeading, List<CSVRecord> certResultRecords) {
        Boolean result = null;
        try {
            result = uploadUtil.parseSingleRowFieldAsBoolean(Headings.HAS_ADDITIONAL_SOFTWARE, certResultHeading, certResultRecords);
        } catch (Exception e) {
        }
        return result;
    }

    private String parseHasAdditionalSoftwareStr(CSVRecord certResultHeading, List<CSVRecord> certResultRecords) {
        return uploadUtil.parseSingleRowField(Headings.HAS_ADDITIONAL_SOFTWARE, certResultHeading, certResultRecords);
    }

    private List<String> parseListingSources(CSVRecord certResultHeading, List<CSVRecord> certResultRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.ADDITIONAL_SOFTWARE_LISTING, certResultHeading, certResultRecords);
        return values;
    }

    private List<String> parseListingGroupings(CSVRecord certResultHeading, List<CSVRecord> certResultRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.ADDITIONAL_SOFTWARE_LISTING_GROUPING, certResultHeading, certResultRecords);
        return values;
    }

    private List<String> parseNonListingSources(CSVRecord certResultHeading, List<CSVRecord> certResultRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.ADDITONAL_SOFTWARE_NONLISTING, certResultHeading, certResultRecords);
        return values;
    }

    private List<String> parseNonListingVersions(CSVRecord certResultHeading, List<CSVRecord> certResultRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.ADDITIONAL_SOFTWARE_NONLISTING_VERSION, certResultHeading, certResultRecords);
        return values;
    }

    private List<String> parseNonListingGroupings(CSVRecord certResultHeading, List<CSVRecord> certResultRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.ADDITIONAL_SOFTWARE_NONLISTING_GROUPING, certResultHeading, certResultRecords);
        return values;
    }
}
