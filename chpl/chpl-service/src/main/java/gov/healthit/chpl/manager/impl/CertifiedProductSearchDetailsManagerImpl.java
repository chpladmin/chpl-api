package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.dao.CQMResultDetailsDAO;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.CertifiedProductSearchDetailsDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CQMResultDetailsDTO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.dto.CertifiedProductSearchDetailsDTO;
import gov.healthit.chpl.manager.CertifiedProductSearchDetailsManager;

@Service
public class CertifiedProductSearchDetailsManagerImpl implements CertifiedProductSearchDetailsManager {

	@Autowired
	CertifiedProductSearchDetailsDAO certifiedProductSearchDetailsDAO;
	
	@Autowired
	private CertificationResultDAO certificationResultDAO;
	
	@Autowired
	private CertificationCriterionDAO certificationCriterionDAO;
	
	@Autowired
	private CQMResultDetailsDAO cqmResultDetailsDAO;
	
	
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
			
			
			List<CertificationResultDTO> certificationResultDTOs = certificationResultDAO.findByCertifiedProductId(dto.getId());
			List<CertificationResult> certificationResults = new ArrayList<CertificationResult>();
			/*
			for (CertificationResultDTO certResult : certificationResultDTOs){
				
				CertificationCriterionDTO certCriterion = certificationCriterionDAO.getById(certResult.getCertificationCriterionId());
				CertificationResult result = new CertificationResult();
				result.setSuccess(certResult.getSuccessful());
				result.setNumber(certCriterion.getNumber());
				result.setTitle(certCriterion.getTitle());
				certificationResults.add(result);
			}
			*/
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

	@Override
	public CertifiedProductSearchDetails getCertifiedProductDetails(
			Long certifiedProductId) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}
