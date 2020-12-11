package gov.healthit.chpl.upload.listing.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.ValidationException;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.ListingMeasure;
import gov.healthit.chpl.domain.Measure;
import gov.healthit.chpl.domain.MeasureType;
import gov.healthit.chpl.upload.listing.Headings;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import lombok.extern.log4j.Log4j2;

@Component("measureUploadHandler")
@Log4j2
public class MeasureUploadHandler {
    private static final String G1_MEASURE_TYPE = "G1";
    private static final String G2_MEASURE_TYPE = "G2";

    private CertificationCriterionUploadHandler criterionHandler;
    private ListingUploadHandlerUtil uploadUtil;

    @Autowired
    public MeasureUploadHandler(CertificationCriterionUploadHandler criterionHandler,
            ListingUploadHandlerUtil uploadUtil) {
        this.criterionHandler = criterionHandler;
        this.uploadUtil = uploadUtil;
    }

    public List<ListingMeasure> parseAsMeasures(CSVRecord headingRecord, List<CSVRecord> listingRecords)
        throws ValidationException {
        List<ListingMeasure> listingMeasures = new ArrayList<ListingMeasure>();

        int prevCertResultIndex = -1;
        int nextCertResultIndex = uploadUtil.getNextIndexOfCertificationResult(0, headingRecord);
        while (nextCertResultIndex >= 0 && prevCertResultIndex != nextCertResultIndex) {
            List<CSVRecord> parsedCertResultRecords = uploadUtil.getCertificationResultRecordsFromIndex(
                    nextCertResultIndex, headingRecord, listingRecords);
            CSVRecord certHeadingRecord = uploadUtil.getHeadingRecord(parsedCertResultRecords);
            CertificationCriterion criterion = criterionHandler.handle(certHeadingRecord);
            if (criterion != null) {
                List<ListingMeasure> g1CertResultMeasures = parseG1MeasuresFromCertificationResult(criterion,
                        certHeadingRecord, parsedCertResultRecords.subList(1, parsedCertResultRecords.size()));
                updateListingMeasureList(listingMeasures, g1CertResultMeasures);
                List<ListingMeasure> g2CertResultMeasures = parseG2MeasuresFromCertificationResult(criterion,
                        certHeadingRecord, parsedCertResultRecords.subList(1, parsedCertResultRecords.size()));
                updateListingMeasureList(listingMeasures, g2CertResultMeasures);
            }
            prevCertResultIndex = nextCertResultIndex;
            nextCertResultIndex = uploadUtil.getNextIndexOfCertificationResult(
                    nextCertResultIndex + certHeadingRecord.size() - 1, headingRecord);
        }
        return listingMeasures;
    }

    private List<ListingMeasure> parseG1MeasuresFromCertificationResult(CertificationCriterion criterion,
            CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        List<ListingMeasure> certResultMeasures = new ArrayList<ListingMeasure>();
        List<String> g1MeasureValues = uploadUtil.parseMultiRowFieldWithoutEmptyValues(
                Headings.MACRA_MEASURE_G1, certHeadingRecord, certResultRecords);
        if (g1MeasureValues == null || g1MeasureValues.size() == 0) {
            return new ArrayList<ListingMeasure>();
        }
        return g1MeasureValues.stream()
            .map(measureVal -> toListingMeasure(measureVal, criterion, G1_MEASURE_TYPE))
            .collect(Collectors.toList());
    }

    private List<ListingMeasure> parseG2MeasuresFromCertificationResult(CertificationCriterion criterion,
            CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        List<ListingMeasure> certResultMeasures = new ArrayList<ListingMeasure>();
        List<String> g2MeasureValues = uploadUtil.parseMultiRowFieldWithoutEmptyValues(
                Headings.MACRA_MEASURE_G2, certHeadingRecord, certResultRecords);
        if (g2MeasureValues == null || g2MeasureValues.size() == 0) {
            return new ArrayList<ListingMeasure>();
        }
        return g2MeasureValues.stream()
            .map(measureVal -> toListingMeasure(measureVal, criterion, G2_MEASURE_TYPE))
            .collect(Collectors.toList());
    }

    private ListingMeasure toListingMeasure(String measureVal, CertificationCriterion criterion,
            String measureTypeName) {
        return ListingMeasure.builder()
                .measureType(MeasureType.builder().name(measureTypeName).build())
                .associatedCriterion(criterion)
                .measure(Measure.builder().legacyMacraMeasureValue(measureVal).build())
                .build();
    }

    private void updateListingMeasureList(List<ListingMeasure> listingMeasures, List<ListingMeasure> certResultMeasures) {
        certResultMeasures.stream().forEach(certResultMeasure -> {
            if (listingContainsMeasure(listingMeasures, certResultMeasure)) {
                addCriteriaToExistingListingMeasure(listingMeasures, certResultMeasure);
            } else {
                listingMeasures.add(certResultMeasure);
            }
        });
    }

    private boolean listingContainsMeasure(List<ListingMeasure> listingMeasures, ListingMeasure certResultMeasure) {
        return listingMeasures.stream()
            .filter(listingMeasure -> listingMeasure.getMeasure() != null
                && listingMeasure.getMeasure().getLegacyMacraMeasureValue().equalsIgnoreCase(
                        certResultMeasure.getMeasure().getLegacyMacraMeasureValue()))
            .findAny().isPresent();
    }

    private void addCriteriaToExistingListingMeasure(List<ListingMeasure> listingMeasures, ListingMeasure certResultMeasure) {
        listingMeasures.stream()
            .filter(listingMeasure -> listingMeasure.getMeasure() != null
                && listingMeasure.getMeasure().getLegacyMacraMeasureValue().equalsIgnoreCase(
                        certResultMeasure.getMeasure().getLegacyMacraMeasureValue()))
            .forEach(listingMeasure -> {
                listingMeasure.getAssociatedCriteria().addAll(certResultMeasure.getAssociatedCriteria());
            });
    }
}
