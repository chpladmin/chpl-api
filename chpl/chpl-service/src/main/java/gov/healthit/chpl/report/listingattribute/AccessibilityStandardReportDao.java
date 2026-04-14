package gov.healthit.chpl.report.listingattribute;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.accessibilityStandard.AccessibilityStandardEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import jakarta.persistence.Query;

@Repository
public class AccessibilityStandardReportDao extends BaseDAOImpl {
    private String unformattedListingDetailsUrl;

    @Autowired
    public AccessibilityStandardReportDao(@Value("${chplUrlBegin}") String chplUrlBegin,
            @Value("${listingDetailsUrlPart}") String listingDetailsUrlPart) {
        this.unformattedListingDetailsUrl = chplUrlBegin + listingDetailsUrlPart;
    }

    public List<AccessibilityStandardReport> getAccessibilityStandardReports() {
        String hql = "SELECT accStd, count(*) as accStandardCount "
                + "FROM CertifiedProductDetailsEntity cpd, "
                + "CertifiedProductAccessibilityStandardEntity cpAccStd, "
                + "AccessibilityStandardEntity accStd "
                + "WHERE cpd.id = cpAccStd.certifiedProductId "
                + "AND accStd.id = cpAccStd.accessibilityStandardId "
                + "AND cpd.certificationStatusId IN (1,6,7) "
                + "AND cpAccStd.deleted = false "
                + "AND cpd.deleted = false "
                + "AND accStd.deleted = false "
                + "GROUP BY accStd.id";

        Query query = entityManager.createQuery(hql);
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(result -> AccessibilityStandardReport.builder()
                        .accessibilityStandard(((AccessibilityStandardEntity) result[0]).toDomain())
                        .count((Long) result[1])
                        .build())
                .toList();
    }

    public List<AccessibilityStandardListingReport> getAccessibilityStandardListingReports() {
        String hql = "SELECT accStd, cpd.id, cpd.chplProductNumber "
                + "FROM CertifiedProductDetailsEntity cpd, "
                + "CertifiedProductAccessibilityStandardEntity cpAccStd, "
                + "AccessibilityStandardEntity accStd "
                + "WHERE cpd.id = cpAccStd.certifiedProductId "
                + "AND accStd.id = cpAccStd.accessibilityStandardId "
                + "AND cpd.certificationStatusId IN (1,6,7) "
                + "AND cpAccStd.deleted = false "
                + "AND cpd.deleted = false "
                + "AND accStd.deleted = false ";

        Query query = entityManager.createQuery(hql);
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(result -> AccessibilityStandardListingReport.builder()
                        .accessibilityStandard(((AccessibilityStandardEntity) result[0]).toDomain())
                        .listingDetailsUrl(String.format(unformattedListingDetailsUrl, (Long) result[1]))
                        .chplProductNumber((String) result[2])
                        .build())
                .toList();
    }

}
