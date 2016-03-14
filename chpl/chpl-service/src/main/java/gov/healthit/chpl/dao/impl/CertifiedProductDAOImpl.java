package gov.healthit.chpl.dao.impl;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.CertifiedProductDetailsEntity;
import gov.healthit.chpl.entity.CertifiedProductEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

@Repository(value="certifiedProductDAO")
public class CertifiedProductDAOImpl extends BaseDAOImpl implements CertifiedProductDAO {
	
	
	public CertifiedProductDTO create(CertifiedProductDTO dto) throws EntityCreationException{
		
		CertifiedProductEntity entity = null;
		try {
			if (dto.getId() != null){
				entity = this.getEntityById(dto.getId());
			}
		} catch (EntityRetrievalException e) {
			throw new EntityCreationException(e);
		}
		
		if (entity != null) {
			throw new EntityCreationException("A product with this ID already exists.");
		} else {
			
			entity = new CertifiedProductEntity();
			
			entity.setAcbCertificationId(dto.getAcbCertificationId());
			entity.setChplProductNumber(dto.getChplProductNumber());
			entity.setProductCode(dto.getProductCode());
			entity.setVersionCode(dto.getVersionCode());
			entity.setAdditionalSoftwareCode(dto.getAdditionalSoftwareCode());
			entity.setIcsCode(dto.getIcsCode());
			entity.setCertifiedDateCode(dto.getCertifiedDateCode());
			entity.setPracticeTypeId(dto.getPracticeTypeId());
			entity.setProductClassificationTypeId(dto.getProductClassificationTypeId());
			entity.setReportFileLocation(dto.getReportFileLocation());
			entity.setSedReportFileLocation(dto.getSedReportFileLocation());
			entity.setProductAdditionalSoftware(dto.getProductAdditionalSoftware());
			entity.setTestingLabId(dto.getTestingLabId());
			entity.setOtherAcb(dto.getOtherAcb());
			entity.setVisibleOnChpl(dto.getVisibleOnChpl());
			entity.setTermsOfUse(dto.getTermsOfUse());
			entity.setApiDocumentation(dto.getApiDocumentation());
			entity.setIcs(dto.getIcs());
			entity.setSedTesting(dto.getSedTesting());
			entity.setQmsTesting(dto.getQmsTesting());
			entity.setTransparencyAttestationUrl(dto.getTransparencyAttestationUrl());
			
			if(dto.getCertificationBodyId() != null) {
				entity.setCertificationBodyId(dto.getCertificationBodyId());
			}
			
			if(dto.getCertificationEditionId() != null) {
				entity.setCertificationEditionId(dto.getCertificationEditionId());
			}
			
			if(dto.getCertificationStatusId() != null) {
				entity.setCertificationStatusId(dto.getCertificationStatusId());
			}
			
			if(dto.getProductVersionId() != null) {
				entity.setProductVersionId(dto.getProductVersionId());
			}
			
			if(dto.getCreationDate() != null) {
				entity.setCreationDate(dto.getCreationDate());
			} else {
				entity.setCreationDate(new Date());
			}
			
			if(dto.getDeleted() != null) {
				entity.setDeleted(dto.getDeleted());
			} else {
				entity.setDeleted(false);
			}
			
			if(dto.getLastModifiedDate() != null) {
				entity.setLastModifiedDate(dto.getLastModifiedDate());
			} else {
				entity.setLastModifiedDate(new Date());
			}
			
			if(dto.getLastModifiedUser() != null) {
				entity.setLastModifiedUser(dto.getLastModifiedUser());
			} else {
				entity.setLastModifiedUser(Util.getCurrentUser().getId());
			}
			
			create(entity);
			return new CertifiedProductDTO(entity);
		}
	}

