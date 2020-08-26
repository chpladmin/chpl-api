package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.entity.CertificationBodyEntity;
import gov.healthit.chpl.entity.UserCertificationBodyMapEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository(value = "certificationBodyDAO")
public class CertificationBodyDAO extends BaseDAOImpl {
    private static final Logger LOGGER = LogManager.getLogger(CertificationBodyDAO.class);
    private AddressDAO addressDao;

    @Autowired
    public CertificationBodyDAO(AddressDAO addressDao) {
        this.addressDao = addressDao;
    }

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

    public List<CertificationBodyDTO> findAll() {
        List<CertificationBodyEntity> entities = getAllEntities();
        List<CertificationBodyDTO> acbs = new ArrayList<>();

        for (CertificationBodyEntity entity : entities) {
            CertificationBodyDTO acb = new CertificationBodyDTO(entity);
            acbs.add(acb);
        }
        return acbs;

    }

    public List<CertificationBodyDTO> findAllActive() {
        List<CertificationBodyEntity> entities = entityManager
                .createQuery("SELECT acb from CertificationBodyEntity acb "
                        + "LEFT OUTER JOIN FETCH acb.address "
                        + "WHERE acb.retired = false "
                        + "AND acb.deleted = false", CertificationBodyEntity.class)
                .getResultList();
        List<CertificationBodyDTO> acbs = new ArrayList<>();

        for (CertificationBodyEntity entity : entities) {
            CertificationBodyDTO acb = new CertificationBodyDTO(entity);
            acbs.add(acb);
        }
        return acbs;

    }

    public CertificationBodyDTO getById(final Long acbId) throws EntityRetrievalException {
        CertificationBodyEntity entity = getEntityById(acbId);

        CertificationBodyDTO dto = null;
        if (entity != null) {
            dto = new CertificationBodyDTO(entity);
        }
        return dto;
    }

    public CertificationBodyDTO getByName(final String name) {
        CertificationBodyEntity entity = getEntityByName(name);

        CertificationBodyDTO dto = null;
        if (entity != null) {
            dto = new CertificationBodyDTO(entity);
        }
        return dto;
    }

    public List<CertificationBodyDTO> getByWebsite(final String website) {
        Query query = entityManager.createQuery("SELECT acb "
                + "FROM CertificationBodyEntity acb "
                + "LEFT OUTER JOIN FETCH acb.address "
                + "WHERE acb.deleted = false "
                + "AND acb.website = :website");
        query.setParameter("website", website);
        List<CertificationBodyEntity> results = query.getResultList();
        List<CertificationBodyDTO> resultDtos = new ArrayList<CertificationBodyDTO>();
        for (CertificationBodyEntity entity : results) {
            resultDtos.add(new CertificationBodyDTO(entity));
        }
        return resultDtos;
    }

    public String getMaxCode() {
        String maxCode = null;
        Query query = entityManager.createQuery("SELECT acb.acbCode "
                        + "from CertificationBodyEntity acb "
                        + "ORDER BY acb.acbCode DESC",
                String.class);
        List<String> result = query.getResultList();

        if (result != null && result.size() > 0) {
            maxCode = result.get(0);
        }
        return maxCode;
    }


    public List<CertificationBodyDTO> getByDeveloperId(final Long developerId) {
        return getEntitiesByDeveloperId(developerId).stream()
                .map(acb -> new CertificationBodyDTO(acb))
                .collect(Collectors.<CertificationBodyDTO> toList());
    }


    public List<CertificationBodyDTO> getCertificationBodiesByUserId(Long userId) {
        Query query = entityManager.createQuery(
                "FROM UserCertificationBodyMapEntity ucbm "
                        + "join fetch ucbm.certificationBody acb "
                        + "left join fetch acb.address "
                        + "join fetch ucbm.user u "
                        + "join fetch u.permission perm "
                        + "join fetch u.contact contact "
                        + "where (ucbm.deleted != true) AND (u.id = :userId) ",
                        UserCertificationBodyMapEntity.class);
        query.setParameter("userId", userId);
        List<UserCertificationBodyMapEntity> result = query.getResultList();

        List<CertificationBodyDTO> dtos = new ArrayList<CertificationBodyDTO>();
        if (result != null) {
            for (UserCertificationBodyMapEntity entity : result) {
                dtos.add(new CertificationBodyDTO(entity.getCertificationBody()));
            }
        }
        return dtos;
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

    private List<CertificationBodyEntity> getEntitiesByDeveloperId(final Long developerId) {
        String hql = "SELECT DISTINCT cp.certificationBody "
                + "FROM CertifiedProductEntity cp "
                + "JOIN FETCH cp.productVersion pv "
                + "JOIN FETCH pv.product prod "
                + "JOIN FETCH prod.developer dev "
                + "WHERE dev.id = :developerId";

        return entityManager.createQuery(hql, CertificationBodyEntity.class)
                .setParameter("developerId", developerId)
                .getResultList();
    }

}
