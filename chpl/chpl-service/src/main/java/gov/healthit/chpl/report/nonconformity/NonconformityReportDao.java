package gov.healthit.chpl.report.nonconformity;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.compliance.surveillance.entity.NonconformityTypeEntity;
import gov.healthit.chpl.compliance.surveillance.entity.SurveillanceNonconformityEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.search.entity.ListingSearchEntity;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.CertificationStatusUtil;
import jakarta.persistence.Query;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository
public class NonconformityReportDao extends BaseDAOImpl {

    private CertificationCriterionService certificationCriterionService;

    @Autowired
    public NonconformityReportDao(CertificationCriterionService certificationCriterionService) {
        this.certificationCriterionService = certificationCriterionService;
    }

    public List<NonconformityTypeCount> getNonconformityCounts() {
        List<SurveillanceNonconformityEntity> allNonconformities = entityManager.createQuery(
                "SELECT sne "
                + "FROM SurveillanceNonconformityEntity sne "
                + "JOIN FETCH sne.type ncType "
                + "WHERE sne.deleted = false ", SurveillanceNonconformityEntity.class)
                .getResultList();

        List<NonconformityTypeEntity> nonconformityTypes = entityManager.createQuery(
                "FROM NonconformityTypeEntity e ", NonconformityTypeEntity.class)
                .getResultList();

        return nonconformityTypes.stream()
            .map(ncType ->
                NonconformityTypeCount.builder()
                        .count(allNonconformities.stream()
                                .filter(nc -> nc.getType().getId().equals(ncType.getId()))
                                .count())
                        .nonconformityType(ncType.toDomain())
                        .displayOrder(certificationCriterionService.getCertificationResultSortIndex(ncType.getId()))
                        .build())
            .filter(ncCount -> !ncCount.getCount().equals(0L))
            .toList();
    }

    public List<NonconformitiesByDeveloperAndType> getNonconformitiesByDeveloperAndType() {
                String hql = "SELECT DISTINCT surv, nc, ncType, listing "
                        + "FROM SurveillanceEntity surv, ListingSearchEntity listing "
                        + "JOIN FETCH surv.surveillanceType "
                        + "JOIN FETCH surv.surveilledRequirements reqs "
                        + "JOIN FETCH reqs.nonconformities nc "
                        + "JOIN FETCH nc.type ncType "
                        + "LEFT JOIN FETCH ncType.certificationEdition "
                        + "WHERE surv.certifiedProductId = listing.id "
                        + "AND listing.certificationStatus IN (:activeStatuses) "
                        + "AND surv.deleted <> true "
                        + "AND reqs.deleted <> true "
                        + "AND nc.deleted <> true ";
                Query query = entityManager.createQuery(hql);
                query.setParameter("activeStatuses", CertificationStatusUtil.getActiveStatusNames());
                List<Object[]> results = query.getResultList();

                AtomicInteger id = new AtomicInteger(0);
                return results.stream()
                        .map(result -> NonconformitiesByDeveloperAndType.builder()
                                .id(id.addAndGet(1))
                                .developerId(((ListingSearchEntity) result[3]).getDeveloperId())
                                .developerName(((ListingSearchEntity) result[3]).getDeveloper())
                                .listingId(((ListingSearchEntity) result[3]).getId())
                                .chplProductNumber(((ListingSearchEntity) result[3]).getChplProductNumber())
                                .nonconformityClassification(((NonconformityTypeEntity) result[2]).getClassification())
                                .nonconformityTypeName(((NonconformityTypeEntity) result[2]).toDomain().getFormattedTitleForReport())
                                .nonconformityCloseDay(((SurveillanceNonconformityEntity) result[1]).getNonconformityCloseDate())
                                .nonconformityOpenDay(((SurveillanceNonconformityEntity) result[1]).getDateOfDetermination())
                                .build())
                        .toList();
    }
}
