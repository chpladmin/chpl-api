package gov.healthit.chpl.manager;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.domain.MeaningfulUseUser;
import gov.healthit.chpl.dto.CQMResultDetailsDTO;
import gov.healthit.chpl.dto.CertifiedProductAccessibilityStandardDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.CertifiedProductQmsStandardDTO;
import gov.healthit.chpl.dto.CertifiedProductTargetedUserDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.web.controller.results.MeaningfulUseUserResults;
public interface CertifiedProductManager {

	public CertifiedProductDTO getById(Long id) throws EntityRetrievalException;
	public CertifiedProductDTO getByChplProductNumber(String chplProductNumber) throws EntityRetrievalException;
	public boolean chplIdExists(String id) throws EntityRetrievalException;
	public List<CertifiedProductDetailsDTO> getDetailsByIds(List<Long> ids) throws EntityRetrievalException;
	public List<CertifiedProductDetailsDTO> getAll();
	public List<CertifiedProductDetailsDTO> getAllWithEditPermission();
	public List<CertifiedProductDetailsDTO> getByProduct(Long productId);
	public List<CertifiedProductDetailsDTO> getByVersion(Long versionId);
	public List<CertifiedProductDetailsDTO> getByVersionWithEditPermission(Long versionId);
	
	public CertifiedProductDTO changeOwnership(Long certifiedProductId, Long acbId) throws EntityRetrievalException, JsonProcessingException, EntityCreationException;
	public CertifiedProductDTO update(Long acbId, CertifiedProductDTO dto, 
			ListingUpdateRequest updateRequest, CertifiedProductSearchDetails existingListing) 
			throws EntityRetrievalException, JsonProcessingException, EntityCreationException;
	public MeaningfulUseUserResults updateMeaningfulUseUsers(Set<MeaningfulUseUser> meaningfulUseUserSet)
			throws EntityCreationException, EntityRetrievalException, JsonProcessingException, IOException;
	
	public CertifiedProductDTO createFromPending(Long acbId, PendingCertifiedProductDTO pendingCp) 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException;	
	public void checkSuspiciousActivity(CertifiedProductSearchDetails original, CertifiedProductSearchDetails changed);
}