	public CertifiedProductDTO update(CertifiedProductDTO dto) throws EntityRetrievalException{
		
		CertifiedProductEntity entity = getEntityById(dto.getId());		
		
		entity.setAcbCertificationId(dto.getAcbCertificationId());
		entity.setChplProductNumber(dto.getChplProductNumber());
		entity.setProductCode(dto.getProductCode());
		entity.setVersionCode(dto.getVersionCode());
		entity.setIcsCode(dto.getIcsCode());
		entity.setAdditionalSoftwareCode(dto.getAdditionalSoftwareCode());
		entity.setCertifiedDateCode(dto.getCertifiedDateCode());
		entity.setPracticeTypeId(dto.getPracticeTypeId());
		entity.setProductClassificationTypeId(dto.getProductClassificationTypeId());
		entity.setReportFileLocation(dto.getReportFileLocation());
		entity.setSedReportFileLocation(dto.getSedReportFileLocation());
		entity.setProductAdditionalSoftware(dto.getProductAdditionalSoftware());
		entity.setTestingLabId(dto.getTestingLabId());
		entity.setOtherAcb(dto.getOtherAcb());
		entity.setTermsOfUse(dto.getTermsOfUse());
		entity.setApiDocumentation(dto.getApiDocumentation());
		entity.setIcs(dto.getIcs());
		entity.setSedTesting(dto.getSedTesting());
		entity.setQmsTesting(dto.getQmsTesting());
		entity.setTransparencyAttestationUrl(dto.getTransparencyAttestationUrl());
		
		if(dto.getCertificationBodyId() != null) {
			entity.setCertificationBodyId(dto.getCertificationBodyId());
		}
		
		if(dto.getCertificationEditionId() != null) {
			entity.setCertificationEditionId(dto.getCertificationEditionId());
		}
		
		if(dto.getCertificationStatusId() != null) {
			entity.setCertificationStatusId(dto.getCertificationStatusId());
		}
		
		if(dto.getProductVersionId() != null) {
			entity.setProductVersionId(dto.getProductVersionId());
		}
		
		if(dto.getCreationDate() != null) {
			entity.setCreationDate(dto.getCreationDate());
		} else {
			entity.setCreationDate(new Date());
		}
		
		if(dto.getDeleted() != null) {
			entity.setDeleted(dto.getDeleted());
		} else {
			entity.setDeleted(false);
		}
		
		if(dto.getLastModifiedDate() != null) {
			entity.setLastModifiedDate(dto.getLastModifiedDate());
		} else {
			entity.setLastModifiedDate(new Date());
		}
		
		if(dto.getLastModifiedUser() != null) {
			entity.setLastModifiedUser(dto.getLastModifiedUser());
		} else {
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
		}
		
		update(entity);
		return new CertifiedProductDTO(entity);
	}
	
	public void delete(Long productId){
		
		// TODO: How to delete this without leaving orphans
		Query query = entityManager.createQuery("UPDATE CertifiedProductEntity SET deleted = true WHERE certified_product_id = :productid");
		query.setParameter("productid", productId);
		query.executeUpdate();
		
	}
	
	public List<CertifiedProductDetailsDTO> findAll(){
		
		List<CertifiedProductDetailsEntity> entities = entityManager.createQuery( 
				"from CertifiedProductDetailsEntity where (NOT deleted = true) ", CertifiedProductDetailsEntity.class).getResultList();

		List<CertifiedProductDetailsDTO> products = new ArrayList<>();
		
		for (CertifiedProductDetailsEntity entity : entities) {
			CertifiedProductDetailsDTO product = new CertifiedProductDetailsDTO(entity);
			products.add(product);
		}
		return products;
		
	}
	
	public CertifiedProductDTO getById(Long productId) throws EntityRetrievalException{
		
		CertifiedProductDTO dto = null;
		CertifiedProductEntity entity = getEntityById(productId);
		
		if (entity != null){
			dto = new CertifiedProductDTO(entity);
		}
		return dto;
	}
	
	public CertifiedProductDTO getByChplNumber(String chplProductNumber) {
		CertifiedProductDTO dto = null;
		CertifiedProductEntity entity = getEntityByChplNumber(chplProductNumber);
		
		if (entity != null){
			dto = new CertifiedProductDTO(entity);
		}
		return dto;
	}
	
