package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.dto.CertifiedProductQmsStandardDTO;
import gov.healthit.chpl.entity.listing.CertifiedProductQmsStandardEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.qmsStandard.QmsStandardDAO;

@Repository(value = "certifiedProductQmsStandardDao")
public class CertifiedProductQmsStandardDAO extends BaseDAOImpl {
    private QmsStandardDAO qmsDao;

    @Autowired
    public CertifiedProductQmsStandardDAO(QmsStandardDAO qmsDao) {
        this.qmsDao = qmsDao;
    }

    public Long createListingQmsStandardMapping(Long listingId, CertifiedProductQmsStandard qmsMapping)
        throws EntityCreationException {
        try {
            CertifiedProductQmsStandardEntity mappingEntity = new CertifiedProductQmsStandardEntity();
            mappingEntity.setCertifiedProductId(listingId);
            mappingEntity.setQmsStandardId(qmsMapping.getQmsStandardId());
            mappingEntity.setApplicableCriteria(qmsMapping.getApplicableCriteria());
            mappingEntity.setModification(qmsMapping.getQmsModification());
            create(mappingEntity);
            return mappingEntity.getId();
        } catch (Exception ex) {
            throw new EntityCreationException(ex);
        }
    }

    public CertifiedProductQmsStandardDTO createCertifiedProductQms(CertifiedProductQmsStandardDTO toCreate)
            throws EntityCreationException {
        CertifiedProductQmsStandardEntity toCreateEntity = new CertifiedProductQmsStandardEntity();
        toCreateEntity.setCertifiedProductId(toCreate.getCertifiedProductId());
        toCreateEntity.setQmsStandardId(toCreate.getQmsStandardId());
        toCreateEntity.setApplicableCriteria(toCreate.getApplicableCriteria());
        toCreateEntity.setModification(toCreate.getQmsModification());
        toCreateEntity.setDeleted(false);
        create(toCreateEntity);
        return new CertifiedProductQmsStandardDTO(toCreateEntity);
    }

    public CertifiedProductQmsStandardDTO deleteCertifiedProductQms(Long id) throws EntityRetrievalException {
        CertifiedProductQmsStandardEntity curr = getEntityById(id);
        if (curr == null) {
            throw new EntityRetrievalException("Could not find mapping with id " + id);
        }
        curr.setDeleted(true);
        update(curr);
        return new CertifiedProductQmsStandardDTO(curr);
    }

    public List<CertifiedProductQmsStandardDTO> getQmsStandardsByCertifiedProductId(Long certifiedProductId)
            throws EntityRetrievalException {
        List<CertifiedProductQmsStandardEntity> entities = getEntitiesByCertifiedProductId(certifiedProductId);
        List<CertifiedProductQmsStandardDTO> dtos = new ArrayList<CertifiedProductQmsStandardDTO>();

        for (CertifiedProductQmsStandardEntity entity : entities) {
            dtos.add(new CertifiedProductQmsStandardDTO(entity));
        }
        return dtos;
    }

    public CertifiedProductQmsStandardDTO lookupMapping(Long certifiedProductId, Long qmsStandardId)
            throws EntityRetrievalException {
        List<CertifiedProductQmsStandardEntity> entities = findSpecificMapping(certifiedProductId, qmsStandardId);

        CertifiedProductQmsStandardDTO result = null;
        if (entities != null && entities.size() > 0) {
            result = new CertifiedProductQmsStandardDTO(entities.get(0));
        }
        return result;
    }

    private CertifiedProductQmsStandardEntity getEntityById(Long id) throws EntityRetrievalException {
        CertifiedProductQmsStandardEntity entity = null;
        Query query = entityManager.createQuery(
                "SELECT qms from CertifiedProductQmsStandardEntity qms "
                        + "LEFT OUTER JOIN FETCH qms.qmsStandard "
                        + "where (NOT qms.deleted = true) "
                        + "AND (certified_product_qms_standard_id = :entityid) ",
                CertifiedProductQmsStandardEntity.class);

        query.setParameter("entityid", id);
        List<CertifiedProductQmsStandardEntity> result = query.getResultList();
        if (result.size() >= 1) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<CertifiedProductQmsStandardEntity> getEntitiesByCertifiedProductId(Long productId)
            throws EntityRetrievalException {
        Query query = entityManager.createQuery(
                "SELECT qms from CertifiedProductQmsStandardEntity qms "
                        + "LEFT OUTER JOIN FETCH qms.qmsStandard "
                        + "where (NOT qms.deleted = true) "
                        + "AND (certified_product_id = :entityid) ",
                CertifiedProductQmsStandardEntity.class);

        query.setParameter("entityid", productId);
        List<CertifiedProductQmsStandardEntity> result = query.getResultList();

        return result;
    }

    private List<CertifiedProductQmsStandardEntity> findSpecificMapping(Long productId, Long qmsId)
            throws EntityRetrievalException {
        Query query = entityManager
                .createQuery(
                        "SELECT qms from CertifiedProductQmsStandardEntity qms "
                                + "LEFT OUTER JOIN FETCH qms.qmsStandard "
                                + "where (NOT qms.deleted = true) "
                                + "AND (certified_product_id = :productId) "
                                + "AND (qms.qmsStandardId = :qmsId)",
                        CertifiedProductQmsStandardEntity.class);

        query.setParameter("productId", productId);
        query.setParameter("qmsId", qmsId);
        List<CertifiedProductQmsStandardEntity> result = query.getResultList();

        return result;
    }
}
