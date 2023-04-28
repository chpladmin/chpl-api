package gov.healthit.chpl.search.dao;
import static gov.healthit.chpl.util.LambdaExceptionUtil.rethrowFunction;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Query;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.IdNamePair;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.search.domain.CertifiedProductSearchResult;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.search.domain.ListingSearchResult.CQMSearchResult;
import gov.healthit.chpl.search.domain.ListingSearchResult.CertificationCriterionSearchResult;
import gov.healthit.chpl.search.domain.ListingSearchResult.CertificationCriterionSearchResultWithLongFields;
import gov.healthit.chpl.search.domain.ListingSearchResult.CertificationCriterionSearchResultWithStringField;
import gov.healthit.chpl.search.domain.ListingSearchResult.DateRangeSearchResult;
import gov.healthit.chpl.search.domain.ListingSearchResult.DeveloperSearchResult;
import gov.healthit.chpl.search.domain.ListingSearchResult.PromotingInteroperabilitySearchResult;
import gov.healthit.chpl.search.domain.ListingSearchResult.StatusEventSearchResult;
import gov.healthit.chpl.search.entity.ListingSearchEntity;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Repository("listingSearchDAO")
@Log4j2
public class ListingSearchDao extends BaseDAOImpl {
    private static final String STANDARD_VALUE_SPLIT_CHAR = "\\|";
    private static final String STANDARD_FIELD_SPLIT_CHAR = ":";
    private static final int CRITERIA_FIELD_COUNT = 3;
    private static final int CQM_FIELD_COUNT = 2;
    private static final int PRODUCT_OWNER_FIELD_COUNT = 2;
    private static final int STATUS_EVENT_FIELD_COUNT = 3;
    private static final int DATE_RANGE_MIN_FIELD_COUNT = 1;
    private static final int DATE_RANGE_FIELD_COUNT = 2;

    public List<ListingSearchResult> getListingSearchResults() {
        LOGGER.info("Starting listing search query.");
        Query query = entityManager.createQuery("SELECT listings "
                + "FROM ListingSearchEntity listings ",
                ListingSearchEntity.class);

        Date startDate = new Date();
        List<ListingSearchEntity> results = query.getResultList();
        Date endDate = new Date();
        LOGGER.info("Got query results in " + (endDate.getTime() - startDate.getTime()) + " millis");
        List<ListingSearchResult> domainResults = null;

        try {
            domainResults = convertToListingSearchResults(results);
        } catch (Exception ex) {
            LOGGER.error("Could not convert to listing search results " + ex.getMessage(), ex);
        }
        return domainResults;
    }

    private List<ListingSearchResult> convertToListingSearchResults(List<ListingSearchEntity> entities)
        throws EntityRetrievalException, DateTimeParseException, NumberFormatException  {
        return entities.stream()
            .map(rethrowFunction(entity -> buildListingSearchResult(entity)))
            .collect(Collectors.toList());
    }

