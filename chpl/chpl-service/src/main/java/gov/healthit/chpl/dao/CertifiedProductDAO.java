package gov.healthit.chpl.dao;

import java.io.IOException;
import java.util.List;

import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;

public interface CertifiedProductDAO {
	
	public CertifiedProductDTO create(CertifiedProductDTO product) throws EntityCreationException;

	public CertifiedProductDTO update(CertifiedProductDTO product) throws EntityRetrievalException;
	
	public CertifiedProductDTO updateMeaningfulUseUsers(CertifiedProductDTO product) throws EntityRetrievalException, IOException;
	
	public void delete(Long productId);
	
	public List<CertifiedProductDetailsDTO> findAll();
	public List<CertifiedProductDetailsDTO> findWithSurveillance();
	public List<CertifiedProductDetailsDTO> findWithInheritance();

	public CertifiedProductDTO getById(Long productId) throws EntityRetrievalException;
	public CertifiedProductDetailsDTO getDetailsById(Long productId) throws EntityRetrievalException;
	public List<CertifiedProductDetailsDTO> getDetailsByIds(List<Long> productIds) throws EntityRetrievalException;
	
	public CertifiedProductDTO getByChplNumber(String chplProductNumber);
	public CertifiedProductDetailsDTO getByChplUniqueId(String chplUniqueId) throws EntityRetrievalException;
	
	public List<CertifiedProductDetailsDTO> getDetailsByChplNumbers(List<String> chplProductNumbers);
	public List<CertifiedProductDetailsDTO> getDetailsByVersionId(Long versionId);
	public List<CertifiedProductDetailsDTO> getDetailsByProductId(Long productId);
	public List<CertifiedProductDetailsDTO> getDetailsByAcbIds(List<Long> acbIds);
	public List<CertifiedProductDetailsDTO> getDetailsByVersionAndAcbIds(Long versionId, List<Long> acbIds);
	
	public List<CertifiedProductDTO> getByVersionIds(List<Long> versionIds);
	public List<CertifiedProductDTO> getCertifiedProductsForDeveloper(Long vendorId);
}
