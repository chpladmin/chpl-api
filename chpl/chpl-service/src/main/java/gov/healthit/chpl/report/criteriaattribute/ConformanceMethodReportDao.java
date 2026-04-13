package gov.healthit.chpl.report.criteriaattribute;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.certificationCriteria.CertificationCriterionEntity;
import gov.healthit.chpl.conformanceMethod.entity.ConformanceMethodEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import jakarta.persistence.Query;

@Repository
public class ConformanceMethodReportDao extends BaseDAOImpl {
    private String unformattedListingDetailsUrl;

    @Autowired
    public ConformanceMethodReportDao(@Value("${chplUrlBegin}") String chplUrlBegin,
            @Value("${listingDetailsUrlPart}") String listingDetailsUrlPart) {
        this.unformattedListingDetailsUrl = chplUrlBegin + listingDetailsUrlPart;
    }

    public List<ConformanceMethodReport> getConformanceMethodReports() {
        String hql = "SELECT cc, cm, count(*) as conrmfanceMethodCount "
                + "FROM CertificationCriterionEntity cc, "
                + "CertificationResultEntity cr, "
                + "CertifiedProductDetailsEntity cpd, "
                + "CertificationResultConformanceMethodEntity crcm, "
                + "ConformanceMethodEntity cm "
                + "WHERE cc.id = cr.certificationCriterionId "
                + "AND cr.certifiedProductId = cpd.id "
                + "AND cr.id = crcm.certificationResultId "
                + "AND crcm.conformanceMethod.id = cm.id "
                + "AND cpd.certificationStatusId IN (1,6,7) "
                + "AND (cc.endDay is null OR cc.endDay > CURRENT_DATE()) "
                + "AND cc.deleted = false "
                + "AND cr.deleted = false "
                + "AND crcm.deleted = false "
                + "AND cpd.deleted = false "
                + "AND cm.deleted = false "
                + "GROUP BY cc.id, cm.id ";

        Query query = entityManager.createQuery(hql);
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(result -> ConformanceMethodReport.builder()
                        .criterion(((CertificationCriterionEntity) result[0]).toDomain())
                        .conformanceMethod(((ConformanceMethodEntity) result[1]).toDomain())
                        .count((Long) result[2])
                        .build())
                .toList();
    }

    public List<ConformanceMethodListingReport> getConformanceMethodListingReports() {
        String hql = "SELECT cc, cm, cpd.id, cpd.chplProductNumber "
                + "FROM CertificationCriterionEntity cc, "
                + "CertificationResultEntity cr, "
                + "CertifiedProductDetailsEntity cpd, "
                + "CertificationResultConformanceMethodEntity crcm, "
                + "ConformanceMethodEntity cm "
                + "WHERE cc.id = cr.certificationCriterionId "
                + "AND cr.certifiedProductId = cpd.id "
                + "AND cr.id = crcm.certificationResultId "
                + "AND crcm.conformanceMethod.id = cm.id "
                + "AND cpd.certificationStatusId IN (1,6,7) "
                + "AND cc.deleted = false "
                + "AND cr.deleted = false "
                + "AND crcm.deleted = false "
                + "AND cpd.deleted = false "
                + "AND cm.deleted = false ";

        Query query = entityManager.createQuery(hql);
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(result -> ConformanceMethodListingReport.builder()
                        .criterion(((CertificationCriterionEntity) result[0]).toDomain())
                        .conformanceMethod(((ConformanceMethodEntity) result[1]).toDomain())
                        .listingDetailsUrl(String.format(unformattedListingDetailsUrl, (Long) result[2]))
                        .chplProductNumber((String) result[3])
                        .build())
                .toList();
    }

}
