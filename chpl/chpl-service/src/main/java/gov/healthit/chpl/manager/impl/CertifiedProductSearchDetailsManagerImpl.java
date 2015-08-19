package gov.healthit.chpl.manager.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.dao.CQMResultDAO;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.CertifiedProductSearchDetailsDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CQMResultDTO;
import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.dto.CertifiedProductSearchDetailsDTO;
import gov.healthit.chpl.manager.CertifiedProductSearchDetailsManager;

public class CertifiedProductSearchDetailsManagerImpl implements CertifiedProductSearchDetailsManager {

	@Autowired
	CertifiedProductSearchDetailsDAO certifiedProductSearchDetailsDAO;
	
	@Autowired
	private CertificationResultDAO certificationResultDAO;
	
	@Autowired
	private CQMResultDAO cqmResultDAO;
	
	
	@Override
	public List<CertifiedProductSearchDetails> getAllCertifiedProducts()
			throws EntityRetrievalException {
		
		for (CertifiedProductSearchDetailsDTO dto : certifiedProductSearchDetailsDAO.findAll()){
			
			List<CertificationResultDTO> certificationResults = certificationResultDAO.findByCertifiedProductId(dto.getId());
			List<CQMResultDTO> cqmResults = cqmResultDAO.findByCertifiedProductId(dto.getId());
			
			
			CertifiedProductSearchDetails searchDetails = new CertifiedProductSearchDetails();
			
			searchDetails.setId(id);
			searchDetails.setAcbCertificationId(dto.getAcbCertificationId());
			//searchDetails.setAdditionalSoftware(additionalSoftware);
			searchDetails.setCertificationDate(dto.getCertificationDate());
			
			Map<String, String> certEdition = new HashMap<String, String>();
			certEdition.put("id", dto.getCertificationEditionId().toString());
			certEdition.put("name", dto.getYear());
			searchDetails.setCertificationEdition(certEdition);
			
			searchDetails.setCertificationResults(certificationResults);
			
			
			searchDetails.setCertificationStatusId();
			searchDetails.setCertificationStatusId();
			searchDetails.setCertifyingBody();
			searchDetails.setCertsAndCQMs(certsAndCQMs);
			searchDetails.setChplProductNumber(chplProductNumber);
			searchDetails.setClassificationType(classificationType);
			searchDetails.setCqmResults(cqmResults);
			searchDetails.setOtherAcb(otherAcb);
			searchDetails.setPracticeType(practiceType);
			searchDetails.setProduct(product);
			searchDetails.setQualityManagementSystemAtt(qualityManagementSystemAtt);
			searchDetails.setReportFileLocation(reportFileLocation);
			searchDetails.setTestingLabId(testingLabId);
			searchDetails.setVendor(vendor);
			
		}
		
		//return 
	}

	@Override
	public CertifiedProductSearchDetails getCertifiedProductDetails(
			Long certifiedProductId) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}
