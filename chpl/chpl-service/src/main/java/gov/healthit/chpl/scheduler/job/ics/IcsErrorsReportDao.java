package gov.healthit.chpl.scheduler.job.ics;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.user.User;
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

    @Transactional
    public void deleteAll() {
        this.findAllEntities().stream()
        .filter(entity -> !entity.getDeleted())
        .forEach(entity -> {
            entity.setDeleted(true);
            entityManager.merge(entity);
        });
        entityManager.flush();
    }

    @Transactional
    public void create(List<IcsErrorsReportItem> reportItems) throws EntityCreationException, EntityRetrievalException {
        for (IcsErrorsReportItem reportItem : reportItems) {
            IcsErrorsReportEntity entity = IcsErrorsReportEntity.builder()
                    .certifiedProductId(reportItem.getListingId())
                    .chplProductNumber(reportItem.getChplProductNumber())
                    .developer(reportItem.getDeveloper())
                    .product(reportItem.getProduct())
                    .version(reportItem.getVersion())
                    .certificationBody(CertificationBodyEntity.getNewAcbEntity(reportItem.getCertificationBody()))
                    .reason(reportItem.getReason())
                    .deleted(false)
                    .lastModifiedUser(getUserId(User.SYSTEM_USER_ID))
                    .build();
            entityManager.persist(entity);
        }
        entityManager.flush();
    }

    private List<IcsErrorsReportEntity> findAllEntities() {
        Query query = entityManager.createQuery("SELECT icsReports "
                + "FROM IcsErrorsReportEntity icsReports "
                + "JOIN FETCH icsReports.certificationBody acb "
                + "JOIN FETCH acb.address "
                + "WHERE (icsReports.deleted = false)",
                IcsErrorsReportEntity.class);
        return query.getResultList();
    }
}
