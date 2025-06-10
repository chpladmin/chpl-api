package gov.healthit.chpl.report.listingattribute;

import java.util.List;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.qmsStandard.QmsStandardEntity;
import jakarta.persistence.Query;

@Repository
public class QmsStandardReportDao extends BaseDAOImpl {

    public List<QmsStandardReport> getQmsStandardReports() {
        String hql = "SELECT qms, count(*) as qmsStandardCount "
                + "FROM CertifiedProductDetailsEntity cpd, "
                + "CertifiedProductQmsStandardEntity cpqms, "
                + "QmsStandardEntity qms "
                + "WHERE cpd.id = cpqms.certifiedProductId "
                + "AND qms.id = cpqms.qmsStandardId "
                + "AND cpd.certificationStatusId IN (1,6,7) "
                + "AND cpqms.deleted = false "
                + "AND cpd.deleted = false "
                + "AND qms.deleted = false "
                + "GROUP BY qms.id";

        Query query = entityManager.createQuery(hql);
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(result -> QmsStandardReport.builder()
                        .qmsStandard(((QmsStandardEntity) result[0]).toDomain())
                        .count((Long) result[1])
                        .build())
                .toList();
    }

    public List<QmsStandardListingReport> getQmsStandardListingReports() {
        String hql = "SELECT qms, cpd.chplProductNumber "
                + "FROM CertifiedProductDetailsEntity cpd, "
                + "CertifiedProductQmsStandardEntity cpqms, "
                + "QmsStandardEntity qms "
                + "WHERE cpd.id = cpqms.certifiedProductId "
                + "AND qms.id = cpqms.qmsStandardId "
                + "AND cpd.certificationStatusId IN (1,6,7) "
                + "AND cpqms.deleted = false "
                + "AND cpd.deleted = false "
                + "AND qms.deleted = false ";

        Query query = entityManager.createQuery(hql);
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(result -> QmsStandardListingReport.builder()
                        .qmsStandard(((QmsStandardEntity) result[0]).toDomain())
                        .chplProductNumber((String) result[1])
                        .build())
                .toList();
    }

}
