package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.CQMResultDetailsDTO;
import gov.healthit.chpl.entity.listing.CQMResultDetailsEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository(value = "cqmResultDetailsDAO")
public class CQMResultDetailsDAO extends BaseDAOImpl {

    @Transactional
    public List<CQMResultDetailsDTO> getCQMResultDetailsByCertifiedProductId(Long certifiedProductId)
            throws EntityRetrievalException {
        List<CQMResultDetailsEntity> entities = getEntitiesByCertifiedProductId(certifiedProductId);
        List<CQMResultDetailsDTO> dtos = new ArrayList<CQMResultDetailsDTO>(entities.size());
        for (CQMResultDetailsEntity entity : entities) {
            dtos.add(new CQMResultDetailsDTO(entity));
        }
        return dtos;
    }

    private List<CQMResultDetailsEntity> getEntitiesByCertifiedProductId(Long productId)
            throws EntityRetrievalException {
        Query query = entityManager.createQuery(
                "FROM CQMResultDetailsEntity "
                + "WHERE (NOT deleted = true) "
                + "AND (certified_product_id = :entityid) ",
                CQMResultDetailsEntity.class);
        query.setParameter("entityid", productId);
        List<CQMResultDetailsEntity> result = query.getResultList();
        return result;
    }

}
