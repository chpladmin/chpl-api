package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.entity.search.CertifiedProductBasicSearchResultEntity;
import gov.healthit.chpl.search.domain.CertifiedProductFlatSearchResult;
import lombok.extern.log4j.Log4j2;

@Repository("certifiedProductSearchDAO")
@Log4j2
public class CertifiedProductSearchDAO extends BaseDAOImpl {
    public Long getListingIdByUniqueChplNumber(String chplProductNumber) {
        Long id = null;
        Query query = entityManager.createQuery(
                "SELECT cps " + "FROM CertifiedProductBasicSearchResultEntity cps "
                        + "WHERE chplProductNumber = :chplProductNumber",
                        CertifiedProductBasicSearchResultEntity.class);
        query.setParameter("chplProductNumber", chplProductNumber);
        List<CertifiedProductBasicSearchResultEntity> results = query.getResultList();
        if (results != null && results.size() > 0) {
            CertifiedProductBasicSearchResultEntity result = results.get(0);
            id = result.getId();
        }
        return id;
    }

    public CertifiedProduct getByChplProductNumber(String chplProductNumber) throws EntityNotFoundException {
        Query query = entityManager.createQuery(
                "SELECT cps " + "FROM CertifiedProductBasicSearchResultEntity cps "
                        + "WHERE cps.chplProductNumber = :chplProductNumber",
                        CertifiedProductBasicSearchResultEntity.class);
        query.setParameter("chplProductNumber", chplProductNumber);

        List<CertifiedProductBasicSearchResultEntity> results = query.getResultList();
        if (results == null || results.size() == 0) {
            throw new EntityNotFoundException(
                    "No listing with CHPL Product Number " + chplProductNumber + " was found.");
        }
        CertifiedProduct result = new CertifiedProduct();
        result.setCertificationDate(results.get(0).getCertificationDate().getTime());
        result.setCertificationStatus(results.get(0).getCertificationStatus());
        result.setChplProductNumber(results.get(0).getChplProductNumber());
        result.setEdition(results.get(0).getEdition());
        result.setCuresUpdate(results.get(0).getCuresUpdate());
        result.setId(results.get(0).getId());
        return result;
    }

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
