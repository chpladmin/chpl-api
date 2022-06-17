package gov.healthit.chpl.upload.listing.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.validation.ValidationException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.ListingMeasure;
import gov.healthit.chpl.domain.Measure;
import gov.healthit.chpl.domain.MeasureType;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.upload.listing.Headings;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import lombok.extern.log4j.Log4j2;

@Component("measuresUploadHandler")
@Log4j2
public class MeasuresUploadHandler {
    private ListingUploadHandlerUtil uploadUtil;
    private CertificationCriterionService criteriaService;

    @Autowired
    public MeasuresUploadHandler(ListingUploadHandlerUtil uploadUtil,
            CertificationCriterionService criteriaService) {
        this.uploadUtil = uploadUtil;
        this.criteriaService = criteriaService;
    }

    public List<ListingMeasure> parseAsMeasures(CSVRecord headingRecord, List<CSVRecord> listingRecords)
        throws ValidationException {
        List<ListingMeasure> listingMeasures = new ArrayList<ListingMeasure>();
        List<String> measureNames = parseMeasureNames(headingRecord, listingRecords);
        List<String> measureRequiredTests = parseMeasureRequiredTests(headingRecord, listingRecords);
        List<String> measureTypeValues = parseMeasureTypeValues(headingRecord, listingRecords);
        List<String> measureAssociatedCriteria = parseMeasureAssociatedCriteria(headingRecord, listingRecords);
        if (uploadUtil.areCollectionsEmpty(measureNames, measureRequiredTests, measureTypeValues, measureAssociatedCriteria)) {
            return listingMeasures;
        }

        int max = 0;
        if (CollectionUtils.isNotEmpty(measureNames)) {
            max = Math.max(max, measureNames.size());
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
            .mapToObj(index -> buildListingMeasure(index, measureNames, measureRequiredTests, measureTypeValues, measureAssociatedCriteria))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        return listingMeasures;
    }

    private List<String> parseMeasureNames(CSVRecord headingRecord, List<CSVRecord> listingRecords) {
        List<String> values = uploadUtil.parseMultiRowField(
                Headings.MEASURE_NAME, headingRecord, listingRecords);
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

    private ListingMeasure buildListingMeasure(int index, List<String> measureNames, List<String> measureRequiredTests,
            List<String> measureTypeValues, List<String> measureAssociatedCriteria) {
        String measureName = (measureNames != null && measureNames.size() > index) ? measureNames.get(index) : null;
        String measureRequiredTest = (measureRequiredTests != null && measureRequiredTests.size() > index)
                ? measureRequiredTests.get(index) : null;
        String measureTypeName = (measureTypeValues != null && measureTypeValues.size() > index)
                ? measureTypeValues.get(index) : null;
        String measureAssociatedCriteriaDelimited = (measureAssociatedCriteria != null && measureAssociatedCriteria.size() > index)
                ? measureAssociatedCriteria.get(index) : null;

        if (StringUtils.isAllEmpty(measureName, measureRequiredTest, measureTypeName, measureAssociatedCriteriaDelimited)) {
            return null;
        }

        Set<CertificationCriterion> associatedCriteria = new LinkedHashSet<CertificationCriterion>();
        if (!StringUtils.isEmpty(measureAssociatedCriteriaDelimited) && !"0".equals(measureAssociatedCriteriaDelimited)) {
            String[] splitCriteriaNumbers = measureAssociatedCriteriaDelimited.split(";");
            if (splitCriteriaNumbers.length == 1) {
                splitCriteriaNumbers = measureAssociatedCriteriaDelimited.split(",");
            }
            List<String> splitTrimmedCriteriaNumbers = Arrays.stream(splitCriteriaNumbers)
                    .map(String::trim)
                    .collect(Collectors.toList());
            associatedCriteria.addAll(splitTrimmedCriteriaNumbers.stream()
                    .flatMap(criterionNumber -> buildCriterion(criterionNumber).stream())
                    .toList());
        }

        return ListingMeasure.builder()
                .measure(Measure.builder()
                        .name(measureName)
                        .requiredTest(measureRequiredTest)
                        .build())
                .measureType(MeasureType.builder().name(measureTypeName).build())
                .associatedCriteria(associatedCriteria)
                .build();
    }

    private List<CertificationCriterion> buildCriterion(String criterionNumber) {
        List<CertificationCriterion> criteria = criteriaService.getByNumber(criterionNumber);
        if (CollectionUtils.isEmpty(criteria)) {
            LOGGER.warn("No criteria was found with number '" + criterionNumber + "'.");
            return new ArrayList<CertificationCriterion>();
        }
        return criteria;
    }
}
