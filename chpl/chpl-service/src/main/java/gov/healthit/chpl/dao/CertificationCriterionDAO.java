package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.entity.CertificationCriterionEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository("certificationCriterionDAO")
public class CertificationCriterionDAO extends BaseDAOImpl {

    @Transactional
    public CertificationCriterionDTO create(CertificationCriterionDTO dto)
            throws EntityCreationException, EntityRetrievalException {

        CertificationCriterionEntity entity = null;
        try {
            if (dto.getId() != null) {
                entity = this.getEntityById(dto.getId());
            }
        } catch (final EntityRetrievalException e) {
            throw new EntityCreationException(e);
        }

        if (entity != null) {
            throw new EntityCreationException("An entity with this ID already exists.");
        } else {

            entity = new CertificationCriterionEntity();
            entity.setAutomatedMeasureCapable(dto.getAutomatedMeasureCapable());
            entity.setAutomatedNumeratorCapable(dto.getAutomatedNumeratorCapable());
            entity.setCertificationEdition(dto.getCertificationEditionId());
            entity.setCreationDate(new Date());
            entity.setDeleted(false);
            entity.setDescription(dto.getDescription());
            entity.setLastModifiedDate(new Date());
            entity.setLastModifiedUser(AuthUtil.getAuditId());
            entity.setNumber(dto.getNumber());
            entity.setRequiresSed(dto.getRequiresSed());
            entity.setTitle(dto.getTitle());
            entity.setRemoved(dto.getRemoved());

            create(entity);
        }
        return new CertificationCriterionDTO(entity);
    }

    @Transactional
    public CertificationCriterionDTO update(CertificationCriterionDTO dto)
            throws EntityRetrievalException, EntityCreationException {

        CertificationCriterionEntity entity = this.getEntityById(dto.getId());

        entity.setAutomatedMeasureCapable(dto.getAutomatedMeasureCapable());
        entity.setAutomatedNumeratorCapable(dto.getAutomatedNumeratorCapable());
        entity.setCertificationEdition(dto.getCertificationEditionId());
        entity.setCreationDate(dto.getCreationDate());
        entity.setDeleted(dto.getDeleted());
        entity.setDescription(dto.getDescription());
        entity.setId(dto.getId());
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setNumber(dto.getNumber());
        entity.setRequiresSed(dto.getRequiresSed());
        entity.setTitle(dto.getTitle());
        entity.setRemoved(dto.getRemoved());
        update(entity);

        return new CertificationCriterionDTO(entity);
    }

    @Transactional
    public void delete(final Long criterionId) {

        Query query = entityManager.createQuery(
                "UPDATE CertificationCriterionEntity SET deleted = true WHERE certification_criterion_id = :entityid");
        query.setParameter("entityid", criterionId);
        query.executeUpdate();

    }

