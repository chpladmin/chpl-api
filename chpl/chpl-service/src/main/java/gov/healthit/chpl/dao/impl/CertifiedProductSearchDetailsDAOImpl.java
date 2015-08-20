package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.CertifiedProductSearchDetailsDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertifiedProductSearchDetailsDTO;
import gov.healthit.chpl.entity.CertifiedProductDetailsEntity;

@Repository(value = "certifiedProductSearchDetailsDAO")
public class CertifiedProductSearchDetailsDAOImpl extends BaseDAOImpl implements
		CertifiedProductSearchDetailsDAO {

	
	@Override
	public List<CertifiedProductSearchDetailsDTO> getCertifiedProductSearchDetails(Integer pageNum, Integer pageSize) {
		
		List<CertifiedProductDetailsEntity> entities =  getPage(pageNum, pageSize);
		List<CertifiedProductSearchDetailsDTO> products = new ArrayList<>();
		
		for (CertifiedProductDetailsEntity entity : entities) {
			CertifiedProductSearchDetailsDTO product = new CertifiedProductSearchDetailsDTO(entity);
			products.add(product);
		}
		return products;	
	}

	
	public CertifiedProductSearchDetailsDTO getById(Long productId) throws EntityRetrievalException {
		
		CertifiedProductDetailsEntity entity = getEntityById(productId);
		CertifiedProductSearchDetailsDTO dto = new CertifiedProductSearchDetailsDTO(entity);
		return dto;
		
	}
	
	
	private List<CertifiedProductDetailsEntity> getPage(Integer pageNum, Integer pageSize) {
		
		Query query = entityManager.createQuery( "from CertifiedProductDetailsEntity where (NOT deleted = true) ", CertifiedProductDetailsEntity.class);
		query.setMaxResults(pageSize);
	    query.setFirstResult(pageNum * pageSize);
		
		List<CertifiedProductDetailsEntity> result = query.getResultList();
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
		
		if (result.size() > 0){
			entity = result.get(0);
		}
		
		return entity;
	}	
	
}
