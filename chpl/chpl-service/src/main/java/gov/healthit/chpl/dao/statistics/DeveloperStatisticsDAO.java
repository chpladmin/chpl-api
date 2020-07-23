package gov.healthit.chpl.dao.statistics;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.DateRange;


@Repository("developerStatisticsDAO")
public class DeveloperStatisticsDAO extends BaseDAOImpl {

    public Long getTotalDevelopers(final DateRange dateRange) {
        String hql = "SELECT count(DISTINCT developerCode) "
                + "FROM DeveloperEntity "
                + " WHERE ";
        if (dateRange == null) {
            hql += " deleted = false";
        } else {
            hql += "(deleted = false AND creationDate <= :endDate) "
                    + " OR "
                    + "(deleted = true AND creationDate <= :endDate AND lastModifiedDate > :endDate) ";
        }
        Query query = entityManager.createQuery(hql);

        if (dateRange != null) {
            query.setParameter("endDate", dateRange.getEndDate());
        }
        return (Long) query.getSingleResult();
    }

    public Long getTotalDevelopersWithListingsByEditionAndStatus(final DateRange dateRange,
            final String edition, final List<String> statuses) {
        String hql = "SELECT count(DISTINCT developerCode) "
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
}
