package gov.healthit.chpl.dao.scheduler;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.scheduler.InheritanceErrorsReportDTO;
import gov.healthit.chpl.entity.scheduler.InheritanceErrorsReportEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * The implementation for InheritanceErrorsReportDAO.
 * @author alarned
 *
 */
@Repository("inheritanceErrorsReportDAO")
public class InheritanceErrorsReportDAOImpl extends BaseDAOImpl implements InheritanceErrorsReportDAO {

    @Override
    public List<InheritanceErrorsReportDTO> findAll() {
        List<InheritanceErrorsReportEntity> result = this.findAllEntities();
        List<InheritanceErrorsReportDTO> dtos = new ArrayList<InheritanceErrorsReportDTO>(result.size());
        for (InheritanceErrorsReportEntity entity : result) {
            dtos.add(new InheritanceErrorsReportDTO(entity));
        }
        return dtos;
    }

    @Override
    @Transactional
    public void deleteAll() {
        List<InheritanceErrorsReportEntity> entities = this.findAllEntities();

        for (InheritanceErrorsReportEntity entity : entities) {
            if (!entity.getDeleted()) {
                entity.setDeleted(true);
                entityManager.merge(entity);
                entityManager.flush();
            }
        }
    }

    @Override
    @Transactional
    public void create(final List<InheritanceErrorsReportDTO> dtos)
            throws EntityCreationException, EntityRetrievalException {
        for (InheritanceErrorsReportDTO dto : dtos) {
            InheritanceErrorsReportEntity entity = new InheritanceErrorsReportEntity();
            entity.setChplProductNumber(dto.getChplProductNumber());
            entity.setDeveloper(dto.getDeveloper());
            entity.setProduct(dto.getProduct());
            entity.setVersion(dto.getVersion());
            entity.setAcb(dto.getAcb());
            entity.setUrl(dto.getUrl());
            entity.setReason(dto.getReason());
            entity.setDeleted(false);
            entity.setLastModifiedUser(getUserId(User.SYSTEM_USER_ID));

            entityManager.persist(entity);
        }
        entityManager.flush();
    }

    private List<InheritanceErrorsReportEntity> findAllEntities() {
        Query query = entityManager.createQuery("from InheritanceErrorsReportEntity iere "
                + "where (iere.deleted = false)",
                InheritanceErrorsReportEntity.class);
        return query.getResultList();
    }
}
