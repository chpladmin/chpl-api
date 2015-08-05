package gov.healthit.chpl.dao.impl;



import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.entity.CertifiedProductEntity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

public class CertifiedProductDAOImpl extends BaseDAOImpl {
	
	
	
	public void create(CertifiedProductDTO product) throws EntityCreationException{
		
		CertifiedProductEntity productEntity = null;
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
			
			productEntity = new CertifiedProductEntity();
			
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
		
		CertifiedProductEntity productEntity = getEntityById(product.getId());		
		
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
		
		List<CertifiedProductEntity> entities = getAllEntities();
		List<CertifiedProductDTO> products = new ArrayList<>();
		
		for (CertifiedProductEntity entity : entities) {
			CertifiedProductDTO product = new CertifiedProductDTO(entity);
			products.add(product);
		}
		return products;
		
	}
	
	public CertifiedProductDTO getById(Long productId) throws EntityRetrievalException{
		
		CertifiedProductEntity entity = getEntityById(productId);
		CertifiedProductDTO dto = new CertifiedProductDTO(entity);
		return dto;
		
	}
	
	private void create(CertifiedProductEntity product) {
		
		entityManager.persist(product);
		
	}
	
	private void update(CertifiedProductEntity product) {
		
		entityManager.merge(product);	
	
	}
	
	private List<CertifiedProductEntity> getAllEntities() {
		
		List<CertifiedProductEntity> result = entityManager.createQuery( "from CertifiedProduct where (NOT deleted = true) ", CertifiedProductEntity.class).getResultList();
		return result;
	}
	
	private CertifiedProductEntity getEntityById(Long userId) throws EntityRetrievalException {
		
		CertifiedProductEntity user = null;
		
		Query query = entityManager.createQuery( "from CertifiedProduct where (NOT deleted = true) AND (user_id = :userid) ", CertifiedProductEntity.class );
		query.setParameter("userid", userId);
		List<CertifiedProductEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate Certified Product id in database.");
		}
		
		if (result.size() < 0){
			user = result.get(0);
		}
		
		return user;
	}
	
	
	
}
