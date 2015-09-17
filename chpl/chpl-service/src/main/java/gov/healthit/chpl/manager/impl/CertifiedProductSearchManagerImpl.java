package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.CQMResultDetailsDAO;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchResult;
import gov.healthit.chpl.domain.SearchRequest;
import gov.healthit.chpl.domain.SearchResponse;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;

@Service
public class CertifiedProductSearchManagerImpl implements CertifiedProductSearchManager {

	@Autowired
	CertifiedProductSearchResultDAO certifiedProductSearchResultDAO;
	
	@Autowired
	private CertificationCriterionDAO certificationCriterionDAO;
	
	@Autowired
	private CQMResultDetailsDAO cqmResultDetailsDAO;
	
	@Autowired
	private CertificationResultDetailsDAO certificationResultDetailsDAO;
	

	@Transactional
	@Override
	public SearchResponse simpleSearch(String searchTerm,
			Integer pageNum, Integer pageSize, String orderBy, Boolean sortDescending) {
		
		List<CertifiedProductSearchResult> searchResults = new ArrayList<CertifiedProductSearchResult>();
		Integer countSearchResults = certifiedProductSearchResultDAO.countSimpleSearchResults(searchTerm).intValue();
		
		for (CertifiedProductDetailsDTO dto : certifiedProductSearchResultDAO.simpleSearch(searchTerm, pageNum, pageSize, orderBy, sortDescending)){
			
			CertifiedProductSearchResult searchResult = new CertifiedProductSearchResult();
			
			searchResult.setId(dto.getId());
			searchResult.setAcbCertificationId(dto.getAcbCertificationId());
			searchResult.setCertificationDate(dto.getCertificationDate().toString());
			
			searchResult.getCertificationEdition().put("id", dto.getCertificationEditionId().toString());
			searchResult.getCertificationEdition().put("name", dto.getYear());
			
			searchResult.setCertificationStatusId(dto.getCertificationStatusId());	
			
			searchResult.getCertifyingBody().put("id", dto.getCertificationBodyId().toString());
			searchResult.getCertifyingBody().put("name", dto.getCertificationBodyName());
			
			searchResult.setChplProductNumber(dto.getChplProductNumber());
			
			searchResult.getClassificationType().put("id", dto.getProductClassificationTypeId().toString());
			searchResult.getClassificationType().put("name", dto.getProductclassificationName());
			
			searchResult.setOtherAcb(dto.getOtherAcb());
			
			searchResult.getPracticeType().put("id", dto.getPracticeTypeId().toString());
			searchResult.getPracticeType().put("name", dto.getPracticeTypeName());
			
			searchResult.getProduct().put("id",dto.getProductId().toString());
			searchResult.getProduct().put("name",dto.getProductName());
			searchResult.getProduct().put("versionId",dto.getProductVersionId().toString());
			searchResult.getProduct().put("version", dto.getProductVersion());
			
			searchResult.setQualityManagementSystemAtt(dto.getQualityManagementSystemAtt());
			searchResult.setReportFileLocation(dto.getReportFileLocation());
			searchResult.setTestingLabId(dto.getTestingLabId());
			
			searchResult.getVendor().put("id", dto.getVendorId().toString());
			searchResult.getVendor().put("name", dto.getVendorName());
			
			searchResult.setCountCerts(dto.getCountCertifications());
			searchResult.setCountCqms(dto.getCountCqms());
			searchResult.setVisibleOnChpl(dto.getVisibleOnChpl());
			
			searchResults.add(searchResult);
		}
		
		SearchResponse response = new SearchResponse(countSearchResults, 
				searchResults,
				pageSize,
				pageNum
				);
		
		return response;
	}

