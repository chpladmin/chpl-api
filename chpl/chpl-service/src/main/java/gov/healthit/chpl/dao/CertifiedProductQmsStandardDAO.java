package gov.healthit.chpl.dao;


import java.util.List;

import gov.healthit.chpl.dto.CertifiedProductQmsStandardDTO;

public interface CertifiedProductQmsStandardDAO {

	public List<CertifiedProductQmsStandardDTO> getQmsStandardsByCertifiedProductId(Long certifiedProductId) throws EntityRetrievalException;
	public CertifiedProductQmsStandardDTO lookupMapping(Long certifiedProductId, Long qmsStandardId) throws EntityRetrievalException;
	public CertifiedProductQmsStandardDTO updateCertifiedProductQms(CertifiedProductQmsStandardDTO toUpdate) throws EntityRetrievalException;
	public CertifiedProductQmsStandardDTO createCertifiedProductQms(CertifiedProductQmsStandardDTO toCreate) throws EntityCreationException;
	public CertifiedProductQmsStandardDTO deleteCertifiedProductQms(Long id) throws EntityRetrievalException;

}
