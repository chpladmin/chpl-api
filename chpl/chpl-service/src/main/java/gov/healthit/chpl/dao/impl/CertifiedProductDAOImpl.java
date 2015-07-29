package gov.healthit.chpl.dao.impl;


import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.entity.CertifiedProduct;

import java.util.List;

import javax.persistence.Query;

public class CertifiedProductDAOImpl extends BaseDAOImpl {
	
	
	
	public void create(CertifiedProductDTO product){
		
	}

	public void update(CertifiedProductDTO product){
		
	}
	
	public void delete(Long productId){
		
	}
	
	public List<CertifiedProductDTO> findAll(){
		
	}
	
	public CertifiedProductDTO getById(Long productId){
		
	}
	
	private void create(CertifiedProduct product) {
		
		entityManager.persist(product);
		
	}
	
	private void update(CertifiedProduct product) {
		
		entityManager.merge(product);	
	
	}
	
	private List<CertifiedProduct> getAllEntities() {
		
		List<CertifiedProduct> result = entityManager.createQuery( "from CertifiedProduct  where (NOT deleted = true) ", CertifiedProduct.class).getResultList();
		return result;
	}
	
	private CertifiedProduct getEntityById(Long userId) throws EntityRetrievalException {
		
		CertifiedProduct user = null;
		
		Query query = entityManager.createQuery( "from CertifiedProduct where (NOT deleted = true) AND (user_id = :userid) ", CertifiedProduct.class );
		query.setParameter("userid", userId);
		List<CertifiedProduct> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate Certified Product id in database.");
		}
		
		if (result.size() < 0){
			user = result.get(0);
		}
		
		return user;
	}
	
	
	
}
