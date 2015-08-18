package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import gov.healthit.chpl.dao.CertifiedProductDetailsDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.CertifiedProductDetailsEntity;


public class CertifiedProductDetailsDAOImpl extends BaseDAOImpl implements
		CertifiedProductDetailsDAO {

	
	@Override
	public List<CertifiedProductDetailsDTO> findAll() {
		
		List<CertifiedProductDetailsEntity> entities = getAllEntities();
		List<CertifiedProductDetailsDTO> products = new ArrayList<>();
		
		for (CertifiedProductDetailsEntity entity : entities) {
			CertifiedProductDetailsDTO product = new CertifiedProductDetailsDTO(entity);
			products.add(product);
		}
		return products;	
	}

	
	public CertifiedProductDetailsDTO getById(Long productId) throws EntityRetrievalException {
		
		CertifiedProductDetailsEntity entity = getEntityById(productId);
		CertifiedProductDetailsDTO dto = new CertifiedProductDetailsDTO(entity);
		return dto;
		
	}
	
	
	private List<CertifiedProductDetailsEntity> getAllEntities() {
		
		List<CertifiedProductDetailsEntity> result = entityManager.createQuery( "from CertifiedProductDetailsEntity where (NOT deleted = true) ", CertifiedProductDetailsEntity.class).getResultList();
		return result;
		
	}
	
	private CertifiedProductDetailsEntity getEntityById(Long entityId) throws EntityRetrievalException {
		
		
		CertifiedProductDetailsEntity entity = null;
		
		Query query = entityManager.createQuery( "from CertifiedProductDetailsEntity where (NOT deleted = true) AND (certified_product_id = :entityid) ", CertifiedProductDetailsEntity.class );
		query.setParameter("entityid", entityId);
		List<CertifiedProductDetailsEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate Certified Product id in database.");
		}
		
		if (result.size() < 0){
			entity = result.get(0);
		}
		
		return entity;
	}	
	
}
