package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.AddressDAO;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.entity.CertificationBodyEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

/**
 * Data access methods for certification bodies (ACBs).
 * 
 * @author kekey
 *
 */
@Repository(value = "certificationBodyDAO")
public class CertificationBodyDAOImpl extends BaseDAOImpl implements CertificationBodyDAO {

    private static final Logger LOGGER = LogManager.getLogger(CertificationBodyDAOImpl.class);
    @Autowired
    AddressDAO addressDao;

    /**
     * Create an ACB.
     */
    @Transactional
    public CertificationBodyDTO create(final CertificationBodyDTO dto)
            throws EntityRetrievalException, EntityCreationException {
        CertificationBodyEntity entity = null;
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
            entity = new CertificationBodyEntity();
            entity.setAddress(addressDao.create(dto.getAddress()));
            entity.setName(dto.getName());
            entity.setWebsite(dto.getWebsite());
            entity.setAcbCode(dto.getAcbCode());
            entity.setRetired(Boolean.FALSE);
            entity.setLastModifiedUser(AuthUtil.getAuditId());
            create(entity);
            return new CertificationBodyDTO(entity);
        }
    }

    /**
     * Update an ACB.
     */
    @Transactional
    public CertificationBodyDTO update(final CertificationBodyDTO dto) throws EntityRetrievalException {

        CertificationBodyEntity entity = getEntityById(dto.getId());
        if (entity == null) {
            throw new EntityRetrievalException(
                    "Cannot update entity with id " + dto.getId() + ". Entity does not exist.");
        }

        if (dto.getAddress() != null) {
            try {
                entity.setAddress(addressDao.saveAddress(dto.getAddress()));
            } catch (final EntityCreationException ex) {
                LOGGER.error("Could not create new address in the database.", ex);
                entity.setAddress(null);
            }
        } else {
            entity.setAddress(null);
        }

        entity.setWebsite(dto.getWebsite());
        entity.setRetired(dto.isRetired());
        entity.setRetirementDate(dto.getRetirementDate());

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }

        if (dto.getAcbCode() != null) {
            entity.setAcbCode(dto.getAcbCode());
        }

        entity.setLastModifiedUser(AuthUtil.getAuditId());
        update(entity);
        return new CertificationBodyDTO(entity);
    }

    /**
     * Get all ACBs.
     */
    public List<CertificationBodyDTO> findAll() {
        List<CertificationBodyEntity> entities = getAllEntities();
        List<CertificationBodyDTO> acbs = new ArrayList<>();

        for (CertificationBodyEntity entity : entities) {
            CertificationBodyDTO acb = new CertificationBodyDTO(entity);
            acbs.add(acb);
        }
        return acbs;

    }

    /**
     * Get all ACBs not marked retired.
     */
    public List<CertificationBodyDTO> findAllActive() {
        List<CertificationBodyEntity> entities = entityManager
                .createQuery("SELECT acb from CertificationBodyEntity acb " + "LEFT OUTER JOIN FETCH acb.address "
                        + "WHERE acb.retired = false AND acb.deleted = false", CertificationBodyEntity.class)
                .getResultList();
        List<CertificationBodyDTO> acbs = new ArrayList<>();

        for (CertificationBodyEntity entity : entities) {
            CertificationBodyDTO acb = new CertificationBodyDTO(entity);
            acbs.add(acb);
        }
        return acbs;

    }

    /**
     * Finds an ACB by ID.
     * 
     * @param acbId
     * @return the ACB
     */
    public CertificationBodyDTO getById(final Long acbId) throws EntityRetrievalException {
        CertificationBodyEntity entity = getEntityById(acbId);

        CertificationBodyDTO dto = null;
        if (entity != null) {
            dto = new CertificationBodyDTO(entity);
        }
        return dto;
    }

    /**
     * Find an ACB by name.
     * 
     * @param name
     * @return the ACB
     */
    public CertificationBodyDTO getByName(final String name) {
        CertificationBodyEntity entity = getEntityByName(name);

        CertificationBodyDTO dto = null;
        if (entity != null) {
            dto = new CertificationBodyDTO(entity);
        }
        return dto;
    }

    /**
     * Get the largest ACB code currently in the database.
     */
    public String getMaxCode() {
        String maxCode = null;
        Query query = entityManager.createQuery(
                "SELECT acb.acbCode " + "from CertificationBodyEntity acb " + "ORDER BY acb.acbCode DESC",
                String.class);
        List<String> result = query.getResultList();

        if (result != null && result.size() > 0) {
            maxCode = result.get(0);
        }
        return maxCode;
    }

    private void create(final CertificationBodyEntity acb) {
        entityManager.persist(acb);
        entityManager.flush();
    }

    private void update(final CertificationBodyEntity acb) {
        entityManager.merge(acb);
        entityManager.flush();

    }

    /**
     * Get all ACBs.
     * 
     * @return
     */
    private List<CertificationBodyEntity> getAllEntities() {
        return entityManager.createQuery("SELECT acb from CertificationBodyEntity acb "
                + "LEFT OUTER JOIN FETCH acb.address " + "WHERE (acb.deleted = false)", CertificationBodyEntity.class)
                .getResultList();
    }

    /**
     * Find an ACB by ID.
     * 
     * @param entityId
     * @return
     * @throws EntityRetrievalException
     */
    private CertificationBodyEntity getEntityById(final Long entityId) throws EntityRetrievalException {
        CertificationBodyEntity entity = null;

        String queryStr = "SELECT acb from CertificationBodyEntity acb " + "LEFT OUTER JOIN FETCH acb.address "
                + "WHERE (acb.id = :entityid)" + " AND (acb.deleted = false)";

        Query query = entityManager.createQuery(queryStr, CertificationBodyEntity.class);
        query.setParameter("entityid", entityId);
        List<CertificationBodyEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            String msg = msgUtil.getMessage("acb.notFound");
            throw new EntityRetrievalException(msg);
        } else if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate certification body id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }

        return entity;
    }

    /**
     * Find an ACB by name.
     * 
     * @param name
     * @return
     */
    private CertificationBodyEntity getEntityByName(final String name) {
        CertificationBodyEntity entity = null;

        Query query = entityManager.createQuery(
                "SELECT acb from CertificationBodyEntity acb " + "LEFT OUTER JOIN FETCH acb.address "
                        + "WHERE (acb.deleted = false) " + "AND (UPPER(acb.name) = :name) ",
                CertificationBodyEntity.class);
        query.setParameter("name", name.toUpperCase());
        List<CertificationBodyEntity> result = query.getResultList();

        if (result != null && result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

}
