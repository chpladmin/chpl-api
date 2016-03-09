package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
	
	@Transactional
	@Override
	public SearchResponse search(
			SearchRequest searchRequest) {
		
		List<CertifiedProductSearchResult> searchResults = new ArrayList<CertifiedProductSearchResult>();
		Integer countSearchResults =  certifiedProductSearchResultDAO.countMultiFilterSearchResults(searchRequest).intValue();
		
		for (CertifiedProductDetailsDTO dto : certifiedProductSearchResultDAO.search(searchRequest))
		{
			
			CertifiedProductSearchResult searchResult = new CertifiedProductSearchResult();
			
			searchResult.setId(dto.getId());
			searchResult.setAcbCertificationId(dto.getAcbCertificationId());
			
			if(dto.getCertificationDate() != null) {
				searchResult.setCertificationDate(dto.getCertificationDate().getTime());
			}
			
			searchResult.getCertificationEdition().put("id", dto.getCertificationEditionId());
			searchResult.getCertificationEdition().put("name", dto.getYear());
			
			searchResult.getCertificationStatus().put("id", dto.getCertificationStatusId());	
			searchResult.getCertificationStatus().put("name", dto.getCertificationStatusName());	

			searchResult.getCertifyingBody().put("id", dto.getCertificationBodyId());
			searchResult.getCertifyingBody().put("name", dto.getCertificationBodyName());
			
			if(!StringUtils.isEmpty(dto.getChplProductNumber())) {
				searchResult.setChplProductNumber(dto.getChplProductNumber());
			} else {
				searchResult.setChplProductNumber(dto.getYearCode() + "." + dto.getTestingLabCode() + "." + dto.getCertificationBodyCode() + "." + 
					dto.getDeveloperCode() + "." + dto.getProductCode() + "." + dto.getVersionCode() + 
					"." + dto.getIcsCode() + "." + dto.getAdditionalSoftwareCode() + 
					"." + dto.getCertifiedDateCode());
			}
			
			searchResult.getClassificationType().put("id", dto.getProductClassificationTypeId());
			searchResult.getClassificationType().put("name", dto.getProductClassificationName());
			
			searchResult.setOtherAcb(dto.getOtherAcb());
			
			searchResult.getPracticeType().put("id", dto.getPracticeTypeId());
			searchResult.getPracticeType().put("name", dto.getPracticeTypeName());
			
			searchResult.getProduct().put("id",dto.getProductId());
			searchResult.getProduct().put("name",dto.getProductName());
			searchResult.getProduct().put("versionId",dto.getProductVersionId());
			searchResult.getProduct().put("version", dto.getProductVersion());
			
			searchResult.setReportFileLocation(dto.getReportFileLocation());
			searchResult.setSedReportFileLocation(dto.getSedReportFileLocation());
			searchResult.setTestingLabId(dto.getTestingLabId());
			searchResult.setTestingLabName(dto.getTestingLabName());
			
			searchResult.getDeveloper().put("id", dto.getDeveloperId());
			searchResult.getDeveloper().put("name", dto.getDeveloperName());
			
			searchResult.setCountCerts(dto.getCountCertifications());
			searchResult.setCountCqms(dto.getCountCqms());
			searchResult.setCountCorrectiveActionPlans(dto.getCountCorrectiveActionPlans());
			searchResult.setCountCurrentCorrectiveActionPlans(dto.getCountCurrentCorrectiveActionPlans());
			searchResult.setCountClosedCorrectiveActionPlans(dto.getCountClosedCorrectiveActionPlans());
			searchResult.setVisibleOnChpl(dto.getVisibleOnChpl());
			searchResult.setApiDocumentation(dto.getApiDocumentation());
			searchResult.setIcs(dto.getIcs());
			searchResult.setSedTesting(dto.getSedTesting());
			searchResult.setQmsTesting(dto.getQmsTesting());
			searchResult.setProductAdditionalSoftware(dto.getProductAdditionalSoftware());
			searchResult.setTermsOfUse(dto.getTermsOfUse());
			searchResult.setTransparencyAttestation(dto.getTransparencyAttestation());
			searchResult.setTransparencyAttestationUrl(dto.getTransparencyAttestationUrl());
			
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
