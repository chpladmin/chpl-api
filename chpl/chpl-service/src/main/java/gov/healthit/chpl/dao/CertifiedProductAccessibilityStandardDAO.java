package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.dto.CertifiedProductAccessibilityStandardDTO;
import gov.healthit.chpl.entity.listing.CertifiedProductAccessibilityStandardEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import jakarta.persistence.Query;

@Repository(value = "certifiedProductAccessibilityStandardDao")
public class CertifiedProductAccessibilityStandardDAO extends BaseDAOImpl {

    public Long createListingAccessibilityStandardMapping(Long listingId, CertifiedProductAccessibilityStandard accStdMapping)
            throws EntityCreationException {
        try {
            CertifiedProductAccessibilityStandardEntity mappingEntity = new CertifiedProductAccessibilityStandardEntity();
            mappingEntity.setCertifiedProductId(listingId);
            mappingEntity.setAccessibilityStandardId(accStdMapping.getAccessibilityStandardId());
            create(mappingEntity);
            return mappingEntity.getId();
        } catch (Exception ex) {
            throw new EntityCreationException(ex);
        }
    }

    public CertifiedProductAccessibilityStandardDTO createCertifiedProductAccessibilityStandard(
            CertifiedProductAccessibilityStandardDTO toCreate) throws EntityCreationException {
        CertifiedProductAccessibilityStandardEntity toCreateEntity = new CertifiedProductAccessibilityStandardEntity();
        toCreateEntity.setCertifiedProductId(toCreate.getCertifiedProductId());
        toCreateEntity.setAccessibilityStandardId(toCreate.getAccessibilityStandardId());
        toCreateEntity.setDeleted(false);
        create(toCreateEntity);
        return new CertifiedProductAccessibilityStandardDTO(toCreateEntity);
    }

    public CertifiedProductAccessibilityStandardDTO deleteCertifiedProductAccessibilityStandards(Long id)
            throws EntityRetrievalException {

        CertifiedProductAccessibilityStandardEntity curr = getEntityById(id);
        if (curr == null) {
            throw new EntityRetrievalException("Could not find mapping with id " + id);
        }
        curr.setDeleted(true);
        update(curr);
        return new CertifiedProductAccessibilityStandardDTO(curr);
    }

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
                "SELECT accStd "
                + "FROM CertifiedProductAccessibilityStandardEntity accStd "
                + "LEFT OUTER JOIN FETCH accStd.accessibilityStandard "
                + "WHERE (NOT accStd.deleted = true) "
                + "AND (accStd.id = :entityid) ",
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
                "SELECT accStd from "
                        + "CertifiedProductAccessibilityStandardEntity accStd "
                        + "LEFT OUTER JOIN FETCH accStd.accessibilityStandard "
                        + "WHERE (NOT accStd.deleted = true) "
                        + "AND (certifiedProductId = :entityid) ",
                CertifiedProductAccessibilityStandardEntity.class);

        query.setParameter("entityid", productId);
        List<CertifiedProductAccessibilityStandardEntity> result = query.getResultList();

        return result;
    }

    private List<CertifiedProductAccessibilityStandardEntity> findSpecificMapping(Long productId, Long accStdId)
            throws EntityRetrievalException {
        Query query = entityManager.createQuery(
                "SELECT accStd from "
                        + "CertifiedProductAccessibilityStandardEntity accStd "
                        + "LEFT OUTER JOIN FETCH accStd.accessibilityStandard "
                        + "WHERE (NOT accStd.deleted = true) "
                        + "AND (certifiedProductId = :productId) "
                        + "AND (accStd.accessibilityStandardId = :accStdId)",
                CertifiedProductAccessibilityStandardEntity.class);

        query.setParameter("productId", productId);
        query.setParameter("accStdId", accStdId);
        List<CertifiedProductAccessibilityStandardEntity> result = query.getResultList();

        return result;
    }
}
