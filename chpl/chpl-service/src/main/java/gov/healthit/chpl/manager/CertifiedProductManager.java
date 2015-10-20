package gov.healthit.chpl.manager;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.dto.AdditionalSoftwareDTO;
import gov.healthit.chpl.dto.CQMCriterionDTO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;

import java.util.List;
import java.util.Map;


public interface CertifiedProductManager {

	public List<CertifiedProductDTO> getAll();
	public CertifiedProductDTO getById(Long id) throws EntityRetrievalException;
	public List<CertifiedProductDTO> getByVersion(Long versionId);
	public List<CertifiedProductDTO> getByVersions(List<Long> versionIds);
	
//	public CertifiedProductDTO create(CertifiedProductDTO dto) throws EntityRetrievalException, EntityCreationException;
	public CertifiedProductDTO changeOwnership(Long certifiedProductId, Long acbId) throws EntityRetrievalException;
	public CertifiedProductDTO updateCertifiedProductVersion(Long certifiedProductId, Long newVersionId) throws EntityRetrievalException;
	public CertifiedProductDTO update(Long acbId, CertifiedProductDTO dto) throws EntityRetrievalException;
//	public void delete(CertifiedProductDTO dto) throws EntityRetrievalException;
//	public void delete(Long certifiedProductId) throws EntityRetrievalException;
	
	public CertifiedProductDTO createFromPending(Long acbId, PendingCertifiedProductDetails pendingCp) 
			throws EntityRetrievalException, EntityCreationException;
	public void replaceCertifications(Long acbId, CertifiedProductDTO dto, Map<CertificationCriterionDTO, Boolean> certResults)
			throws EntityCreationException, EntityRetrievalException;
	public void replaceCqms(Long acbId, CertifiedProductDTO productDto, Map<CQMCriterionDTO, Boolean> cqmResults)
			throws EntityRetrievalException, EntityCreationException;
	public void replaceAdditionalSoftware(Long acbId, CertifiedProductDTO productDto, List<AdditionalSoftwareDTO> newSoftware) 
			throws EntityCreationException;
	
}
