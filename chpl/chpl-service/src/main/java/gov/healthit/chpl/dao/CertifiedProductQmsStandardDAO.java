package gov.healthit.chpl.dao;


import gov.healthit.chpl.dto.CertifiedProductQmsStandardDTO;

import java.util.List;

public interface CertifiedProductQmsStandardDAO {
	
	public List<CertifiedProductQmsStandardDTO> getQmsStandardsByCertifiedProductId(Long certifiedProductId) throws EntityRetrievalException;
	public CertifiedProductQmsStandardDTO updateCertifiedProductQms(CertifiedProductQmsStandardDTO toUpdate) throws EntityRetrievalException;
	public CertifiedProductQmsStandardDTO createCertifiedProductQms(CertifiedProductQmsStandardDTO toCreate) throws EntityCreationException;
	public CertifiedProductQmsStandardDTO deleteCertifiedProductQms(Long id) throws EntityRetrievalException;

}
