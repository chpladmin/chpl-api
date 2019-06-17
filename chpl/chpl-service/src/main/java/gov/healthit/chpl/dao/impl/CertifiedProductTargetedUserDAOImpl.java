package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.CertifiedProductTargetedUserDAO;
import gov.healthit.chpl.dto.CertifiedProductTargetedUserDTO;
import gov.healthit.chpl.entity.listing.CertifiedProductTargetedUserEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository(value = "certifiedProductTargetedUserDao")
public class CertifiedProductTargetedUserDAOImpl extends BaseDAOImpl implements CertifiedProductTargetedUserDAO {

    @Override
    public CertifiedProductTargetedUserDTO createCertifiedProductTargetedUser(CertifiedProductTargetedUserDTO toCreate)
            throws EntityCreationException {

        CertifiedProductTargetedUserEntity toCreateEntity = new CertifiedProductTargetedUserEntity();
        toCreateEntity.setCertifiedProductId(toCreate.getCertifiedProductId());
        toCreateEntity.setTargetedUserId(toCreate.getTargetedUserId());
        toCreateEntity.setLastModifiedDate(new Date());
        toCreateEntity.setLastModifiedUser(AuthUtil.getAuditId());
        toCreateEntity.setCreationDate(new Date());
        toCreateEntity.setDeleted(false);
        entityManager.persist(toCreateEntity);
        entityManager.flush();

        return new CertifiedProductTargetedUserDTO(toCreateEntity);
    }

    @Override
    public CertifiedProductTargetedUserDTO deleteCertifiedProductTargetedUser(Long id) throws EntityRetrievalException {

        CertifiedProductTargetedUserEntity curr = getEntityById(id);
        if (curr == null) {
            throw new EntityRetrievalException("Could not find mapping with id " + id);
        }
        curr.setDeleted(true);
        curr.setLastModifiedDate(new Date());
        curr.setLastModifiedUser(AuthUtil.getAuditId());
        entityManager.persist(curr);
        entityManager.flush();

        return new CertifiedProductTargetedUserDTO(curr);
    }

    @Override
    public List<CertifiedProductTargetedUserDTO> getTargetedUsersByCertifiedProductId(Long certifiedProductId)
            throws EntityRetrievalException {
        List<CertifiedProductTargetedUserEntity> entities = getEntitiesByCertifiedProductId(certifiedProductId);
        List<CertifiedProductTargetedUserDTO> dtos = new ArrayList<CertifiedProductTargetedUserDTO>();

        for (CertifiedProductTargetedUserEntity entity : entities) {
            dtos.add(new CertifiedProductTargetedUserDTO(entity));
        }
        return dtos;
    }

    @Override
    public CertifiedProductTargetedUserDTO lookupMapping(Long certifiedProductId, Long tuId)
            throws EntityRetrievalException {
        List<CertifiedProductTargetedUserEntity> entities = findSpecificMapping(certifiedProductId, tuId);

        CertifiedProductTargetedUserDTO result = null;
        if (entities != null && entities.size() > 0) {
            result = new CertifiedProductTargetedUserDTO(entities.get(0));
        }
        return result;
    }

    private CertifiedProductTargetedUserEntity getEntityById(Long id) throws EntityRetrievalException {
        CertifiedProductTargetedUserEntity entity = null;
        Query query = entityManager.createQuery(
                "SELECT tu from CertifiedProductTargetedUserEntity tu " + "LEFT OUTER JOIN FETCH tu.targetedUser "
                        + "where (NOT tu.deleted = true) AND (certified_product_targeted_user_id = :entityid) ",
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
                "SELECT tu from CertifiedProductTargetedUserEntity tu " + "LEFT OUTER JOIN FETCH tu.targetedUser "
                        + "where (NOT tu.deleted = true) AND (certified_product_id = :entityid) ",
                CertifiedProductTargetedUserEntity.class);

        query.setParameter("entityid", productId);
        List<CertifiedProductTargetedUserEntity> result = query.getResultList();

        return result;
    }

    private List<CertifiedProductTargetedUserEntity> findSpecificMapping(Long productId, Long tuId)
            throws EntityRetrievalException {
        Query query = entityManager
                .createQuery(
                        "SELECT tu from CertifiedProductTargetedUserEntity tu "
                                + "LEFT OUTER JOIN FETCH tu.targetedUser " + "where (NOT tu.deleted = true) "
                                + "AND (certified_product_id = :productId) " + "AND (tu.targetedUserId = :tuId)",
                        CertifiedProductTargetedUserEntity.class);

        query.setParameter("productId", productId);
        query.setParameter("tuId", tuId);
        List<CertifiedProductTargetedUserEntity> result = query.getResultList();

        return result;
    }

}
