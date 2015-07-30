package gov.healthit.chpl.dao.impl;



import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.entity.CertifiedProduct;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

public class CertifiedProductDAOImpl extends BaseDAOImpl {
	
	
	
	public void create(CertifiedProductDTO product) throws EntityCreationException{
		
		CertifiedProduct productEntity = null;
		try {
			if (product.getId() != null){
				productEntity = this.getEntityById(product.getId());
			}
		} catch (EntityRetrievalException e) {
			throw new EntityCreationException(e);
		}
		
		if (productEntity != null) {
			throw new EntityCreationException("A product with this ID already exists.");
		} else {
			
			productEntity = new CertifiedProduct();
			
			productEntity.setAtcbCertificationId(product.getAtcbCertificationId());
			productEntity.setCertificationBodyId(product.getCertificationBodyId());
			productEntity.setCertificationEditionId(product.getCertificationEditionId());
			productEntity.setChplProductNumber(product.getChplProductNumber());
			productEntity.setCreationDate(product.getCreationDate());
			productEntity.setDeleted(product.getDeleted());
			productEntity.setLastModifiedDate(product.getLastModifiedDate());
			productEntity.setLastModifiedUser(product.getLastModifiedUser());
			productEntity.setPracticeTypeId(product.getPracticeTypeId());
			productEntity.setProductClassificationTypeId(product.getProductClassificationTypeId());
			productEntity.setProductVersionId(product.getProductVersionId());
			productEntity.setQualityManagementSystemAtt(product.getQualityManagementSystemAtt());
			productEntity.setReportFileLocation(product.getReportFileLocation());
			productEntity.setTestingLabId(product.getTestingLabId());
			
			create(productEntity);	
		}
		
	}

	public void update(CertifiedProductDTO product) throws EntityRetrievalException{
		
		CertifiedProduct productEntity = getEntityById(product.getId());		
		
		productEntity.setId(product.getId());
		productEntity.setAtcbCertificationId(product.getAtcbCertificationId());
		productEntity.setCertificationBodyId(product.getCertificationBodyId());
		productEntity.setCertificationEditionId(product.getCertificationEditionId());
		productEntity.setChplProductNumber(product.getChplProductNumber());
		productEntity.setCreationDate(product.getCreationDate());
		productEntity.setDeleted(product.getDeleted());
		productEntity.setLastModifiedDate(product.getLastModifiedDate());
		productEntity.setLastModifiedUser(product.getLastModifiedUser());
		productEntity.setPracticeTypeId(product.getPracticeTypeId());
		productEntity.setProductClassificationTypeId(product.getProductClassificationTypeId());
		productEntity.setProductVersionId(product.getProductVersionId());
		productEntity.setQualityManagementSystemAtt(product.getQualityManagementSystemAtt());
		productEntity.setReportFileLocation(product.getReportFileLocation());
		productEntity.setTestingLabId(product.getTestingLabId());
		
		update(productEntity);
		
	}
	
	public void delete(Long productId){
		
		// TODO: How to delete this without leaving orphans
		Query query = entityManager.createQuery("UPDATE CertifiedProduct SET deleted = true WHERE certified_product_id = :productid");
		query.setParameter("productid", productId);
		query.executeUpdate();
		
	}
	
	public List<CertifiedProductDTO> findAll(){
		
		List<CertifiedProduct> entities = getAllEntities();
		List<CertifiedProductDTO> products = new ArrayList<>();
		
		for (CertifiedProduct entity : entities) {
			CertifiedProductDTO product = new CertifiedProductDTO(entity);
			products.add(product);
		}
		return products;
		
	}
	
	public CertifiedProductDTO getById(Long productId) throws EntityRetrievalException{
		
		CertifiedProduct entity = getEntityById(productId);
		CertifiedProductDTO dto = new CertifiedProductDTO(entity);
		return dto;
		
	}
	
	private void create(CertifiedProduct product) {
		
		entityManager.persist(product);
		
	}
	
	private void update(CertifiedProduct product) {
		
		entityManager.merge(product);	
	
	}
	
	private List<CertifiedProduct> getAllEntities() {
		
		List<CertifiedProduct> result = entityManager.createQuery( "from CertifiedProduct where (NOT deleted = true) ", CertifiedProduct.class).getResultList();
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
