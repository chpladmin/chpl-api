package gov.healthit.chpl.dao.statistics;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;


@Repository("developerStatisticsDAO")
public class DeveloperStatisticsDAO extends BaseDAOImpl {

    public Long getTotalDevelopers(Date endDate) {
        String hql = "SELECT count(DISTINCT developerCode) "
                + "FROM DeveloperEntity "
                + " WHERE ";
        if (endDate == null) {
            hql += " deleted = false";
        } else {
            hql += "(deleted = false AND creationDate <= :endDate) "
                    + " OR "
                    + "(deleted = true AND creationDate <= :endDate AND lastModifiedDate > :endDate) ";
        }
        Query query = entityManager.createQuery(hql);

        if (endDate != null) {
            query.setParameter("endDate", endDate);
        }
        return (Long) query.getSingleResult();
    }

    public Long getTotalDevelopersWithListingsByEditionAndStatus(Date endDate, String edition, List<String> statuses) {
        String hql = "SELECT count(DISTINCT developerCode) "
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
}
