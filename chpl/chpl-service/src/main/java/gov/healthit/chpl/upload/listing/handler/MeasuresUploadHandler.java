package gov.healthit.chpl.upload.listing.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.validation.ValidationException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.ListingMeasure;
import gov.healthit.chpl.domain.Measure;
import gov.healthit.chpl.domain.MeasureDomain;
import gov.healthit.chpl.domain.MeasureType;
import gov.healthit.chpl.upload.listing.Headings;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import lombok.extern.log4j.Log4j2;

@Component("measuresUploadHandler")
@Log4j2
public class MeasuresUploadHandler {
    private ListingUploadHandlerUtil uploadUtil;

    @Autowired
    public MeasuresUploadHandler(ListingUploadHandlerUtil uploadUtil) {
        this.uploadUtil = uploadUtil;
    }

    public List<ListingMeasure> parseAsMeasures(CSVRecord headingRecord, List<CSVRecord> listingRecords)
        throws ValidationException {
        List<ListingMeasure> listingMeasures = new ArrayList<ListingMeasure>();
        List<String> measureDomains = parseMeasureDomains(headingRecord, listingRecords);
        List<String> measureRequiredTests = parseMeasureRequiredTests(headingRecord, listingRecords);
        List<String> measureTypeValues = parseMeasureTypeValues(headingRecord, listingRecords);
        List<String> measureAssociatedCriteria = parseMeasureAssociatedCriteria(headingRecord, listingRecords);
        if (uploadUtil.areCollectionsEmpty(measureDomains, measureRequiredTests, measureTypeValues, measureAssociatedCriteria)) {
            return listingMeasures;
        }

        int max = 0;
        if (CollectionUtils.isNotEmpty(measureDomains)) {
            max = Math.max(max, measureDomains.size());
        }
        if (CollectionUtils.isNotEmpty(measureRequiredTests)) {
            max = Math.max(max, measureRequiredTests.size());
        }
        if (CollectionUtils.isNotEmpty(measureTypeValues)) {
            max = Math.max(max, measureTypeValues.size());
        }
        if (CollectionUtils.isNotEmpty(measureAssociatedCriteria)) {
            max = Math.max(max, measureAssociatedCriteria.size());
        }
        //I think everything remains ordered using these data structures so this should be okay.
        listingMeasures = IntStream.range(0, max)
            .mapToObj(index -> buildListingMeasure(index, measureDomains, measureRequiredTests, measureTypeValues, measureAssociatedCriteria))
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(ArrayList::new));
        return listingMeasures;
    }

    private List<String> parseMeasureDomains(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.MEASURE_DOMAIN, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseMeasureRequiredTests(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.MEASURE_REQUIRED_TEST, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseMeasureTypeValues(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.MEASURE_TYPE, headingRecord, listingRecords);
        return values;
    }

    private List<String> parseMeasureAssociatedCriteria(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.MEASURE_ASSOCIATED_CRITERIA, headingRecord, listingRecords);
        return values;
    }

    private ListingMeasure buildListingMeasure(int index, List<String> measureDomains, List<String> measureRequiredTests,
            List<String> measureTypeValues, List<String> measureAssociatedCriteria) {
        String measureDomain = (measureDomains != null && measureDomains.size() > index) ? measureDomains.get(index) : null;
        String measureRequiredTest = (measureRequiredTests != null && measureRequiredTests.size() > index)
                ? measureRequiredTests.get(index) : null;
        String measureTypeName = (measureTypeValues != null && measureTypeValues.size() > index)
                ? measureTypeValues.get(index) : null;
        String measureAssociatedCriteriaDelimited = (measureAssociatedCriteria != null && measureAssociatedCriteria.size() > index)
                ? measureAssociatedCriteria.get(index) : null;

        if (StringUtils.isAllEmpty(measureDomain, measureRequiredTest, measureTypeName, measureAssociatedCriteriaDelimited)) {
            return null;
        }

        LinkedHashSet<CertificationCriterion> associatedCriteria = new LinkedHashSet<CertificationCriterion>();
        if (!StringUtils.isEmpty(measureAssociatedCriteriaDelimited) && !"0".equals(measureAssociatedCriteriaDelimited)) {
            String[] splitCriteriaNumbers = measureAssociatedCriteriaDelimited.split(";");
            if (splitCriteriaNumbers.length == 1) {
                splitCriteriaNumbers = measureAssociatedCriteriaDelimited.split(",");
            }
            List<String> splitTrimmedCriteriaNumbers = Arrays.stream(splitCriteriaNumbers)
                    .map(String::trim)
                    .collect(Collectors.toList());
            associatedCriteria.addAll(splitTrimmedCriteriaNumbers.stream()
                    .map(criterionNumber -> buildCriterion(criterionNumber))
                    .collect(Collectors.toList()));
        }

        return ListingMeasure.builder()
                .measure(Measure.builder()
                        .domain(MeasureDomain.builder()
                                .name(measureDomain)
                                .build())
                        .abbreviation(measureRequiredTest)
                        .build())
                .measureType(MeasureType.builder().name(measureTypeName).build())
                .associatedCriteria(associatedCriteria)
                .build();
    }

    private CertificationCriterion buildCriterion(String criterionNumber) {
        return CertificationCriterion.builder()
                .number(criterionNumber)
                .build();
    }
}
