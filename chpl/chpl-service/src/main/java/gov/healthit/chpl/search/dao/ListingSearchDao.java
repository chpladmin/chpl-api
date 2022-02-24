package gov.healthit.chpl.search.dao;
import static gov.healthit.chpl.util.LambdaExceptionUtil.rethrowFunction;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Query;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.search.domain.CertifiedProductSearchResult;
import gov.healthit.chpl.search.domain.ListingSearchResult;
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
                .edition(ListingSearchResult.Edition.builder()
                        .id(entity.getCertificationEditionId())
                        .year(entity.getCertificationEditionYear())
                        .build())
                .curesUpdate(entity.getCuresUpdate())
                .certificationBody(ListingSearchResult.CertificationBody.builder()
                        .id(entity.getCertificationBodyId())
                        .name(entity.getCertificationBodyName())
                        .build())
                .acbCertificationId(entity.getAcbCertificationId())
                .practiceType(entity.getPracticeTypeId() != null
                    ? ListingSearchResult.PracticeType.builder()
                        .id(entity.getPracticeTypeId())
                        .name(entity.getPracticeTypeName())
                        .build()
                    : null)
                .developer(ListingSearchResult.Developer.builder()
                        .id(entity.getDeveloperId())
                        .name(entity.getDeveloper())
                        .status(ListingSearchResult.Status.builder()
                                .id(entity.getDeveloperStatusId())
                                .name(entity.getDeveloperStatus())
                                .build())
                        .build())
                .product(ListingSearchResult.Product.builder()
                        .id(entity.getProductId())
                        .name(entity.getProduct())
                        .build())
                .version(ListingSearchResult.Version.builder()
                        .id(entity.getVersionId())
                        .name(entity.getVersion())
                        .build())
                .promotingInteroperability(convertToPromotingInteroperability(entity.getPromotingInteroperabilityUserCount(),
                        entity.getPromotingInteroperabilityUserCountDate()))
                .decertificationDate(entity.getDecertificationDate() == null ? null :
                    DateUtil.toLocalDate(entity.getDecertificationDate().getTime()))
                .certificationDate(DateUtil.toLocalDate(entity.getCertificationDate().getTime()))
                .certificationStatus(ListingSearchResult.Status.builder()
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
                .surveillanceDateRanges(convertToSetOfDateRangesWithDelimiter(entity.getSurveillanceDates(), STANDARD_VALUE_SPLIT_CHAR))
                .statusEvents(convertToSetOfStatusEvents(entity.getStatusEvents(), STANDARD_VALUE_SPLIT_CHAR))
                .criteriaMet(convertToSetOfCriteria(entity.getCertificationCriteriaMet(), STANDARD_VALUE_SPLIT_CHAR))
                .cqmsMet(convertToSetOfCqms(entity.getCqmsMet(), STANDARD_VALUE_SPLIT_CHAR))
                .previousDevelopers(convertToSetOfProductOwners(entity.getPreviousDevelopers(), ListingSearchEntity.SMILEY_SPLIT_CHAR))
                .apiDocumentation(convertToSetOfCriteriaWithStringFields(entity.getCriteriaWithApiDocumentation(), CertifiedProductSearchResult.SMILEY_SPLIT_CHAR))
                .serviceBaseUrl(convertToCriterionWithStringField(entity.getCriteriaWithServiceBaseUrl()))
                .build();
    }

    private ListingSearchResult.PromotingInteroperability convertToPromotingInteroperability(Long userCount, LocalDate userDate) {
        if (userCount == null && userDate == null) {
            return null;
        }

        return ListingSearchResult.PromotingInteroperability.builder()
            .userCount(userCount)
            .userDate(userDate)
            .build();
    }

    private Set<ListingSearchResult.DateRange> convertToSetOfDateRangesWithDelimiter(String delimitedDateRangeString, String delimeter)
            throws EntityRetrievalException, DateTimeParseException, NumberFormatException {
        if (ObjectUtils.isEmpty(delimitedDateRangeString)) {
            return new LinkedHashSet<ListingSearchResult.DateRange>();
        }

        String[] dateRanges = delimitedDateRangeString.split(delimeter);
        return Stream.of(dateRanges)
                .map(rethrowFunction(aggregatedDateRangeString -> convertToDateRange(aggregatedDateRangeString)))
                .collect(Collectors.toSet());
    }

    private ListingSearchResult.DateRange convertToDateRange(String aggregatedDateRangeString)
            throws EntityRetrievalException, DateTimeParseException, NumberFormatException {
        String[] dateRangeFields = aggregatedDateRangeString.split(STANDARD_FIELD_SPLIT_CHAR);
        if (dateRangeFields == null || dateRangeFields.length < DATE_RANGE_MIN_FIELD_COUNT) {
            throw new EntityRetrievalException("Unable to parse date range fields from '" + aggregatedDateRangeString + "'.");
        }
        return ListingSearchResult.DateRange.builder()
                        .start(DateUtil.toLocalDate(Long.parseLong(dateRangeFields[0])))
                        .end(dateRangeFields.length < DATE_RANGE_FIELD_COUNT || StringUtils.isEmpty(dateRangeFields[1])
                                ? null : DateUtil.toLocalDate(Long.parseLong(dateRangeFields[1])))
                        .build();
    }

    private Set<ListingSearchResult.StatusEvent> convertToSetOfStatusEvents(String delimitedStatusEventString, String delimeter)
            throws EntityRetrievalException, DateTimeParseException, NumberFormatException {
        if (ObjectUtils.isEmpty(delimitedStatusEventString)) {
            return new LinkedHashSet<ListingSearchResult.StatusEvent>();
        }

        String[] statusEvents = delimitedStatusEventString.split(delimeter);
        return Stream.of(statusEvents)
                .map(rethrowFunction(aggregatedStatusEventString -> convertToStatusEvent(aggregatedStatusEventString)))
                .collect(Collectors.toSet());
    }

    private ListingSearchResult.StatusEvent convertToStatusEvent(String aggregatedStatusEventString)
            throws EntityRetrievalException, DateTimeParseException, NumberFormatException {
        String[] statusEventFields = aggregatedStatusEventString.split(STANDARD_FIELD_SPLIT_CHAR);
        if (statusEventFields == null || statusEventFields.length != STATUS_EVENT_FIELD_COUNT) {
            throw new EntityRetrievalException("Unable to parse status event fields from '" + aggregatedStatusEventString + "'.");
        }
        return ListingSearchResult.StatusEvent.builder()
                        .status(ListingSearchResult.Status.builder()
                                .id(Long.parseLong(statusEventFields[0]))
                                .name(statusEventFields[1])
                                .build())
                        .statusBegin(LocalDate.parse(statusEventFields[2]))
                        .build();
    }

    private Set<ListingSearchResult.CertificationCriterion> convertToSetOfCriteria(String delimitedCriteriaString, String delimeter)
            throws EntityRetrievalException, NumberFormatException {
        if (ObjectUtils.isEmpty(delimitedCriteriaString)) {
            return new LinkedHashSet<ListingSearchResult.CertificationCriterion>();
        }

        String[] criteria = delimitedCriteriaString.split(delimeter);
        return Stream.of(criteria)
                .map(rethrowFunction(aggregatedCriterionString -> convertToCriterion(aggregatedCriterionString)))
                .collect(Collectors.toSet());
    }

    private ListingSearchResult.CertificationCriterion convertToCriterion(String aggregatedCriterionString)
            throws EntityRetrievalException, NumberFormatException {
        String[] criterionFields = aggregatedCriterionString.split(STANDARD_FIELD_SPLIT_CHAR);
        if (criterionFields == null || criterionFields.length != CRITERIA_FIELD_COUNT) {
            throw new EntityRetrievalException("Unable to parse criteria fields from '" + aggregatedCriterionString + "'.");
        }
        return ListingSearchResult.CertificationCriterion.builder()
                        .id(Long.parseLong(criterionFields[0]))
                        .number(criterionFields[1])
                        .title(criterionFields[2])
                        .build();
    }

    private Set<ListingSearchResult.CQM> convertToSetOfCqms(String delimitedCqmString, String delimeter)
            throws EntityRetrievalException, NumberFormatException {
        if (ObjectUtils.isEmpty(delimitedCqmString)) {
            return new LinkedHashSet<ListingSearchResult.CQM>();
        }

        String[] cqms = delimitedCqmString.split(delimeter);
        return Stream.of(cqms)
                .map(rethrowFunction(aggregatedCqmString -> convertToCqm(aggregatedCqmString)))
                .collect(Collectors.toSet());
    }

    private ListingSearchResult.CQM convertToCqm(String aggregatedCqmString)
            throws EntityRetrievalException, NumberFormatException {
        String[] cqmFields = aggregatedCqmString.split(STANDARD_FIELD_SPLIT_CHAR);
        if (cqmFields == null || cqmFields.length != CQM_FIELD_COUNT) {
            throw new EntityRetrievalException("Unable to parse CQM fields from '" + aggregatedCqmString + "'.");
        }
        return ListingSearchResult.CQM.builder()
                        .id(Long.parseLong(cqmFields[0]))
                        .number(cqmFields[1])
                        .build();
    }

    private Set<ListingSearchResult.CertificationCriterionWithStringField> convertToSetOfCriteriaWithStringFields(String delimitedCriteriaWithValueString, String delimeter)
        throws EntityRetrievalException, NumberFormatException {
        if (ObjectUtils.isEmpty(delimitedCriteriaWithValueString)) {
            return new LinkedHashSet<ListingSearchResult.CertificationCriterionWithStringField>();
        }

        String[] criteriaWithStringFields = delimitedCriteriaWithValueString.split(delimeter);
        return Stream.of(criteriaWithStringFields)
                .map(rethrowFunction(criterionWithValue -> convertToCriterionWithStringField(criterionWithValue)))
                .collect(Collectors.toSet());
    }

    private ListingSearchResult.CertificationCriterionWithStringField convertToCriterionWithStringField(String value)
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
        return ListingSearchResult.CertificationCriterionWithStringField.builder()
                .criterion(convertToCriterion(aggregatedCriterionFields))
                .value(fieldValue)
                .build();
    }

    private Set<ListingSearchResult.ProductOwner> convertToSetOfProductOwners(String delimitedProductOwnerString, String delimeter)
            throws EntityRetrievalException, NumberFormatException {
        if (ObjectUtils.isEmpty(delimitedProductOwnerString)) {
            return new LinkedHashSet<ListingSearchResult.ProductOwner>();
        }

        String[] productOwners = delimitedProductOwnerString.split(delimeter);
        return Stream.of(productOwners)
                .map(rethrowFunction(aggregatedProductOwnerString -> convertToProductOwner(aggregatedProductOwnerString)))
                .collect(Collectors.toSet());
    }

    private ListingSearchResult.ProductOwner convertToProductOwner(String aggregatedProductOwnerString)
            throws EntityRetrievalException, NumberFormatException {
        String[] productOwnerFields = aggregatedProductOwnerString.split(ListingSearchEntity.FROWNEY_SPLIT_CHAR);
        if (productOwnerFields == null || productOwnerFields.length != PRODUCT_OWNER_FIELD_COUNT) {
            throw new EntityRetrievalException("Unable to parse product owner fields from '" + aggregatedProductOwnerString + "'.");
        }
        return ListingSearchResult.ProductOwner.builder()
                        .id(Long.parseLong(productOwnerFields[0]))
                        .name(productOwnerFields[1])
                        .build();
    }
}
