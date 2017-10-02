package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.CQMResultDetailsDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CQMResultDetailsDTO;
import gov.healthit.chpl.entity.listing.CQMResultDetailsEntity;

@Repository(value = "cqmResultDetailsDAO")
public class CQMResultDetailsDAOImpl extends BaseDAOImpl implements
		CQMResultDetailsDAO {


	public List<CQMResultDetailsDTO> getCQMResultDetailsByCertifiedProductId(Long certifiedProductId)throws EntityRetrievalException {

		List<CQMResultDetailsEntity> entities = getEntitiesByCertifiedProductId(certifiedProductId);
		List<CQMResultDetailsDTO> dtos = new ArrayList<CQMResultDetailsDTO>(entities.size());

		for (CQMResultDetailsEntity entity : entities) {
			dtos.add(new CQMResultDetailsDTO(entity));
		}
		return dtos;
	}

	private List<CQMResultDetailsEntity> getEntitiesByCertifiedProductId(Long productId) throws EntityRetrievalException {

		CQMResultDetailsEntity entity = null;

		Query query = entityManager.createQuery( "from CQMResultDetailsEntity where (NOT deleted = true) AND (certified_product_id = :entityid) ", CQMResultDetailsEntity.class );
		query.setParameter("entityid", productId);
		List<CQMResultDetailsEntity> result = query.getResultList();

		return result;
	}

}
