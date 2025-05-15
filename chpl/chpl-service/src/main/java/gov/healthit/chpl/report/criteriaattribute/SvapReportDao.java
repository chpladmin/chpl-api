package gov.healthit.chpl.report.criteriaattribute;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.certificationCriteria.CertificationCriterionEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.report.svap.CriteriaWithAnySvap;
import gov.healthit.chpl.report.svap.CriterionCount;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.svap.entity.SvapEntity;
import jakarta.persistence.Query;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository
public class SvapReportDao extends BaseDAOImpl {
    private CertificationCriterionService certificationCriterionService;

    @Autowired
    public SvapReportDao(CertificationCriterionService certificationCriterionService) {
        this.certificationCriterionService = certificationCriterionService;
    }

    public List<SvapReport> getSvapReports() {
        String hql = "SELECT cc, s, count(*) as svapCount "
                + "FROM CertificationCriterionEntity cc, "
                + "CertificationResultEntity cr, "
                + "CertifiedProductDetailsEntity cpd, "
                + "CertificationResultSvapEntity crs, "
                + "SvapEntity s "
                + "WHERE cc.id = cr.certificationCriterionId "
                + "AND cr.certifiedProductId = cpd.id "
                + "AND cr.id = crs.certificationResultId "
                + "AND crs.svap.id = s.id "
                + "AND cpd.certificationStatusId IN (1,6,7) "
                + "AND (cc.endDay is null OR cc.endDay > CURRENT_DATE()) "
                + "AND cc.deleted = false "
                + "AND cr.deleted = false "
                + "AND crs.deleted = false "
                + "AND cpd.deleted = false "
                + "AND s.deleted = false "
                + "GROUP BY cc.id, s.id ";

        Query query = entityManager.createQuery(hql);
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(result -> SvapReport.builder()
                        .criterion(((CertificationCriterionEntity) result[0]).toDomain())
                        .svap(((SvapEntity) result[1]).toDomain())
                        .count((Long) result[2])
                        .build())
                .toList();
    }

    public List<SvapReportByCertificationStatus> getSvapReports(CertificationStatus certificationStatus) {
        String hql = "SELECT cc, s, count(*) as svapCount "
                + "FROM CertificationCriterionEntity cc, "
                + "CertificationResultEntity cr, "
                + "CertifiedProductDetailsEntity cpd, "
                + "CertificationResultSvapEntity crs, "
                + "SvapEntity s "
                + "WHERE cc.id = cr.certificationCriterionId "
                + "AND cr.certifiedProductId = cpd.id "
                + "AND cr.id = crs.certificationResultId "
                + "AND crs.svap.id = s.id "
                + "AND cpd.certificationStatusId = :certificationStatusId "
                + "AND (cc.endDay is null OR cc.endDay > CURRENT_DATE()) "
                + "AND cc.deleted = false "
                + "AND cr.deleted = false "
                + "AND crs.deleted = false "
                + "AND cpd.deleted = false "
                + "AND s.deleted = false "
                + "GROUP BY cc.id, s.id ";

        Query query = entityManager.createQuery(hql);
        query.setParameter("cerificationStatusId", certificationStatus.getId());
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(result -> SvapReportByCertificationStatus.builder()
                        .criterion(((CertificationCriterionEntity) result[0]).toDomain())
                        .svap(((SvapEntity) result[1]).toDomain())
                        .certificationStatus(certificationStatus.getName())
                        .count((Long) result[2])
                        .build())
                .toList();
    }

    public List<SvapListingReport> getSvapListingReports() {
        String hql = "SELECT cc, s, cpd.chplProductNumber, cpd.certificationStatusName "
                + "FROM CertificationCriterionEntity cc, "
                + "CertificationResultEntity cr, "
                + "CertifiedProductDetailsEntity cpd, "
                + "CertificationResultSvapEntity crs, "
                + "SvapEntity s "
                + "WHERE cc.id = cr.certificationCriterionId "
                + "AND cr.certifiedProductId = cpd.id "
                + "AND cr.id = crs.certificationResultId "
                + "AND crs.svap.id = s.id "
                + "AND cc.deleted = false "
                + "AND cr.deleted = false "
                + "AND crs.deleted = false "
                + "AND cpd.deleted = false "
                + "AND s.deleted = false ";

        Query query = entityManager.createQuery(hql);
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(result -> SvapListingReport.builder()
                        .criterion(((CertificationCriterionEntity) result[0]).toDomain())
                        .svap(((SvapEntity) result[1]).toDomain())
                        .chplProductNumber((String) result[2])
                        .certificationStatus((String) result[3])
                        .build())
                .toList();
    }

