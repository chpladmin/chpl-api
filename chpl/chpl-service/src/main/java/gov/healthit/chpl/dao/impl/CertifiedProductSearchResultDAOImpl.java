package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

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

	@Override
	public CertifiedProductDetailsDTO getById(Long productId) throws EntityRetrievalException {
		
		CertifiedProductDetailsEntity entity = getEntityById(productId);
		CertifiedProductDetailsDTO dto = new CertifiedProductDetailsDTO(entity);
		return dto;
		
	}
	
	@Override
	public List<CertifiedProductDetailsDTO> simpleSearch(
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
		
		Query query = getQueryForSearchFilters(searchFilters);
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

	private Query getQueryForSearchFilters(SearchFilters searchFilters){
	
		Query query = null;
		
		if (searchFilters.getCertificationCriteria().isEmpty() && searchFilters.getCqms().isEmpty()){
			
			query = this.getBasicQuery(searchFilters);
			
		} else if (searchFilters.getCertificationCriteria().isEmpty()){
			
			query = this.getCQMOnlyQuery(searchFilters);
			
		} else if (searchFilters.getCertificationCriteria().isEmpty()) {
			
			query = this.getCertOnlyQuery(searchFilters);
			
		} else {
			
			query = this.getCertCQMQuery(searchFilters);
		}
		return query;
		
	}
	
	private Query getCQMOnlyQuery(SearchFilters searchFilters){
		
		String queryStr = "SELECT "
				+ "c.certified_product_id as \"certified_product_id\", " 
				+ "certification_edition_id, " 
				+ "product_version_id, "
				+ "certification_body_id," 
				+ "testing_lab_id, "
				+ "chpl_product_number,"
				+ "report_file_location, "
				+ "quality_management_system_att, "
				+ "acb_certification_id, "
				+ "practice_type_id, "
				+ "product_classification_type_id, "
				+ "other_acb, "
				+ "certification_status_id, "
				+ "deleted, "
				+ "year, "
				+ "certification_body_name, "
				+ "product_classification_name, "
				+ "practice_type_name, "
				+ "product_version, "
				+ "product_id, "
				+ "product_name, "
				+ "vendor_id, "
				+ "vendor_name, "
				+ "certification_date, "
				+ "count_certifications, "
				+ "count_cqms "
 
		+ " FROM "

		+ " (SELECT "
		+ " DISTINCT ON (certified_product_id) certified_product_id FROM openchpl.cqm_result_details "
		+ " WHERE deleted <> true AND success = true AND number IN :cqms ) a "
		+ " INNER JOIN openchpl.certified_product_details b " 
		+ " ON a.certified_product_id = b.certified_product_id " 
		+ "";
		
		
		if ((searchFilters.getVendor() != null) && (searchFilters.getProduct() != null)){
			queryStr +=  " AND ((UPPER(vendor_name) LIKE UPPER(:vendorname)) OR (UPPER(product_name) LIKE UPPER(:productname)) ";
		} else if (searchFilters.getVendor() != null){
			queryStr +=  " AND (UPPER(vendor_name) LIKE UPPER(:vendorname)) ";
		} else if (searchFilters.getProduct() != null){
			queryStr +=  " AND (UPPER(product_name) LIKE UPPER(:productname)) ";
		}
		
		if (searchFilters.getCertificationEdition() != null) {
			queryStr += " AND (year = :certificationedition) ";
		}
		
		if (searchFilters.getPracticeType() != null) {
			queryStr += " AND (practice_type = :practicetype) "; 
		}
		
		if (searchFilters.getProductClassification() != null) {
			queryStr += " AND (product_classification = :productclassification) ";
		}
		
		if (searchFilters.getVersion() != null) {
			queryStr += " AND (UPPER(version) LIKE UPPER(:version)) ";
		}
		
		queryStr += ";";
		
		Query query = entityManager.createNativeQuery(queryStr, CertifiedProductDetailsEntity.class);
		
		query.setParameter("cqms", searchFilters.getCqms());
		
		
		if (searchFilters.getVendor() != null){
			query.setParameter("vendorname", "%"+searchFilters.getVendor()+"%");
		}
		
		if (searchFilters.getProduct() != null){
			query.setParameter("productname", "%"+searchFilters.getProduct()+"%");
		}
		
		if (searchFilters.getCertificationEdition() != null) {
			query.setParameter("certificationedition", searchFilters.getCertificationEdition());
		}
		
		if (searchFilters.getPracticeType() != null) {
			query.setParameter("practicetype", searchFilters.getPracticeType());
		}
		
		if (searchFilters.getProductClassification() != null) {
			query.setParameter("productclassification", searchFilters.getProductClassification());
		}
		
		if (searchFilters.getVersion() != null) {
			query.setParameter("version", searchFilters.getVersion());
		}
		return query;
	}
	
	private Query getCertOnlyQuery(SearchFilters searchFilters){
		
		String queryStr = "SELECT "
				+ "c.certified_product_id as \"certified_product_id\", " 
				+ "certification_edition_id, " 
				+ "product_version_id, "
				+ "certification_body_id," 
				+ "testing_lab_id, "
				+ "chpl_product_number,"
				+ "report_file_location, "
				+ "quality_management_system_att, "
				+ "acb_certification_id, "
				+ "practice_type_id, "
				+ "product_classification_type_id, "
				+ "other_acb, "
				+ "certification_status_id, "
				+ "deleted, "
				+ "year, "
				+ "certification_body_name, "
				+ "product_classification_name, "
				+ "practice_type_name, "
				+ "product_version, "
				+ "product_id, "
				+ "product_name, "
				+ "vendor_id, "
				+ "vendor_name, "
				+ "certification_date, "
				+ "count_certifications, "
				+ "count_cqms "
 
		+ " FROM "

		+ " (SELECT DISTINCT ON (certified_product_id) certified_product_id FROM openchpl.certification_result_details "
		+ " WHERE deleted <> true AND successful = true AND number IN :certs) a "
		

		+ " INNER JOIN openchpl.certified_product_details b "
		+ " ON a.certified_product_id = b.certified_product_id "
		+ " WHERE deleted <> true "
		+ "";
		
		
		if ((searchFilters.getVendor() != null) && (searchFilters.getProduct() != null)){
			queryStr +=  " AND ((UPPER(vendor_name) LIKE UPPER(:vendorname)) OR (UPPER(product_name) LIKE UPPER(:productname)) ";
		} else if (searchFilters.getVendor() != null){
			queryStr +=  " AND (UPPER(vendor_name) LIKE UPPER(:vendorname)) ";
		} else if (searchFilters.getProduct() != null){
			queryStr +=  " AND (UPPER(product_name) LIKE UPPER(:productname)) ";
		}
		
		if (searchFilters.getCertificationEdition() != null) {
			queryStr += " AND (year = :certificationedition) ";
		}
		
		if (searchFilters.getPracticeType() != null) {
			queryStr += " AND (practice_type = :practicetype) "; 
		}
		
		if (searchFilters.getProductClassification() != null) {
			queryStr += " AND (product_classification = :productclassification) ";
		}
		
		if (searchFilters.getVersion() != null) {
			queryStr += " AND (UPPER(version) LIKE UPPER(:version)) ";
		}
		
		queryStr += ";";
		
		Query query = entityManager.createNativeQuery(queryStr, CertifiedProductDetailsEntity.class);
		
		query.setParameter("certs", searchFilters.getCertificationCriteria());
		
		if (searchFilters.getVendor() != null){
			query.setParameter("vendorname", "%"+searchFilters.getVendor()+"%");
		}
		
		if (searchFilters.getProduct() != null){
			query.setParameter("productname", "%"+searchFilters.getProduct()+"%");
		}
		
		if (searchFilters.getCertificationEdition() != null) {
			query.setParameter("certificationedition", searchFilters.getCertificationEdition());
		}
		
		if (searchFilters.getPracticeType() != null) {
			query.setParameter("practicetype", searchFilters.getPracticeType());
		}
		
		if (searchFilters.getProductClassification() != null) {
			query.setParameter("productclassification", searchFilters.getProductClassification());
		}
		
		if (searchFilters.getVersion() != null) {
			query.setParameter("version", searchFilters.getVersion());
		}
		return query;
		
	}
	
	private Query getCertCQMQuery(SearchFilters searchFilters){
		
		String queryStr = "SELECT "
				+ "c.certified_product_id as \"certified_product_id\", " 
				+ "certification_edition_id, " 
				+ "product_version_id, "
				+ "certification_body_id," 
				+ "testing_lab_id, "
				+ "chpl_product_number,"
				+ "report_file_location, "
				+ "quality_management_system_att, "
				+ "acb_certification_id, "
				+ "practice_type_id, "
				+ "product_classification_type_id, "
				+ "other_acb, "
				+ "certification_status_id, "
				+ "deleted, "
				+ "year, "
				+ "certification_body_name, "
				+ "product_classification_name, "
				+ "practice_type_name, "
				+ "product_version, "
				+ "product_id, "
				+ "product_name, "
				+ "vendor_id, "
				+ "vendor_name, "
				+ "certification_date, "
				+ "count_certifications, "
				+ "count_cqms "
 
		+ " FROM "
		+ " ("
		+ " (SELECT DISTINCT ON (certified_product_id) certified_product_id FROM openchpl.cqm_result_details "
		+ " WHERE deleted <> true AND success = true AND number IN :cqms ) a "
		+ " INNER JOIN "

		+ " (SELECT DISTINCT ON (certified_product_id) certified_product_id as \"cert_certified_product_id\" FROM openchpl.certification_result_details "
		+ " WHERE deleted <> true AND successful = true AND number IN  :certs ) b "
		+ " ON a.certified_product_id = b.cert_certified_product_id "
		+ " ) c "
		+ " INNER JOIN openchpl.certified_product_details d "
		+ " ON c.certified_product_id = d.certified_product_id "
		+ " WHERE deleted <> true "
		+ "";
		
		
		if ((searchFilters.getVendor() != null) && (searchFilters.getProduct() != null)){
			queryStr +=  " AND ((UPPER(vendor_name) LIKE UPPER(:vendorname)) OR (UPPER(product_name) LIKE UPPER(:productname)) ";
		} else if (searchFilters.getVendor() != null){
			queryStr +=  " AND (UPPER(vendor_name) LIKE UPPER(:vendorname)) ";
		} else if (searchFilters.getProduct() != null){
			queryStr +=  " AND (UPPER(product_name) LIKE UPPER(:productname)) ";
		}
		
		if (searchFilters.getCertificationEdition() != null) {
			queryStr += " AND (year = :certificationedition) ";
		}
		
		if (searchFilters.getPracticeType() != null) {
			queryStr += " AND (practice_type = :practicetype) "; 
		}
		
		if (searchFilters.getProductClassification() != null) {
			queryStr += " AND (product_classification = :productclassification) ";
		}
		
		if (searchFilters.getVersion() != null) {
			queryStr += " AND (UPPER(version) LIKE UPPER(:version)) ";
		}
		
		queryStr += ";";
		
		Query query = entityManager.createNativeQuery(queryStr, CertifiedProductDetailsEntity.class);
		
		query.setParameter("certs", searchFilters.getCertificationCriteria());
		query.setParameter("cqms", searchFilters.getCqms());
		
		
		if (searchFilters.getVendor() != null){
			query.setParameter("vendorname", "%"+searchFilters.getVendor()+"%");
		}
		
		if (searchFilters.getProduct() != null){
			query.setParameter("productname", "%"+searchFilters.getProduct()+"%");
		}
		
		if (searchFilters.getCertificationEdition() != null) {
			query.setParameter("certificationedition", searchFilters.getCertificationEdition());
		}
		
		if (searchFilters.getPracticeType() != null) {
			query.setParameter("practicetype", searchFilters.getPracticeType());
		}
		
		if (searchFilters.getProductClassification() != null) {
			query.setParameter("productclassification", searchFilters.getProductClassification());
		}
		
		if (searchFilters.getVersion() != null) {
			query.setParameter("version", searchFilters.getVersion());
		}
		return query;
	}
	
	private Query getBasicQuery(SearchFilters searchFilters){
		
		String queryStr = "from CertifiedProductDetailsEntity where (NOT deleted = true)";
		
		if ((searchFilters.getVendor() != null) && (searchFilters.getProduct() != null)){
			queryStr +=  " AND ((UPPER(vendor_name) LIKE UPPER(:vendorname)) OR (UPPER(product_name) LIKE UPPER(:productname))) ";
		} else if (searchFilters.getVendor() != null){
			queryStr +=  " AND (UPPER(vendor_name) LIKE UPPER(:vendorname)) ";
		} else if (searchFilters.getProduct() != null){
			queryStr +=  " AND (UPPER(product_name) LIKE UPPER(:productname)) ";
		}
		
		if (searchFilters.getCertificationEdition() != null) {
			queryStr += " AND (year = :certificationedition) ";
		}
		
		if (searchFilters.getPracticeType() != null) {
			queryStr += " AND (practice_type = :practicetype) "; 
		}
		
		if (searchFilters.getProductClassification() != null) {
			queryStr += " AND (product_classification = :productclassification) ";
		}
		
		if (searchFilters.getVersion() != null) {
			queryStr += " AND (version = :version) ";
		}
		
		Query query = entityManager.createQuery(queryStr, CertifiedProductDetailsEntity.class);
		
		if (searchFilters.getVendor() != null){
			query.setParameter("vendorname", "%"+searchFilters.getVendor()+"%");
		}
		
		if (searchFilters.getProduct() != null){
			query.setParameter("productname", "%"+searchFilters.getProduct()+"%");
		}
		
		if (searchFilters.getCertificationEdition() != null) {
			query.setParameter("certificationedition", searchFilters.getCertificationEdition());
		}
		
		if (searchFilters.getPracticeType() != null) {
			query.setParameter("practicetype", searchFilters.getPracticeType());
		}
		
		if (searchFilters.getProductClassification() != null) {
			query.setParameter("productclassification", searchFilters.getProductClassification());
		}
		
		if (searchFilters.getVersion() != null) {
			query.setParameter("version", searchFilters.getVersion());
		}
		
		return query;
	}
	
}
