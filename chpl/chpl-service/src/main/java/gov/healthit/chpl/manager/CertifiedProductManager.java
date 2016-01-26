package gov.healthit.chpl.manager;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.dto.AdditionalSoftwareDTO;
import gov.healthit.chpl.dto.CQMCriterionDTO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
public interface CertifiedProductManager {

	public CertifiedProductDTO getById(Long id) throws EntityRetrievalException;
	public List<CertifiedProductDetailsDTO> getAll();
	public List<CertifiedProductDetailsDTO> getAllWithEditPermission();
	public List<CertifiedProductDetailsDTO> getByVersion(Long versionId);
	public List<CertifiedProductDetailsDTO> getByVersions(List<Long> versionIds);
	public List<CertifiedProductDetailsDTO> getByVersionWithEditPermission(Long versionId);
	
//	public CertifiedProductDTO create(CertifiedProductDTO dto) throws EntityRetrievalException, EntityCreationException;
//	public CertifiedProductDTO update(CertifiedProductDTO dto) throws EntityRetrievalException, JsonProcessingException, EntityCreationException;
	public CertifiedProductDTO changeOwnership(Long certifiedProductId, Long acbId) throws EntityRetrievalException, JsonProcessingException, EntityCreationException;
	public CertifiedProductDTO update(Long acbId, CertifiedProductDTO dto) throws EntityRetrievalException, JsonProcessingException, EntityCreationException;
	public CertifiedProductDTO updateCertifiedProductVersion(Long certifiedProductId, Long newVersionId) throws EntityRetrievalException;
//	public void delete(CertifiedProductDTO dto) throws EntityRetrievalException;
//	public void delete(Long certifiedProductId) throws EntityRetrievalException;
	
	public CertifiedProductDTO createFromPending(Long acbId, PendingCertifiedProductDetails pendingCp) 
			throws EntityRetrievalException, EntityCreationException, JsonProcessingException;
	//public void replaceCertifications(Long acbId, CertifiedProductDTO dto, Map<CertificationCriterionDTO, Boolean> certResults)
	//		throws EntityCreationException, EntityRetrievalException, JsonProcessingException;
	//public void replaceCqms(Long acbId, CertifiedProductDTO productDto, Map<CQMCriterionDTO, Boolean> cqmResults)
	//		throws EntityRetrievalException, EntityCreationException, JsonProcessingException;
	//public void replaceAdditionalSoftware(Long acbId, CertifiedProductDTO productDto, List<AdditionalSoftwareDTO> newSoftware) 
	//		throws EntityCreationException, EntityRetrievalException, JsonProcessingException;
	//public void updateCertifications(Long acbId, CertifiedProductDTO productDto, Map<CertificationCriterionDTO, Boolean> certResults)
	//		throws EntityCreationException, EntityRetrievalException, JsonProcessingException;
	public void updateCqms(Long acbId, CertifiedProductDTO productDto, Map<CQMCriterionDTO, Boolean> cqmResults)
			throws EntityCreationException, EntityRetrievalException,
			JsonProcessingException;
	public void updateAdditionalSoftware(Long acbId, CertifiedProductDTO productDto, List<AdditionalSoftwareDTO> newSoftware)
			throws EntityCreationException, EntityRetrievalException,
			JsonProcessingException;
	public void updateCertifications(Long acbId, CertifiedProductDTO productDto, List<CertificationResult> certResults)
			throws EntityCreationException, EntityRetrievalException,
			JsonProcessingException;
}