    private ListingSearchResult buildListingSearchResult(ListingSearchEntity entity)
        throws EntityRetrievalException, DateTimeParseException, NumberFormatException {
        return ListingSearchResult.builder()
                .id(entity.getId())
                .chplProductNumber(entity.getChplProductNumber())
                .edition(IdNamePair.builder()
                        .id(entity.getCertificationEditionId())
                        .name(entity.getCertificationEditionYear())
                        .build())
                .curesUpdate(entity.getCuresUpdate())
                .certificationBody(IdNamePair.builder()
                        .id(entity.getCertificationBodyId())
                        .name(entity.getCertificationBodyName())
                        .build())
                .acbCertificationId(entity.getAcbCertificationId())
                .practiceType(entity.getPracticeTypeId() != null
                    ? IdNamePair.builder()
                        .id(entity.getPracticeTypeId())
                        .name(entity.getPracticeTypeName())
                        .build()
                    : null)
                .developer(DeveloperSearchResult.builder()
                        .id(entity.getDeveloperId())
                        .name(entity.getDeveloper())
                        .status(IdNamePair.builder()
                                .id(entity.getDeveloperStatusId())
                                .name(entity.getDeveloperStatus())
                                .build())
                        .build())
                .product(IdNamePair.builder()
                        .id(entity.getProductId())
                        .name(entity.getProduct())
                        .build())
                .version(IdNamePair.builder()
                        .id(entity.getVersionId())
                        .name(entity.getVersion())
                        .build())
                .promotingInteroperability(convertToPromotingInteroperability(entity.getPromotingInteroperabilityUserCount(),
                        entity.getPromotingInteroperabilityUserCountDate()))
                .decertificationDate(entity.getDecertificationDate() == null ? null :
                    DateUtil.toLocalDate(entity.getDecertificationDate().getTime()))
                .certificationDate(DateUtil.toLocalDate(entity.getCertificationDate().getTime()))
                .certificationStatus(IdNamePair.builder()
                        .id(entity.getCertificationStatusId())
                        .name(entity.getCertificationStatus())
                        .build())
                .mandatoryDisclosures(entity.getMandatoryDisclosures())
                .surveillanceCount(entity.getSurveillanceCount())
                .openSurveillanceCount(entity.getOpenSurveillanceCount())
                .closedSurveillanceCount(entity.getClosedSurveillanceCount())
                .openSurveillanceNonConformityCount(entity.getOpenSurveillanceNonConformityCount())
                .closedSurveillanceNonConformityCount(entity.getClosedSurveillanceNonConformityCount())
                .rwtPlansUrl(entity.getRwtPlansUrl())
                .rwtResultsUrl(entity.getRwtResultsUrl())
                .svapNoticeUrl(entity.getSvapNoticeUrl())
                .surveillanceDateRanges(convertToSetOfDateRangesWithDelimiter(entity.getSurveillanceDates(), STANDARD_VALUE_SPLIT_CHAR))
                .statusEvents(convertToSetOfStatusEvents(entity.getStatusEvents(), STANDARD_VALUE_SPLIT_CHAR))
                .criteriaMet(convertToSetOfCriteria(entity.getCertificationCriteriaMet(), STANDARD_VALUE_SPLIT_CHAR))
                .cqmsMet(convertToSetOfCqms(entity.getCqmsMet(), STANDARD_VALUE_SPLIT_CHAR))
                .previousChplProductNumbers(convertToSetOfStrings(entity.getPreviousChplProductNumbers(), entity.getChplProductNumber(), STANDARD_VALUE_SPLIT_CHAR))
                .previousDevelopers(convertToSetOfProductOwners(entity.getPreviousDevelopers(), ListingSearchEntity.SMILEY_SPLIT_CHAR))
                .apiDocumentation(convertToSetOfCriteriaWithStringFields(entity.getCriteriaWithApiDocumentation(), CertifiedProductSearchResult.SMILEY_SPLIT_CHAR))
                .serviceBaseUrlList(convertToCriterionWithStringField(entity.getCriteriaWithServiceBaseUrlList()))
                .svaps(convertToSetOfCriteriaWithLongFields(entity.getCriteriaWithSvap(), CertifiedProductSearchResult.SMILEY_SPLIT_CHAR))
                .build();
    }

    private PromotingInteroperabilitySearchResult convertToPromotingInteroperability(Long userCount, LocalDate userDate) {
        if (userCount == null && userDate == null) {
            return null;
        }

        return PromotingInteroperabilitySearchResult.builder()
            .userCount(userCount)
            .userDate(userDate)
            .build();
    }

    private Set<String> convertToSetOfStrings(String delimitedString, String valueToIgnore, String delimeter)
            throws EntityRetrievalException, NumberFormatException {
        if (ObjectUtils.isEmpty(delimitedString)) {
            return new LinkedHashSet<String>();
        }

        String[] splitStrings = delimitedString.split(delimeter);
        return Stream.of(splitStrings)
                .filter(str -> !str.equals(valueToIgnore))
                .collect(Collectors.toSet());
    }

