package gov.healthit.chpl.report.criteriaattribute;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.certificationCriteria.CertificationCriterionEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.testdata.TestDataEntity;
import jakarta.persistence.Query;

@Repository
public class TestDataReportDao extends BaseDAOImpl {

    private String unformattedListingDetailsUrl;

    @Autowired
    public TestDataReportDao(@Value("${chplUrlBegin}") String chplUrlBegin,
            @Value("${listingDetailsUrlPart}") String listingDetailsUrlPart) {
        this.unformattedListingDetailsUrl = chplUrlBegin + listingDetailsUrlPart;
    }

    public List<TestDataReport> getTestDataReports() {
        String hql = "SELECT cc, td, count(*) as testDataCount "
                + "FROM CertificationCriterionEntity cc, "
                + "CertificationResultEntity cr, "
                + "CertifiedProductDetailsEntity cpd, "
                + "CertificationResultTestDataEntity crtd, "
                + "TestDataEntity td "
                + "WHERE cc.id = cr.certificationCriterionId "
                + "AND cr.certifiedProductId = cpd.id "
                + "AND cr.id = crtd.certificationResultId "
                + "AND crtd.testData.id = td.id "
                + "AND cpd.certificationStatusId IN (1,6,7) "
                + "AND (cc.endDay is null OR cc.endDay > CURRENT_DATE()) "
                + "AND cc.deleted = false "
                + "AND cr.deleted = false "
                + "AND crtd.deleted = false "
                + "AND cpd.deleted = false "
                + "AND td.deleted = false "
                + "GROUP BY cc.id, td.id ";

        Query query = entityManager.createQuery(hql);
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(result -> TestDataReport.builder()
                        .criterion(((CertificationCriterionEntity) result[0]).toDomain())
                        .testData(((TestDataEntity) result[1]).toDomain())
                        .count((Long) result[2])
                        .build())
                .toList();
    }

    public List<TestDataListingReport> getTestDataListingReports() {
        String hql = "SELECT cc, td, cpd.id, cpd.chplProductNumber "
                + "FROM CertificationCriterionEntity cc, "
                + "CertificationResultEntity cr, "
                + "CertifiedProductDetailsEntity cpd, "
                + "CertificationResultTestDataEntity crtd, "
                + "TestDataEntity td "
                + "WHERE cc.id = cr.certificationCriterionId "
                + "AND cr.certifiedProductId = cpd.id "
                + "AND cr.id = crtd.certificationResultId "
                + "AND crtd.testData.id = td.id "
                + "AND cpd.certificationStatusId IN (1,6,7) "
                + "AND (cc.endDay is null OR cc.endDay > CURRENT_DATE()) "
                + "AND cc.deleted = false "
                + "AND cr.deleted = false "
                + "AND crtd.deleted = false "
                + "AND cpd.deleted = false "
                + "AND td.deleted = false ";

        Query query = entityManager.createQuery(hql);
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(result -> TestDataListingReport.builder()
                        .criterion(((CertificationCriterionEntity) result[0]).toDomain())
                        .testData(((TestDataEntity) result[1]).toDomain())
                        .listingDetailsUrl(String.format(unformattedListingDetailsUrl, (Long) result[2]))
                        .chplProductNumber((String) result[3])
                        .build())
                .toList();
    }

}
