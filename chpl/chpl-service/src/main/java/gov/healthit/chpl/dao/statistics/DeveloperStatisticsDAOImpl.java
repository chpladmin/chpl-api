package gov.healthit.chpl.dao.statistics;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;

@Repository("developerStatisticsDAO")
public class DeveloperStatisticsDAOImpl extends BaseDAOImpl implements DeveloperStatisticsDAO {

    /**
     * Total # of Unique Developers (Regardless of Edition)
     */
    @Override
    public Long getTotalDevelopers(DateRange dateRange) {
        String hql = "SELECT count(DISTINCT developerCode) "
                + "FROM DeveloperEntity "
                + " WHERE ";
        if(dateRange == null) {
                hql += " deleted = false";
        } else {
            hql += "(deleted = false AND creationDate <= :endDate) "
                    + " OR "
                    + "(deleted = true AND creationDate <= :endDate AND lastModifiedDate > :endDate) ";
        }

        Query query = entityManager.createQuery(hql);
        
        if(dateRange != null) {
            query.setParameter("endDate", dateRange.getEndDate());
        }
        return (Long) query.getSingleResult();
    }

    /**
     * Total # of Developers with Active 2014 Listings
     */
    @Override
    public Long getTotalDevelopersWithListingsByEditionAndStatus(DateRange dateRange, String edition, List<String> statuses) {
        String hql = "SELECT count(DISTINCT developerCode) "
                + "FROM CertifiedProductDetailsEntity ";
        boolean hasWhere = false;
        if(edition != null) {
            hql += " WHERE year = :edition ";
            hasWhere = true;
        }
        if(statuses != null && statuses.size() > 0) {
            if(!hasWhere) {
                hql += " WHERE ";
                hasWhere = true;
            } else {
                hql += " AND ";
            }
            hql += " UPPER(certificationStatusName) IN (:statuses) ";
        }
        
        if(dateRange == null) {
            if(!hasWhere) {
                hql += " WHERE ";
                hasWhere = true;
            } else {
                hql += " AND ";
            }
            hql += " deleted = false ";
        } else {
            if(!hasWhere) {
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
        
        if(edition != null) {
            query.setParameter("edition", edition);
        }
        if(statuses != null && statuses.size() > 0) {
            query.setParameter("statuses", statuses);
        }
        if(dateRange != null) {
            query.setParameter("endDate", dateRange.getEndDate());
        }
        return (Long) query.getSingleResult();
    }

    /**
     * Total # of Developers with listings by certified body in each year
     */
    @Override
    public List<CertifiedBodyStatistics> getTotalDevelopersByCertifiedBodyWithListingsEachYear(DateRange dateRange) {
        String hql = "SELECT certificationBodyName, year, count(DISTINCT developerCode) "
                + "FROM CertifiedProductDetailsEntity "
                + "WHERE ";
        if(dateRange == null) {
            hql += " deleted = false ";
        } else {
            hql += "(deleted = false AND creationDate <= :endDate) "
                    + " OR "
                    + "(deleted = true AND creationDate <= :endDate AND lastModifiedDate > :endDate) ";
        }
        hql += " GROUP BY certificationBodyName, year " 
                + " ORDER BY certificationBodyName ";
        
        Query query = entityManager.createQuery(hql);
        
        if(dateRange != null) {
            //query.setParameter("creationStartDate", dateRange.getStartDate());
            query.setParameter("endDate", dateRange.getEndDate());
        }
        
        List<Object[]> results = query.getResultList();
        List<CertifiedBodyStatistics> cbStats = new ArrayList<CertifiedBodyStatistics>();
        for (Object[] obj : results) {
            CertifiedBodyStatistics cbStat = new CertifiedBodyStatistics();
            cbStat.setName(obj[0].toString());
            cbStat.setYear(Integer.valueOf(obj[1].toString()));
            cbStat.setTotalDevelopersWithListings(Long.valueOf(obj[2].toString()));
            cbStat.setCertificationStatusName(null);
            cbStats.add(cbStat);
        }
        return cbStats;
    }

    /**
     * Total # of Developers with listings by certified body in each year
     */
    @Override
    public List<CertifiedBodyStatistics> getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(
            DateRange dateRange) {
        String hql = "SELECT certificationBodyName, year, count(DISTINCT developerCode), certificationStatusName "
                + "FROM CertifiedProductDetailsEntity "
                + "WHERE ";
        if(dateRange == null) {
            hql += " deleted = false ";
        } else {
            hql += "(deleted = false AND creationDate <= :endDate) "
                    + " OR "
                    + "(deleted = true AND creationDate <= :endDate AND lastModifiedDate > :endDate) ";
        }
        hql += " GROUP BY certificationBodyName, year, certificationStatusName " 
                + " ORDER BY certificationBodyName ";
        
        Query query = entityManager.createQuery(hql);
        
        if(dateRange != null) {
            query.setParameter("endDate", dateRange.getEndDate());
        }

        List<Object[]> results = query.getResultList();
        List<CertifiedBodyStatistics> cbStats = new ArrayList<CertifiedBodyStatistics>();
        for (Object[] obj : results) {
            CertifiedBodyStatistics stat = new CertifiedBodyStatistics();
            stat.setName(obj[0].toString());
            stat.setYear(Integer.valueOf(obj[1].toString()));
            stat.setTotalDevelopersWithListings(Long.valueOf(obj[2].toString()));
            stat.setCertificationStatusName(obj[3].toString());
            cbStats.add(stat);
        }
        return cbStats;
    }

}