	@Transactional
	@Override
	public SearchResponse simpleSearch(String searchTerm,
			Integer pageNum, Integer pageSize) {
		
		List<CertifiedProductSearchResult> searchResults = new ArrayList<CertifiedProductSearchResult>();
		Integer countSearchResults =  certifiedProductSearchResultDAO.countSimpleSearchResults(searchTerm).intValue();
		
		for (CertifiedProductDetailsDTO dto : certifiedProductSearchResultDAO.simpleSearch(searchTerm, pageNum, pageSize, "product", false)){
			
			CertifiedProductSearchResult searchResult = new CertifiedProductSearchResult();
			
			searchResult.setId(dto.getId());
			searchResult.setAcbCertificationId(dto.getAcbCertificationId());
			searchResult.setCertificationDate(dto.getCertificationDate().toString());
			
			searchResult.getCertificationEdition().put("id", dto.getCertificationEditionId().toString());
			searchResult.getCertificationEdition().put("name", dto.getYear());
			
			searchResult.setCertificationStatusId(dto.getCertificationStatusId());	
			
			searchResult.getCertifyingBody().put("id", dto.getCertificationBodyId().toString());
			searchResult.getCertifyingBody().put("name", dto.getCertificationBodyName());
			
			searchResult.setChplProductNumber(dto.getChplProductNumber());
			
			searchResult.getClassificationType().put("id", dto.getProductClassificationTypeId().toString());
			searchResult.getClassificationType().put("name", dto.getProductclassificationName());
			
			searchResult.setOtherAcb(dto.getOtherAcb());
			
			searchResult.getPracticeType().put("id", dto.getPracticeTypeId().toString());
			searchResult.getPracticeType().put("name", dto.getPracticeTypeName());
			
			searchResult.getProduct().put("id",dto.getProductId().toString());
			searchResult.getProduct().put("name",dto.getProductName());
			searchResult.getProduct().put("versionId",dto.getProductVersionId().toString());
			searchResult.getProduct().put("version", dto.getProductVersion());
			
			searchResult.setQualityManagementSystemAtt(dto.getQualityManagementSystemAtt());
			searchResult.setReportFileLocation(dto.getReportFileLocation());
			searchResult.setTestingLabId(dto.getTestingLabId());
			
			searchResult.getVendor().put("id", dto.getVendorId().toString());
			searchResult.getVendor().put("name", dto.getVendorName());
			
			searchResult.setCountCerts(dto.getCountCertifications());
			searchResult.setCountCqms(dto.getCountCqms());
			searchResult.setVisibleOnChpl(dto.getVisibleOnChpl());
			
			searchResults.add(searchResult);
		}
		
		SearchResponse response = new SearchResponse(countSearchResults, 
				searchResults,
				pageSize,
				pageNum
				);
		return response;
	}
	
	@Transactional
	@Override
	public SearchResponse multiFilterSearch(
			SearchRequest searchRequest) {
		
		List<CertifiedProductSearchResult> searchResults = new ArrayList<CertifiedProductSearchResult>();
		Integer countSearchResults =  certifiedProductSearchResultDAO.countMultiFilterSearchResults(searchRequest).intValue();
		
		for (CertifiedProductDetailsDTO dto : certifiedProductSearchResultDAO.multiFilterSearch(searchRequest))
		{
			
			CertifiedProductSearchResult searchResult = new CertifiedProductSearchResult();
			
			searchResult.setId(dto.getId());
			searchResult.setAcbCertificationId(dto.getAcbCertificationId());
			searchResult.setCertificationDate(dto.getCertificationDate().toString());
			
			searchResult.getCertificationEdition().put("id", dto.getCertificationEditionId().toString());
			searchResult.getCertificationEdition().put("name", dto.getYear());
			
			searchResult.setCertificationStatusId(dto.getCertificationStatusId());	
			
			searchResult.getCertifyingBody().put("id", dto.getCertificationBodyId().toString());
			searchResult.getCertifyingBody().put("name", dto.getCertificationBodyName());
			
			searchResult.setChplProductNumber(dto.getChplProductNumber());
			
			searchResult.getClassificationType().put("id", dto.getProductClassificationTypeId().toString());
			searchResult.getClassificationType().put("name", dto.getProductclassificationName());
			
			searchResult.setOtherAcb(dto.getOtherAcb());
			
			searchResult.getPracticeType().put("id", dto.getPracticeTypeId().toString());
			searchResult.getPracticeType().put("name", dto.getPracticeTypeName());
			
			searchResult.getProduct().put("id",dto.getProductId().toString());
			searchResult.getProduct().put("name",dto.getProductName());
			searchResult.getProduct().put("versionId",dto.getProductVersionId().toString());
			searchResult.getProduct().put("version", dto.getProductVersion());
			
			searchResult.setQualityManagementSystemAtt(dto.getQualityManagementSystemAtt());
			searchResult.setReportFileLocation(dto.getReportFileLocation());
			searchResult.setTestingLabId(dto.getTestingLabId());
			
			searchResult.getVendor().put("id", dto.getVendorId().toString());
			searchResult.getVendor().put("name", dto.getVendorName());
			
			searchResult.setCountCerts(dto.getCountCertifications());
			searchResult.setCountCqms(dto.getCountCqms());
			searchResult.setVisibleOnChpl(dto.getVisibleOnChpl());
			
			searchResults.add(searchResult);
		}
		
		SearchResponse response = new SearchResponse(countSearchResults,
				searchResults,
				searchRequest.getPageSize(),
				searchRequest.getPageNumber()
				);
		return response;
	}
	
}
