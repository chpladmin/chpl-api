package gov.healthit.chpl.search.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Query;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.search.domain.CertifiedProductSearchResult;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.search.entity.ListingSearchEntity;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Repository("listingSearchDAO")
@Log4j2
public class ListingSearchDao extends BaseDAOImpl {

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


    private List<ListingSearchResult> convertToListingSearchResults(List<ListingSearchEntity> entities) {
        List<ListingSearchResult> results = new ArrayList<ListingSearchResult>(entities.size());
        return entities.stream()
            .map(entity -> buildListingSearchResult(entity))
            .collect(Collectors.toList());
    }

    private ListingSearchResult buildListingSearchResult(ListingSearchEntity entity) {
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
                .promotingInteroperability(ListingSearchResult.PromotingInteroperability.builder()
                        .userCount(entity.getPromotingInteroperabilityUserCount())
                        .userDate(entity.getPromotingInteroperabilityUserCountDate())
                        .build())
                .decertificationDate(entity.getDecertificationDate() == null ? null :
                    DateUtil.toLocalDate(entity.getDecertificationDate().getTime()))
                .certificationDate(DateUtil.toLocalDate(entity.getCertificationDate().getTime()))
                .certificationStatus(ListingSearchResult.Status.builder()
                        .id(entity.getCertificationStatusId())
                        .name(entity.getCertificationStatus())
                        .build())
                .mandatoryDisclosures(entity.getMandatoryDisclosures())
                .apiDocumentation(convertToSetOfCriteriaWithValues(entity.getCriteriaWithApiDocumentation(), CertifiedProductSearchResult.SMILEY_SPLIT_CHAR))
                .serviceBaseUrlList(convertToSetOfStringsWithDelimiter(entity.getServiceBaseUrlList(), CertifiedProductSearchResult.SMILEY_SPLIT_CHAR))
                .surveillanceCount(entity.getSurveillanceCount())
                .openSurveillanceCount(entity.getOpenSurveillanceCount())
                .closedSurveillanceCount(entity.getClosedSurveillanceCount())
                .openSurveillanceNonConformityCount(entity.getOpenSurveillanceNonConformityCount())
                .closedSurveillanceNonConformityCount(entity.getClosedSurveillanceNonConformityCount())
                .rwtPlansUrl(entity.getRwtPlansUrl())
                .rwtResultsUrl(entity.getRwtResultsUrl())
                .surveillanceDates(convertToSetOfStringsWithDelimiter(entity.getSurveillanceDates(), CertifiedProductSearchResult.SMILEY_SPLIT_CHAR))
                .statusEvents(convertToSetOfStringsWithDelimiter(entity.getStatusEvents(), "&"))
                .criteriaMet(convertToSetOfLongsWithDelimiter(entity.getCerts(), CertifiedProductSearchResult.SMILEY_SPLIT_CHAR))
                .cqmsMet(convertToSetOfStringsWithDelimiter(entity.getCqms(), CertifiedProductSearchResult.SMILEY_SPLIT_CHAR))
                .previousDevelopers(convertToSetOfStringsWithDelimiter(entity.getPreviousDevelopers(), "|"))
                .build();
    }

    private Set<ListingSearchResult.CertificationCriterionWithStringField> convertToSetOfCriteriaWithValues(String delimitedCriteriaWithValueString, String delimeter) {
        if (ObjectUtils.isEmpty(delimitedCriteriaWithValueString)) {
            return new LinkedHashSet<ListingSearchResult.CertificationCriterionWithStringField>();
        }

        String[] criteriaWithValues = delimitedCriteriaWithValueString.split(delimeter);
        return Stream.of(criteriaWithValues)
                .map(criterionWithValue -> )
                .collect(Collectors.toSet());
    }
}
