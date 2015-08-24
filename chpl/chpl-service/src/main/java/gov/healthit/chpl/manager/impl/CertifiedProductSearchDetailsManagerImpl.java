package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.dao.CQMResultDetailsDAO;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductSearchResult;
import gov.healthit.chpl.dto.CQMResultDetailsDTO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.manager.CertifiedProductSearchDetailsManager;

@Service
public class CertifiedProductSearchDetailsManagerImpl implements CertifiedProductSearchDetailsManager {

	@Autowired
	CertifiedProductSearchResultDAO certifiedProductSearchDetailsDAO;
	
	@Autowired
	private CertificationCriterionDAO certificationCriterionDAO;
	
	@Autowired
	private CQMResultDetailsDAO cqmResultDetailsDAO;
	
	@Autowired
	private CertificationResultDetailsDAO certificationResultDetailsDAO;
	
	
	@Override
	public CertifiedProductSearchDetails getCertifiedProductDetails(
			Long certifiedProductId) throws EntityRetrievalException {
		
		CertifiedProductDetailsDTO dto = certifiedProductSearchDetailsDAO.getById(certifiedProductId);
		
		CertifiedProductSearchDetails searchDetails = new CertifiedProductSearchDetails();
		
		searchDetails.setId(dto.getId());
		searchDetails.setAcbCertificationId(dto.getAcbCertificationId());
		
		//TODO: fetch & add additional software here. 
		//searchDetails.setAdditionalSoftware(additionalSoftware);
		searchDetails.setCertificationDate(dto.getCertificationDate().toString());
			
		searchDetails.getCertificationEdition().put("id", dto.getCertificationEditionId().toString());
		searchDetails.getCertificationEdition().put("name", dto.getYear());
				
		searchDetails.setCertificationStatusId(dto.getCertificationStatusId());	
			
		searchDetails.getCertifyingBody().put("id", dto.getCertificationBodyId().toString());
		searchDetails.getCertifyingBody().put("name", dto.getCertificationBodyName());
			
		searchDetails.setChplProductNumber(dto.getChplProductNumber());
		
		searchDetails.getClassificationType().put("id", dto.getProductClassificationTypeId().toString());
		searchDetails.getClassificationType().put("name", dto.getProductclassificationName());
		
		searchDetails.setOtherAcb(dto.getOtherAcb());
		
		searchDetails.getPracticeType().put("id", dto.getPracticeTypeId().toString());
		searchDetails.getPracticeType().put("name", dto.getPracticeTypeName());
		
		searchDetails.getProduct().put("id",dto.getProductId().toString());
		searchDetails.getProduct().put("name",dto.getProductName());
		searchDetails.getProduct().put("versionId",dto.getProductVersionId().toString());
		searchDetails.getProduct().put("version", dto.getProductVersion());
				
		searchDetails.setQualityManagementSystemAtt(dto.getQualityManagementSystemAtt());
		searchDetails.setReportFileLocation(dto.getReportFileLocation());
		searchDetails.setTestingLabId(dto.getTestingLabId());
		
		searchDetails.getVendor().put("id", dto.getVendorId().toString());
		searchDetails.getVendor().put("name", dto.getVendorName());
		
		List<CertificationResultDetailsDTO> certificationResultDetailsDTOs = certificationResultDetailsDAO.getCertificationResultDetailsByCertifiedProductId(dto.getId());
		List<CertificationResult> certificationResults = new ArrayList<CertificationResult>();
		
		searchDetails.setCountCerts(dto.getCountCertifications());
		searchDetails.setCountCqms(dto.getCountCqms());
		
		for (CertificationResultDetailsDTO certResult : certificationResultDetailsDTOs){
			CertificationResult result = new CertificationResult();
				
			result.setNumber(certResult.getNumber());
			result.setSuccess(certResult.getSuccess());
			result.setTitle(certResult.getTitle());	
			certificationResults.add(result);
			
		}
				
				
		searchDetails.setCertificationResults(certificationResults);
			
		List<CQMResultDetailsDTO> cqmResultDTOs = cqmResultDetailsDAO.getCQMResultDetailsByCertifiedProductId(dto.getId());
		List<CQMResultDetails> cqmResults = new ArrayList<CQMResultDetails>();
			
		for (CQMResultDetailsDTO cqmResultDTO : cqmResultDTOs){
				
			CQMResultDetails result = new CQMResultDetails();
					
			result.setCmsId(cqmResultDTO.getCmsId());
			result.setNqfNumber(cqmResultDTO.getNqfNumber());
			result.setNumber(cqmResultDTO.getNumber());
			result.setSuccess(cqmResultDTO.getSuccess());
			result.setTitle(cqmResultDTO.getTitle());
			result.setVersion(cqmResultDTO.getVersion());
			cqmResults.add(result);
					
		}
				
		searchDetails.setCqmResults(cqmResults);
		
		return searchDetails;
	}



