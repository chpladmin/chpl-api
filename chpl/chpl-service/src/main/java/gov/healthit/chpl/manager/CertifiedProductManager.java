package gov.healthit.chpl.manager;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.dto.CQMCriterionDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.CertifiedProductQmsStandardDTO;
import gov.healthit.chpl.dto.CertifiedProductTargetedUserDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
public interface CertifiedProductManager {

	public CertifiedProductDTO getById(Long id) throws EntityRetrievalException;
	public List<CertifiedProductDetailsDTO> getAll();
	public List<CertifiedProductDetailsDTO> getAllWithEditPermission();
	public List<CertifiedProductDetailsDTO> getByVersion(Long versionId);
	public List<CertifiedProductDetailsDTO> getByVersions(List<Long> versionIds);
	public List<CertifiedProductDetailsDTO> getByVersionWithEditPermission(Long versionId);
	
	public CertifiedProductDTO changeOwnership(Long certifiedProductId, Long acbId) throws EntityRetrievalException, JsonProcessingException, EntityCreationException;
	public CertifiedProductDTO update(Long acbId, CertifiedProductDTO dto) throws EntityRetrievalException, JsonProcessingException, EntityCreationException;
	public CertifiedProductDTO updateCertifiedProductVersion(Long certifiedProductId, Long newVersionId) throws EntityRetrievalException;
	
	public CertifiedProductDTO createFromPending(Long acbId, PendingCertifiedProductDTO pendingCp) 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException;
	public void updateQmsStandards(Long acbId, CertifiedProductDTO productDto, List<CertifiedProductQmsStandardDTO> newQmsStandards)
			throws EntityCreationException, EntityRetrievalException, JsonProcessingException;
	public void updateTargetedUsers(Long acbId, CertifiedProductDTO productDto, List<CertifiedProductTargetedUserDTO> newTargetedUsers)
			throws EntityCreationException, EntityRetrievalException, JsonProcessingException;	
	public void updateCqms(Long acbId, CertifiedProductDTO productDto, Map<CQMCriterionDTO, Boolean> cqmResults)
			throws EntityCreationException, EntityRetrievalException,
			JsonProcessingException;
	public void updateCertifications(Long acbId, CertifiedProductDTO productDto, List<CertificationResult> certResults)
			throws EntityCreationException, EntityRetrievalException,
			JsonProcessingException;
}
