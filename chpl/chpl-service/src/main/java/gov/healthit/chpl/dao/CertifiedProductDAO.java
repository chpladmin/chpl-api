package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.CertifiedProductDTO;

import java.util.List;

public interface CertifiedProductDAO {
	
	public CertifiedProductDTO create(CertifiedProductDTO product) throws EntityCreationException;

	public CertifiedProductDTO update(CertifiedProductDTO product) throws EntityRetrievalException;
	
	public void delete(Long productId);
	
	public List<CertifiedProductDTO> findAll();
	
	public CertifiedProductDTO getById(Long productId) throws EntityRetrievalException;

	public List<CertifiedProductDTO> getByVersionId(Long versionId);
	
	public List<CertifiedProductDTO> getByVersionIds(List<Long> versionIds);
	public List<CertifiedProductDTO> getByAcbIds(List<Long> acbIds);
	public List<CertifiedProductDTO> getByVersionAndAcbIds(Long versionId, List<Long> acbIds);
	public String getLargestChplNumber();
}
