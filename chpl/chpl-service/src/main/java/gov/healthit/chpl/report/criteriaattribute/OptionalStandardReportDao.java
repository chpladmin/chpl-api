package gov.healthit.chpl.report.criteriaattribute;

import java.util.List;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.certificationCriteria.CertificationCriterionEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.optionalStandard.entity.OptionalStandardEntity;
import jakarta.persistence.Query;

@Repository
public class OptionalStandardReportDao extends BaseDAOImpl {
    public List<OptionalStandardReport> getOptionalStandardReports() {
        String hql = "SELECT cc, os, count(*) as optionalStandardCount "
                + "FROM CertificationCriterionEntity cc, "
                + "CertificationResultEntity cr, "
                + "CertifiedProductDetailsEntity cpd, "
                + "CertificationResultOptionalStandardEntity cros, "
                + "OptionalStandardEntity os "
                + "WHERE cc.id = cr.certificationCriterionId "
                + "AND cr.certifiedProductId = cpd.id "
                + "AND cr.id = cros.certificationResultId "
                + "AND cros.optionalStandard.id = os.id "
                + "AND cpd.certificationStatusId IN (1,6,7) "
                + "AND (cc.endDay is null OR cc.endDay > CURRENT_DATE()) "
                + "AND cc.deleted = false "
                + "AND cr.deleted = false "
                + "AND cros.deleted = false "
                + "AND cpd.deleted = false "
                + "AND os.deleted = false "
                + "GROUP BY cc.id, os.id ";

        Query query = entityManager.createQuery(hql);
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(result -> OptionalStandardReport.builder()
                        .criterion(((CertificationCriterionEntity) result[0]).toDomain())
                        .optionalStandard(((OptionalStandardEntity) result[1]).toDomain())
                        .count((Long) result[2])
                        .build())
                .toList();
    }

    public List<OptionalStandardListingReport> getOptionalStandardListingReports() {
        String hql = "SELECT cc, os, cpd.chplProductNumber "
                + "FROM CertificationCriterionEntity cc, "
                + "CertificationResultEntity cr, "
                + "CertifiedProductDetailsEntity cpd, "
                + "CertificationResultOptionalStandardEntity cros, "
                + "OptionalStandardEntity os "
                + "WHERE cc.id = cr.certificationCriterionId "
                + "AND cr.certifiedProductId = cpd.id "
                + "AND cr.id = cros.certificationResultId "
                + "AND cros.optionalStandard.id = os.id "
                + "AND cpd.certificationStatusId IN (1,6,7) "
                + "AND cc.deleted = false "
                + "AND cr.deleted = false "
                + "AND cros.deleted = false "
                + "AND cpd.deleted = false "
                + "AND os.deleted = false ";

        Query query = entityManager.createQuery(hql);
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(result -> OptionalStandardListingReport.builder()
                        .criterion(((CertificationCriterionEntity) result[0]).toDomain())
                        .optionalStandard(((OptionalStandardEntity) result[1]).toDomain())
                        .chplProductNumber((String) result[2])
                        .build())
                .toList();
    }

}