	public CertifiedProductDetailsDTO getByChplUniqueId(String chplUniqueId) throws EntityRetrievalException {
		CertifiedProductDetailsDTO dto = null;
		String[] idParts = chplUniqueId.split("\\.");
		if(idParts.length < 9) {
			throw new EntityRetrievalException("CHPL ID must have 9 parts separated by '.'");
		}
		CertifiedProductDetailsEntity entity = getEntityByUniqueIdParts(idParts[0], idParts[1], idParts[2], 
				idParts[3], idParts[4], idParts[5], idParts[6], idParts[7], idParts[8]);
		
		if (entity != null){
			dto = new CertifiedProductDetailsDTO(entity);
		}
		return dto;
	}

	public List<CertifiedProductDTO> getByVersionIds(List<Long> versionIds) {
		Query query = entityManager.createQuery( "from CertifiedProductEntity where (NOT deleted = true) and product_version_id IN :idList", CertifiedProductEntity.class );
		query.setParameter("idList", versionIds);
		List<CertifiedProductEntity> results = query.getResultList();
		
		List<CertifiedProductDTO> dtoResults = new ArrayList<CertifiedProductDTO>(results.size());
		for(CertifiedProductEntity result : results) {
			dtoResults.add(new CertifiedProductDTO(result));
		}
		return dtoResults;
	}
	
	@Override
	public List<CertifiedProductDTO> getCertifiedProductsForDeveloper(Long developerId) {
		Query getCertifiedProductsQuery = entityManager.createQuery(
				"FROM CertifiedProductEntity cpe, ProductVersionEntity pve,"
				+ "ProductEntity pe, DeveloperEntity ve " 
				+ "WHERE (NOT cpe.deleted = true) "
				+ "AND cpe.productVersion = pve.id " 
				+ "AND pve.productId = pe.id " 
				+ "AND ve.id = pe.developerId "
				+ "AND ve.id = :developerId", CertifiedProductEntity.class);
		getCertifiedProductsQuery.setParameter("developerId", developerId);
		List<CertifiedProductEntity> results = getCertifiedProductsQuery.getResultList();
		
		List<CertifiedProductDTO> dtoResults = new ArrayList<CertifiedProductDTO>(results.size());
		for(CertifiedProductEntity result : results) {
			dtoResults.add(new CertifiedProductDTO(result));
		}
		return dtoResults;
	}
	
	public List<CertifiedProductDetailsDTO> getDetailsByVersionId(Long versionId) {
		Query query = entityManager.createQuery( "from CertifiedProductDetailsEntity where (NOT deleted = true) and product_version_id = :versionId)", CertifiedProductDetailsEntity.class );
		query.setParameter("versionId", versionId);
		List<CertifiedProductDetailsEntity> results = query.getResultList();
		
		List<CertifiedProductDetailsDTO> dtoResults = new ArrayList<CertifiedProductDetailsDTO>();
		for(CertifiedProductDetailsEntity result : results) {
			dtoResults.add(new CertifiedProductDetailsDTO(result));
		}
		return dtoResults;
	}
	
	public List<CertifiedProductDetailsDTO> getDetailsByVersionIds(List<Long> versionIds) {
		Query query = entityManager.createQuery( "from CertifiedProductDetailsEntity where (NOT deleted = true) and product_version_id IN :idList", CertifiedProductDetailsEntity.class );
		query.setParameter("idList", versionIds);
		List<CertifiedProductDetailsEntity> results = query.getResultList();
		
		List<CertifiedProductDetailsDTO> dtoResults = new ArrayList<CertifiedProductDetailsDTO>(results.size());
		for(CertifiedProductDetailsEntity result : results) {
			dtoResults.add(new CertifiedProductDetailsDTO(result));
		}
		return dtoResults;
	}
	
	public List<CertifiedProductDetailsDTO> getDetailsByAcbIds(List<Long> acbIds) {
		Query query = entityManager.createQuery( "from CertifiedProductDetailsEntity where (NOT deleted = true) and certification_body_id IN :idList", CertifiedProductDetailsEntity.class );
		query.setParameter("idList", acbIds);
		List<CertifiedProductDetailsEntity> results = query.getResultList();
		
		List<CertifiedProductDetailsDTO> dtoResults = new ArrayList<CertifiedProductDetailsDTO>(results.size());
		for(CertifiedProductDetailsEntity result : results) {
			dtoResults.add(new CertifiedProductDetailsDTO(result));
		}
		return dtoResults;
	}
	
