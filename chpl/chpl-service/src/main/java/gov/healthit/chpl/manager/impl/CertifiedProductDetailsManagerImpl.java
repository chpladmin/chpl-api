package gov.healthit.chpl.manager.impl;

import gov.healthit.chpl.dao.CQMResultDetailsDAO;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CQMResultDetailsDTO;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CertifiedProductDetailsManagerImpl implements CertifiedProductDetailsManager {

	@Autowired
	CertifiedProductSearchResultDAO certifiedProductSearchResultDAO;
	
	@Autowired
	private CertificationCriterionDAO certificationCriterionDAO;
	
	@Autowired
	private CQMResultDetailsDAO cqmResultDetailsDAO;
	
	@Autowired
	private CertificationResultDetailsDAO certificationResultDetailsDAO;
	
	
	@Override
	public CertifiedProductSearchDetails getCertifiedProductDetails(
			Long certifiedProductId) throws EntityRetrievalException {
		
		CertifiedProductDetailsDTO dto = certifiedProductSearchResultDAO.getById(certifiedProductId);
		
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
}