    private Set<DateRangeSearchResult> convertToSetOfDateRangesWithDelimiter(String delimitedDateRangeString, String delimeter)
            throws EntityRetrievalException, DateTimeParseException, NumberFormatException {
        if (ObjectUtils.isEmpty(delimitedDateRangeString)) {
            return new LinkedHashSet<DateRangeSearchResult>();
        }

        String[] dateRanges = delimitedDateRangeString.split(delimeter);
        return Stream.of(dateRanges)
                .map(rethrowFunction(aggregatedDateRangeString -> convertToDateRange(aggregatedDateRangeString)))
                .collect(Collectors.toSet());
    }

    private DateRangeSearchResult convertToDateRange(String aggregatedDateRangeString)
            throws EntityRetrievalException, DateTimeParseException, NumberFormatException {
        String[] dateRangeFields = aggregatedDateRangeString.split(STANDARD_FIELD_SPLIT_CHAR);
        if (dateRangeFields == null || dateRangeFields.length < DATE_RANGE_MIN_FIELD_COUNT) {
            throw new EntityRetrievalException("Unable to parse date range fields from '" + aggregatedDateRangeString + "'.");
        }
        return DateRangeSearchResult.builder()
                        .start(DateUtil.toLocalDate(Long.parseLong(dateRangeFields[0])))
                        .end(dateRangeFields.length < DATE_RANGE_FIELD_COUNT || StringUtils.isEmpty(dateRangeFields[1])
                                ? null : DateUtil.toLocalDate(Long.parseLong(dateRangeFields[1])))
                        .build();
    }

    private Set<StatusEventSearchResult> convertToSetOfStatusEvents(String delimitedStatusEventString, String delimeter)
            throws EntityRetrievalException, DateTimeParseException, NumberFormatException {
        if (ObjectUtils.isEmpty(delimitedStatusEventString)) {
            return new LinkedHashSet<StatusEventSearchResult>();
        }

        String[] statusEvents = delimitedStatusEventString.split(delimeter);
        return Stream.of(statusEvents)
                .map(rethrowFunction(aggregatedStatusEventString -> convertToStatusEvent(aggregatedStatusEventString)))
                .collect(Collectors.toSet());
    }

    private StatusEventSearchResult convertToStatusEvent(String aggregatedStatusEventString)
            throws EntityRetrievalException, DateTimeParseException, NumberFormatException {
        String[] statusEventFields = aggregatedStatusEventString.split(STANDARD_FIELD_SPLIT_CHAR);
        if (statusEventFields == null || statusEventFields.length != STATUS_EVENT_FIELD_COUNT) {
            throw new EntityRetrievalException("Unable to parse status event fields from '" + aggregatedStatusEventString + "'.");
        }
        return StatusEventSearchResult.builder()
                        .status(IdNamePair.builder()
                                .id(Long.parseLong(statusEventFields[0]))
                                .name(statusEventFields[1])
                                .build())
                        .statusStart(LocalDate.parse(statusEventFields[2]))
                        .build();
    }

    private Set<CertificationCriterionSearchResult> convertToSetOfCriteria(String delimitedCriteriaString, String delimeter)
            throws EntityRetrievalException, NumberFormatException {
        if (ObjectUtils.isEmpty(delimitedCriteriaString)) {
            return new LinkedHashSet<CertificationCriterionSearchResult>();
        }

        String[] criteria = delimitedCriteriaString.split(delimeter);
        return Stream.of(criteria)
                .map(rethrowFunction(aggregatedCriterionString -> convertToCriterion(aggregatedCriterionString)))
                .collect(Collectors.toSet());
    }

