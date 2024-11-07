package gov.healthit.chpl.report.criteriaattribute;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.standard.Standard;
import gov.healthit.chpl.standard.StandardDAO;
import jakarta.persistence.Query;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository
public class StandardReportDao extends BaseDAOImpl {

    private StandardDAO standardDao;
    private CertificationCriterionDAO certificationCriterionDao;

    @Autowired
    public StandardReportDao(StandardDAO standardDao, CertificationCriterionDAO certificationCriterionDao) {
        this.standardDao = standardDao;
        this.certificationCriterionDao = certificationCriterionDao;
    }

    public List<StandardReport> getStandardReports() {
        String hql = "SELECT cc.id as certificationCriterionId, s.id as standardId, count(*) as standardCount "
                + "FROM CertificationCriterionEntity cc, "
                + "CertificationResultEntity cr, "
                + "CertifiedProductDetailsEntity cpd, "
                + "CertificationResultStandardEntity crs, "
                + "StandardEntity s "
                + "WHERE cc.id = cr.certificationCriterionId "
                + "AND cr.certifiedProductId = cpd.id "
                + "AND cr.id = crs.certificationResultId "
                + "AND crs.standard.id = s.id "
                + "AND cpd.certificationStatusId IN (1,6,7) "
                + "AND (cc.endDay is null OR cc.endDay > CURRENT_DATE()) "
                + "AND cc.deleted = false "
                + "AND cr.deleted = false "
                + "AND crs.deleted = false "
                + "AND cpd.deleted = false "
                + "AND s.deleted = false "
                + "GROUP BY cc.id, s.id ";

        Query query = entityManager.createQuery(hql);
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(result -> StandardReport.builder()
                        .criterion(getCertificationCriterion((Long) result[0]))
                        .standard(getStandard((Long) result[1]))
                        .count((Long) result[2])
                        .build())
                .toList();
    }

    public List<StandardListingReport> getStandardListingReports() {
        String hql = "SELECT cc.id as certificationCriterionId, s.id as standardId, cpd.chplProductNumber "
                + "FROM CertificationCriterionEntity cc, "
                + "CertificationResultEntity cr, "
                + "CertifiedProductDetailsEntity cpd, "
                + "CertificationResultStandardEntity crs, "
                + "StandardEntity s "
                + "WHERE cc.id = cr.certificationCriterionId "
                + "AND cr.certifiedProductId = cpd.id "
                + "AND cr.id = crs.certificationResultId "
                + "AND crs.standard.id = s.id "
                + "AND cpd.certificationStatusId IN (1,6,7) "
                + "AND cc.deleted = false "
                + "AND cr.deleted = false "
                + "AND crs.deleted = false "
                + "AND cpd.deleted = false ";

        Query query = entityManager.createQuery(hql);
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(result -> StandardListingReport.builder()
                        .criterion(getCertificationCriterion((Long) result[0]))
                        .standard(getStandard((Long) result[1]))
                        .chplProductNumber((String) result[2])
                        .build())
                .toList();
    }

    private CertificationCriterion getCertificationCriterion(Long id) {
        try {
            return certificationCriterionDao.getById(id);
        } catch (EntityRetrievalException e) {
            LOGGER.error("Could not retrieve criterion id: {}", id, e);
            return null;
        }
    }

    private Standard getStandard(Long id) {
        return standardDao.getById(id);
    }

}
