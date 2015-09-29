package gov.healthit.chpl.manager.impl;

import gov.healthit.chpl.dao.AdditionalSoftwareDAO;
import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.dao.CQMResultDetailsDAO;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationEventDAO;
import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.EventTypeDAO;
import gov.healthit.chpl.domain.AdditionalSoftware;
import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationEvent;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.AdditionalSoftwareDTO;
import gov.healthit.chpl.dto.CQMCriterionDTO;
import gov.healthit.chpl.dto.CQMResultDetailsDTO;
import gov.healthit.chpl.dto.CertificationEventDTO;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.EventTypeDTO;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CertifiedProductDetailsManagerImpl implements CertifiedProductDetailsManager {

	@Autowired
	private CertifiedProductSearchResultDAO certifiedProductSearchResultDAO;
	
	@Autowired
	private CertificationCriterionDAO certificationCriterionDAO;
	
	@Autowired
	private CQMResultDetailsDAO cqmResultDetailsDAO;
	
	@Autowired
	private CertificationResultDetailsDAO certificationResultDetailsDAO;
	
	@Autowired
	private AdditionalSoftwareDAO additionalSoftwareDAO;
	
	@Autowired
	private CertificationEventDAO certificationEventDAO;
	
	@Autowired
	private EventTypeDAO eventTypeDAO;
	

	private CQMCriterionDAO cqmCriterionDAO;
	
	private List<CQMCriterion> cqmCriteria = new ArrayList<CQMCriterion>();
	
	@Autowired
	public CertifiedProductDetailsManagerImpl(CQMCriterionDAO cqmCriterionDAO){
		this.cqmCriterionDAO = cqmCriterionDAO;
		loadCQMCriteria();
	}
	
	@Override
	@Transactional
	public CertifiedProductSearchDetails getCertifiedProductDetails(
			Long certifiedProductId) throws EntityRetrievalException {
		
		CertifiedProductDetailsDTO dto = certifiedProductSearchResultDAO.getById(certifiedProductId);
		
		CertifiedProductSearchDetails searchDetails = new CertifiedProductSearchDetails();
		
		searchDetails.setId(dto.getId());
		searchDetails.setAcbCertificationId(dto.getAcbCertificationId());
		
		searchDetails.setCertificationDate(dto.getCertificationDate().getTime() + "");
			
		searchDetails.getCertificationEdition().put("id", dto.getCertificationEditionId().toString());
		searchDetails.getCertificationEdition().put("name", dto.getYear());
				
		searchDetails.setCertificationStatusId(dto.getCertificationStatusId());	
			
		searchDetails.getCertifyingBody().put("id", dto.getCertificationBodyId().toString());
		searchDetails.getCertifyingBody().put("name", dto.getCertificationBodyName());
			
		searchDetails.setChplProductNumber(dto.getChplProductNumber());
		
		searchDetails.getClassificationType().put("id", dto.getProductClassificationTypeId().toString());
		searchDetails.getClassificationType().put("name", dto.getProductClassificationName());
		
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
		
		searchDetails.setVisibleOnChpl(dto.getVisibleOnChpl());
		
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
		
		
		
		List<AdditionalSoftwareDTO> additionalSoftwareDTOs = additionalSoftwareDAO.findByCertifiedProductId(dto.getId());
		List<AdditionalSoftware> additionalSoftware = new ArrayList<AdditionalSoftware>();
		
		
		for (AdditionalSoftwareDTO additionalSoftwareDTO : additionalSoftwareDTOs){
			AdditionalSoftware software = new AdditionalSoftware();
			
			software.setAdditionalSoftwareid(additionalSoftwareDTO.getId());
			software.setCertifiedProductId(additionalSoftwareDTO.getCertifiedProductId());
			software.setJustification(additionalSoftwareDTO.getJustification());
			software.setName(additionalSoftwareDTO.getName());
			software.setVersion(additionalSoftwareDTO.getVersion());
			additionalSoftware.add(software);
			
		}
		
		searchDetails.setAdditionalSoftware(additionalSoftware);
		
		if (dto.getYear().startsWith("2011")){
			searchDetails.setApplicableCqmCriteria(getAvailableNQFVersions());
		} else {
			searchDetails.setApplicableCqmCriteria(getAvailableCQMVersions());
		}
		
		
		searchDetails.setCertificationEvents(getCertificationEvents(dto.getId()));
		
		return searchDetails;
	}
	
	public List<CQMCriterion> getCqmCriteria() {
		return cqmCriteria;
	}
	
	public void setCqmCriteria(List<CQMCriterion> cqmCriteria) {
		this.cqmCriteria = cqmCriteria;
	}
	
	private List<CertificationEvent> getCertificationEvents(Long certifiedProductId) throws EntityRetrievalException{
		
		List<CertificationEvent> certEvents = new ArrayList<CertificationEvent>();
		List<CertificationEventDTO> eventDTOs = certificationEventDAO.findByCertifiedProductId(certifiedProductId);	
		
		for (CertificationEventDTO event : eventDTOs){
			
			CertificationEvent ce = new CertificationEvent();
			
			ce.setId(event.getId());
			ce.setCity(event.getCity());
								
			ce.setEventDate(event.getEventDate().getTime() + "");
			ce.setLastModifiedUser(event.getLastModifiedUser());
			ce.setLastModifiedDate(event.getLastModifiedDate().getTime() + "");
			ce.setState(event.getState());
			ce.setEventTypeId(event.getEventTypeId());
			
			EventTypeDTO eventTypeDTO = eventTypeDAO.getById(event.getEventTypeId());
			ce.setEventTypeDescription(eventTypeDTO.getDescription());
			ce.setEventTypeName(eventTypeDTO.getName());
			
			certEvents.add(ce);
		}
		
		return certEvents;
	}
	
	private void loadCQMCriteria(){
		
		List<CQMCriterionDTO> dtos = cqmCriterionDAO.findAll();
		
		for (CQMCriterionDTO dto: dtos){
			
			CQMCriterion criterion = new CQMCriterion();
			
			criterion.setCmsId(dto.getCmsId());
			criterion.setCqmCriterionTypeId(dto.getCqmCriterionTypeId());
			criterion.setCqmDomain(dto.getCqmDomain());
			criterion.setCqmVersionId(dto.getCqmVersionId());
			criterion.setCqmVersion(dto.getCqmVersion());
			criterion.setCriterionId(dto.getId());
			criterion.setDescription(dto.getDescription());
			criterion.setNqfNumber(dto.getNqfNumber());
			criterion.setNumber(dto.getNumber());
			criterion.setTitle(dto.getTitle());
			cqmCriteria.add(criterion);
			
		}
	}
	
	private List<CQMCriterion> getAvailableCQMVersions(){
		
		List<CQMCriterion> criteria = new ArrayList<CQMCriterion>();
		
		for (CQMCriterion criterion : cqmCriteria){
			
			if (criterion.getNumber().startsWith("CMS")){
				criteria.add(criterion);
			}
		}
		return criteria;
	}
	
	private List<CQMCriterion> getAvailableNQFVersions(){
		
		List<CQMCriterion> nqfs = new ArrayList<CQMCriterion>();
		
		for (CQMCriterion criterion : cqmCriteria){
			
			if (criterion.getNumber().startsWith("NQF")){
				nqfs.add(criterion);
			}
		}
		return nqfs;
	}
	
}
