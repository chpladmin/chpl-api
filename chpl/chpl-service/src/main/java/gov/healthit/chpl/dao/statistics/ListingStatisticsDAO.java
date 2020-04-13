package gov.healthit.chpl.dao.statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.domain.statistics.CertifiedBodyAltTestStatistics;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;

@Repository("listingStatisticsDAO")
public class ListingStatisticsDAO extends BaseDAOImpl {

    public Long getTotalUniqueProductsByEditionAndStatus(final DateRange dateRange,
            final String edition, final List<String> statuses) {
        String hql = "SELECT DISTINCT UPPER(productName) || UPPER(developerName) "
                + "FROM CertifiedProductSummaryEntity ";

        boolean hasWhere = false;
        if (edition != null) {
            hql += " WHERE year = :edition ";
            hasWhere = true;
        }
        if (statuses != null && statuses.size() > 0) {
            if (!hasWhere) {
                hql += " WHERE ";
                hasWhere = true;
            } else {
                hql += " AND ";
            }
            hql += " UPPER(certificationStatus) IN (:statuses) ";
        }
        if (dateRange == null) {
            if (!hasWhere) {
                hql += " WHERE ";
                hasWhere = true;
            } else {
                hql += " AND ";
            }
            hql += " deleted = false ";
        } else {
            if (!hasWhere) {
                hql += " WHERE ";
                hasWhere = true;
            } else {
                hql += " AND ";
            }
            hql += "((deleted = false AND creationDate <= :endDate) "
                    + " OR "
                    + "(deleted = true AND creationDate <= :endDate AND lastModifiedDate > :endDate)) ";
        }

        Query query = entityManager.createQuery(hql);
        if (edition != null) {
            query.setParameter("edition", edition);
        }
        if (statuses != null && statuses.size() > 0) {
            query.setParameter("statuses", statuses);
        }
        if (dateRange != null) {
            query.setParameter("endDate", dateRange.getEndDate());
        }
        return (long) query.getResultList().size();
    }

    public List<CertifiedBodyStatistics> getTotalCPListingsEachYearByCertifiedBody(final DateRange dateRange) {
        String sql = "SELECT t.certification_body_name, t.year, count(DISTINCT t.products) "
                + "FROM( "
                + "SELECT DISTINCT certification_body_name, year, CONCAT(UPPER(product_Name), "
                + "UPPER(vendor_Name)) AS products "
                + "FROM " + SCHEMA_NAME + ".certified_product_details "
                + "WHERE ";
        if (dateRange == null) {
            sql += " deleted = false ";
        } else {
            sql += " ((deleted = false AND creation_date <= :endDate) "
                    + " OR "
                    + "(deleted = true AND creation_date <= :endDate AND last_modified_date > :startDate)) ";
        }
        sql += ") t "
                + " GROUP BY certification_body_name, year "
                + " ORDER BY t.certification_body_name ";

        Query query = entityManager.createNativeQuery(sql);

        if (dateRange != null) {
            query.setParameter("startDate", dateRange.getStartDate());
            query.setParameter("endDate", dateRange.getEndDate());
        }

        List<Object[]> results = query.getResultList();
        List<CertifiedBodyStatistics> cbStats = new ArrayList<CertifiedBodyStatistics>();
        for (Object[] obj : results) {
            CertifiedBodyStatistics stat = new CertifiedBodyStatistics();
            stat.setName(obj[0].toString());
            stat.setYear(Integer.valueOf(obj[1].toString()));
            stat.setTotalListings(Long.valueOf(obj[2].toString()));
            stat.setCertificationStatusName(null);
            cbStats.add(stat);
        }
        return cbStats;
    }

    public List<CertifiedBodyStatistics> getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(
            final DateRange dateRange) {
        String sql = "SELECT t.certification_body_name, t.year, count(DISTINCT t.products), "
                + "t.certification_status_name "
                + "FROM( "
                + "SELECT DISTINCT certification_body_name, year, CONCAT(UPPER(product_Name), "
                + "UPPER(vendor_Name)) AS products, certification_status_name "
                + "FROM " + SCHEMA_NAME + ".certified_product_details "
                + "WHERE ";
        if (dateRange == null) {
            sql += " deleted = false ";
        } else {
            sql += " ((deleted = false AND creation_date <= :endDate) "
                    + " OR "
                    + "(deleted = true AND creation_date <= :endDate AND last_modified_date > :startDate)) ";
        }
        sql += ") t "
                + " GROUP BY certification_body_name, year, certification_status_name "
                + " ORDER BY t.certification_body_name ";

        Query query = entityManager.createNativeQuery(sql);
        if (dateRange != null) {
            query.setParameter("startDate", dateRange.getStartDate());
            query.setParameter("endDate", dateRange.getEndDate());
        }

        List<Object[]> results = query.getResultList();
        List<CertifiedBodyStatistics> cbStats = new ArrayList<CertifiedBodyStatistics>();
        for (Object[] obj : results) {
            CertifiedBodyStatistics stat = new CertifiedBodyStatistics();
            stat.setName(obj[0].toString());
            stat.setYear(Integer.valueOf(obj[1].toString()));
            stat.setTotalListings(Long.valueOf(obj[2].toString()));
            stat.setCertificationStatusName(obj[3].toString());
            cbStats.add(stat);
        }
        return cbStats;
    }

