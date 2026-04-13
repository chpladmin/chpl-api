package gov.healthit.chpl.report.listingattribute;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.listing.measure.MeasureEntity;
import jakarta.persistence.Query;

@Repository
public class MeasureReportDao extends BaseDAOImpl {
    private String unformattedListingDetailsUrl;

    @Autowired
    public MeasureReportDao(@Value("${chplUrlBegin}") String chplUrlBegin,
            @Value("${listingDetailsUrlPart}") String listingDetailsUrlPart) {
        this.unformattedListingDetailsUrl = chplUrlBegin + listingDetailsUrlPart;
    }

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
                        .measure(((MeasureEntity) result[0]).toSimpleMeasure())
                        .count((Long) result[1])
                        .build())
                .toList();
    }

    public List<MeasureListingReport> getMeasureListingReports() {
        String hql = "SELECT measure, cpd.id, cpd.chplProductNumber "
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
                        .measure(((MeasureEntity) result[0]).toSimpleMeasure())
                        .listingDetailsUrl(String.format(unformattedListingDetailsUrl, (Long) result[1]))
                        .chplProductNumber((String) result[2])
                        .build())
                .toList();
    }

}