	@Override
	public List<CertifiedProductSearchResult> getCertifiedProducts(
			Integer pageNum, Integer pageSize) throws EntityRetrievalException {
		
		List<CertifiedProductSearchResult> searchResults = new ArrayList<CertifiedProductSearchResult>();
		
		for (CertifiedProductDetailsDTO dto : certifiedProductSearchDetailsDAO.getCertifiedProductSearchDetails(pageNum, pageSize)){
			
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
			
			searchResults.add(searchResult);
		}
		
		return searchResults; 
	}


	@Override
	public List<CertifiedProductSearchResult> simpleSearch(String searchTerm,
			Integer pageNum, Integer pageSize) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public List<CertifiedProductSearchResult> multiFilterSearch(
			Map<String, String> searchValues, Integer pageNum, Integer pageSize) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public List<CertifiedProductSearchResult> multiFilterSearch(
			Map<String, String> searchValues) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	@Override
	public List<CertifiedProductSearchDetails> getCertifiedProducts(Integer pageNum, Integer pageSize)
			throws EntityRetrievalException {
		
		List<CertifiedProductSearchDetails> searchResults = new ArrayList<CertifiedProductSearchDetails>();
		
		for (CertifiedProductSearchDetailsDTO dto : certifiedProductSearchDetailsDAO.getCertifiedProductSearchDetails(pageNum, pageSize)){
			
			CertifiedProductSearchDetails searchDetails = new CertifiedProductSearchDetails();
			
			searchDetails.setId(dto.getId());
			searchDetails.setAcbCertificationId(dto.getAcbCertificationId());
			//searchDetails.setAdditionalSoftware(additionalSoftware);
			searchDetails.setCertificationDate(dto.getCertificationDate());
			
			searchDetails.getCertificationEdition().put("id", dto.getCertificationEditionId().toString());
			searchDetails.getCertificationEdition().put("name", dto.getYear());
			
			searchDetails.setCertificationStatusId(dto.getCertificationStatusId());	
			
			searchDetails.getCertifyingBody().put("id", dto.getCertificationBodyId().toString());
			searchDetails.getCertifyingBody().put("name", dto.getCertificationBodyName());
			
			searchDetails.setCountCertsSuccessful(searchDetails.getCertificationResults().size());
			searchDetails.setCountCQMsSuccessful(searchDetails.getCqmResults().size());
			
			searchDetails.setChplProductNumber(dto.getChplProductNumber());
			
			searchDetails.getClassificationType().put("id", dto.getProductClassificationTypeId().toString());
			searchDetails.getClassificationType().put("name", dto.getProductclassificationName());
			
			searchDetails.setOtherAcb(dto.getOtherAcb());
			
			searchDetails.getPracticeType().put("id", dto.getPracticeTypeId().toString());
			searchDetails.getPracticeType().put("name", dto.getPracticeTypeName());
			
			searchDetails.getProduct().put("id",dto.getProductId().toString());
			searchDetails.getProduct().put("name",dto.getProductName());
			searchDetails.getProduct().put("versionId",dto.getProductVersionId().toString());
			searchDetails.getProduct().put("version", dto.getProductVersion());
			
			searchDetails.setQualityManagementSystemAtt(dto.getQualityManagementSystemAtt());
			searchDetails.setReportFileLocation(dto.getReportFileLocation());
			searchDetails.setTestingLabId(dto.getTestingLabId());
			
			searchDetails.getVendor().put("id", dto.getVendorId().toString());
			searchDetails.getVendor().put("name", dto.getVendorName());
			
			List<CertificationResultDetailsDTO> certificationResultDetailsDTOs = certificationResultDetailsDAO.getCertificationResultDetailsByCertifiedProductId(dto.getId());
			List<CertificationResult> certificationResults = new ArrayList<CertificationResult>();
			
			for (CertificationResultDetailsDTO certResult : certificationResultDetailsDTOs){
				CertificationResult result = new CertificationResult();
				
				result.setNumber(certResult.getNumber());
				result.setSuccess(certResult.getSuccess());
				result.setTitle(certResult.getTitle());
				
				certificationResults.add(result);
			}
			
			
			searchDetails.setCertificationResults(certificationResults);
			
			List<CQMResultDetailsDTO> cqmResultDTOs = cqmResultDetailsDAO.getCQMResultDetailsByCertifiedProductId(dto.getId());
			List<CQMResultDetails> cqmResults = new ArrayList<CQMResultDetails>();
			
			for (CQMResultDetailsDTO cqmResultDTO : cqmResultDTOs){
				
				CQMResultDetails result = new CQMResultDetails();
				
				result.setCmsId(cqmResultDTO.getCmsId());
				result.setNqfNumber(cqmResultDTO.getNqfNumber());
				result.setNumber(cqmResultDTO.getNumber());
				result.setSuccess(cqmResultDTO.getSuccess());
				result.setTitle(cqmResultDTO.getTitle());
				result.setVersion(cqmResultDTO.getVersion());
				
				cqmResults.add(result);
				
			}
			
			searchDetails.setCqmResults(cqmResults);
			
			searchResults.add(searchDetails);
			
		}
		
		return searchResults; 
	}
	*/
	
}
