package gov.healthit.chpl.dao;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.entity.CertificationBodyEntity;
import gov.healthit.chpl.entity.UserCertificationBodyMapEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository(value = "certificationBodyDAO")
public class CertificationBodyDAO extends BaseDAOImpl {
    private AddressDAO addressDao;

    @Autowired
    public CertificationBodyDAO(AddressDAO addressDao) {
        this.addressDao = addressDao;
    }

    @Transactional
    public CertificationBody create(CertificationBody acb)
            throws EntityRetrievalException, EntityCreationException {
        CertificationBodyEntity entity = null;
        try {
            if (acb.getId() != null) {
                entity = this.getEntityById(acb.getId());
            }
        } catch (final EntityRetrievalException e) {
            throw new EntityCreationException(e);
        }

        if (entity != null) {
            throw new EntityCreationException("An entity with this ID already exists.");
        } else {
            entity = new CertificationBodyEntity();
            Long addressId = addressDao.create(acb.getAddress());
            entity.setAddress(addressDao.getEntityById(addressId));
            entity.setName(acb.getName());
            entity.setWebsite(acb.getWebsite());
            entity.setAcbCode(acb.getAcbCode());
            entity.setRetired(Boolean.FALSE);
            entity.setRetirementDate(null);
            entity.setLastModifiedUser(AuthUtil.getAuditId());
            create(entity);
            return entity.toDomain();
        }
    }

    @Transactional
    public CertificationBody update(CertificationBody acb) throws EntityRetrievalException {

        CertificationBodyEntity entity = getEntityById(acb.getId());
        if (entity == null) {
            throw new EntityRetrievalException(
                    "Cannot update entity with id " + acb.getId() + ". Entity does not exist.");
        }

        if (acb.getAddress() != null) {
            try {
                Long addressId = addressDao.saveAddress(acb.getAddress());
                entity.setAddress(addressDao.getEntityById(addressId));
            } catch (final EntityCreationException ex) {
                LOGGER.error("Could not create new address in the database.", ex);
                entity.setAddress(null);
            }
        } else {
            entity.setAddress(null);
        }

        entity.setWebsite(acb.getWebsite());
        entity.setRetired(acb.isRetired());
        entity.setRetirementDate(acb.getRetirementDate());

        if (acb.getName() != null) {
            entity.setName(acb.getName());
        }

        if (acb.getAcbCode() != null) {
            entity.setAcbCode(acb.getAcbCode());
        }

        entity.setLastModifiedUser(AuthUtil.getAuditId());
        update(entity);
        return entity.toDomain();
    }

    public List<CertificationBody> findAll() {
        List<CertificationBodyEntity> entities = getAllEntities();
        return entities.stream()
                    .map(entity -> entity.toDomain())
                    .collect(Collectors.toList());
    }

    public List<CertificationBody> findAllActive() {
        List<CertificationBodyEntity> entities = entityManager
                .createQuery("SELECT acb "
                        + "FROM CertificationBodyEntity acb "
                        + "LEFT OUTER JOIN FETCH acb.address "
                        + "WHERE acb.retired = false "
                        + "AND acb.deleted = false", CertificationBodyEntity.class)
                .getResultList();
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    public CertificationBody getById(Long acbId) throws EntityRetrievalException {
        CertificationBodyEntity entity = getEntityById(acbId);
        return entity.toDomain();
    }

    public CertificationBody getByName(String name) {
        CertificationBodyEntity entity = getEntityByName(name);
        if (entity == null) {
            return null;
        }
        return entity.toDomain();
    }

    public CertificationBody getByCode(String code) {
        Query query = entityManager.createQuery("SELECT acb "
                + "FROM CertificationBodyEntity acb "
                + "LEFT OUTER JOIN FETCH acb.address "
                + "WHERE (acb.deleted = false) "
                + "AND (UPPER(acb.acbCode) = :code) ",
                CertificationBodyEntity.class);
        query.setParameter("code", code.toUpperCase());
        List<CertificationBodyEntity> result = query.getResultList();

        if (result != null && result.size() > 0) {
            return result.get(0).toDomain();
        }
        return null;
    }

    public List<CertificationBody> getByWebsite(String website) {
        Query query = entityManager.createQuery("SELECT acb "
                + "FROM CertificationBodyEntity acb "
                + "LEFT OUTER JOIN FETCH acb.address "
                + "WHERE acb.deleted = false "
                + "AND acb.website = :website");
        query.setParameter("website", website);
        List<CertificationBodyEntity> results = query.getResultList();
        return results.stream()
                .map(result -> result.toDomain())
                .collect(Collectors.toList());
    }

    public String getMaxCode() {
        String maxCode = null;
        Query query = entityManager.createQuery("SELECT acb.acbCode "
                        + "FROM CertificationBodyEntity acb "
                        + "ORDER BY acb.acbCode DESC",
                String.class);
        List<String> result = query.getResultList();

        if (result != null && result.size() > 0) {
            maxCode = result.get(0);
        }
        return maxCode;
    }


    public List<CertificationBody> getByDeveloperId(Long developerId) {
        return getEntitiesByDeveloperId(developerId).stream()
                .map(acb -> acb.toDomain())
                .collect(Collectors.<CertificationBody>toList());
    }


    public List<CertificationBody> getCertificationBodiesByUserId(Long userId) {
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
        List<UserCertificationBodyMapEntity> results = query.getResultList();
        return results.stream()
                .map(result -> result.getCertificationBody().toDomain())
                .collect(Collectors.toList());
    }

    private List<CertificationBodyEntity> getAllEntities() {
        return entityManager.createQuery("SELECT acb "
                + "FROM CertificationBodyEntity acb "
                + "LEFT OUTER JOIN FETCH acb.address "
                + "WHERE (acb.deleted = false)", CertificationBodyEntity.class)
                .getResultList();
    }

    private CertificationBodyEntity getEntityById(Long entityId) throws EntityRetrievalException {
        CertificationBodyEntity entity = null;

        String queryStr = "SELECT acb "
                + "FROM CertificationBodyEntity acb "
                + "LEFT OUTER JOIN FETCH acb.address "
                + "WHERE (acb.id = :entityid) "
                + "AND (acb.deleted = false)";

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

    private CertificationBodyEntity getEntityByName(String name) {
        CertificationBodyEntity entity = null;

        Query query = entityManager.createQuery("SELECT acb "
                + "FROM CertificationBodyEntity acb "
                + "LEFT OUTER JOIN FETCH acb.address "
                + "WHERE (acb.deleted = false) "
                + "AND (UPPER(acb.name) = :name) ",
                CertificationBodyEntity.class);
        query.setParameter("name", name.toUpperCase());
        List<CertificationBodyEntity> result = query.getResultList();

        if (result != null && result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<CertificationBodyEntity> getEntitiesByDeveloperId(Long developerId) {
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
