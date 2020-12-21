package gov.healthit.chpl.dao.statistics;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;

@Repository("listingStatisticsDAO")
public class ListingStatisticsDAO extends BaseDAOImpl {

    public Long getTotalUniqueProductsByEditionAndStatus(Date endDate, String edition, List<String> statuses) {
        String hql = "SELECT DISTINCT UPPER(productName) || UPPER(developerName) "
                + "FROM CertifiedProductDetailsEntity ";

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
            hql += " UPPER(certificationStatusName) IN (:statuses) ";
        }
        if (endDate == null) {
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
            hql += "((deleted = false AND certificationDate <= :endDate) "
                    + " OR "
                    + "(deleted = true AND certificationDate <= :endDate AND lastModifiedDate > :endDate)) ";
        }

        Query query = entityManager.createQuery(hql);
        if (edition != null) {
            query.setParameter("edition", edition);
        }
        if (statuses != null && statuses.size() > 0) {
            query.setParameter("statuses", statuses);
        }
        if (endDate != null) {
            query.setParameter("endDate", endDate);
        }
        return (long) query.getResultList().size();
    }

    public Long getTotalUniqueProducts(List<String> statuses) {
        String hql = "SELECT DISTINCT productId,  developerId "
                + "FROM CertifiedProductDetailsEntity "
                + "WHERE UPPER(certificationStatusName) IN (:statuses) "
                + "AND  deleted = false ";

        Query query = entityManager.createQuery(hql);
        query.setParameter("statuses", statuses);
        return (long) query.getResultList().size();
    }

    public Long getTotalListingsByEditionAndStatus(Date endDate, String edition, List<String> statuses) {
        String hql = "SELECT COUNT(*) "
                + "FROM CertifiedProductDetailsEntity ";
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
            hql += " UPPER(certificationStatusName) IN (:statuses) ";
        }
        if (endDate == null) {
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
            hql += "((deleted = false AND certificationDate <= :endDate) "
                    + " OR "
                    + "(deleted = true AND certificationDate <= :endDate AND lastModifiedDate > :endDate)) ";
        }

        Query query = entityManager.createQuery(hql);
        if (edition != null) {
            query.setParameter("edition", edition);
        }
        if (statuses != null && statuses.size() > 0) {
            query.setParameter("statuses", statuses);
        }
        if (endDate != null) {
            query.setParameter("endDate", endDate);
        }

        return (Long) query.getSingleResult();
    }

    public Long getTotal2015ListingsByStatus(List<String> statuses) {
        String hql = "SELECT COUNT(*) "
                + "FROM CertifiedProductSummaryEntity "
                + "WHERE year = '2015' "
                + "AND curesUpdate = false ";
        if (statuses != null && statuses.size() > 0) {
            hql += " AND UPPER(certificationStatus) IN (:statuses) ";
        }

        Query query = entityManager.createQuery(hql);
        if (statuses != null && statuses.size() > 0) {
            query.setParameter("statuses", statuses);
        }

        return (Long) query.getSingleResult();
    }
}
