package gov.healthit.chpl.manager;


import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.security.access.AccessDeniedException;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.entity.PendingCertifiedProductEntity;
import gov.healthit.chpl.web.controller.exception.ObjectMissingValidationException;


public interface PendingCertifiedProductManager {
	public PendingCertifiedProductDetails getById(List<CertificationBodyDTO> userAcbs, Long id) throws EntityNotFoundException, EntityRetrievalException, AccessDeniedException;
	public List<PendingCertifiedProductDTO> getPendingCertifiedProductsByAcb(Long acbId);	
	public PendingCertifiedProductDTO createOrReplace(Long acbId, PendingCertifiedProductEntity toCreate) throws EntityRetrievalException, EntityCreationException, JsonProcessingException;
	public void deletePendingCertifiedProduct(List<CertificationBodyDTO> userAcbs, Long pendingProductId) 
			throws EntityRetrievalException, EntityNotFoundException, EntityCreationException, 
			AccessDeniedException, JsonProcessingException, ObjectMissingValidationException;
	public void confirm(Long acbId, Long pendingProductId) throws EntityRetrievalException, JsonProcessingException, EntityCreationException;
	public boolean isPendingListingAvailableForUpdate(Long acbId, PendingCertifiedProductDTO pendingCp) 
			throws EntityRetrievalException, ObjectMissingValidationException;
	public boolean isPendingListingAvailableForUpdate(Long acbId, Long pendingProductId)
			throws EntityRetrievalException, ObjectMissingValidationException;
	
	public void addAllVersionsToCmsCriterion(PendingCertifiedProductDetails pcpDetails);
	public void addAllMeasuresToCertificationCriteria(PendingCertifiedProductDetails pcpDetails);
	
}
