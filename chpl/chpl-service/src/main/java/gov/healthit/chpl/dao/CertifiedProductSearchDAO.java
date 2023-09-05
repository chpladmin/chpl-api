package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.entity.search.CertifiedProductBasicSearchResultEntity;
import gov.healthit.chpl.search.domain.CertifiedProductFlatSearchResult;
import lombok.extern.log4j.Log4j2;

@Deprecated
@Repository("certifiedProductSearchDAO")
@Log4j2
public class CertifiedProductSearchDAO extends BaseDAOImpl {
    @Deprecated
    public List<CertifiedProductFlatSearchResult> getFlatCertifiedProducts() {
        LOGGER.info("Starting basic search query.");
        Query query = entityManager.createQuery("SELECT cps "
                + "FROM CertifiedProductBasicSearchResultEntity cps ",
                CertifiedProductBasicSearchResultEntity.class);

        Date startDate = new Date();
        List<CertifiedProductBasicSearchResultEntity> results = query.getResultList();
        Date endDate = new Date();
        LOGGER.info("Got query results in " + (endDate.getTime() - startDate.getTime()) + " millis");
        List<CertifiedProductFlatSearchResult> domainResults = null;

        try {
            domainResults = convertToFlatListings(results);
        } catch (Exception ex) {
            LOGGER.error("Could not convert to flat listings " + ex.getMessage(), ex);
        }
        return domainResults;
    }

    private List<CertifiedProductFlatSearchResult> convertToFlatListings(List<CertifiedProductBasicSearchResultEntity> dbResults) {
        List<CertifiedProductFlatSearchResult> results = new ArrayList<CertifiedProductFlatSearchResult>(
                dbResults.size());
        return dbResults.stream()
            .map(dbResult -> buildFlatSearchResult(dbResult))
            .collect(Collectors.toList());
    }

    private CertifiedProductFlatSearchResult buildFlatSearchResult(CertifiedProductBasicSearchResultEntity entity) {
        return CertifiedProductFlatSearchResult.builder()
                .id(entity.getId())
                .chplProductNumber(entity.getChplProductNumber())
                .edition(entity.getEdition())
                .curesUpdate(entity.getCuresUpdate())
                .acb(entity.getAcbName())
                .acbCertificationId(entity.getAcbCertificationId())
                .practiceType(entity.getPracticeTypeName())
                .developerId(entity.getDeveloperId())
                .developer(entity.getDeveloper())
                .developerStatus(entity.getDeveloperStatus())
                .product(entity.getProduct())
                .version(entity.getVersion())
                .promotingInteroperabilityUserCount(entity.getPromotingInteroperabilityUserCount())
                .promotingInteroperabilityUserDate(entity.getPromotingInteroperabilityUserCountDate())
                .decertificationDate(entity.getDecertificationDate() == null ? null : entity.getDecertificationDate().getTime())
                .certificationDate(entity.getCertificationDate().getTime())
                .certificationStatus(entity.getCertificationStatus())
                .mandatoryDisclosures(entity.getMandatoryDisclosures())
                .apiDocumentation(entity.getApiDocumentation())
                .serviceBaseUrlList(entity.getServiceBaseUrlList() != null ? entity.getServiceBaseUrlList() : "")
                .surveillanceCount(entity.getSurveillanceCount())
                .openSurveillanceCount(entity.getOpenSurveillanceCount())
                .closedSurveillanceCount(entity.getClosedSurveillanceCount())
                .openSurveillanceNonConformityCount(entity.getOpenSurveillanceNonConformityCount())
                .closedSurveillanceNonConformityCount(entity.getClosedSurveillanceNonConformityCount())
                .surveillanceDates(entity.getSurveillanceDates())
                .statusEvents(entity.getStatusEvents())
                .criteriaMet(entity.getCerts())
                .cqmsMet(entity.getCqms())
                .previousDevelopers(entity.getPreviousDevelopers())
                .build();
    }
}
