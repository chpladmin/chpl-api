package gov.healthit.chpl.dao.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.SearchRequest;
import gov.healthit.chpl.domain.SurveillanceSearchOptions;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.listing.CertifiedProductDetailsEntity;

@Repository(value = "certifiedProductSearchResultDAO")
public class CertifiedProductSearchResultDAOImpl extends BaseDAOImpl implements
		CertifiedProductSearchResultDAO {
	private static final Logger logger = LogManager.getLogger(CertifiedProductSearchResultDAOImpl.class);
	
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

	private static final String COLUMNS_MINUS_CERTIFIED_PRODUCT_ID = "creation_date, " 
			+ "certification_edition_id, " 
			+ "product_version_id, " 
			+ "testing_lab_id, "
			+ "testing_lab_name, "
			+ "testing_lab_code, "
			+ "certification_body_id, " 
			+ "chpl_product_number, "
			+ "report_file_location, "
			+ "sed_report_file_location, "
			+ "sed_intended_user_description, "
			+ "sed_testing_end, "
			+ "acb_certification_id, "
			+ "practice_type_id, "
			+ "product_classification_type_id, "
			+ "other_acb, "
			+ "certification_status_id, "
			+ "last_certification_status_change, "
			+ "deleted, "
			+ "product_additional_software, "
			+ "ics, "
			+ "sed, "
			+ "qms, "
			+ "accessibility_certified, "
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
			+ "vendor_status_id, "
			+ "vendor_status_name, "
			+ "last_vendor_status_change, "
			+ "address_id, "
			+ "street_line_1, "
			+ "street_line_2, " 
			+ "city, " 
			+ "state, " 
			+ "zipcode, " 
			+ "country, "
			+ "contact_id, "
			+ "first_name, "
			+ "last_name, "
			+ "email, "
			+ "phone_number, "
			+ "title, "
			+ "certification_date, "
			+ "decertification_date, "
			+ "count_certifications, "
			+ "count_cqms, "
			+ "last_modified_date, "
			+ "certification_status_name, "
			+ "product_code, "
			+ "version_code, "
			+ "ics_code, "
			+ "additional_software_code, "
			+ "certified_date_code, "
			+ "meaningful_use_users, "
			+ "acb_is_deleted, "
			+ "count_surveillance_activities, "
			+ "count_open_surveillance_activities, "
			+ "count_closed_surveillance_activities, "
			+ "count_open_nonconformities, "
			+ "count_closed_nonconformities ";
	
	@Override
	public CertifiedProductDetailsDTO getById(Long productId) throws EntityRetrievalException {
		
		CertifiedProductDetailsDTO dto = null;
		CertifiedProductDetailsEntity entity = getEntityById(productId);
		
		if (entity != null){
			dto = new CertifiedProductDetailsDTO(entity);
		} else {
			String msg = String.format(messageSource.getMessage(new DefaultMessageSourceResolvable("listing.notFound"), LocaleContextHolder.getLocale()));
			logger.error("Error retreiving listing with ID " + productId + ": " + msg);
			throw new EntityRetrievalException(msg);
		}
		return dto;
	}
	
	@Override
	@Transactional
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
		Query query = entityManager.createQuery( "SELECT deets "
				+ "FROM CertifiedProductDetailsEntity deets "
				+ "LEFT OUTER JOIN FETCH deets.product "
				+ "WHERE (deets.id = :entityid) "
				+ "AND deets.deleted <> true", CertifiedProductDetailsEntity.class );
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
			query = this.getCertCQMQuery(searchRequest);
		}
		return query;
		
	}
	
	private Query getCQMOnlyQuery(SearchRequest searchRequest){
		String queryStr = "SELECT "
				+ "b.certified_product_id_cqms as \"certified_product_id\", " 
				+ COLUMNS_MINUS_CERTIFIED_PRODUCT_ID
				+ "FROM "
				
				+ "(SELECT certified_product_id_cqms FROM ( "
				+ "SELECT DISTINCT ON(cqm_id, certified_product_id_cqms) certified_product_id as \"certified_product_id_cqms\" "
			  	+ "FROM openchpl.cqm_result_details  WHERE deleted = false AND success = true AND cqm_id IN (:cqms)) a "
				+ "GROUP BY certified_product_id_cqms HAVING COUNT(*) = :ncqms ) b "

				+ "INNER JOIN openchpl.certified_product_details d "
				+ "ON b.certified_product_id_cqms = d.certified_product_id "
				
				+ "WHERE deleted <> true ";
		
		
		queryStr += buildSearchTermFilter(searchRequest);
		queryStr += buildPracticeTypeFilter(searchRequest);
		queryStr += buildDeveloperFilter(searchRequest);
		queryStr += buildProductFilter(searchRequest);
		queryStr += buildProductVersionFilter(searchRequest);
		queryStr += buildCertificationDateFilter(searchRequest);
		queryStr += buildCertificationBodiesFilter(searchRequest);
		queryStr += buildCertificationStatusFilter(searchRequest);
		queryStr += buildCertificationEditionsFilter(searchRequest);
		queryStr += buildSurveillanceFilter(searchRequest);
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
		
		populateSearchTermParameter(searchRequest, query);
		populatePracticeTypeParameter(searchRequest, query);
		populateDeveloperParameter(searchRequest, query);
		populateProductParameter(searchRequest, query);
		populateProductVersionParameter(searchRequest, query);
		populateCertificationDateFilter(searchRequest, query);
		populateCertificationStatusParameter(searchRequest, query);
		populateCertificationBodiesParameter(searchRequest, query);
		populateCertificationEditionsParameter(searchRequest, query);
		return query;
	}
	
	
	private Query getCertOnlyQuery(SearchRequest searchRequest){
		String queryStr = "SELECT "
				+ "c.certified_product_id as \"certified_product_id\", " 
				+ COLUMNS_MINUS_CERTIFIED_PRODUCT_ID
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
		
		queryStr += buildSearchTermFilter(searchRequest);
		queryStr += buildPracticeTypeFilter(searchRequest);
		queryStr += buildDeveloperFilter(searchRequest);
		queryStr += buildProductFilter(searchRequest);
		queryStr += buildProductVersionFilter(searchRequest);
		queryStr += buildCertificationDateFilter(searchRequest);
		queryStr += buildCertificationBodiesFilter(searchRequest);
		queryStr += buildCertificationStatusFilter(searchRequest);
		queryStr += buildCertificationEditionsFilter(searchRequest);
		queryStr += buildSurveillanceFilter(searchRequest);
		queryStr += " ORDER BY "+columnNameRef.get(searchRequest.getOrderBy())+" ";
		
		String sortOrder = "ASC ";
		if (searchRequest.getSortDescending()){
			sortOrder = "DESC ";
		}
		queryStr += sortOrder;
		
		Query query = entityManager.createNativeQuery(queryStr, CertifiedProductDetailsEntity.class);
		query.setParameter("certs", searchRequest.getCertificationCriteria());
		// Use hashset in case list contains duplicates
		query.setParameter("ncerts", new HashSet<String>(searchRequest.getCertificationCriteria()).size());
		
		populateSearchTermParameter(searchRequest, query);
		populatePracticeTypeParameter(searchRequest, query);
		populateDeveloperParameter(searchRequest, query);
		populateProductParameter(searchRequest, query);
		populateProductVersionParameter(searchRequest, query);
		populateCertificationDateFilter(searchRequest, query);
		populateCertificationStatusParameter(searchRequest, query);
		populateCertificationBodiesParameter(searchRequest, query);
		populateCertificationEditionsParameter(searchRequest, query);
		return query;
		
	}
	
	private Query getCertCQMQuery(SearchRequest searchRequest){
		String queryStr = "SELECT "
				+ "c.cpid as \"certified_product_id\", "
				+ COLUMNS_MINUS_CERTIFIED_PRODUCT_ID
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
		
		queryStr += buildSearchTermFilter(searchRequest);
		queryStr += buildPracticeTypeFilter(searchRequest);
		queryStr += buildDeveloperFilter(searchRequest);
		queryStr += buildProductFilter(searchRequest);
		queryStr += buildProductVersionFilter(searchRequest);
		queryStr += buildCertificationDateFilter(searchRequest);
		queryStr += buildCertificationBodiesFilter(searchRequest);
		queryStr += buildCertificationStatusFilter(searchRequest);
		queryStr += buildCertificationEditionsFilter(searchRequest);
		queryStr += buildSurveillanceFilter(searchRequest);
		queryStr += " ORDER BY "+columnNameRef.get(searchRequest.getOrderBy())+" ";
		
		String sortOrder = "ASC ";
		if (searchRequest.getSortDescending()){
			sortOrder = "DESC ";
		}
		queryStr += sortOrder;
		
		Query query = entityManager.createNativeQuery(queryStr, CertifiedProductDetailsEntity.class);
		query.setParameter("certs", searchRequest.getCertificationCriteria());
		// Use hashset in case list contains duplicates
		query.setParameter("ncerts", new HashSet<String>(searchRequest.getCertificationCriteria()).size());
		
		query.setParameter("cqms", searchRequest.getCqms());
		// Use hashset in case list contains duplicates
		query.setParameter("ncqms", new HashSet<String>(searchRequest.getCqms()).size());
		
		populateSearchTermParameter(searchRequest, query);
		populatePracticeTypeParameter(searchRequest, query);
		populateDeveloperParameter(searchRequest, query);
		populateProductParameter(searchRequest, query);
		populateProductVersionParameter(searchRequest, query);
		populateCertificationDateFilter(searchRequest, query);
		populateCertificationStatusParameter(searchRequest, query);
		populateCertificationBodiesParameter(searchRequest, query);
		populateCertificationEditionsParameter(searchRequest, query);
		return query;
	}
	
	private Query getBasicQuery(SearchRequest searchRequest){
		String queryStr = "from CertifiedProductDetailsEntity where (NOT deleted = true)";

		queryStr += buildSearchTermFilter(searchRequest);
		queryStr += buildPracticeTypeFilter(searchRequest);
		queryStr += buildDeveloperFilter(searchRequest);
		queryStr += buildProductFilter(searchRequest);
		queryStr += buildProductVersionFilter(searchRequest);
		queryStr += buildCertificationDateFilter(searchRequest);
		queryStr += buildCertificationBodiesFilter(searchRequest);
		queryStr += buildCertificationStatusFilter(searchRequest);
		queryStr += buildCertificationEditionsFilter(searchRequest);
		queryStr += buildSurveillanceFilter(searchRequest);
		queryStr += " ORDER BY "+columnNameRef.get(searchRequest.getOrderBy())+" ";
		
		String sortOrder = "ASC ";
		if (searchRequest.getSortDescending()){
			sortOrder = "DESC ";
		}
		queryStr += sortOrder;
		
		Query query = entityManager.createQuery(queryStr, CertifiedProductDetailsEntity.class);
		
		populateSearchTermParameter(searchRequest, query);
		populatePracticeTypeParameter(searchRequest, query);
		populateDeveloperParameter(searchRequest, query);
		populateProductParameter(searchRequest, query);
		populateProductVersionParameter(searchRequest, query);
		populateCertificationDateFilter(searchRequest, query);
		populateCertificationStatusParameter(searchRequest, query);
		populateCertificationBodiesParameter(searchRequest, query);
		populateCertificationEditionsParameter(searchRequest, query);
		
		return query;
	}

	@Override
	@Transactional
	@Cacheable(CacheNames.COUNT_MULTI_FILTER_SEARCH_RESULTS)
	public Long countMultiFilterSearchResults(SearchRequest searchRequest) {
		
		Query query = getCountQueryForSearchFilters(searchRequest);
		
		Object queryResult = query.getSingleResult();
		if (queryResult instanceof java.math.BigInteger){
			java.math.BigInteger bigIntResult = (java.math.BigInteger) queryResult;
			return bigIntResult.longValue();
		} else {
			return (Long) queryResult;
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

		queryStr += buildSearchTermFilter(searchRequest);
		queryStr += buildPracticeTypeFilter(searchRequest);
		queryStr += buildDeveloperFilter(searchRequest);
		queryStr += buildProductFilter(searchRequest);
		queryStr += buildProductVersionFilter(searchRequest);
		queryStr += buildCertificationDateFilter(searchRequest);
		queryStr += buildCertificationBodiesFilter(searchRequest);
		queryStr += buildCertificationStatusFilter(searchRequest);
		queryStr += buildCertificationEditionsFilter(searchRequest);
		queryStr += buildSurveillanceFilter(searchRequest);
		Query query = entityManager.createNativeQuery(queryStr);
		query.setParameter("cqms", searchRequest.getCqms());	
		// Use hashset in case list contains duplicates
		query.setParameter("ncqms", new HashSet<String>(searchRequest.getCqms()).size());
		
		populateSearchTermParameter(searchRequest, query);
		populatePracticeTypeParameter(searchRequest, query);
		populateDeveloperParameter(searchRequest, query);
		populateProductParameter(searchRequest, query);
		populateProductVersionParameter(searchRequest, query);
		populateCertificationDateFilter(searchRequest, query);
		populateCertificationStatusParameter(searchRequest, query);
		populateCertificationBodiesParameter(searchRequest, query);
		populateCertificationEditionsParameter(searchRequest, query);
		
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
		
		queryStr += buildSearchTermFilter(searchRequest);
		queryStr += buildPracticeTypeFilter(searchRequest);
		queryStr += buildDeveloperFilter(searchRequest);
		queryStr += buildProductFilter(searchRequest);
		queryStr += buildProductVersionFilter(searchRequest);
		queryStr += buildCertificationDateFilter(searchRequest);
		queryStr += buildCertificationBodiesFilter(searchRequest);
		queryStr += buildCertificationStatusFilter(searchRequest);
		queryStr += buildCertificationEditionsFilter(searchRequest);
		queryStr += buildSurveillanceFilter(searchRequest);
		
		Query query = entityManager.createNativeQuery(queryStr);
		query.setParameter("certs", searchRequest.getCertificationCriteria());
		// Use hashset in case list contains duplicates
		query.setParameter("ncerts", new HashSet<String>(searchRequest.getCertificationCriteria()).size());
		
		populateSearchTermParameter(searchRequest, query);
		populatePracticeTypeParameter(searchRequest, query);
		populateDeveloperParameter(searchRequest, query);
		populateProductParameter(searchRequest, query);
		populateProductVersionParameter(searchRequest, query);
		populateCertificationDateFilter(searchRequest, query);
		populateCertificationStatusParameter(searchRequest, query);
		populateCertificationBodiesParameter(searchRequest, query);
		populateCertificationEditionsParameter(searchRequest, query);
		
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
				+ "WHERE deleted <> true ";
		
		queryStr += buildSearchTermFilter(searchRequest);
		queryStr += buildPracticeTypeFilter(searchRequest);
		queryStr += buildDeveloperFilter(searchRequest);
		queryStr += buildProductFilter(searchRequest);
		queryStr += buildProductVersionFilter(searchRequest);
		queryStr += buildCertificationDateFilter(searchRequest);
		queryStr += buildCertificationBodiesFilter(searchRequest);
		queryStr += buildCertificationStatusFilter(searchRequest);
		queryStr += buildCertificationEditionsFilter(searchRequest);
		queryStr += buildSurveillanceFilter(searchRequest);
		
		Query query = entityManager.createNativeQuery(queryStr);
		query.setParameter("certs", searchRequest.getCertificationCriteria());
		// Use hashset in case list contains duplicates
		query.setParameter("ncerts", new HashSet<String>(searchRequest.getCertificationCriteria()).size());
		
		query.setParameter("cqms", searchRequest.getCqms());
		// Use hashset in case list contains duplicates
		query.setParameter("ncqms", new HashSet<String>(searchRequest.getCqms()).size());
		
		populateSearchTermParameter(searchRequest, query);
		populatePracticeTypeParameter(searchRequest, query);
		populateDeveloperParameter(searchRequest, query);
		populateProductParameter(searchRequest, query);
		populateProductVersionParameter(searchRequest, query);
		populateCertificationDateFilter(searchRequest, query);
		populateCertificationStatusParameter(searchRequest, query);
		populateCertificationBodiesParameter(searchRequest, query);
		populateCertificationEditionsParameter(searchRequest, query);
		
		return query;
	}
	
	private Query getBasicCountQuery(SearchRequest searchRequest){
		String queryStr = "Select count(e.id) from CertifiedProductDetailsEntity e where (NOT deleted = true)";

		queryStr += buildSearchTermFilter(searchRequest);
		queryStr += buildPracticeTypeFilter(searchRequest);
		queryStr += buildDeveloperFilter(searchRequest);
		queryStr += buildProductFilter(searchRequest);
		queryStr += buildProductVersionFilter(searchRequest);
		queryStr += buildCertificationDateFilter(searchRequest);
		queryStr += buildCertificationBodiesFilter(searchRequest);
		queryStr += buildCertificationStatusFilter(searchRequest);
		queryStr += buildCertificationEditionsFilter(searchRequest);
		queryStr += buildSurveillanceFilter(searchRequest);
		
		Query query = entityManager.createQuery(queryStr);
		
		populateSearchTermParameter(searchRequest, query);
		populatePracticeTypeParameter(searchRequest, query);
		populateDeveloperParameter(searchRequest, query);
		populateProductParameter(searchRequest, query);
		populateProductVersionParameter(searchRequest, query);
		populateCertificationDateFilter(searchRequest, query);
		populateCertificationStatusParameter(searchRequest, query);
		populateCertificationBodiesParameter(searchRequest, query);
		populateCertificationEditionsParameter(searchRequest, query);
		return query;
	}
	
	private String buildSearchTermFilter(SearchRequest searchRequest) {
		String result = "";
		if (searchRequest.getSearchTerm() != null){
			if(searchRequest.getSearchTerm().toUpperCase().startsWith("CHP-")) {
				result += " AND UPPER(chpl_product_number) LIKE UPPER(:searchterm) ";
			} else if(searchRequest.getSearchTerm().split("\\.").length == 9) {				
				result += " AND year = '20' || :yearCode "
						+ " AND testing_lab_code = :atlCode "
						+ " AND certification_body_code = :acbCode "
						+ " AND vendor_code = :developerCode "
						+ " AND UPPER(product_code) = UPPER(:productCode) "
						+ " AND UPPER(version_code) = UPPER(:versionCode) "
						+ " AND ics_code = :icsCode "
						+ " AND additional_software_code = :additionalSoftwareCode "
						+ " AND certified_date_code = :certifiedDateCode ";
			} else {
				result += " AND ("
						+ "(UPPER(vendor_name) LIKE UPPER(:searchterm)) "
						+ "OR "
						+ "(UPPER(product_name) LIKE UPPER(:searchterm) )"
						+ "OR "
						+ "(UPPER(acb_certification_id) LIKE UPPER(:searchterm)) "
						+ ")";
			}
		}
		return result;
	}
	
	private void populateSearchTermParameter(SearchRequest searchRequest, Query query) {
		if (searchRequest.getSearchTerm() != null){			
			if(searchRequest.getSearchTerm().split("\\.").length == 9) {
				String[] idParts = searchRequest.getSearchTerm().split("\\.");
				query.setParameter("yearCode", idParts[0]);
				query.setParameter("atlCode", idParts[1]);
				query.setParameter("acbCode", idParts[2]);
				query.setParameter("developerCode", idParts[3]);
				query.setParameter("productCode", idParts[4]);
				query.setParameter("versionCode", idParts[5]);
				query.setParameter("icsCode", idParts[6]);
				query.setParameter("additionalSoftwareCode", idParts[7]);
				query.setParameter("certifiedDateCode", idParts[8]);
			} else {
				query.setParameter("searchterm", "%"+searchRequest.getSearchTerm()+"%");
			}
		}
	}
	
	private String buildCertificationDateFilter(SearchRequest searchRequest) {
		String result = "";
		if(!StringUtils.isEmpty(searchRequest.getCertificationDateStart())) {
			result += " AND (certification_date >= :certificationDateStart) "; 
		}
		if(!StringUtils.isEmpty(searchRequest.getCertificationDateEnd())) {
			result += " AND (certification_date <= :certificationDateEnd) ";
		}
		return result;
	}
	
	private String populateCertificationDateFilter(SearchRequest searchRequest, Query query) {
		SimpleDateFormat format = new SimpleDateFormat(SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT);
		
		String result = "";
		if(!StringUtils.isEmpty(searchRequest.getCertificationDateStart())) {
			Date start = null;
			try {
				start = format.parse(searchRequest.getCertificationDateStart());
				Calendar beginningOfDay = Calendar.getInstance();
				beginningOfDay.setTime(start);
				beginningOfDay.set(Calendar.HOUR, 0);
				beginningOfDay.set(Calendar.MINUTE, 0);
				beginningOfDay.set(Calendar.SECOND, 0);
				beginningOfDay.set(Calendar.MILLISECOND, 0);
				query.setParameter("certificationDateStart", beginningOfDay.getTime());
			} catch(ParseException ex) {
				logger.error("Could not parse " + searchRequest.getCertificationDateStart() + " as date in the format " + SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT);
			}
		}
		if(!StringUtils.isEmpty(searchRequest.getCertificationDateEnd())) {
			Date end = null;
			try {
				end = format.parse(searchRequest.getCertificationDateEnd());
				Calendar endOfDay = Calendar.getInstance();
				endOfDay.setTime(end);
				endOfDay.set(Calendar.HOUR, 23);
				endOfDay.set(Calendar.MINUTE, 59);
				endOfDay.set(Calendar.SECOND, 59);
				endOfDay.set(Calendar.MILLISECOND, 999);
				query.setParameter("certificationDateEnd", endOfDay.getTime());
			} catch(ParseException ex) {
				logger.error("Could not parse " + searchRequest.getCertificationDateStart() + " as date in the format " + SearchRequest.CERTIFICATION_DATE_SEARCH_FORMAT);
			}
		}
		return result;
	}
	
	private String buildPracticeTypeFilter(SearchRequest searchRequest) {
		String result = "";
		if (searchRequest.getPracticeType() != null) {
			result += " AND (practice_type_name= :practicetype) "; 
		}
		return result;
	}
	
	private void populatePracticeTypeParameter(SearchRequest searchRequest, Query query ) {
		if (searchRequest.getPracticeType() != null) {
			query.setParameter("practicetype", searchRequest.getPracticeType());
		}
	}
	
	private String buildDeveloperFilter(SearchRequest searchRequest) {
		String result = "";
		if (searchRequest.getDeveloper() != null){
			result +=  " AND (UPPER(vendor_name) LIKE UPPER(:vendorname)) ";
		}
		return result;
	}
	
	private void populateDeveloperParameter(SearchRequest searchRequest, Query query) {
		if (searchRequest.getDeveloper() != null){
			query.setParameter("vendorname", "%"+searchRequest.getDeveloper()+"%");
		}
	}
	
	private String buildProductFilter(SearchRequest searchRequest) {
		String result = "";
		if (searchRequest.getProduct() != null){
			result +=  " AND (UPPER(product_name) LIKE UPPER(:productname)) ";
		}
		return result;
	}
	
	private void populateProductParameter(SearchRequest searchRequest, Query query) {
		if (searchRequest.getProduct() != null){
			query.setParameter("productname", "%"+searchRequest.getProduct()+"%");
		}
	}
	
	private String buildProductVersionFilter(SearchRequest searchRequest) {
		String result = "";
		if (searchRequest.getVersion() != null) {
			result += " AND (UPPER(product_version) LIKE UPPER(:version)) ";
		}
		return result;
	}
	
	private void populateProductVersionParameter(SearchRequest searchRequest, Query query) {
		if (searchRequest.getVersion() != null) {
			query.setParameter("version", "%"+searchRequest.getVersion()+"%");
		}
	}
	
	private String buildCertificationStatusFilter(SearchRequest searchRequest) {
		String result = "";
		if(searchRequest.getCertificationStatuses() != null && searchRequest.getCertificationStatuses().size() > 0) {
			result += " AND certification_status_name in (";
			for(int i = 0; i < searchRequest.getCertificationStatuses().size(); i++) {
				result += ":certificationStatus" + i;
				if(i != searchRequest.getCertificationStatuses().size()-1) {
					result += ", ";
				}
			}
			result += ") ";			
		} else {
			result += " AND (UPPER(certification_status_name) NOT LIKE 'RETIRED')";
		}
		return result;
	}
	
	private void populateCertificationStatusParameter(SearchRequest searchRequest, Query query) {
		if(searchRequest.getCertificationStatuses() != null && searchRequest.getCertificationStatuses().size() > 0) {
			for(int i = 0; i < searchRequest.getCertificationStatuses().size(); i++) {
				query.setParameter("certificationStatus"+i, searchRequest.getCertificationStatuses().get(i));
			}
		} 
	}
	
	private String buildCertificationBodiesFilter(SearchRequest searchRequest) {
		String result = "";
		if (searchRequest.getCertificationBodies() != null && searchRequest.getCertificationBodies().size() > 0) {
			result += " AND certification_body_name in (";
			for(int i = 0; i < searchRequest.getCertificationBodies().size(); i++) {
				result += ":certificationbody" + i;
				if(i != searchRequest.getCertificationBodies().size()-1) {
					result += ", ";
				}
			}
			result += ") ";
		}
		return result;
	}
	
	private void populateCertificationBodiesParameter(SearchRequest searchRequest, Query query) {
		if (searchRequest.getCertificationBodies() != null && searchRequest.getCertificationBodies().size() > 0) {
			for(int i = 0; i < searchRequest.getCertificationBodies().size(); i++) {
				query.setParameter("certificationbody"+i, searchRequest.getCertificationBodies().get(i));
			}			
		}
	}
	
	private String buildCertificationEditionsFilter(SearchRequest searchRequest) {
		String result = "";
		if (searchRequest.getCertificationEditions() != null && searchRequest.getCertificationEditions().size() > 0) {
			result += " AND year in (";
			for(int i = 0; i < searchRequest.getCertificationEditions().size(); i++) {
				result += ":certificationedition" + i;
				if(i != searchRequest.getCertificationEditions().size()-1) {
					result += ", ";
				}
			}
			result += ") ";
		} else {
			result += " AND year NOT LIKE '2011'";
		}
		return result;
	}
	
	private void populateCertificationEditionsParameter(SearchRequest searchRequest, Query query) {
		if (searchRequest.getCertificationEditions() != null && searchRequest.getCertificationEditions().size() > 0) {
			for(int i = 0; i < searchRequest.getCertificationEditions().size(); i++) {
				query.setParameter("certificationedition"+i, searchRequest.getCertificationEditions().get(i));
			}
		}
	}
	
	private String buildSurveillanceFilter(SearchRequest searchRequest) {
		String result = "";
		if(searchRequest.getHasHadSurveillance() != null) {
			if(searchRequest.getHasHadSurveillance().booleanValue() == true) {
				result += " AND count_surveillance_activities > 0 ";
			} else if(searchRequest.getHasHadSurveillance().booleanValue() == false) {
				result += " AND count_surveillance_activities = 0 ";
			}
		}
		
		if(searchRequest.getSurveillance() != null && searchRequest.getSurveillance().size() > 0) {
			result += " AND (";
			Iterator<SurveillanceSearchOptions> opts = searchRequest.getSurveillance().iterator();
			while(opts.hasNext()) {
				SurveillanceSearchOptions opt = opts.next();
				switch(opt) {
				case OPEN_SURVEILLANCE:
					result += " count_open_surveillance_activities > 0 ";
					break;
				case CLOSED_SURVEILLANCE:
					result += " count_closed_surveillance_activities > 0 ";
					break;
				case OPEN_NONCONFORMITY:
					result += " count_open_nonconformities > 0 ";
					break;
				case CLOSED_NONCONFORMITY:
					result += " count_closed_nonconformities > 0 ";
					break;
				}
				
				if(opts.hasNext()) {
					result += " AND ";
				}
			}
			result += ") ";
		}
		return result;
	}
}
