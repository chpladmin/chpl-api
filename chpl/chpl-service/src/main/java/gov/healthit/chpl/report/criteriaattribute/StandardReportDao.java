package gov.healthit.chpl.report.criteriaattribute;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.certificationCriteria.CertificationCriterionEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.standard.StandardEntity;
import jakarta.persistence.Query;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository
public class StandardReportDao extends BaseDAOImpl {

    private String unformattedListingDetailsUrl;

    @Autowired
    public StandardReportDao(@Value("${chplUrlBegin}") String chplUrlBegin,
            @Value("${listingDetailsUrlPart}") String listingDetailsUrlPart) {
        this.unformattedListingDetailsUrl = chplUrlBegin + listingDetailsUrlPart;
    }

    public List<StandardReport> getStandardReports() {
        String hql = "SELECT cc, s, count(*) as standardCount "
                + "FROM CertificationCriterionEntity cc, "
                + "CertificationResultEntity cr, "
                + "CertifiedProductDetailsEntity cpd, "
                + "CertificationResultStandardEntity crs, "
                + "StandardEntity s "
                + "WHERE cc.id = cr.certificationCriterionId "
                + "AND cr.certifiedProductId = cpd.id "
                + "AND cr.id = crs.certificationResultId "
                + "AND crs.standard.id = s.id "
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
                .map(result -> StandardReport.builder()
                        .criterion(((CertificationCriterionEntity) result[0]).toDomain())
                        .standard(((StandardEntity) result[1]).toDomain())
                        .count((Long) result[2])
                        .build())
                .toList();
    }

    public List<StandardListingReport> getStandardListingReports() {
        String hql = "SELECT cc, s, cpd.id, cpd.chplProductNumber "
                + "FROM CertificationCriterionEntity cc, "
                + "CertificationResultEntity cr, "
                + "CertifiedProductDetailsEntity cpd, "
                + "CertificationResultStandardEntity crs, "
                + "StandardEntity s "
                + "WHERE cc.id = cr.certificationCriterionId "
                + "AND cr.certifiedProductId = cpd.id "
                + "AND cr.id = crs.certificationResultId "
                + "AND crs.standard.id = s.id "
                + "AND cpd.certificationStatusId IN (1,6,7) "
                + "AND cc.deleted = false "
                + "AND cr.deleted = false "
                + "AND crs.deleted = false "
                + "AND cpd.deleted = false ";

        Query query = entityManager.createQuery(hql);
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(result -> StandardListingReport.builder()
                        .criterion(((CertificationCriterionEntity) result[0]).toDomain())
                        .standard(((StandardEntity) result[1]).toDomain())
                        .listingDetailsUrl(String.format(unformattedListingDetailsUrl, (Long) result[2]))
                        .chplProductNumber((String) result[3])
                        .build())
                .toList();
    }
}
