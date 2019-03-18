package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.CertifiedProductTestingLabDAO;
import gov.healthit.chpl.dto.CertifiedProductTestingLabDTO;
import gov.healthit.chpl.entity.listing.CertifiedProductTargetedUserEntity;
import gov.healthit.chpl.entity.listing.CertifiedProductTestingLabMapEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * Implementation of Certified Product Testing Lab DAO.
 * @author alarned
 *
 */
@Repository(value = "certifiedProductTestingLabDao")
public class CertifiedProductTestingLabDAOImpl extends BaseDAOImpl implements CertifiedProductTestingLabDAO {

    @Override
    public CertifiedProductTestingLabDTO createCertifiedProductTestingLab(final CertifiedProductTestingLabDTO toCreate)
            throws EntityCreationException {

        CertifiedProductTestingLabMapEntity toCreateEntity = new CertifiedProductTestingLabMapEntity();
        toCreateEntity.setCertifiedProductId(toCreate.getCertifiedProductId());
        toCreateEntity.setTestingLabId(toCreate.getTestingLabId());
        toCreateEntity.setLastModifiedDate(new Date());
        toCreateEntity.setLastModifiedUser(Util.getAuditId());
        toCreateEntity.setCreationDate(new Date());
        toCreateEntity.setDeleted(false);
        entityManager.persist(toCreateEntity);
        entityManager.flush();

        return new CertifiedProductTestingLabDTO(toCreateEntity);
    }

    @Override
    public CertifiedProductTestingLabDTO deleteCertifiedProductTestingLab(final Long id)
            throws EntityRetrievalException {

        CertifiedProductTestingLabMapEntity curr = getEntityById(id);
        if (curr == null) {
            throw new EntityRetrievalException("Could not find mapping with id " + id);
        }
        curr.setDeleted(true);
        curr.setLastModifiedDate(new Date());
        curr.setLastModifiedUser(Util.getAuditId());
        entityManager.persist(curr);
        entityManager.flush();

        return new CertifiedProductTestingLabDTO(curr);
    }

    @Override
    public List<CertifiedProductTestingLabDTO> getTestingLabsByCertifiedProductId(final Long certifiedProductId)
            throws EntityRetrievalException {
        List<CertifiedProductTestingLabMapEntity> entities = getEntitiesByCertifiedProductId(certifiedProductId);
        List<CertifiedProductTestingLabDTO> dtos = new ArrayList<CertifiedProductTestingLabDTO>();

        for (CertifiedProductTestingLabMapEntity entity : entities) {
            dtos.add(new CertifiedProductTestingLabDTO(entity));
        }
        return dtos;
    }

    @Override
    public CertifiedProductTestingLabDTO lookupMapping(final Long certifiedProductId, final Long tlId)
            throws EntityRetrievalException {
        List<CertifiedProductTestingLabMapEntity> entities = findSpecificMapping(certifiedProductId, tlId);

        CertifiedProductTestingLabDTO result = null;
        if (entities != null && entities.size() > 0) {
            result = new CertifiedProductTestingLabDTO(entities.get(0));
        }
        return result;
    }

    private CertifiedProductTestingLabMapEntity getEntityById(final Long id) throws EntityRetrievalException {
        CertifiedProductTestingLabMapEntity entity = null;
        Query query = entityManager.createQuery(
                "SELECT tl from CertifiedProductTestingLabMapEntity tl " + "LEFT OUTER JOIN FETCH tl.testingLab "
                        + "where (NOT tl.deleted = true) AND (id = :entityid) ",
                CertifiedProductTestingLabMapEntity.class);

        query.setParameter("entityid", id);
        List<CertifiedProductTestingLabMapEntity> result = query.getResultList();
        if (result.size() >= 1) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<CertifiedProductTestingLabMapEntity> getEntitiesByCertifiedProductId(final Long productId)
            throws EntityRetrievalException {
        Query query = entityManager.createQuery(
                "SELECT tl from CertifiedProductTestingLabMapEntity tl " + "LEFT OUTER JOIN FETCH tl.testingLab "
                        + "where (NOT tl.deleted = true) AND (certified_product_id = :entityid) ",
                        CertifiedProductTestingLabMapEntity.class);

        query.setParameter("entityid", productId);
        List<CertifiedProductTestingLabMapEntity> result = query.getResultList();

        return result;
    }

    private List<CertifiedProductTestingLabMapEntity> findSpecificMapping(final Long productId, final Long tlId)
            throws EntityRetrievalException {
        Query query = entityManager
                .createQuery(
                        "SELECT tu from CertifiedProductTestingLabEntity tu "
                                + "LEFT OUTER JOIN FETCH tl.testingLab " + "where (NOT tl.deleted = true) "
                                + "AND (certified_product_id = :productId) " + "AND (tl.testingLabId = :tlId)",
                        CertifiedProductTargetedUserEntity.class);

        query.setParameter("productId", productId);
        query.setParameter("tlId", tlId);
        List<CertifiedProductTestingLabMapEntity> result = query.getResultList();

        return result;
    }
}
