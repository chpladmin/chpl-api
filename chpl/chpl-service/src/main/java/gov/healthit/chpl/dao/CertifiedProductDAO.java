package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;

import java.util.List;

public interface CertifiedProductDAO {
	
	public CertifiedProductDTO create(CertifiedProductDTO product) throws EntityCreationException;

	public CertifiedProductDTO update(CertifiedProductDTO product) throws EntityRetrievalException;
	
	public void delete(Long productId);
	
	public List<CertifiedProductDetailsDTO> findAll();
	
	public CertifiedProductDTO getById(Long productId) throws EntityRetrievalException;
	public CertifiedProductDTO getByChplNumber(String chplProductNumber);
	public CertifiedProductDetailsDTO getByChplUniqueId(String chplUniqueId) throws EntityRetrievalException;
	
	public List<CertifiedProductDetailsDTO> getDetailsByVersionId(Long versionId);
	public List<CertifiedProductDetailsDTO> getDetailsByVersionIds(List<Long> versionIds);
	public List<CertifiedProductDetailsDTO> getDetailsByAcbIds(List<Long> acbIds);
	public List<CertifiedProductDetailsDTO> getDetailsByVersionAndAcbIds(Long versionId, List<Long> acbIds);
	
	public List<CertifiedProductDTO> getByVersionIds(List<Long> versionIds);
	public List<CertifiedProductDTO> getCertifiedProductsForDeveloper(Long vendorId);
}