    private CertificationCriterionSearchResult convertToCriterion(String aggregatedCriterionString)
            throws EntityRetrievalException, NumberFormatException {
        String[] criterionFields = aggregatedCriterionString.split(STANDARD_FIELD_SPLIT_CHAR);
        if (criterionFields == null || criterionFields.length != CRITERIA_FIELD_COUNT) {
            throw new EntityRetrievalException("Unable to parse criteria fields from '" + aggregatedCriterionString + "'.");
        }
        return CertificationCriterionSearchResult.builder()
                        .id(Long.parseLong(criterionFields[0]))
                        .number(criterionFields[1])
                        .title(criterionFields[2])
                        .build();
    }

    private Set<CQMSearchResult> convertToSetOfCqms(String delimitedCqmString, String delimeter)
            throws EntityRetrievalException, NumberFormatException {
        if (ObjectUtils.isEmpty(delimitedCqmString)) {
            return new LinkedHashSet<CQMSearchResult>();
        }

        String[] cqms = delimitedCqmString.split(delimeter);
        return Stream.of(cqms)
                .map(rethrowFunction(aggregatedCqmString -> convertToCqm(aggregatedCqmString)))
                .collect(Collectors.toSet());
    }

    private CQMSearchResult convertToCqm(String aggregatedCqmString)
            throws EntityRetrievalException, NumberFormatException {
        String[] cqmFields = aggregatedCqmString.split(STANDARD_FIELD_SPLIT_CHAR);
        if (cqmFields == null || cqmFields.length != CQM_FIELD_COUNT) {
            throw new EntityRetrievalException("Unable to parse CQM fields from '" + aggregatedCqmString + "'.");
        }
        return CQMSearchResult.builder()
                        .id(Long.parseLong(cqmFields[0]))
                        .number(cqmFields[1])
                        .build();
    }

    private Set<CertificationCriterionSearchResultWithStringField> convertToSetOfCriteriaWithStringFields(String delimitedCriteriaWithValueString, String delimeter)
        throws EntityRetrievalException, NumberFormatException {
        if (ObjectUtils.isEmpty(delimitedCriteriaWithValueString)) {
            return new LinkedHashSet<CertificationCriterionSearchResultWithStringField>();
        }

        String[] criteriaWithStringFields = delimitedCriteriaWithValueString.split(delimeter);
        return Stream.of(criteriaWithStringFields)
                .map(rethrowFunction(criterionWithValue -> convertToCriterionWithStringField(criterionWithValue)))
                .collect(Collectors.toSet());
    }

    private CertificationCriterionSearchResultWithStringField convertToCriterionWithStringField(String value)
        throws EntityRetrievalException, NumberFormatException {
        if (StringUtils.isEmpty(value)) {
            return null;
        }

        String[] criteriaSplitFromStringData = value.split(CertifiedProductSearchResult.FROWNEY_SPLIT_CHAR);
        if (criteriaSplitFromStringData == null || criteriaSplitFromStringData.length != 2) {
            throw new EntityRetrievalException("Unable to parse criteria with string value from '" + value + "'.");
        }
        String aggregatedCriterionFields = criteriaSplitFromStringData[0];
        String fieldValue = criteriaSplitFromStringData[1];
        return CertificationCriterionSearchResultWithStringField.builder()
                .criterion(convertToCriterion(aggregatedCriterionFields))
                .value(fieldValue)
                .build();
    }

    private Set<CertificationCriterionSearchResultWithLongFields> convertToSetOfCriteriaWithLongFields(String delimitedCriteriaWithLongValue, String delimeter)
            throws EntityRetrievalException, NumberFormatException {
            if (ObjectUtils.isEmpty(delimitedCriteriaWithLongValue)) {
                return new LinkedHashSet<CertificationCriterionSearchResultWithLongFields>();
            }
            Set<CertificationCriterionSearchResultWithLongFields> result = new LinkedHashSet<CertificationCriterionSearchResultWithLongFields>();

            String[] criteriaWithLongFields = delimitedCriteriaWithLongValue.split(delimeter);
            Stream.of(criteriaWithLongFields)
                    .map(rethrowFunction(criterionWithValue -> convertToCriterionWithLongField(criterionWithValue)))
                    .forEach(criterionWithLongField -> addToResult(criterionWithLongField, result));
            return result;
    }

