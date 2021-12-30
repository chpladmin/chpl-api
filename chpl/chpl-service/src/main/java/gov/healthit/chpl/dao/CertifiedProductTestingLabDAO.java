package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.CertifiedProductTestingLabDTO;
import gov.healthit.chpl.entity.listing.CertifiedProductTargetedUserEntity;
import gov.healthit.chpl.entity.listing.CertifiedProductTestingLabMapEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository(value = "certifiedProductTestingLabDao")
public class CertifiedProductTestingLabDAO extends BaseDAOImpl {

    public Long createListingTestingLabMapping(Long listingId, Long atlId) {
        CertifiedProductTestingLabMapEntity mappingEntity = new CertifiedProductTestingLabMapEntity();
        mappingEntity.setCertifiedProductId(listingId);
        mappingEntity.setTestingLabId(atlId);
        mappingEntity.setLastModifiedUser(AuthUtil.getAuditId());
        create(mappingEntity);
        return mappingEntity.getId();
    }

    public CertifiedProductTestingLabDTO createCertifiedProductTestingLab(CertifiedProductTestingLabDTO toCreate)
            throws EntityCreationException {

        CertifiedProductTestingLabMapEntity toCreateEntity = new CertifiedProductTestingLabMapEntity();
        toCreateEntity.setCertifiedProductId(toCreate.getCertifiedProductId());
        toCreateEntity.setTestingLabId(toCreate.getTestingLabId());
        toCreateEntity.setLastModifiedDate(new Date());
        toCreateEntity.setLastModifiedUser(AuthUtil.getAuditId());
        toCreateEntity.setCreationDate(new Date());
        toCreateEntity.setDeleted(false);
        create(toCreateEntity);

        return new CertifiedProductTestingLabDTO(toCreateEntity);
    }

    public CertifiedProductTestingLabDTO deleteCertifiedProductTestingLab(Long id)
            throws EntityRetrievalException {

        CertifiedProductTestingLabMapEntity curr = getEntityById(id);
        if (curr == null) {
            throw new EntityRetrievalException("Could not find mapping with id " + id);
        }
        curr.setDeleted(true);
        curr.setLastModifiedDate(new Date());
        curr.setLastModifiedUser(AuthUtil.getAuditId());
        update(curr);

        return new CertifiedProductTestingLabDTO(curr);
    }

    public List<CertifiedProductTestingLabDTO> getTestingLabsByCertifiedProductId(Long certifiedProductId)
            throws EntityRetrievalException {
        List<CertifiedProductTestingLabMapEntity> entities = getEntitiesByCertifiedProductId(certifiedProductId);
        List<CertifiedProductTestingLabDTO> dtos = new ArrayList<CertifiedProductTestingLabDTO>();

        for (CertifiedProductTestingLabMapEntity entity : entities) {
            dtos.add(new CertifiedProductTestingLabDTO(entity));
        }
        return dtos;
    }

    public CertifiedProductTestingLabDTO lookupMapping(Long certifiedProductId, Long tlId)
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
                "SELECT tl from CertifiedProductTestingLabMapEntity tl "
                        + "LEFT OUTER JOIN FETCH tl.testingLab "
                        + "WHERE (NOT tl.deleted = true) "
                        + "AND (id = :entityid) ",
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
                "SELECT tl from CertifiedProductTestingLabMapEntity tl "
                        + "LEFT OUTER JOIN FETCH tl.testingLab "
                        + "WHERE (NOT tl.deleted = true) "
                        + "AND (certified_product_id = :entityid) ",
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
                                + "LEFT OUTER JOIN FETCH tl.testingLab "
                                + "WHERE (NOT tl.deleted = true) "
                                + "AND (certified_product_id = :productId) "
                                + "AND (tl.testingLabId = :tlId)",
                        CertifiedProductTargetedUserEntity.class);

        query.setParameter("productId", productId);
        query.setParameter("tlId", tlId);
        List<CertifiedProductTestingLabMapEntity> result = query.getResultList();

        return result;
    }
}
