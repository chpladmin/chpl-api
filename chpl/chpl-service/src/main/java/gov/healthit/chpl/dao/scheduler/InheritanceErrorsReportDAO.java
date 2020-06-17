package gov.healthit.chpl.dao.scheduler;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.scheduler.InheritanceErrorsReportDTO;
import gov.healthit.chpl.entity.CertificationBodyEntity;
import gov.healthit.chpl.entity.scheduler.InheritanceErrorsReportEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * The implementation for InheritanceErrorsReportDAO.
 *
 * @author alarned
 *
 */
@Repository("inheritanceErrorsReportDAO")
public class InheritanceErrorsReportDAO extends BaseDAOImpl {

    public List<InheritanceErrorsReportDTO> findAll() {
        List<InheritanceErrorsReportEntity> result = this.findAllEntities();
        List<InheritanceErrorsReportDTO> dtos = new ArrayList<InheritanceErrorsReportDTO>(result.size());
        for (InheritanceErrorsReportEntity entity : result) {
            dtos.add(new InheritanceErrorsReportDTO(entity));
        }
        return dtos;
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
    public void create(final List<InheritanceErrorsReportDTO> dtos)
            throws EntityCreationException, EntityRetrievalException {
        for (InheritanceErrorsReportDTO dto : dtos) {
            InheritanceErrorsReportEntity entity = new InheritanceErrorsReportEntity();
            entity.setChplProductNumber(dto.getChplProductNumber());
            entity.setDeveloper(dto.getDeveloper());
            entity.setProduct(dto.getProduct());
            entity.setVersion(dto.getVersion());
            entity.setCertificationBody(CertificationBodyEntity.getNewAcbEntity(dto.getCertificationBody()));
            entity.setUrl(dto.getUrl());
            entity.setReason(dto.getReason());
            entity.setDeleted(false);
            entity.setLastModifiedUser(getUserId(User.SYSTEM_USER_ID));

            entityManager.persist(entity);
        }
        entityManager.flush();
    }

    @Transactional
    public void create(InheritanceErrorsReportDTO dto)
            throws EntityCreationException, EntityRetrievalException {
        InheritanceErrorsReportEntity entity = new InheritanceErrorsReportEntity();
        entity.setChplProductNumber(dto.getChplProductNumber());
        entity.setDeveloper(dto.getDeveloper());
        entity.setProduct(dto.getProduct());
        entity.setVersion(dto.getVersion());
        entity.setCertificationBody(CertificationBodyEntity.getNewAcbEntity(dto.getCertificationBody()));
        entity.setUrl(dto.getUrl());
        entity.setReason(dto.getReason());
        entity.setDeleted(false);
        entity.setLastModifiedUser(getUserId(User.SYSTEM_USER_ID));

        entityManager.persist(entity);
        entityManager.flush();
    }

    private List<InheritanceErrorsReportEntity> findAllEntities() {
        Query query = entityManager.createQuery("from InheritanceErrorsReportEntity iere "
                + "join fetch iere.certificationBody cb "
                + "join fetch cb.address "
                + "where (iere.deleted = false)",
                InheritanceErrorsReportEntity.class);
        return query.getResultList();
    }
}
