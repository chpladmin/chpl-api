package gov.healthit.chpl.manager;


import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.SurveillanceDetails;
import gov.healthit.chpl.dto.SurveillanceCertificationResultDTO;
import gov.healthit.chpl.dto.SurveillanceDTO;

public interface SurveillanceManager {
			
	public SurveillanceDetails create(Long acbId, SurveillanceDTO toCreate)
		throws EntityRetrievalException, EntityCreationException, JsonProcessingException;
	
	public SurveillanceDetails addCertificationsToSurveillance(Long acbId, Long surveillanceId, 
			List<SurveillanceCertificationResultDTO> certs)
		throws EntityRetrievalException, EntityCreationException, JsonProcessingException;
	
	public void removeCertificationsFromSurveillance(Long acbId, List<SurveillanceCertificationResultDTO> certs)
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException;
	
	public SurveillanceCertificationResultDTO updateCertification(Long acbId, SurveillanceCertificationResultDTO cert)
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException;
	
	public SurveillanceDTO getSurveillanceById(Long capId) throws EntityRetrievalException;
	
	public List<SurveillanceDTO> getSurveillanceForCertifiedProduct(Long certifiedProductId) throws EntityRetrievalException;
	
	public List<SurveillanceCertificationResultDTO> getCertificationsForSurveillance(Long surveillanceId) throws EntityRetrievalException;
	
	public List<SurveillanceDetails> getSurveillanceForCertifiedProductDetails(Long certifiedProductId) 
			throws EntityRetrievalException;
	
	public SurveillanceDetails getSurveillanceDetails(Long surveillanceId) throws EntityRetrievalException;
	
	public SurveillanceDTO update(Long acbId, SurveillanceDTO toUpdate) 
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException;
	
	public void delete(Long acbId, Long surveillanceId)  throws EntityRetrievalException, EntityCreationException, JsonProcessingException;
}