    public Long getTotalListingsByEditionAndStatus(final DateRange dateRange,
            final String edition, final List<String> statuses) {
        String hql = "SELECT COUNT(*) "
                + "FROM CertifiedProductSummaryEntity ";
        boolean hasWhere = false;
        if (edition != null) {
            hql += " WHERE year = :edition ";
            hasWhere = true;
        }
        if (statuses != null && statuses.size() > 0) {
            if (!hasWhere) {
                hql += " WHERE ";
                hasWhere = true;
            } else {
                hql += " AND ";
            }
            hql += " UPPER(certificationStatus) IN (:statuses) ";
        }
        if (dateRange == null) {
            if (!hasWhere) {
                hql += " WHERE ";
                hasWhere = true;
            } else {
                hql += " AND ";
            }
            hql += " deleted = false ";
        } else {
            if (!hasWhere) {
                hql += " WHERE ";
                hasWhere = true;
            } else {
                hql += " AND ";
            }
            hql += "((deleted = false AND creationDate <= :endDate) "
                    + " OR "
                    + "(deleted = true AND creationDate <= :endDate AND lastModifiedDate > :endDate)) ";
        }

        Query query = entityManager.createQuery(hql);
        if (edition != null) {
            query.setParameter("edition", edition);
        }
        if (statuses != null && statuses.size() > 0) {
            query.setParameter("statuses", statuses);
        }
        if (dateRange != null) {
            query.setParameter("endDate", dateRange.getEndDate());
        }

        return (Long) query.getSingleResult();
    }

    public List<CertifiedBodyStatistics> getTotalActiveListingsByCertifiedBody(final DateRange dateRange) {
        String hql = "SELECT certificationBodyName, year, count(*) "
                + "FROM CertifiedProductSummaryEntity "
                + "WHERE UPPER(certificationStatus) in ('ACTIVE', 'SUSPENDED BY ONC-ACB', 'SUSPENDED BY ONC') ";
        if (dateRange == null) {
            hql += " AND deleted = false ";
        } else {
            hql += " AND "
                    + "((deleted = false AND creationDate <= :endDate) "
                    + " OR "
                    + "(deleted = true AND creationDate <= :endDate AND lastModifiedDate > :startDate)) ";
        }
        hql += " GROUP BY certificationBodyName, year "
                + " ORDER BY certificationBodyName ";
        Query query = entityManager.createQuery(hql);
        if (dateRange != null) {
            query.setParameter("startDate", dateRange.getStartDate());
            query.setParameter("endDate", dateRange.getEndDate());
        }

        List<Object[]> results = query.getResultList();
        List<CertifiedBodyStatistics> cbStats = new ArrayList<CertifiedBodyStatistics>();
        for (Object[] obj : results) {
            CertifiedBodyStatistics stat = new CertifiedBodyStatistics();
            stat.setName(obj[0].toString());
            stat.setYear(Integer.valueOf(obj[1].toString()));
            stat.setTotalListings(Long.valueOf(obj[2].toString()));
            stat.setCertificationStatusName(null);
            cbStats.add(stat);
        }
        return cbStats;
    }

    public Long getTotalListingsWithAlternateTestMethods() {
        String sql = "select"
                + "    distinct"
                + "    cp.id, cb.name"
                + " from CertificationResultTestProcedureEntity crtp,"
                + "    TestProcedureEntity tp,"
                + "    CertificationResultEntity cr,"
                + "    CertifiedProductEntity cp,"
                + "    CertificationBodyEntity cb"
                + " where tp.name != 'ONC Test Method'"
                + "    and crtp.testProcedureId = tp.id"
                + "    and crtp.certificationResultId = cr.id"
                + "    and cr.certifiedProductId = cp.id"
                + "    and cp.certificationBodyId = cb.id"
                + "    and cp.certificationEditionId = 3"
                + "    and tp.deleted = false"
                + "    and crtp.deleted = false"
                + "    and cr.deleted = false"
                + "    and cp.deleted = false";
        Query query = entityManager.createQuery(sql);

        return (long) query.getResultList().size();
    }

    public List<CertifiedBodyAltTestStatistics> getTotalListingsWithCertifiedBodyAndAlternativeTestMethods() {
        String sql = "select"
                + "    distinct"
                + "    cp.id, cb.name"
                + " from CertificationResultTestProcedureEntity crtp,"
                + "    TestProcedureEntity tp,"
                + "    CertificationResultEntity cr,"
                + "    CertifiedProductEntity cp,"
                + "    CertificationBodyEntity cb"
                + " where tp.name != 'ONC Test Method'"
                + "    and crtp.testProcedureId = tp.id"
                + "    and crtp.certificationResultId = cr.id"
                + "    and cr.certifiedProductId = cp.id"
                + "    and cp.certificationBodyId = cb.id"
                + "    and cp.certificationEditionId = 3"
                + "    and tp.deleted = false"
                + "    and crtp.deleted = false"
                + "    and cr.deleted = false"
                + "    and cp.deleted = false";
        Query query = entityManager.createQuery(sql);
        List<Object[]> results = query.getResultList();
        HashMap<String, Integer> listings = new HashMap<String, Integer>();
        for (Object[] obj : results) {
            if (!listings.containsKey(obj[1].toString())) {
                listings.put(obj[1].toString(), 0);
            }
            listings.put(obj[1].toString(), listings.get(obj[1].toString()) + 1);
        }

        List<CertifiedBodyAltTestStatistics> cbStats = new ArrayList<CertifiedBodyAltTestStatistics>();
        for (String val : listings.keySet()) {
            CertifiedBodyAltTestStatistics stat = new CertifiedBodyAltTestStatistics();
            stat.setName(val.toString());
            stat.setTotalListings(Long.valueOf(listings.get(val.toString())));
            cbStats.add(stat);
        }
        return cbStats;
    }
}
