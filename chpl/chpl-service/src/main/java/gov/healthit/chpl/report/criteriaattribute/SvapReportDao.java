package gov.healthit.chpl.report.criteriaattribute;

import java.util.List;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.certificationCriteria.CertificationCriterionEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.svap.entity.SvapEntity;
import jakarta.persistence.Query;

@Repository
public class SvapReportDao extends BaseDAOImpl {

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

    public List<SvapListingReport> getSvapListingReports() {
        String hql = "SELECT cc, s, cpd.chplProductNumber "
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
                        .build())
                .toList();
    }

}
