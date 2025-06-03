package gov.healthit.chpl.targeteduser;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import jakarta.persistence.Query;

@Repository(value = "certifiedProductTargetedUserDao")
public class CertifiedProductTargetedUserDAO extends BaseDAOImpl {
    private TargetedUserDAO targetedUserDao;

    @Autowired
    public CertifiedProductTargetedUserDAO(TargetedUserDAO targetedUserDao) {
        this.targetedUserDao = targetedUserDao;
    }

    public Long createListingTargetedUserMapping(Long listingId, CertifiedProductTargetedUser targetedUserMapping)
            throws EntityCreationException {
        try {
            CertifiedProductTargetedUserEntity mappingEntity = new CertifiedProductTargetedUserEntity();
            mappingEntity.setCertifiedProductId(listingId);

            if (targetedUserMapping.getTargetedUserId() == null) {
                Long targetedUserId = targetedUserDao.create(targetedUserMapping.getTargetedUserName());
                mappingEntity.setTargetedUserId(targetedUserId);
            } else {
                mappingEntity.setTargetedUserId(targetedUserMapping.getTargetedUserId());
            }

            create(mappingEntity);
            return mappingEntity.getId();
        } catch (Exception ex) {
            throw new EntityCreationException(ex);
        }
    }

    public void createCertifiedProductTargetedUser(Long certifiedProductId, TargetedUser toCreate)
            throws EntityCreationException {
        CertifiedProductTargetedUserEntity toCreateEntity = new CertifiedProductTargetedUserEntity();
        toCreateEntity.setCertifiedProductId(certifiedProductId);
        toCreateEntity.setTargetedUserId(toCreate.getId());
        toCreateEntity.setDeleted(false);
        create(toCreateEntity);
    }

    public void deleteCertifiedProductTargetedUser(Long id) throws EntityRetrievalException {
        CertifiedProductTargetedUserEntity curr = getEntityById(id);
        if (curr == null) {
            throw new EntityRetrievalException("Could not find mapping with id " + id);
        }
        curr.setDeleted(true);
        entityManager.persist(curr);
        entityManager.flush();
    }

    public List<CertifiedProductTargetedUser> getTargetedUsersByCertifiedProductId(Long certifiedProductId)
            throws EntityRetrievalException {
        List<CertifiedProductTargetedUserEntity> entities = getEntitiesByCertifiedProductId(certifiedProductId);
        return entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.toList());
    }

    private CertifiedProductTargetedUserEntity getEntityById(Long id) throws EntityRetrievalException {
        CertifiedProductTargetedUserEntity entity = null;
        Query query = entityManager.createQuery(
                "SELECT tu from CertifiedProductTargetedUserEntity tu "
                        + "LEFT OUTER JOIN FETCH tu.targetedUser "
                        + "where (NOT tu.deleted = true) "
                        + "AND (tu.id = :entityid) ",
                CertifiedProductTargetedUserEntity.class);

        query.setParameter("entityid", id);
        List<CertifiedProductTargetedUserEntity> result = query.getResultList();
        if (result.size() >= 1) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<CertifiedProductTargetedUserEntity> getEntitiesByCertifiedProductId(Long productId)
            throws EntityRetrievalException {
        Query query = entityManager.createQuery(
                "SELECT tu from CertifiedProductTargetedUserEntity tu "
                        + "LEFT OUTER JOIN FETCH tu.targetedUser "
                        + "where (NOT tu.deleted = true) "
                        + "AND (certifiedProductId = :entityid) ",
                CertifiedProductTargetedUserEntity.class);

        query.setParameter("entityid", productId);
        List<CertifiedProductTargetedUserEntity> result = query.getResultList();

        return result;
    }
}