    public List<CertificationCriterionDTO> findAll() {

        List<CertificationCriterionEntity> entities = getAllEntities();
        List<CertificationCriterionDTO> dtos = new ArrayList<>();

        for (CertificationCriterionEntity entity : entities) {
            CertificationCriterionDTO dto = new CertificationCriterionDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    public List<CertificationCriterionDTO> findByCertificationEditionYear(String year) {

        List<CertificationCriterionEntity> entities = getEntitiesByCertificationEditionYear(year);
        List<CertificationCriterionDTO> dtos = new ArrayList<>();

        for (CertificationCriterionEntity entity : entities) {
            CertificationCriterionDTO dto = new CertificationCriterionDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    public List<CertificationCriterionDTO> getAllByNumber(String criterionName) {
        List<CertificationCriterionEntity> entities = getEntitiesByNumber(criterionName);
        List<CertificationCriterionDTO> dtos = new ArrayList<>();

        for (CertificationCriterionEntity entity : entities) {
            CertificationCriterionDTO dto = new CertificationCriterionDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    public CertificationCriterionDTO getById(Long criterionId) throws EntityRetrievalException {

        CertificationCriterionDTO dto = null;
        CertificationCriterionEntity entity = getEntityById(criterionId);

        if (entity != null) {
            dto = new CertificationCriterionDTO(entity);
        }
        return dto;
    }

    @Transactional
    public CertificationCriterionDTO getByNumberAndTitle(String criterionNumber, String criterionTitle) {
        CertificationCriterionDTO result = null;
        CertificationCriterionEntity entity = getEntityByNumberAndTitle(criterionNumber, criterionTitle);
        if (entity != null) {
            result = new CertificationCriterionDTO(entity);
        }
        return result;
    }

    @Transactional
    private void create(final CertificationCriterionEntity entity) {

        entityManager.persist(entity);
        entityManager.flush();

    }

    @Transactional
    private void update(final CertificationCriterionEntity entity) {

        entityManager.merge(entity);
        entityManager.flush();

    }

    private List<CertificationCriterionEntity> getAllEntities() {
        Query query = entityManager
                .createQuery(
                        "SELECT cce "
                                + "FROM CertificationCriterionEntity cce "
                                + "LEFT JOIN FETCH cce.certificationEdition "
                                + "WHERE cce.deleted = false",
                                CertificationCriterionEntity.class);
        @SuppressWarnings("unchecked") List<CertificationCriterionEntity> result = query.getResultList();

        return result;
    }

    @SuppressWarnings("unchecked")
    private List<CertificationCriterionEntity> getEntitiesByCertificationEditionYear(String year) {
        Query query = entityManager.createQuery("SELECT cce "
                + "FROM CertificationCriterionEntity cce "
                + "LEFT JOIN FETCH cce.certificationEdition "
                + "WHERE (NOT cce.deleted = true) "
                + "AND (cce.certificationEditionId = cce.certificationEdition.id) "
                + "AND (cce.certificationEdition.year = :year)", CertificationCriterionEntity.class);
        query.setParameter("year", year);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<CertificationCriterionEntity> getEntitiesByNumber(String number) {
        Query query = entityManager.createQuery("SELECT cce "
                + "FROM CertificationCriterionEntity cce "
                + "LEFT JOIN FETCH cce.certificationEdition "
                + "WHERE (NOT cce.deleted = true) "
                + "AND (cce.certificationEditionId = cce.certificationEdition.id) "
                + "AND (cce.certificationEdition.number = :number)", CertificationCriterionEntity.class);
        query.setParameter("number", number);
        return query.getResultList();
    }

    public CertificationCriterionEntity getEntityById(Long id) throws EntityRetrievalException {
        CertificationCriterionEntity entity = null;
        if (id != null) {
            Query query = entityManager.createQuery(
                    "SELECT cce "
                            + "FROM CertificationCriterionEntity cce "
                            + "LEFT JOIN FETCH cce.certificationEdition "
                            + "WHERE (cce.deleted <> true) AND (cce.id = :entityid) ",
                            CertificationCriterionEntity.class);
            query.setParameter("entityid", id);
            @SuppressWarnings("unchecked") List<CertificationCriterionEntity> result = query.getResultList();

            if (result.size() > 1) {
                throw new EntityRetrievalException("Data error. Duplicate criterion id in database.");
            }

            if (result.size() > 0) {
                entity = result.get(0);
            }
        }

        return entity;
    }

    public CertificationCriterionEntity getEntityByNumberAndTitle(String criterionNumber, String criterionTitle) {
        Query query = entityManager
                .createQuery(
                        "SELECT cce " + "FROM CertificationCriterionEntity cce "
                                + "WHERE (NOT cce.deleted = true) "
                                + "AND (cce.number = :number) "
                                + "AND (cce.title = :title) ",
                                CertificationCriterionEntity.class);
        query.setParameter("number", criterionNumber);
        query.setParameter("title", criterionTitle);
        @SuppressWarnings("unchecked") List<CertificationCriterionEntity> results = query.getResultList();

        CertificationCriterionEntity entity = null;
        if (results.size() > 0) {
            entity = results.get(0);
        }
        return entity;
    }
}
