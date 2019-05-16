package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.MeaningfulUseUserDAO;
import gov.healthit.chpl.dto.MeaningfulUseUserDTO;
import gov.healthit.chpl.entity.MeaningfulUseUserEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

/**
 * Meaningful use user DAO implementation.
 * @author kekey
 *
 */
@Repository("meaningfulUseUserDAO")
public class MeaningfulUseUserDAOImpl extends BaseDAOImpl implements MeaningfulUseUserDAO {

    @Override
    @Transactional
    public MeaningfulUseUserDTO create(final MeaningfulUseUserDTO dto)
            throws EntityCreationException, EntityRetrievalException {

        MeaningfulUseUserEntity entity = new MeaningfulUseUserEntity();
        entity.setCertifiedProductId(dto.getCertifiedProductId());
        entity.setMuuCount(dto.getMuuCount());
        entity.setMuuDate(dto.getMuuDate());
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setDeleted(false);
        entityManager.persist(entity);
        entityManager.flush();
        return new MeaningfulUseUserDTO(entity);
    }

    @Override
    public MeaningfulUseUserDTO update(final MeaningfulUseUserDTO dto) throws EntityRetrievalException {
        MeaningfulUseUserEntity entity = getEntityById(dto.getId());

        if (entity == null) {
            throw new EntityRetrievalException("Meaningful Use User entry with id " + dto.getId() + " does not exist");
        }
        entity.setMuuCount(dto.getMuuCount());
        entity.setMuuDate(dto.getMuuDate());
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entityManager.merge(entity);
        entityManager.flush();
        return new MeaningfulUseUserDTO(entity);
    }

    @Override
    public void delete(final Long id) throws EntityRetrievalException {

        MeaningfulUseUserEntity toDelete = getEntityById(id);

        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedUser(AuthUtil.getAuditId());
            entityManager.merge(toDelete);
            entityManager.flush();
        }
    }

    @Override
    public MeaningfulUseUserDTO getById(final Long id) throws EntityRetrievalException {

        MeaningfulUseUserDTO dto = null;
        MeaningfulUseUserEntity entity = getEntityById(id);

        if (entity != null) {
            dto = new MeaningfulUseUserDTO(entity);
        }
        return dto;
    }

    @Override
    public List<MeaningfulUseUserDTO> findByCertifiedProductId(final Long certifiedProductId) {

        List<MeaningfulUseUserEntity> entities = getEntitiesByCertifiedProductId(certifiedProductId);
        List<MeaningfulUseUserDTO> dtos = new ArrayList<MeaningfulUseUserDTO>();

        for (MeaningfulUseUserEntity entity : entities) {
            MeaningfulUseUserDTO dto = new MeaningfulUseUserDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    private MeaningfulUseUserEntity getEntityById(final Long id) throws EntityRetrievalException {
        MeaningfulUseUserEntity entity = null;

        Query query = entityManager.createQuery("SELECT muu "
                + "FROM MeaningfulUseUserEntity muu "
                + "WHERE muu.id = :entityid "
                + "AND (NOT muu.deleted = true)",
                MeaningfulUseUserEntity.class);
        query.setParameter("entityid", id);
        List<MeaningfulUseUserEntity> result = query.getResultList();

        if (result != null && result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    private List<MeaningfulUseUserEntity> getEntitiesByCertifiedProductId(final Long cpId) {

        Query query = entityManager.createQuery("SELECT muu "
                + "FROM MeaningfulUseUserEntity muu "
                + "WHERE muu.certifiedProductId = :cpId "
                + "AND (NOT muu.deleted = true)",
                MeaningfulUseUserEntity.class);
        query.setParameter("cpId", cpId);
        List<MeaningfulUseUserEntity> result = query.getResultList();

        return result;
    }

}