	public List<CertifiedProductDetailsDTO> getDetailsByVersionAndAcbIds(Long versionId, List<Long> acbIds) {
		Query query = entityManager.createQuery( "from CertifiedProductDetailsEntity where (NOT deleted = true) and certification_body_id IN :idList and product_version_id = :versionId", CertifiedProductDetailsEntity.class );
		query.setParameter("idList", acbIds);
		query.setParameter("versionId", versionId);
		List<CertifiedProductDetailsEntity> results = query.getResultList();
		
		List<CertifiedProductDetailsDTO> dtoResults = new ArrayList<CertifiedProductDetailsDTO>(results.size());
		for(CertifiedProductDetailsEntity result : results) {
			dtoResults.add(new CertifiedProductDetailsDTO(result));
		}
		return dtoResults;
	}
	
	private void create(CertifiedProductEntity product) {
		
		entityManager.persist(product);
		entityManager.flush();
	}
	
	private void update(CertifiedProductEntity product) {
		
		entityManager.merge(product);	
		entityManager.flush();
	}
	
	private List<CertifiedProductEntity> getAllEntities() {
		
		List<CertifiedProductEntity> result = entityManager.createQuery( "from CertifiedProductEntity where (NOT deleted = true) ", CertifiedProductEntity.class).getResultList();
		return result;
		
	}
	
	private CertifiedProductEntity getEntityById(Long entityId) throws EntityRetrievalException {
		
		CertifiedProductEntity entity = null;
		
		Query query = entityManager.createQuery( "from CertifiedProductEntity where (certified_product_id = :entityid) ", CertifiedProductEntity.class );
		query.setParameter("entityid", entityId);
		List<CertifiedProductEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate Certified Product id in database.");
		}
		
		if (result.size() > 0){
			entity = result.get(0);
		}
		
		return entity;
	}
	
	private CertifiedProductEntity getEntityByChplNumber(String chplProductNumber) {
		
		CertifiedProductEntity entity = null;
		
		Query query = entityManager.createQuery( "from CertifiedProductEntity where (chplProductNumber = :chplProductNumber) ", CertifiedProductEntity.class );
		query.setParameter("chplProductNumber", chplProductNumber);
		List<CertifiedProductEntity> result = query.getResultList();
		
		if (result.size() > 0){
			entity = result.get(0);
		}
		
		return entity;
	}
	
	private CertifiedProductDetailsEntity getEntityByUniqueIdParts(String year, String atlCode, String acbCode, 
			String developerCode, String productCode, String versionCode, String icsCode, 
			String additionalSoftwareCode, String certifiedDateCode) {
		
		CertifiedProductDetailsEntity entity = null;
		
		Query query = entityManager.createQuery( "from CertifiedProductDetailsEntity where "
				+ "year = :year AND "
				+ "testingLabCode = :atlCode AND "
				+ "certificationBodyCode = :acbCode AND "
				+ "developerCode = :developerCode AND "
				+ "productCode = :productCode AND "
				+ "versionCode = :versionCode AND "
				+ "icsCode = :icsCode AND "
				+ "additionalSoftwareCode = :additionalSoftwareCode AND "
				+ "certifiedDateCode = :certifiedDateCode", 
				CertifiedProductDetailsEntity.class );
		
		query.setParameter("year", year);
		query.setParameter("atlCode", atlCode);
		query.setParameter("acbCode", acbCode);
		query.setParameter("developerCode", developerCode);
		query.setParameter("productCode", productCode);
		query.setParameter("versionCode", versionCode);
		query.setParameter("icsCode", icsCode);
		query.setParameter("additionalSoftwareCode", additionalSoftwareCode);
		query.setParameter("certifiedDateCode", certifiedDateCode);
		
		List<CertifiedProductDetailsEntity> result = query.getResultList();
		
		if (result.size() > 0){
			entity = result.get(0);
		}
		
		return entity;
	}
}
