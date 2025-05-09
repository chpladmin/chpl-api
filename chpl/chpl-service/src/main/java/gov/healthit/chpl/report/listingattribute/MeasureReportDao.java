package gov.healthit.chpl.report.listingattribute;

import java.util.List;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.listing.measure.MeasureEntity;
import jakarta.persistence.Query;

@Repository
public class MeasureReportDao extends BaseDAOImpl {

    public List<MeasureReport> getMeasureReports() {
        String hql = "SELECT measure, count(*) as measureCount "
                + "FROM CertifiedProductDetailsEntity cpd, "
                + "ListingMeasureEntity listingMeasureMap, "
                + "MeasureEntity measure "
                + "WHERE cpd.id = listingMeasureMap.listingId "
                + "AND measure.id = listingMeasureMap.measureId "
                + "AND cpd.certificationStatusId IN (1,6,7) "
                + "AND listingMeasureMap.deleted = false "
                + "AND cpd.deleted = false "
                + "AND measure.deleted = false "
                + "GROUP BY measure.id";

        Query query = entityManager.createQuery(hql);
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(result -> MeasureReport.builder()
                        .measure(((MeasureEntity) result[0]).toDomain())
                        .count((Long) result[1])
                        .build())
                .toList();
    }

    public List<MeasureListingReport> getMeasureListingReports() {
        String hql = "SELECT measure, cpd.chplProductNumber "
                + "FROM CertifiedProductDetailsEntity cpd, "
                + "ListingMeasureEntity listingMeasureMap, "
                + "MeasureEntity measure "
                + "WHERE cpd.id = listingMeasureMap.listingId "
                + "AND measure.id = listingMeasureMap.measureId "
                + "AND cpd.certificationStatusId IN (1,6,7) "
                + "AND listingMeasureMap.deleted = false "
                + "AND cpd.deleted = false "
                + "AND measure.deleted = false ";

        Query query = entityManager.createQuery(hql);
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(result -> MeasureListingReport.builder()
                        .measure(((MeasureEntity) result[0]).toDomain())
                        .chplProductNumber((String) result[1])
                        .build())
                .toList();
    }

}
