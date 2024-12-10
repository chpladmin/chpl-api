package gov.healthit.chpl.report.criteriaattribute;

import java.util.List;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.certificationCriteria.CertificationCriterionEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import jakarta.persistence.Query;

@Repository
public class PrivacyAndSecurityFrameworkReportDao extends BaseDAOImpl {
    public List<PrivacyAndSecurityFrameworkReport> getPrivacyAndSecurityFrameworkReports() {
        String hql = "SELECT cc, cr.privacySecurityFramework, count(*) as privacyAndSecurityFrameworkCount "
                + "FROM CertificationCriterionEntity cc, "
                + "CertificationResultEntity cr, "
                + "CertifiedProductDetailsEntity cpd "
                + "WHERE cc.id = cr.certificationCriterionId "
                + "AND cr.certifiedProductId = cpd.id "
                + "AND cpd.certificationStatusId IN (1,6,7) "
                + "AND (cc.endDay is null OR cc.endDay > CURRENT_DATE()) "
                + "AND cc.deleted = false "
                + "AND cr.deleted = false "
                + "AND cpd.deleted = false "
                + "GROUP BY cc.id, cr.privacySecurityFramework ";

        Query query = entityManager.createQuery(hql);
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(result -> PrivacyAndSecurityFrameworkReport.builder()
                        .criterion(((CertificationCriterionEntity) result[0]).toDomain())
                        .privacyAndSecurityFramework((String) result[1])
                        .count((Long) result[2])
                        .build())
                .toList();

    }

    public List<PrivacyAndSecurityFrameworkListingReport> getPrivacyAndSecurityFrameworkListingReports() {
        String hql = "SELECT cc, cr.privacySecurityFramework, cpd.chplProductNumber "
                + "FROM CertificationCriterionEntity cc, "
                + "CertificationResultEntity cr, "
                + "CertifiedProductDetailsEntity cpd "
                + "WHERE cc.id = cr.certificationCriterionId "
                + "AND cr.certifiedProductId = cpd.id "
                + "AND cpd.certificationStatusId IN (1,6,7) "
                + "AND cc.deleted = false "
                + "AND cr.deleted = false "
                + "AND cpd.deleted = false ";

        Query query = entityManager.createQuery(hql);
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(result -> PrivacyAndSecurityFrameworkListingReport.builder()
                        .criterion(((CertificationCriterionEntity) result[0]).toDomain())
                        .privacyAndSecurityFramework((String) result[1])
                        .chplProductNumber((String) result[2])
                        .build())
                .toList();
    }
}
