package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.SearchFilters;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.CertifiedProductDetailsEntity;

@Repository(value = "certifiedProductSearchResultDAO")
public class CertifiedProductSearchResultDAOImpl extends BaseDAOImpl implements
		CertifiedProductSearchResultDAO {

	
	@Override
	public List<CertifiedProductDetailsDTO> getCertifiedProductSearchDetails(Integer pageNum, Integer pageSize) {
		
		List<CertifiedProductDetailsEntity> entities =  getPage(pageNum, pageSize);
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


	@Override
	public List<CertifiedProductDetailsDTO> getSimpleSearchResults(
			String searchTerm, Integer pageNum, Integer pageSize) {
		
		Query query = entityManager.createQuery( "from CertifiedProductDetailsEntity where (NOT deleted = true) AND ((UPPER(vendor_name)  LIKE UPPER(:vendorname)) OR (UPPER(product_name) LIKE UPPER(:productname)))  ", CertifiedProductDetailsEntity.class );
		query.setParameter("vendorname", "%"+searchTerm+"%");
		query.setParameter("productname", "%"+searchTerm+"%");
		
		query.setMaxResults(pageSize);
	    query.setFirstResult(pageNum * pageSize);
		
		List<CertifiedProductDetailsEntity> result = query.getResultList();
		
		List<CertifiedProductDetailsDTO> products = new ArrayList<>();
		
		for (CertifiedProductDetailsEntity entity : result) {
			CertifiedProductDetailsDTO product = new CertifiedProductDetailsDTO(entity);
			products.add(product);
		}
		return products;	
		
	}


	@Override
	public List<CertifiedProductDetailsDTO> multiFilterSearch(
			SearchFilters searchFilters, Integer pageNum, Integer pageSize) {
		// TODO Auto-generated method stub
		return null;
	}	
	
}
