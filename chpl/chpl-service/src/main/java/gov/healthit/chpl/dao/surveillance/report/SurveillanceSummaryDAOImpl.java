package gov.healthit.chpl.dao.surveillance.report;

import java.util.Date;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.SurveillanceTypeDTO;
import gov.healthit.chpl.dto.surveillance.report.SurveillanceProcessTypeDTO;

@Repository("surveillanceSummaryDao")
public class SurveillanceSummaryDAOImpl extends BaseDAOImpl implements SurveillanceSummaryDAO {
    private static final Logger LOGGER = LogManager.getLogger(SurveillanceSummaryDAOImpl.class);

    @Override
    public int getCountOfListingsSurveilled(final Long acbId,
            final SurveillanceTypeDTO survType, final Date startDate, final Date endDate) {
        String queryStr = "SELECT COUNT(DISTINCT cp) "
                + "FROM CertifiedProductDetailsEntity cp"
                + "JOIN SurveillanceBasicEntity surv "
                + "LEFT JOIN FETCH surv.surveillanceType survType "
                + "WHERE cp.certificationBodyId = :acbId "
                + "AND surv.certifiedProductId = cp.id "
                + "AND survType.id = :survTypeId "
                + "AND cp.deleted = false "
                + "AND surv.startDate <= :endDate "
                + "AND (surv.endDate IS NULL OR surv.endDate >= :startDate)";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("acbId", acbId);
        query.setParameter("survTypeId", survType.getId());
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        Object countObj = query.getSingleResult();
        return (Integer) countObj;
    }

    @Override
    public int getCountOfSurveillancesByProcessType(final Long acbId,
            final SurveillanceProcessTypeDTO procType, final SurveillanceTypeDTO survType,
            final Date startDate, final Date endDate) {
        String queryStr = "SELECT COUNT(DISTINCT cp) "
                + "FROM CertifiedProductDetailsEntity cp"
                + "JOIN PrivilegedSurveillanceEntity surv "
                + "LEFT JOIN FETCH surv.surveillanceType survType "
                + "LEFT JOIN FETCH surv.processType procType "
                + "WHERE cp.certificationBodyId = :acbId "
                + "AND surv.certifiedProductId = cp.id "
                + "AND survType.id = :survTypeId "
                + "AND procType.id = :procTypeId "
                + "AND cp.deleted = false "
                + "AND surv.startDate <= :endDate "
                + "AND (surv.endDate IS NULL OR surv.endDate >= :startDate)";
        Query query = entityManager.createQuery(queryStr);
        query.setParameter("acbId", acbId);
        query.setParameter("procTypeId", procType.getId());
        query.setParameter("survTypeId", survType.getId());
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        Object countObj = query.getSingleResult();
        return (Integer) countObj;
    }

}
