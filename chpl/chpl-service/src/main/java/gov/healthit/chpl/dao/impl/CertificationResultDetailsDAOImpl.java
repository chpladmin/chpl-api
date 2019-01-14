package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.entity.listing.CertificationResultDetailsEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository(value = "certificationResultDetailsDAO")
public class CertificationResultDetailsDAOImpl extends BaseDAOImpl implements CertificationResultDetailsDAO {

    @Override
    @Transactional
    public List<CertificationResultDetailsDTO> getCertificationResultDetailsByCertifiedProductId(
            Long certifiedProductId) throws EntityRetrievalException {
        List<CertificationResultDetailsEntity> entities = getEntitiesByCertifiedProductId(certifiedProductId);
        List<CertificationResultDetailsDTO> dtos = new ArrayList<CertificationResultDetailsDTO>(entities.size());

        for (CertificationResultDetailsEntity entity : entities) {
            dtos.add(new CertificationResultDetailsDTO(entity));
        }
        return dtos;
    }

    private List<CertificationResultDetailsEntity> getEntitiesByCertifiedProductId(Long productId)
            throws EntityRetrievalException {

        CertificationResultDetailsEntity entity = null;

        Query query = entityManager.createQuery(
                "from CertificationResultDetailsEntity where (NOT deleted = true) AND (certified_product_id = :entityid) ",
                CertificationResultDetailsEntity.class);
        query.setParameter("entityid", productId);
        List<CertificationResultDetailsEntity> result = query.getResultList();

        return result;
    }

    @Override
    public List<CertificationResultDetailsDTO> getCertificationResultDetailsByCertifiedProductIdSED(
            Long certifiedProductId) throws EntityRetrievalException {
        List<CertificationResultDetailsEntity> entities = getEntitiesByCertifiedProductIdSED(certifiedProductId);
        List<CertificationResultDetailsDTO> dtos = new ArrayList<CertificationResultDetailsDTO>(entities.size());

        for (CertificationResultDetailsEntity entity : entities) {
            dtos.add(new CertificationResultDetailsDTO(entity));
        }
        return dtos;
    }

    private List<CertificationResultDetailsEntity> getEntitiesByCertifiedProductIdSED(Long productId)
            throws EntityRetrievalException {

        CertificationResultDetailsEntity entity = null;

        Query query = entityManager.createQuery(
                "from CertificationResultDetailsEntity where (NOT deleted = true) AND (certified_product_id = :entityid) AND (success = true) AND (sed = true) ",
                CertificationResultDetailsEntity.class);
        query.setParameter("entityid", productId);
        List<CertificationResultDetailsEntity> result = query.getResultList();

        return result;
    }

}
