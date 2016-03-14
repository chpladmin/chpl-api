package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.SearchRequest;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.CertifiedProductDetailsEntity;

@Repository(value = "certifiedProductSearchResultDAO")
public class CertifiedProductSearchResultDAOImpl extends BaseDAOImpl implements
		CertifiedProductSearchResultDAO {
	
	static final Map<String, String> columnNameRef = new HashMap<String, String>();
	static {
		
		columnNameRef.put("developer","vendor_name");
		columnNameRef.put("product","product_name");
		columnNameRef.put("version","product_version");
		columnNameRef.put("certificationEdition","year");
		columnNameRef.put("productClassification","product_classification_name");
		columnNameRef.put("certificationBody", "certification_body_name");
		columnNameRef.put("certificationDate", "certification_date");
		columnNameRef.put("practiceType","practice_type_name");
		
	}

	@Override
	public CertifiedProductDetailsDTO getById(Long productId) throws EntityRetrievalException {
		
		CertifiedProductDetailsDTO dto = null;
		CertifiedProductDetailsEntity entity = getEntityById(productId);
		
		if (entity != null){
			dto = new CertifiedProductDetailsDTO(entity);
		}
		return dto;
	}
	
	@Override
	public List<CertifiedProductDetailsDTO> search(
			SearchRequest searchRequest) {
		
		Query query = getQueryForSearchFilters(searchRequest);
		query.setMaxResults(searchRequest.getPageSize());
	    query.setFirstResult(searchRequest.getPageNumber() * searchRequest.getPageSize());
	    
		List<CertifiedProductDetailsEntity> result = query.getResultList();
		
		List<CertifiedProductDetailsDTO> products = new ArrayList<>();
		
		for (CertifiedProductDetailsEntity entity : result) {
			CertifiedProductDetailsDTO product = new CertifiedProductDetailsDTO(entity);
			products.add(product);
		}
		return products;
	}
	
	
	private CertifiedProductDetailsEntity getEntityById(Long entityId) throws EntityRetrievalException {
		
		CertifiedProductDetailsEntity entity = null;
		
		Query query = entityManager.createQuery( "from CertifiedProductDetailsEntity where (certified_product_id = :entityid) ", CertifiedProductDetailsEntity.class );
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

	private Query getQueryForSearchFilters(SearchRequest searchRequest){
	
		Query query = null;
		
		if (searchRequest.getCertificationCriteria().isEmpty() && searchRequest.getCqms().isEmpty()){
			
			query = this.getBasicQuery(searchRequest);
			
		} else if (searchRequest.getCertificationCriteria().isEmpty()){
			
			query = this.getCQMOnlyQuery(searchRequest);
			
		} else if (searchRequest.getCqms().isEmpty()) {
			
			query = this.getCertOnlyQuery(searchRequest);
			
		} else {
			System.out.println(searchRequest.getCqms().isEmpty());
			query = this.getCertCQMQuery(searchRequest);
		}
		return query;
		
	}
	
	private Query getCQMOnlyQuery(SearchRequest searchRequest){
		
		String queryStr = "SELECT "
				+ "b.certified_product_id_cqms as \"certified_product_id\", " 
				+ "certification_edition_id, " 
				+ "product_version_id, "
				+ "certification_body_id, " 
				+ "testing_lab_id, "
				+ "testing_lab_name, "
				+ "testing_lab_code, "
				+ "chpl_product_number,"
				+ "report_file_location, "
				+ "sed_report_file_location, "
				+ "acb_certification_id, "
				+ "practice_type_id, "
				+ "product_classification_type_id, "
				+ "other_acb, "
				+ "certification_status_id, "
				+ "deleted, "
				+ "visible_on_chpl, "
				+ "terms_of_use_url, "
				+ "api_documentation_url, "
				+ "product_additional_software, "
				+ "ics, "
				+ "sed, "
				+ "qms, "
				+ "transparency_attestation, "
				+ "transparency_attestation_url, "
				+ "year, "
				+ "certification_body_name, "
				+ "certification_body_code, "
				+ "product_classification_name, "
				+ "practice_type_name, "
				+ "product_version, "
				+ "product_id, "
				+ "product_name, "
				+ "vendor_id, "
				+ "vendor_name, "
				+ "vendor_code, "
				+ "vendor_website, " 
				+ "certification_date, "
				+ "count_certifications, "
				+ "count_cqms, "
				+ "last_modified_date, "
				+ "certification_status_name, "
				+ "product_code, "
				+ "version_code, "
				+ "ics_code, "
				+ "additional_software_code, "
				+ "certified_date_code, "
				+ "count_corrective_action_plans, "
				+ "count_current_corrective_action_plans, "
				+ "count_closed_corrective_action_plans "
 
				+ "FROM "
				
				+ "(SELECT certified_product_id_cqms FROM ( "
				+ "SELECT DISTINCT ON(cqm_id, certified_product_id_cqms) certified_product_id as \"certified_product_id_cqms\" "
			  	+ "FROM openchpl.cqm_result_details  WHERE deleted = false AND success = true AND cqm_id IN (:cqms)) a "
				+ "GROUP BY certified_product_id_cqms HAVING COUNT(*) = :ncqms ) b "

				+ "INNER JOIN openchpl.certified_product_details d "
				+ "ON b.certified_product_id_cqms = d.certified_product_id "
				
				+ "WHERE deleted <> true ";
		
		
		if ((searchRequest.getDeveloper() != null) && (searchRequest.getProduct() != null)){
			queryStr +=  " AND ((UPPER(vendor_name) LIKE UPPER(:vendorname)) AND (UPPER(product_name) LIKE UPPER(:productname))) ";
		} else if (searchRequest.getDeveloper() != null){
			queryStr +=  " AND (UPPER(vendor_name) LIKE UPPER(:vendorname)) ";
		} else if (searchRequest.getProduct() != null){
			queryStr +=  " AND (UPPER(product_name) LIKE UPPER(:productname)) ";
		}
		
		if (searchRequest.getSearchTerm() != null){
			queryStr += " AND ((UPPER(vendor_name) LIKE UPPER(:searchterm)) OR (UPPER(product_name) LIKE UPPER(:searchterm) ) )";
		}
		
		if (searchRequest.getCertificationEdition() != null) {
			queryStr += " AND (year = :certificationedition) ";
		}
		
		if (searchRequest.getPracticeType() != null) {
			queryStr += " AND (practice_type_name= :practicetype) "; 
		}
		
		if (searchRequest.getProductClassification() != null) {
			queryStr += " AND (product_classification_name = :productclassification) ";
		}
		
		if (searchRequest.getCertificationBody() != null) {
			queryStr += " AND (certification_body_name = :certificationbody) ";
		}
		
		if (searchRequest.getVersion() != null) {
			queryStr += " AND (UPPER(product_version) LIKE UPPER(:version)) ";
		}
		
		
		if (searchRequest.getVisibleOnCHPL().toLowerCase().startsWith("yes") ){
			queryStr += " AND (visible_on_chpl = true ) ";
		} else if (searchRequest.getVisibleOnCHPL().toLowerCase().startsWith("no")){
			queryStr += " AND (visible_on_chpl = false ) ";	
		} else if (searchRequest.getVisibleOnCHPL().toLowerCase().startsWith("both")){
			// Add nothing to the query here. We want all results
		} else {
			queryStr += " AND (visible_on_chpl = true ) ";
		}

		if(!StringUtils.isEmpty(searchRequest.getHasCAP())) {
			if (searchRequest.getHasCAP().toLowerCase().equals(SearchRequest.CURRENT_CAP)){
				queryStr += " AND (count_current_corrective_action_plans > 0) ";
			} else if (searchRequest.getHasCAP().toLowerCase().equals(SearchRequest.CLOSED_CAP)){
				queryStr += " AND (count_closed_corrective_action_plans > 0 ) ";
			} else if (searchRequest.getHasCAP().toLowerCase().equals(SearchRequest.NO_CAP)){
				queryStr += " AND (count_corrective_action_plans = 0 ) ";
			}// In the case 'ANY_CAP' we just want all the products
		}
		
		queryStr += " ORDER BY "+columnNameRef.get(searchRequest.getOrderBy())+" ";
		
		String sortOrder = "ASC ";
		if (searchRequest.getSortDescending()){
			sortOrder = "DESC ";
		}
		
		queryStr += sortOrder;
		
		Query query = entityManager.createNativeQuery(queryStr, CertifiedProductDetailsEntity.class);
		
		query.setParameter("cqms", searchRequest.getCqms());
		
		// Use hashset in case list contains duplicates
		query.setParameter("ncqms", new HashSet<String>(searchRequest.getCqms()).size());
		
		if (searchRequest.getSearchTerm() != null){
			query.setParameter("searchterm", "%"+searchRequest.getSearchTerm()+"%");
		}
		
		if (searchRequest.getDeveloper() != null){
			query.setParameter("vendorname", "%"+searchRequest.getDeveloper()+"%");
		}
		
		if (searchRequest.getProduct() != null){
			query.setParameter("productname", "%"+searchRequest.getProduct()+"%");
		}
		
		if (searchRequest.getCertificationEdition() != null) {
			query.setParameter("certificationedition", searchRequest.getCertificationEdition());
		}
		
		if (searchRequest.getPracticeType() != null) {
			query.setParameter("practicetype", searchRequest.getPracticeType());
		}
		
		if (searchRequest.getProductClassification() != null) {
			query.setParameter("productclassification", searchRequest.getProductClassification());
		}
		
		if (searchRequest.getCertificationBody() != null) {
			query.setParameter("certificationbody", searchRequest.getCertificationBody());
		}
		
		if (searchRequest.getVersion() != null) {
			query.setParameter("version", "%"+searchRequest.getVersion()+"%");
		}
		return query;
	}
	
	
	private Query getCertOnlyQuery(SearchRequest searchRequest){
		
		String queryStr = "SELECT "
				+ "c.certified_product_id as \"certified_product_id\", " 
				+ "certification_edition_id, " 
				+ "product_version_id, "
				+ "certification_body_id," 
				+ "testing_lab_id, "
				+ "testing_lab_name, "
				+ "testing_lab_code, "
				+ "chpl_product_number,"
				+ "report_file_location, "
				+ "sed_report_file_location "
				+ "acb_certification_id, "
				+ "practice_type_id, "
				+ "product_classification_type_id, "
				+ "other_acb, "
				+ "certification_status_id, "
				+ "deleted, "
				+ "visible_on_chpl, "
				+ "terms_of_use_url, "
				+ "api_documentation_url, "
				+ "product_additional_software, "
				+ "ics, "
				+ "sed, "
				+ "qms, "
				+ "transparency_attestation, "
				+ "transparency_attestation_url, "
				+ "year, "
				+ "certification_body_name, "
				+ "certification_body_code, "
				+ "product_classification_name, "
				+ "practice_type_name, "
				+ "product_version, "
				+ "product_id, "
				+ "product_name, "
				+ "vendor_id, "
				+ "vendor_name, "
				+ "vendor_code, "
				+ "vendor_website, " 
				+ "certification_date, "
				+ "count_certifications, "
				+ "count_cqms, "
				+ "last_modified_date, "
				+ "certification_status_name, "
				+ "product_code, "
				+ "version_code, "
				+ "ics_code, "
				+ "additional_software_code, "
				+ "certified_date_code, "
				+ "count_corrective_action_plans, "
				+ "count_current_corrective_action_plans, "
				+ "count_closed_corrective_action_plans "
				
				+ "FROM ( "

				+"SELECT * FROM ( "
				+"SELECT certified_product_id, COUNT(*) as \"cqms_met\" "
				+"FROM ( "
				+"SELECT certified_product_id FROM openchpl.certification_result_details "
				+"WHERE deleted = false AND success = true AND number IN (:certs)) a "
				+"GROUP BY certified_product_id  "
				+"		) b where cqms_met = :ncerts "
				+"		) c "
				+"INNER JOIN openchpl.certified_product_details d "
				+"ON c.certified_product_id = d.certified_product_id "
				+"WHERE deleted <> true ";
		
		
		if ((searchRequest.getDeveloper() != null) && (searchRequest.getProduct() != null)){
			queryStr +=  " AND ((UPPER(vendor_name) LIKE UPPER(:vendorname)) AND (UPPER(product_name) LIKE UPPER(:productname))) ";
		} else if (searchRequest.getDeveloper() != null){
			queryStr +=  " AND (UPPER(vendor_name) LIKE UPPER(:vendorname)) ";
		} else if (searchRequest.getProduct() != null){
			queryStr +=  " AND (UPPER(product_name) LIKE UPPER(:productname)) ";
		}
		
		if (searchRequest.getSearchTerm() != null){
			queryStr += " AND ((UPPER(vendor_name) LIKE UPPER(:searchterm)) OR (UPPER(product_name) LIKE UPPER(:searchterm) ) )";
		}
		
		if (searchRequest.getCertificationEdition() != null) {
			queryStr += " AND (year = :certificationedition) ";
		}
		
		if (searchRequest.getPracticeType() != null) {
			queryStr += " AND (practice_type_name= :practicetype) "; 
		}
		
		if (searchRequest.getProductClassification() != null) {
			queryStr += " AND (product_classification_name = :productclassification) ";
		}
		
		if (searchRequest.getCertificationBody() != null) {
			queryStr += " AND (certification_body_name = :certificationbody) ";
		}
		
		if (searchRequest.getVersion() != null) {
			queryStr += " AND (UPPER(product_version) LIKE UPPER(:version)) ";
		}
		
		if (searchRequest.getVisibleOnCHPL().toLowerCase().startsWith("yes") ){
			queryStr += " AND (visible_on_chpl = true ) ";
		} else if (searchRequest.getVisibleOnCHPL().toLowerCase().startsWith("no")){
			queryStr += " AND (visible_on_chpl = false ) ";	
		} else if (searchRequest.getVisibleOnCHPL().toLowerCase().startsWith("both")){
			// Add nothing to the query here. We want all results
		} else {
			queryStr += " AND (visible_on_chpl = true ) ";
		}
		
		if(!StringUtils.isEmpty(searchRequest.getHasCAP())) {
			if (searchRequest.getHasCAP().toLowerCase().equals(SearchRequest.CURRENT_CAP)){
				queryStr += " AND (count_current_corrective_action_plans > 0) ";
			} else if (searchRequest.getHasCAP().toLowerCase().equals(SearchRequest.CLOSED_CAP)){
				queryStr += " AND (count_closed_corrective_action_plans > 0 ) ";
			} else if (searchRequest.getHasCAP().toLowerCase().equals(SearchRequest.NO_CAP)){
				queryStr += " AND (count_corrective_action_plans = 0 ) ";
			}// In the case 'ANY_CAP' we just want all the products
		}

		queryStr += " ORDER BY "+columnNameRef.get(searchRequest.getOrderBy())+" ";
		
		String sortOrder = "ASC ";
		if (searchRequest.getSortDescending()){
			sortOrder = "DESC ";
		}
		
		queryStr += sortOrder;
		
		Query query = entityManager.createNativeQuery(queryStr, CertifiedProductDetailsEntity.class);
		
		if (searchRequest.getSearchTerm() != null){
			query.setParameter("searchterm", "%"+searchRequest.getSearchTerm()+"%");
		}
		
		query.setParameter("certs", searchRequest.getCertificationCriteria());
		
		// Use hashset in case list contains duplicates
		query.setParameter("ncerts", new HashSet<String>(searchRequest.getCertificationCriteria()).size());
		
		if (searchRequest.getDeveloper() != null){
			query.setParameter("vendorname", "%"+searchRequest.getDeveloper()+"%");
		}
		
		if (searchRequest.getProduct() != null){
			query.setParameter("productname", "%"+searchRequest.getProduct()+"%");
		}
		
		if (searchRequest.getCertificationEdition() != null) {
			query.setParameter("certificationedition", searchRequest.getCertificationEdition());
		}
		
		if (searchRequest.getPracticeType() != null) {
			query.setParameter("practicetype", searchRequest.getPracticeType());
		}
		
		if (searchRequest.getProductClassification() != null) {
			query.setParameter("productclassification", searchRequest.getProductClassification());
		}
		
		if (searchRequest.getCertificationBody() != null) {
			query.setParameter("certificationbody", searchRequest.getCertificationBody());
		}
		
		if (searchRequest.getVersion() != null) {
			query.setParameter("version", "%"+searchRequest.getVersion()+"%");
		}
		return query;
		
	}
	
	private Query getCertCQMQuery(SearchRequest searchRequest){
		
		String queryStr = "SELECT "
				+ "c.cpid as \"certified_product_id\", "
				+ "certification_edition_id, " 
				+ "product_version_id, " 
				+ "testing_lab_id, "
				+ "testing_lab_name, "
				+ "testing_lab_code, "
				+ "certification_body_id, " 
				+ "chpl_product_number, "
				+ "report_file_location, "
				+ "sed_report_file_location, "
				+ "acb_certification_id, "
				+ "practice_type_id, "
				+ "product_classification_type_id, "
				+ "other_acb, "
				+ "certification_status_id, "
				+ "deleted, "
				+ "visible_on_chpl, "
				+ "terms_of_use_url, "
				+ "api_documentation_url, "
				+ "product_additional_software, "
				+ "ics, "
				+ "sed, "
				+ "qms, "
				+ "transparency_attestation, "
				+ "transparency_attestation_url, "
				+ "year, "
				+ "certification_body_name, "
				+ "certification_body_code, "
				+ "product_classification_name, "
				+ "practice_type_name, "
				+ "product_version, "
				+ "product_id, "
				+ "product_name, "
				+ "vendor_id, "
				+ "vendor_name, "
				+ "vendor_code, "
				+ "vendor_website, " 
				+ "certification_date, "
				+ "count_certifications, "
				+ "count_cqms, "
				+ "last_modified_date, "
				+ "certification_status_name, "
				+ "product_code, "
				+ "version_code, "
				+ "ics_code, "
				+ "additional_software_code, "
				+ "certified_date_code, "
				+ "count_corrective_action_plans, "
				+ "count_current_corrective_action_plans, "
				+ "count_closed_corrective_action_plans "

				+ "FROM "

				+ "(SELECT certified_product_id_certs as \"cpid\" FROM "

				+ "(SELECT certified_product_id as \"certified_product_id_certs\" FROM openchpl.certification_result_details  "
				+ "WHERE deleted = false AND success = true AND number IN (:certs) "
				+ "GROUP BY certified_product_id_certs HAVING COUNT(*) = :ncerts) a "

				+ "INNER JOIN  "

				+ "(SELECT certified_product_id_cqms FROM ( "
				+ "SELECT DISTINCT ON(cqm_id, certified_product_id_cqms) certified_product_id as \"certified_product_id_cqms\" "
				+ "FROM openchpl.cqm_result_details  WHERE deleted = false AND success = true AND cqm_id IN (:cqms)) e "
				+ "GROUP BY certified_product_id_cqms HAVING COUNT(*) = :ncqms ) b "

				+ "on a.certified_product_id_certs = b.certified_product_id_cqms) c "

				+ "INNER JOIN openchpl.certified_product_details d  "
				+ "ON c.cpid = d.certified_product_id "
				+ "WHERE deleted <> true "
				+ " ";
		
		
		if ((searchRequest.getDeveloper() != null) && (searchRequest.getProduct() != null)){
			queryStr +=  " AND ((UPPER(vendor_name) LIKE UPPER(:vendorname)) AND (UPPER(product_name) LIKE UPPER(:productname))) ";
		} else if (searchRequest.getDeveloper() != null){
			queryStr +=  " AND (UPPER(vendor_name) LIKE UPPER(:vendorname)) ";
		} else if (searchRequest.getProduct() != null){
			queryStr +=  " AND (UPPER(product_name) LIKE UPPER(:productname)) ";
		}
		
		if (searchRequest.getSearchTerm() != null){
			queryStr += " AND ((UPPER(vendor_name) LIKE UPPER(:searchterm)) OR (UPPER(product_name) LIKE UPPER(:searchterm) ) )";
		}
		
		if (searchRequest.getCertificationEdition() != null) {
			queryStr += " AND (year = :certificationedition) ";
		}
		
		if (searchRequest.getPracticeType() != null) {
			queryStr += " AND (practice_type_name= :practicetype) "; 
		}
		
		if (searchRequest.getProductClassification() != null) {
			queryStr += " AND (product_classification_name= :productclassification) ";
		}
		
		if (searchRequest.getCertificationBody() != null) {
			queryStr += " AND (certification_body_name = :certificationbody) ";
		}
		
		if (searchRequest.getVersion() != null) {
			queryStr += " AND (UPPER(product_version) LIKE UPPER(:version)) ";
		}
		
		
		if (searchRequest.getVisibleOnCHPL().toLowerCase().startsWith("yes") ){
			queryStr += " AND (visible_on_chpl = true ) ";
		} else if (searchRequest.getVisibleOnCHPL().toLowerCase().startsWith("no")){
			queryStr += " AND (visible_on_chpl = false ) ";	
		} else if (searchRequest.getVisibleOnCHPL().toLowerCase().startsWith("both")){
			// Add nothing to the query here. We want all results
		} else {
			queryStr += " AND (visible_on_chpl = true ) ";
		}

		if(!StringUtils.isEmpty(searchRequest.getHasCAP())) {
			if (searchRequest.getHasCAP().toLowerCase().equals(SearchRequest.CURRENT_CAP)){
				queryStr += " AND (count_current_corrective_action_plans > 0) ";
			} else if (searchRequest.getHasCAP().toLowerCase().equals(SearchRequest.CLOSED_CAP)){
				queryStr += " AND (count_closed_corrective_action_plans > 0 ) ";
			} else if (searchRequest.getHasCAP().toLowerCase().equals(SearchRequest.NO_CAP)){
				queryStr += " AND (count_corrective_action_plans = 0 ) ";
			}// In the case 'ANY_CAP' we just want all the products
		}
		
		queryStr += " ORDER BY "+columnNameRef.get(searchRequest.getOrderBy())+" ";
		
		String sortOrder = "ASC ";
		if (searchRequest.getSortDescending()){
			sortOrder = "DESC ";
		}
		
		queryStr += sortOrder;
		
		Query query = entityManager.createNativeQuery(queryStr, CertifiedProductDetailsEntity.class);
		
		if (searchRequest.getSearchTerm() != null){
			query.setParameter("searchterm", "%"+searchRequest.getSearchTerm()+"%");
		}
		
		query.setParameter("certs", searchRequest.getCertificationCriteria());
		// Use hashset in case list contains duplicates
		query.setParameter("ncerts", new HashSet<String>(searchRequest.getCertificationCriteria()).size());
		
		query.setParameter("cqms", searchRequest.getCqms());
		// Use hashset in case list contains duplicates
		query.setParameter("ncqms", new HashSet<String>(searchRequest.getCqms()).size());
		
		if (searchRequest.getDeveloper() != null){
			query.setParameter("vendorname", "%"+searchRequest.getDeveloper()+"%");
		}
		
		if (searchRequest.getProduct() != null){
			query.setParameter("productname", "%"+searchRequest.getProduct()+"%");
		}
		
		if (searchRequest.getCertificationEdition() != null) {
			query.setParameter("certificationedition", searchRequest.getCertificationEdition());
		}
		
		if (searchRequest.getPracticeType() != null) {
			query.setParameter("practicetype", searchRequest.getPracticeType());
		}
		
		if (searchRequest.getProductClassification() != null) {
			query.setParameter("productclassification", searchRequest.getProductClassification());
		}
		
		if (searchRequest.getCertificationBody() != null) {
			query.setParameter("certificationbody", searchRequest.getCertificationBody());
		}
		
		if (searchRequest.getVersion() != null) {
			query.setParameter("version", "%"+searchRequest.getVersion()+"%");
		}
		return query;
	}
	
	private Query getBasicQuery(SearchRequest searchRequest){
		
		String queryStr = "from CertifiedProductDetailsEntity where (NOT deleted = true)";
		
		if ((searchRequest.getDeveloper() != null) && (searchRequest.getProduct() != null)){
			queryStr +=  " AND ((UPPER(vendor_name) LIKE UPPER(:vendorname)) AND (UPPER(product_name) LIKE UPPER(:productname))) ";
		} else if (searchRequest.getDeveloper() != null){
			queryStr +=  " AND (UPPER(vendor_name) LIKE UPPER(:vendorname)) ";
		} else if (searchRequest.getProduct() != null){
			queryStr +=  " AND (UPPER(product_name) LIKE UPPER(:productname)) ";
		}
		
		if (searchRequest.getSearchTerm() != null){
			queryStr += " AND ((UPPER(vendor_name) LIKE UPPER(:searchterm)) OR (UPPER(product_name) LIKE UPPER(:searchterm) ) )";
		}
		
		if (searchRequest.getCertificationEdition() != null) {
			queryStr += " AND (year = :certificationedition) ";
		}
		
		if (searchRequest.getPracticeType() != null) {
			queryStr += " AND (practice_type_name= :practicetype) "; 
		}
		
		if (searchRequest.getProductClassification() != null) {
			queryStr += " AND (product_classification_name= :productclassification) ";
		}
		
		if (searchRequest.getCertificationBody() != null) {
			queryStr += " AND (certification_body_name = :certificationbody) ";
		}
		
		if (searchRequest.getVersion() != null) {
			queryStr += " AND (UPPER(product_version) LIKE UPPER(:version))";
		}
		
		if (searchRequest.getVisibleOnCHPL().toLowerCase().startsWith("yes") ){
			queryStr += " AND (visible_on_chpl = true ) ";
		} else if (searchRequest.getVisibleOnCHPL().toLowerCase().startsWith("no")){
			queryStr += " AND (visible_on_chpl = false ) ";	
		} else if (searchRequest.getVisibleOnCHPL().toLowerCase().startsWith("both")){
			// Add nothing to the query here. We want all results
		} else {
			queryStr += " AND (visible_on_chpl = true ) ";
		}
		
		if(!StringUtils.isEmpty(searchRequest.getHasCAP())) {
			if (searchRequest.getHasCAP().toLowerCase().equals(SearchRequest.CURRENT_CAP)){
				queryStr += " AND (count_current_corrective_action_plans > 0) ";
			} else if (searchRequest.getHasCAP().toLowerCase().equals(SearchRequest.CLOSED_CAP)){
				queryStr += " AND (count_closed_corrective_action_plans > 0 ) ";
			} else if (searchRequest.getHasCAP().toLowerCase().equals(SearchRequest.NO_CAP)){
				queryStr += " AND (count_corrective_action_plans = 0 ) ";
			}// In the case 'ANY_CAP' we just want all the products
		}
		
		queryStr += " ORDER BY "+columnNameRef.get(searchRequest.getOrderBy())+" ";
		
		String sortOrder = "ASC ";
		if (searchRequest.getSortDescending()){
			sortOrder = "DESC ";
		}
		
		queryStr += sortOrder;
		
		Query query = entityManager.createQuery(queryStr, CertifiedProductDetailsEntity.class);
		
		if (searchRequest.getSearchTerm() != null){
			query.setParameter("searchterm", "%"+searchRequest.getSearchTerm()+"%");
		}
		
		if (searchRequest.getDeveloper() != null){
			query.setParameter("vendorname", "%"+searchRequest.getDeveloper()+"%");
		}
		
		if (searchRequest.getProduct() != null){
			query.setParameter("productname", "%"+searchRequest.getProduct()+"%");
		}
		
		if (searchRequest.getCertificationEdition() != null) {
			query.setParameter("certificationedition", searchRequest.getCertificationEdition());
		}
		
		if (searchRequest.getPracticeType() != null) {
			query.setParameter("practicetype", searchRequest.getPracticeType());
		}
		
		if (searchRequest.getProductClassification() != null) {
			query.setParameter("productclassification", searchRequest.getProductClassification());
		}
		
		if (searchRequest.getCertificationBody() != null) {
			query.setParameter("certificationbody", searchRequest.getCertificationBody());
		}
		
		if (searchRequest.getVersion() != null) {
			query.setParameter("version", "%"+searchRequest.getVersion()+"%");
		}
		
		return query;
	}

	@Override
	public Long countMultiFilterSearchResults(SearchRequest searchRequest) {
		
		Query query = getCountQueryForSearchFilters(searchRequest);
		
		Object queryResult = query.getSingleResult();
		if (queryResult instanceof java.math.BigInteger){
			java.math.BigInteger bigIntResult = (java.math.BigInteger) query.getSingleResult();
			return bigIntResult.longValue();
		} else {
			Long result = (Long) query.getSingleResult();
			return result;
		}
	}

	private Query getCountQueryForSearchFilters(SearchRequest searchRequest){
	
		Query query = null;
		
		if (searchRequest.getCertificationCriteria().isEmpty() && searchRequest.getCqms().isEmpty()){
			
			query = this.getBasicCountQuery(searchRequest);
			
		} else if (searchRequest.getCertificationCriteria().isEmpty()){
			
			query = this.getCQMOnlyCountQuery(searchRequest);
			
		} else if (searchRequest.getCqms().isEmpty()) {
			
			query = this.getCertOnlyCountQuery(searchRequest);
			
		} else {
			
			query = this.getCertCQMCountQuery(searchRequest);
		}
		return query;
		
	}
	
	private Query getCQMOnlyCountQuery(SearchRequest searchRequest){
		
		String queryStr = "SELECT "
				+ "COUNT(*) as \"count\" "
				+ "FROM "
				
				+ "(SELECT certified_product_id_cqms FROM ( "
				+ "SELECT DISTINCT ON(cqm_id, certified_product_id_cqms) certified_product_id as \"certified_product_id_cqms\" "
			  	+ "FROM openchpl.cqm_result_details  WHERE deleted = false AND success = true AND cqm_id IN (:cqms)) a "
				+ "GROUP BY certified_product_id_cqms HAVING COUNT(*) = :ncqms ) b "

				+ "INNER JOIN openchpl.certified_product_details d "
				+ "ON b.certified_product_id_cqms = d.certified_product_id "
				
				+ "WHERE deleted <> true ";
		
		if ((searchRequest.getDeveloper() != null) && (searchRequest.getProduct() != null)){
			queryStr +=  " AND ((UPPER(vendor_name) LIKE UPPER(:vendorname)) AND (UPPER(product_name) LIKE UPPER(:productname))) ";
		} else if (searchRequest.getDeveloper() != null){
			queryStr +=  " AND (UPPER(vendor_name) LIKE UPPER(:vendorname)) ";
		} else if (searchRequest.getProduct() != null){
			queryStr +=  " AND (UPPER(product_name) LIKE UPPER(:productname)) ";
		}
		
		if (searchRequest.getSearchTerm() != null){
			queryStr += " AND ((UPPER(vendor_name) LIKE UPPER(:searchterm)) OR (UPPER(product_name) LIKE UPPER(:searchterm) ) )";
		}
		
		if (searchRequest.getCertificationEdition() != null) {
			queryStr += " AND (year = :certificationedition) ";
		}
		
		if (searchRequest.getPracticeType() != null) {
			queryStr += " AND (practice_type_name= :practicetype) "; 
		}
		
		if (searchRequest.getProductClassification() != null) {
			queryStr += " AND (product_classification_name = :productclassification) ";
		}
		
		if (searchRequest.getCertificationBody() != null) {
			queryStr += " AND (certification_body_name = :certificationbody) ";
		}
		
		if (searchRequest.getVersion() != null) {
			queryStr += " AND (UPPER(product_version) LIKE UPPER(:version)) ";
		}
		
		if (searchRequest.getVisibleOnCHPL().toLowerCase().startsWith("yes") ){
			queryStr += " AND (visible_on_chpl = true ) ";
		} else if (searchRequest.getVisibleOnCHPL().toLowerCase().startsWith("no")){
			queryStr += " AND (visible_on_chpl = false ) ";	
		} else if (searchRequest.getVisibleOnCHPL().toLowerCase().startsWith("both")){
			// Add nothing to the query here. We want all results
		} else {
			queryStr += " AND (visible_on_chpl = true ) ";
		}
		
		if(!StringUtils.isEmpty(searchRequest.getHasCAP())) {
			if (searchRequest.getHasCAP().toLowerCase().equals(SearchRequest.CURRENT_CAP)){
				queryStr += " AND (count_current_corrective_action_plans > 0) ";
			} else if (searchRequest.getHasCAP().toLowerCase().equals(SearchRequest.CLOSED_CAP)){
				queryStr += " AND (count_closed_corrective_action_plans > 0 ) ";
			} else if (searchRequest.getHasCAP().toLowerCase().equals(SearchRequest.NO_CAP)){
				queryStr += " AND (count_corrective_action_plans = 0 ) ";
			}// In the case 'ANY_CAP' we just want all the products
		}
		
		Query query = entityManager.createNativeQuery(queryStr);
		
		if (searchRequest.getSearchTerm() != null){
			query.setParameter("searchterm", "%"+searchRequest.getSearchTerm()+"%");
		}
		
		query.setParameter("cqms", searchRequest.getCqms());
		
		// Use hashset in case list contains duplicates
		query.setParameter("ncqms", new HashSet<String>(searchRequest.getCqms()).size());
		
		if (searchRequest.getDeveloper() != null){
			query.setParameter("vendorname", "%"+searchRequest.getDeveloper()+"%");
		}
		
		if (searchRequest.getProduct() != null){
			query.setParameter("productname", "%"+searchRequest.getProduct()+"%");
		}
		
		if (searchRequest.getCertificationEdition() != null) {
			query.setParameter("certificationedition", searchRequest.getCertificationEdition());
		}
		
		if (searchRequest.getPracticeType() != null) {
			query.setParameter("practicetype", searchRequest.getPracticeType());
		}
		
		if (searchRequest.getProductClassification() != null) {
			query.setParameter("productclassification", searchRequest.getProductClassification());
		}
		
		if (searchRequest.getCertificationBody() != null) {
			query.setParameter("certificationbody", searchRequest.getCertificationBody());
		}
		
		if (searchRequest.getVersion() != null) {
			query.setParameter("version", "%"+searchRequest.getVersion()+"%");
		}
		return query;
	}
	
	
	
	private Query getCertOnlyCountQuery(SearchRequest searchRequest){
		
		String queryStr = "SELECT "
				+ "COUNT(*) as \"count\" "
				+ "FROM ( "

				+"SELECT * FROM ( "
				+"SELECT certified_product_a, COUNT(*) as \"cqms_met\" "
				+"FROM ( "
				+"SELECT certified_product_id as \"certified_product_a\" FROM openchpl.certification_result_details "
				+"WHERE deleted = false AND success = true AND number IN (:certs)) a "
				+"GROUP BY certified_product_a "
				+"		) b where cqms_met = :ncerts "
				+"		) c "
				+"INNER JOIN openchpl.certified_product_details d "
				+"ON c.certified_product_a = d.certified_product_id "
				+"WHERE deleted <> true ";
		
		
		if ((searchRequest.getDeveloper() != null) && (searchRequest.getProduct() != null)){
			queryStr +=  " AND ((UPPER(vendor_name) LIKE UPPER(:vendorname)) AND (UPPER(product_name) LIKE UPPER(:productname))) ";
		} else if (searchRequest.getDeveloper() != null){
			queryStr +=  " AND (UPPER(vendor_name) LIKE UPPER(:vendorname)) ";
		} else if (searchRequest.getProduct() != null){
			queryStr +=  " AND (UPPER(product_name) LIKE UPPER(:productname)) ";
		}
		
		if (searchRequest.getSearchTerm() != null){
			queryStr += " AND ((UPPER(vendor_name) LIKE UPPER(:searchterm)) OR (UPPER(product_name) LIKE UPPER(:searchterm) ) )";
		}
		
		if (searchRequest.getCertificationEdition() != null) {
			queryStr += " AND (year = :certificationedition) ";
		}
		
		if (searchRequest.getPracticeType() != null) {
			queryStr += " AND (practice_type_name= :practicetype) "; 
		}
		
		if (searchRequest.getProductClassification() != null) {
			queryStr += " AND (product_classification_name = :productclassification) ";
		}
		
		if (searchRequest.getCertificationBody() != null) {
			queryStr += " AND (certification_body_name = :certificationbody) ";
		}
		
		if (searchRequest.getVersion() != null) {
			queryStr += " AND (UPPER(product_version) LIKE UPPER(:version)) ";
		}
		
		if (searchRequest.getVisibleOnCHPL().toLowerCase().startsWith("yes") ){
			queryStr += " AND (visible_on_chpl = true ) ";
		} else if (searchRequest.getVisibleOnCHPL().toLowerCase().startsWith("no")){
			queryStr += " AND (visible_on_chpl = false ) ";	
		} else if (searchRequest.getVisibleOnCHPL().toLowerCase().startsWith("both")){
			// Add nothing to the query here. We want all results
		} else {
			queryStr += " AND (visible_on_chpl = true ) ";
		}
				
		if(!StringUtils.isEmpty(searchRequest.getHasCAP())) {
			if (searchRequest.getHasCAP().toLowerCase().equals(SearchRequest.CURRENT_CAP)){
				queryStr += " AND (count_current_corrective_action_plans > 0) ";
			} else if (searchRequest.getHasCAP().toLowerCase().equals(SearchRequest.CLOSED_CAP)){
				queryStr += " AND (count_closed_corrective_action_plans > 0 ) ";
			} else if (searchRequest.getHasCAP().toLowerCase().equals(SearchRequest.NO_CAP)){
				queryStr += " AND (count_corrective_action_plans = 0 ) ";
			}// In the case 'ANY_CAP' we just want all the products
		}
		
		Query query = entityManager.createNativeQuery(queryStr);
		
		if (searchRequest.getSearchTerm() != null){
			query.setParameter("searchterm", "%"+searchRequest.getSearchTerm()+"%");
		}
		
		query.setParameter("certs", searchRequest.getCertificationCriteria());
		
		// Use hashset in case list contains duplicates
		query.setParameter("ncerts", new HashSet<String>(searchRequest.getCertificationCriteria()).size());
		
		if (searchRequest.getDeveloper() != null){
			query.setParameter("vendorname", "%"+searchRequest.getDeveloper()+"%");
		}
		
		if (searchRequest.getProduct() != null){
			query.setParameter("productname", "%"+searchRequest.getProduct()+"%");
		}
		
		if (searchRequest.getCertificationEdition() != null) {
			query.setParameter("certificationedition", searchRequest.getCertificationEdition());
		}
		
		if (searchRequest.getPracticeType() != null) {
			query.setParameter("practicetype", searchRequest.getPracticeType());
		}
		
		if (searchRequest.getProductClassification() != null) {
			query.setParameter("productclassification", searchRequest.getProductClassification());
		}
		
		if (searchRequest.getCertificationBody() != null) {
			query.setParameter("certificationbody", searchRequest.getCertificationBody());
		}
		
		if (searchRequest.getVersion() != null) {
			query.setParameter("version", "%"+searchRequest.getVersion()+"%");
		}
		return query;
		
	}
	
	private Query getCertCQMCountQuery(SearchRequest searchRequest){
		
		String queryStr = "SELECT COUNT(*) as \"count\" "

				+ "FROM "

				+ "(SELECT certified_product_id_certs as \"cpid\" FROM "

				+ "(SELECT certified_product_id as \"certified_product_id_certs\" FROM openchpl.certification_result_details  "
				+ "WHERE deleted = false AND success = true AND number IN (:certs) "
				+ "GROUP BY certified_product_id_certs HAVING COUNT(*) = :ncerts) a "

				+ "INNER JOIN  "

				+ "(SELECT certified_product_id_cqms FROM ( "
				+ "SELECT DISTINCT ON(cqm_id, certified_product_id_cqms) certified_product_id as \"certified_product_id_cqms\" "
				+ "FROM openchpl.cqm_result_details  WHERE deleted = false AND success = true AND cqm_id IN (:cqms)) e "
				+ "GROUP BY certified_product_id_cqms HAVING COUNT(*) = :ncqms ) b "

				+ "on a.certified_product_id_certs = b.certified_product_id_cqms) c "

				+ "INNER JOIN openchpl.certified_product_details d "
				+ "ON c.cpid = d.certified_product_id "
				+ "WHERE deleted <> true "
				+ " ";
		
		
		if ((searchRequest.getDeveloper() != null) && (searchRequest.getProduct() != null)){
			queryStr +=  " AND ((UPPER(vendor_name) LIKE UPPER(:vendorname)) AND (UPPER(product_name) LIKE UPPER(:productname))) ";
		} else if (searchRequest.getDeveloper() != null){
			queryStr +=  " AND (UPPER(vendor_name) LIKE UPPER(:vendorname)) ";
		} else if (searchRequest.getProduct() != null){
			queryStr +=  " AND (UPPER(product_name) LIKE UPPER(:productname)) ";
		}
		
		if (searchRequest.getSearchTerm() != null){
			queryStr += " AND ((UPPER(vendor_name) LIKE UPPER(:searchterm)) OR (UPPER(product_name) LIKE UPPER(:searchterm) ) )";
		}
		
		if (searchRequest.getCertificationEdition() != null) {
			queryStr += " AND (year = :certificationedition) ";
		}
		
		if (searchRequest.getPracticeType() != null) {
			queryStr += " AND (practice_type_name= :practicetype) "; 
		}
		
		if (searchRequest.getProductClassification() != null) {
			queryStr += " AND (product_classification_name= :productclassification) ";
		}
		
		if (searchRequest.getCertificationBody() != null) {
			queryStr += " AND (certification_body_name = :certificationbody) ";
		}
		
		if (searchRequest.getVersion() != null) {
			queryStr += " AND (UPPER(product_version) LIKE UPPER(:version)) ";
		}
		
		if (searchRequest.getVisibleOnCHPL().toLowerCase().startsWith("yes") ){
			queryStr += " AND (visible_on_chpl = true ) ";
		} else if (searchRequest.getVisibleOnCHPL().toLowerCase().startsWith("no")){
			queryStr += " AND (visible_on_chpl = false ) ";	
		} else if (searchRequest.getVisibleOnCHPL().toLowerCase().startsWith("both")){
			// Add nothing to the query here. We want all results
		} else {
			queryStr += " AND (visible_on_chpl = true ) ";
		}
		
		if(!StringUtils.isEmpty(searchRequest.getHasCAP())) {
			if (searchRequest.getHasCAP().toLowerCase().equals(SearchRequest.CURRENT_CAP)){
				queryStr += " AND (count_current_corrective_action_plans > 0) ";
			} else if (searchRequest.getHasCAP().toLowerCase().equals(SearchRequest.CLOSED_CAP)){
				queryStr += " AND (count_closed_corrective_action_plans > 0 ) ";
			} else if (searchRequest.getHasCAP().toLowerCase().equals(SearchRequest.NO_CAP)){
				queryStr += " AND (count_corrective_action_plans = 0 ) ";
			}// In the case 'ANY_CAP' we just want all the products
		}
			
		Query query = entityManager.createNativeQuery(queryStr);
		
		if (searchRequest.getSearchTerm() != null){
			query.setParameter("searchterm", "%"+searchRequest.getSearchTerm()+"%");
		}
		
		query.setParameter("certs", searchRequest.getCertificationCriteria());
		// Use hashset in case list contains duplicates
		query.setParameter("ncerts", new HashSet<String>(searchRequest.getCertificationCriteria()).size());
		
		query.setParameter("cqms", searchRequest.getCqms());
		// Use hashset in case list contains duplicates
		query.setParameter("ncqms", new HashSet<String>(searchRequest.getCqms()).size());
		
		if (searchRequest.getDeveloper() != null){
			query.setParameter("vendorname", "%"+searchRequest.getDeveloper()+"%");
		}
		
		if (searchRequest.getProduct() != null){
			query.setParameter("productname", "%"+searchRequest.getProduct()+"%");
		}
		
		if (searchRequest.getCertificationEdition() != null) {
			query.setParameter("certificationedition", searchRequest.getCertificationEdition());
		}
		
		if (searchRequest.getPracticeType() != null) {
			query.setParameter("practicetype", searchRequest.getPracticeType());
		}
		
		if (searchRequest.getProductClassification() != null) {
			query.setParameter("productclassification", searchRequest.getProductClassification());
		}
		
		if (searchRequest.getCertificationBody() != null) {
			query.setParameter("certificationbody", searchRequest.getCertificationBody());
		}
		
		if (searchRequest.getVersion() != null) {
			query.setParameter("version", "%"+searchRequest.getVersion()+"%");
		}
		return query;
	}
	
	private Query getBasicCountQuery(SearchRequest searchRequest){
		
		String queryStr = "Select count(e.id) from CertifiedProductDetailsEntity e where (NOT deleted = true)";
		
		if ((searchRequest.getDeveloper() != null) && (searchRequest.getProduct() != null)){
			queryStr +=  " AND ((UPPER(vendor_name) LIKE UPPER(:vendorname)) AND (UPPER(product_name) LIKE UPPER(:productname))) ";
		} else if (searchRequest.getDeveloper() != null){
			queryStr +=  " AND (UPPER(vendor_name) LIKE UPPER(:vendorname)) ";
		} else if (searchRequest.getProduct() != null){
			queryStr +=  " AND (UPPER(product_name) LIKE UPPER(:productname)) ";
		}
		
		if (searchRequest.getSearchTerm() != null){
			queryStr += " AND ((UPPER(vendor_name) LIKE UPPER(:searchterm)) OR (UPPER(product_name) LIKE UPPER(:searchterm) ) )";
		}
		
		if (searchRequest.getCertificationEdition() != null) {
			queryStr += " AND (year = :certificationedition) ";
		}
		
		if (searchRequest.getPracticeType() != null) {
			queryStr += " AND (practice_type_name= :practicetype) "; 
		}
		
		if (searchRequest.getProductClassification() != null) {
			queryStr += " AND (product_classification_name= :productclassification) ";
		}
		
		if (searchRequest.getCertificationBody() != null) {
			queryStr += " AND (certification_body_name = :certificationbody) ";
		}
		
		if (searchRequest.getVersion() != null) {
			queryStr += " AND (UPPER(product_version) LIKE UPPER(:version)) ";
		}
		
		if (searchRequest.getVisibleOnCHPL().toLowerCase().startsWith("yes") ){
			queryStr += " AND (visible_on_chpl = true ) ";
		} else if (searchRequest.getVisibleOnCHPL().toLowerCase().startsWith("no")){
			queryStr += " AND (visible_on_chpl = false ) ";	
		} else if (searchRequest.getVisibleOnCHPL().toLowerCase().startsWith("both")){
			// Add nothing to the query here. We want all results
		} else {
			queryStr += " AND (visible_on_chpl = true ) ";
		}
		
		if(!StringUtils.isEmpty(searchRequest.getHasCAP())) {
			if (searchRequest.getHasCAP().toLowerCase().equals(SearchRequest.CURRENT_CAP)){
				queryStr += " AND (count_current_corrective_action_plans > 0) ";
			} else if (searchRequest.getHasCAP().toLowerCase().equals(SearchRequest.CLOSED_CAP)){
				queryStr += " AND (count_closed_corrective_action_plans > 0 ) ";
			} else if (searchRequest.getHasCAP().toLowerCase().equals(SearchRequest.NO_CAP)){
				queryStr += " AND (count_corrective_action_plans = 0 ) ";
			}// In the case 'ANY_CAP' we just want all the products
		}
		
		Query query = entityManager.createQuery(queryStr);
		
		
		if (searchRequest.getSearchTerm() != null){
			query.setParameter("searchterm", "%"+searchRequest.getSearchTerm()+"%");
		}
		
		if (searchRequest.getDeveloper() != null){
			query.setParameter("vendorname", "%"+searchRequest.getDeveloper()+"%");
		}
		
		if (searchRequest.getProduct() != null){
			query.setParameter("productname", "%"+searchRequest.getProduct()+"%");
		}
		
		if (searchRequest.getCertificationEdition() != null) {
			query.setParameter("certificationedition", searchRequest.getCertificationEdition());
		}
		
		if (searchRequest.getPracticeType() != null) {
			query.setParameter("practicetype", searchRequest.getPracticeType());
		}
		
		if (searchRequest.getProductClassification() != null) {
			query.setParameter("productclassification", searchRequest.getProductClassification());
		}
		
		if (searchRequest.getCertificationBody() != null) {
			query.setParameter("certificationbody", searchRequest.getCertificationBody());
		}
		
		if (searchRequest.getVersion() != null) {
			query.setParameter("version", "%"+searchRequest.getVersion()+"%");
		}
		return query;
	}
	
}
