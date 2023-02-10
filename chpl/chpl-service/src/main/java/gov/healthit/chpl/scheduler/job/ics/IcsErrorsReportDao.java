package gov.healthit.chpl.scheduler.job.ics;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.entity.CertificationBodyEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository("icsErrorsReportDao")
public class IcsErrorsReportDao extends BaseDAOImpl {

    public List<IcsErrorsReportItem> findAll() {
        List<IcsErrorsReportEntity> entities = this.findAllEntities();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    public void deleteAll() {
        Query query = entityManager.createQuery(
                "DELETE FROM IcsErrorsReportEntity");
        query.executeUpdate();
    }

    public void create(List<IcsErrorsReportItem> reportItems) throws EntityCreationException, EntityRetrievalException {
        for (IcsErrorsReportItem reportItem : reportItems) {
            IcsErrorsReportEntity entity = IcsErrorsReportEntity.builder()
                    .certifiedProductId(reportItem.getListingId())
                    .certificationBody(CertificationBodyEntity.getNewAcbEntity(reportItem.getCertificationBody()))
                    .reason(reportItem.getReason())
                    .build();
            entityManager.persist(entity);
        }
        entityManager.flush();
    }

    private List<IcsErrorsReportEntity> findAllEntities() {
        Query query = entityManager.createQuery("SELECT icsReports "
                + "FROM IcsErrorsReportEntity icsReports "
                + "JOIN FETCH icsReports.certificationBody acb "
                + "JOIN FETCH acb.address ",
                IcsErrorsReportEntity.class);
        return query.getResultList();
    }
}
