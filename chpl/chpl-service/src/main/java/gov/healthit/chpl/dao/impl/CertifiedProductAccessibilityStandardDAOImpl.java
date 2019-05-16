package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.CertifiedProductAccessibilityStandardDAO;
import gov.healthit.chpl.dto.CertifiedProductAccessibilityStandardDTO;
import gov.healthit.chpl.entity.listing.CertifiedProductAccessibilityStandardEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository(value = "certifiedProductAccessibilityStandardDao")
public class CertifiedProductAccessibilityStandardDAOImpl extends BaseDAOImpl
        implements CertifiedProductAccessibilityStandardDAO {

    @Override
    public CertifiedProductAccessibilityStandardDTO createCertifiedProductAccessibilityStandard(
            CertifiedProductAccessibilityStandardDTO toCreate) throws EntityCreationException {

        CertifiedProductAccessibilityStandardEntity toCreateEntity = new CertifiedProductAccessibilityStandardEntity();
        toCreateEntity.setCertifiedProductId(toCreate.getCertifiedProductId());
        toCreateEntity.setAccessibilityStandardId(toCreate.getAccessibilityStandardId());
        toCreateEntity.setLastModifiedDate(new Date());
        toCreateEntity.setLastModifiedUser(AuthUtil.getAuditId());
        toCreateEntity.setCreationDate(new Date());
        toCreateEntity.setDeleted(false);
        entityManager.persist(toCreateEntity);
        entityManager.flush();

        return new CertifiedProductAccessibilityStandardDTO(toCreateEntity);
    }

    @Override
    public CertifiedProductAccessibilityStandardDTO deleteCertifiedProductAccessibilityStandards(Long id)
            throws EntityRetrievalException {

        CertifiedProductAccessibilityStandardEntity curr = getEntityById(id);
        if (curr == null) {
            throw new EntityRetrievalException("Could not find mapping with id " + id);
        }
        curr.setDeleted(true);
        curr.setLastModifiedDate(new Date());
        curr.setLastModifiedUser(AuthUtil.getAuditId());
        entityManager.persist(curr);
        entityManager.flush();

        return new CertifiedProductAccessibilityStandardDTO(curr);
    }

    @Override
    public List<CertifiedProductAccessibilityStandardDTO> getAccessibilityStandardsByCertifiedProductId(
            Long certifiedProductId) throws EntityRetrievalException {
        List<CertifiedProductAccessibilityStandardEntity> entities = getEntitiesByCertifiedProductId(
                certifiedProductId);
        List<CertifiedProductAccessibilityStandardDTO> dtos = new ArrayList<CertifiedProductAccessibilityStandardDTO>();

        for (CertifiedProductAccessibilityStandardEntity entity : entities) {
            dtos.add(new CertifiedProductAccessibilityStandardDTO(entity));
        }
        return dtos;
    }

    @Override
    public CertifiedProductAccessibilityStandardDTO lookupMapping(Long certifiedProductId, Long accStdId)
            throws EntityRetrievalException {
        List<CertifiedProductAccessibilityStandardEntity> entities = findSpecificMapping(certifiedProductId, accStdId);

        CertifiedProductAccessibilityStandardDTO result = null;
        if (entities != null && entities.size() > 0) {
            result = new CertifiedProductAccessibilityStandardDTO(entities.get(0));
        }
        return result;
    }

    private CertifiedProductAccessibilityStandardEntity getEntityById(Long id) throws EntityRetrievalException {
        CertifiedProductAccessibilityStandardEntity entity = null;
        Query query = entityManager.createQuery(
                "SELECT accStd from CertifiedProductAccessibilityStandardEntity accStd "
                        + "LEFT OUTER JOIN FETCH accStd.accessibilityStandard "
                        + "where (NOT accStd.deleted = true) AND (accStd.id = :entityid) ",
                CertifiedProductAccessibilityStandardEntity.class);

        query.setParameter("entityid", id);
        List<CertifiedProductAccessibilityStandardEntity> result = query.getResultList();
        if (result.size() >= 1) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<CertifiedProductAccessibilityStandardEntity> getEntitiesByCertifiedProductId(Long productId)
            throws EntityRetrievalException {
        Query query = entityManager.createQuery(
                "SELECT accStd from " + "CertifiedProductAccessibilityStandardEntity accStd "
                        + "LEFT OUTER JOIN FETCH accStd.accessibilityStandard "
                        + "where (NOT accStd.deleted = true) AND " + "(certified_product_id = :entityid) ",
                CertifiedProductAccessibilityStandardEntity.class);

        query.setParameter("entityid", productId);
        List<CertifiedProductAccessibilityStandardEntity> result = query.getResultList();

        return result;
    }

    private List<CertifiedProductAccessibilityStandardEntity> findSpecificMapping(Long productId, Long accStdId)
            throws EntityRetrievalException {
        Query query = entityManager.createQuery(
                "SELECT accStd from " + "CertifiedProductAccessibilityStandardEntity accStd "
                        + "LEFT OUTER JOIN FETCH accStd.accessibilityStandard " + "where (NOT accStd.deleted = true) "
                        + "AND (certified_product_id = :productId) "
                        + "AND (accStd.accessibilityStandardId = :accStdId)",
                CertifiedProductAccessibilityStandardEntity.class);

        query.setParameter("productId", productId);
        query.setParameter("accStdId", accStdId);
        List<CertifiedProductAccessibilityStandardEntity> result = query.getResultList();

        return result;
    }
}
