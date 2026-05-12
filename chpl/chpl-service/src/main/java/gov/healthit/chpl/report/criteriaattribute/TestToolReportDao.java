package gov.healthit.chpl.report.criteriaattribute;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.certificationCriteria.CertificationCriterionEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.testtool.TestToolEntity;
import jakarta.persistence.Query;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository
public class TestToolReportDao extends BaseDAOImpl {

    private String unformattedListingDetailsUrl;

    @Autowired
    public TestToolReportDao(@Value("${chplUrlBegin}") String chplUrlBegin,
            @Value("${listingDetailsUrlPart}") String listingDetailsUrlPart) {
        this.unformattedListingDetailsUrl = chplUrlBegin + listingDetailsUrlPart;
    }

    public List<TestToolReport> getTestToolReports() {
        String hql = "SELECT cc, tt, count(*) as testToolCount "
                + "FROM CertificationCriterionEntity cc, "
                + "CertificationResultEntity cr, "
                + "CertifiedProductDetailsEntity cpd, "
                + "CertificationResultTestToolEntity crtt, "
                + "TestToolEntity tt "
                + "WHERE cc.id = cr.certificationCriterionId "
                + "AND cr.certifiedProductId = cpd.id "
                + "AND cr.id = crtt.certificationResultId "
                + "AND crtt.testTool.id = tt.id "
                + "AND cpd.certificationStatusId IN (1,6,7) "
                + "AND (cc.endDay is null OR cc.endDay > CURRENT_DATE()) "
                + "AND cc.deleted = false "
                + "AND cr.deleted = false "
                + "AND crtt.deleted = false "
                + "AND cpd.deleted = false "
                + "AND tt.deleted = false "
                + "GROUP BY cc.id, tt.id ";

        Query query = entityManager.createQuery(hql);
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(result -> TestToolReport.builder()
                        .criterion(((CertificationCriterionEntity) result[0]).toDomain())
                        .testTool(((TestToolEntity) result[1]).toDomain())
                        .count((Long) result[2])
                        .build())
                .toList();
    }

    public List<TestToolListingReport> getTestToolListingReports() {
        String hql = "SELECT cc, tt, cpd.id, cpd.chplProductNumber "
                + "FROM CertificationCriterionEntity cc, "
                + "CertificationResultEntity cr, "
                + "CertifiedProductDetailsEntity cpd, "
                + "CertificationResultTestToolEntity crtt, "
                + "TestToolEntity tt "
                + "WHERE cc.id = cr.certificationCriterionId "
                + "AND cr.certifiedProductId = cpd.id "
                + "AND cr.id = crtt.certificationResultId "
                + "AND crtt.testTool.id = tt.id "
                + "AND cpd.certificationStatusId IN (1,6,7) "
                + "AND cc.deleted = false "
                + "AND cr.deleted = false "
                + "AND crtt.deleted = false "
                + "AND cpd.deleted = false ";

        Query query = entityManager.createQuery(hql);
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(result -> TestToolListingReport.builder()
                        .criterion(((CertificationCriterionEntity) result[0]).toDomain())
                        .testTool(((TestToolEntity) result[1]).toDomain())
                        .listingDetailsUrl(String.format(unformattedListingDetailsUrl, (Long) result[2]))
                        .chplProductNumber((String) result[3])
                        .build())
                .toList();
    }

}