    private CertificationCriterionSearchResultWithLongFields convertToCriterionWithLongField(String value)
            throws EntityRetrievalException, NumberFormatException {
            if (StringUtils.isEmpty(value)) {
                return null;
            }

            String[] criteriaSplitFromData = value.split(CertifiedProductSearchResult.FROWNEY_SPLIT_CHAR);
            if (criteriaSplitFromData == null || criteriaSplitFromData.length != 2) {
                throw new EntityRetrievalException("Unable to parse criteria with long value from '" + value + "'.");
            }
            String aggregatedCriterionFields = criteriaSplitFromData[0];
            String fieldValueStr = criteriaSplitFromData[1];
            Long fieldValue = null;
            try {
                fieldValue = Long.parseLong(fieldValueStr);
            } catch (NumberFormatException ex) {
                LOGGER.error("Cannot parse " + fieldValueStr + " as a Long.", ex);
                fieldValue = null;
            }

            Set<Long> values = new LinkedHashSet<Long>();
            values.add(fieldValue);

            return CertificationCriterionSearchResultWithLongFields.builder()
                .criterion(convertToCriterion(aggregatedCriterionFields))
                .values(values)
                .build();
    }

    private void addToResult(CertificationCriterionSearchResultWithLongFields item, Set<CertificationCriterionSearchResultWithLongFields> results) {
        CertificationCriterionSearchResultWithLongFields resultWithCriterion = getResultWithCriterion(item.getCriterion(), results);
        if (resultWithCriterion != null) {
            resultWithCriterion.getValues().addAll(item.getValues());
        } else {
            results.add(item);
        }
    }

    private CertificationCriterionSearchResultWithLongFields getResultWithCriterion(CertificationCriterionSearchResult criterion, Set<CertificationCriterionSearchResultWithLongFields> results) {
        CertificationCriterionSearchResultWithLongFields resultWithCriterion = null;
        Optional<CertificationCriterionSearchResultWithLongFields> resultWithCriterionOpt = results.stream()
            .filter(result -> result.getCriterion().getId().equals(criterion.getId()))
            .findAny();
        if (resultWithCriterionOpt.isPresent()) {
            resultWithCriterion = resultWithCriterionOpt.get();
        }
        return resultWithCriterion;
    }

    private Set<IdNamePair> convertToSetOfProductOwners(String delimitedProductOwnerString, String delimeter)
            throws EntityRetrievalException, NumberFormatException {
        if (ObjectUtils.isEmpty(delimitedProductOwnerString)) {
            return new LinkedHashSet<IdNamePair>();
        }

        String[] productOwners = delimitedProductOwnerString.split(delimeter);
        return Stream.of(productOwners)
                .map(rethrowFunction(aggregatedProductOwnerString -> convertToProductOwner(aggregatedProductOwnerString)))
                .collect(Collectors.toSet());
    }

    private IdNamePair convertToProductOwner(String aggregatedProductOwnerString)
            throws EntityRetrievalException, NumberFormatException {
        String[] productOwnerFields = aggregatedProductOwnerString.split(ListingSearchEntity.FROWNEY_SPLIT_CHAR);
        if (productOwnerFields == null || productOwnerFields.length != PRODUCT_OWNER_FIELD_COUNT) {
            throw new EntityRetrievalException("Unable to parse product owner fields from '" + aggregatedProductOwnerString + "'.");
        }
        return IdNamePair.builder()
                        .id(Long.parseLong(productOwnerFields[0]))
                        .name(productOwnerFields[1])
                        .build();
    }
}