    public List<CriteriaWithAnySvap> getCriteriaWithAnySvap() {
        String criteriaCountsWithSvapHql = "SELECT cc, count(*) as criteriaCount "
                + "FROM CertificationCriterionEntity cc, "
                + "CertificationResultEntity cr, "
                + "CertifiedProductDetailsEntity cpd, "
                + "CertificationResultSvapEntity crs, "
                + "CertificationCriterionAttributeEntity cca "
                + "WHERE cc.id = cr.certificationCriterionId "
                + "AND cr.certifiedProductId = cpd.id "
                + "AND cr.id = crs.certificationResultId "
                + "AND cc.id = cca.criterion.id "
                + "AND cpd.certificationStatusId IN (1,6,7) "
                + "AND (cc.endDay is null OR cc.endDay > CURRENT_DATE()) "
                + "AND cca.svap = true "
                + "AND cc.deleted = false "
                + "AND cr.deleted = false "
                + "AND crs.deleted = false "
                + "AND cpd.deleted = false "
                + "GROUP BY cc.id";

        Query criteriaWithAnySvapCountsQuery = entityManager.createQuery(criteriaCountsWithSvapHql);
        List<Object[]> criteriaWithAnySvapCountsResults = criteriaWithAnySvapCountsQuery.getResultList();

        List<CriterionCount> criteriaWithAnySvapCounts = criteriaWithAnySvapCountsResults.stream()
                .map(result -> CriterionCount.builder()
                        .criterion(((CertificationCriterionEntity) result[0]).toDomain())
                        .count((Long) result[1])
                        .build())
                .toList();

        String criteriaCountsHql = "SELECT cc, count(*) as criteriaCount "
                + "FROM CertificationCriterionEntity cc, "
                + "CertificationResultEntity cr, "
                + "CertifiedProductDetailsEntity cpd, "
                + "CertificationCriterionAttributeEntity cca "
                + "WHERE cc.id = cr.certificationCriterionId "
                + "AND cr.certifiedProductId = cpd.id "
                + "AND cc.id = cca.criterion.id "
                + "AND cpd.certificationStatusId IN (1,6,7) "
                + "AND (cc.endDay is null OR cc.endDay > CURRENT_DATE()) "
                + "AND cca.svap = true "
                + "AND cc.deleted = false "
                + "AND cr.deleted = false "
                + "AND cpd.deleted = false "
                + "GROUP BY cc.id";

        Query criteriaCountsQuery = entityManager.createQuery(criteriaCountsHql);
        List<Object[]> criteriaCountsResults = criteriaCountsQuery.getResultList();

        List<CriterionCount> criteriaCounts = criteriaCountsResults.stream()
                .map(result -> CriterionCount.builder()
                        .criterion(((CertificationCriterionEntity) result[0]).toDomain())
                        .count((Long) result[1])
                        .build())
                .toList();

        return criteriaCounts.stream()
                .map(cc -> CriteriaWithAnySvap.builder()
                        .certificationCriterion(cc.getCriterion())
                        .activeListingCountAttestingToCriteria(cc.getCount())
                        .activeListingCountAttestingToCriteriaAndAnySvap(lookupCountByCriteria(criteriaWithAnySvapCounts, cc.getCriterion()))
                        .sortOrder(certificationCriterionService.getCertificationResultSortIndex(cc.getCriterion().getId()))
                        .build())
                .peek(x -> LOGGER.info(x.toString()))
                .toList();
    }

    private Long lookupCountByCriteria(List<CriterionCount> criteriaCounts, CertificationCriterion criterion) {
        Optional<CriterionCount> criterionCount = criteriaCounts.stream()
                .filter(cc -> cc.getCriterion().getId().equals(criterion.getId()))
                .findAny();

        if (criterionCount.isPresent()) {
            return criterionCount.get().getCount();
        } else {
            return 0L;
        }
    }
}
