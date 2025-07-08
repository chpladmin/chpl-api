package gov.healthit.chpl.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertifiedProductTestingLab;
import gov.healthit.chpl.entity.listing.CertifiedProductTestingLabMapEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import jakarta.persistence.Query;

@Repository(value = "certifiedProductTestingLabDao")
public class CertifiedProductTestingLabDAO extends BaseDAOImpl {

    public Long createListingTestingLabMapping(Long listingId, Long atlId) throws EntityCreationException {
        try {
            CertifiedProductTestingLabMapEntity mappingEntity = new CertifiedProductTestingLabMapEntity();
            mappingEntity.setCertifiedProductId(listingId);
            mappingEntity.setTestingLabId(atlId);
            create(mappingEntity);
            return mappingEntity.getId();
        } catch (Exception ex) {
            throw new EntityCreationException(ex);
        }
    }

    public void createCertifiedProductTestingLab(CertifiedProductTestingLab toCreate, Long certifiedProductId)
            throws EntityCreationException {

        CertifiedProductTestingLabMapEntity toCreateEntity = new CertifiedProductTestingLabMapEntity();
        toCreateEntity.setCertifiedProductId(certifiedProductId);
        toCreateEntity.setTestingLabId(toCreate.getTestingLab().getId());
        toCreateEntity.setDeleted(false);
        create(toCreateEntity);
    }

    public void deleteCertifiedProductTestingLab(Long id) throws EntityRetrievalException {
        CertifiedProductTestingLabMapEntity curr = getEntityById(id);
        if (curr == null) {
            throw new EntityRetrievalException("Could not find mapping with id " + id);
        }
        curr.setDeleted(true);
        update(curr);
    }

    public List<CertifiedProductTestingLab> getTestingLabsByCertifiedProductId(Long certifiedProductId) throws EntityRetrievalException {
        return getEntitiesByCertifiedProductId(certifiedProductId).stream()
                .map(entity -> entity.toDomain())
                .toList();
    }

    private CertifiedProductTestingLabMapEntity getEntityById(final Long id) throws EntityRetrievalException {
        CertifiedProductTestingLabMapEntity entity = null;
        Query query = entityManager.createQuery(
                "SELECT tlm from CertifiedProductTestingLabMapEntity tlm "
                        + "LEFT OUTER JOIN FETCH tlm.testingLab "
                        + "WHERE (NOT tlm.deleted = true) "
                        + "AND (tlm.id = :entityid) ",
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
                        + "AND (certifiedProductId = :entityid) ",
                        CertifiedProductTestingLabMapEntity.class);

        query.setParameter("entityid", productId);
        List<CertifiedProductTestingLabMapEntity> result = query.getResultList();

        return result;
    }
}
