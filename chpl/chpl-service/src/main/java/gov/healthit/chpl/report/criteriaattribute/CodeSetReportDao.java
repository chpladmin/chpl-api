package gov.healthit.chpl.report.criteriaattribute;

import java.util.List;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.certificationCriteria.CertificationCriterionEntity;
import gov.healthit.chpl.codeset.CodeSetEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import jakarta.persistence.Query;

@Repository
public class CodeSetReportDao extends BaseDAOImpl {
    public List<CodeSetReport> getCodeSetReports() {
        String hql = "SELECT cc, cs, count(*) as codeSetCount "
                + "FROM CertificationCriterionEntity cc, "
                + "CertificationResultEntity cr, "
                + "CertifiedProductDetailsEntity cpd, "
                + "CertificationResultCodeSetEntity crcs, "
                + "CodeSetEntity cs "
                + "WHERE cc.id = cr.certificationCriterionId "
                + "AND cr.certifiedProductId = cpd.id "
                + "AND cr.id = crcs.certificationResultId "
                + "AND crcs.codeSet.id = cs.id "
                + "AND cpd.certificationStatusId IN (1,6,7) "
                + "AND (cc.endDay is null OR cc.endDay > CURRENT_DATE()) "
                + "AND cc.deleted = false "
                + "AND cr.deleted = false "
                + "AND crcs.deleted = false "
                + "AND cpd.deleted = false "
                + "AND cs.deleted = false "
                + "GROUP BY cc.id, cs.id ";

        Query query = entityManager.createQuery(hql);
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(result -> CodeSetReport.builder()
                        .criterion(((CertificationCriterionEntity) result[0]).toDomain())
                        .codeSet(((CodeSetEntity) result[1]).toDomain())
                        .count((Long) result[2])
                        .build())
                .toList();
    }

    public List<CodeSetListingReport> getCodeSetListingReports() {
        String hql = "SELECT cc, cs, cpd.chplProductNumber "
                + "FROM CertificationCriterionEntity cc, "
                + "CertificationResultEntity cr, "
                + "CertifiedProductDetailsEntity cpd, "
                + "CertificationResultCodeSetEntity crcs, "
                + "CodeSetEntity cs "
                + "WHERE cc.id = cr.certificationCriterionId "
                + "AND cr.certifiedProductId = cpd.id "
                + "AND cr.id = crcs.certificationResultId "
                + "AND crcs.codeSet.id = cs.id "
                + "AND cpd.certificationStatusId IN (1,6,7) "
                + "AND cc.deleted = false "
                + "AND cr.deleted = false "
                + "AND crcs.deleted = false "
                + "AND cpd.deleted = false "
                + "AND cs.deleted = false ";

        Query query = entityManager.createQuery(hql);
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(result -> CodeSetListingReport.builder()
                        .criterion(((CertificationCriterionEntity) result[0]).toDomain())
                        .codeSet(((CodeSetEntity) result[1]).toDomain())
                        .chplProductNumber((String) result[2])
                        .build())
                .toList();
    }

}
