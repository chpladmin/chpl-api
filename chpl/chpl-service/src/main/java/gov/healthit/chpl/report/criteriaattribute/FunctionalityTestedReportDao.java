package gov.healthit.chpl.report.criteriaattribute;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.certificationCriteria.CertificationCriterionEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.functionalitytested.FunctionalityTestedEntity;
import jakarta.persistence.Query;

@Repository
public class FunctionalityTestedReportDao extends BaseDAOImpl {

    private String unformattedListingDetailsUrl;

    @Autowired
    public FunctionalityTestedReportDao(@Value("${chplUrlBegin}") String chplUrlBegin,
            @Value("${listingDetailsUrlPart}") String listingDetailsUrlPart) {
        this.unformattedListingDetailsUrl = chplUrlBegin + listingDetailsUrlPart;
    }

    public List<FunctionalityTestedReport> getFunctionalityTestedReports() {
        String hql = "SELECT cc, ft, count(*) as functionalityTestedCount "
                + "FROM CertificationCriterionEntity cc, "
                + "CertificationResultEntity cr, "
                + "CertifiedProductDetailsEntity cpd, "
                + "CertificationResultFunctionalityTestedEntity crft, "
                + "FunctionalityTestedEntity ft "
                + "WHERE cc.id = cr.certificationCriterionId "
                + "AND cr.certifiedProductId = cpd.id "
                + "AND cr.id = crft.certificationResultId "
                + "AND crft.functionalityTested.id = ft.id "
                + "AND cpd.certificationStatusId IN (1,6,7) "
                + "AND (cc.endDay is null OR cc.endDay > CURRENT_DATE()) "
                + "AND cc.deleted = false "
                + "AND cr.deleted = false "
                + "AND crft.deleted = false "
                + "AND cpd.deleted = false "
                + "AND ft.deleted = false "
                + "GROUP BY cc.id, ft.id ";

        Query query = entityManager.createQuery(hql);
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(result -> FunctionalityTestedReport.builder()
                        .criterion(((CertificationCriterionEntity) result[0]).toDomain())
                        .functionalityTested(((FunctionalityTestedEntity) result[1]).toDomain())
                        .count((Long) result[2])
                        .build())
                .toList();
    }

    public List<FunctionalityTestedListingReport> getFunctionalityTestedListingReports() {
        String hql = "SELECT cc, ft, cpd.id, cpd.chplProductNumber "
                + "FROM CertificationCriterionEntity cc, "
                + "CertificationResultEntity cr, "
                + "CertifiedProductDetailsEntity cpd, "
                + "CertificationResultFunctionalityTestedEntity crft, "
                + "FunctionalityTestedEntity ft "
                + "WHERE cc.id = cr.certificationCriterionId "
                + "AND cr.certifiedProductId = cpd.id "
                + "AND cr.id = crft.certificationResultId "
                + "AND crft.functionalityTested.id = ft.id "
                + "AND cpd.certificationStatusId IN (1,6,7) "
                + "AND cc.deleted = false "
                + "AND cr.deleted = false "
                + "AND crft.deleted = false "
                + "AND cpd.deleted = false "
                + "AND ft.deleted = false ";

        Query query = entityManager.createQuery(hql);
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(result -> FunctionalityTestedListingReport.builder()
                        .criterion(((CertificationCriterionEntity) result[0]).toDomain())
                        .functionalityTested(((FunctionalityTestedEntity) result[1]).toDomain())
                        .listingDetailsUrl(String.format(unformattedListingDetailsUrl, (Long) result[2]))
                        .chplProductNumber((String) result[3])
                        .build())
                .toList();
    }

}
