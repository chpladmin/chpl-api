package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.CertifiedProductTargetedUserDTO;

import java.util.List;

public interface CertifiedProductTargetedUserDAO {
	
	public List<CertifiedProductTargetedUserDTO> getTargetedUsersByCertifiedProductId(Long certifiedProductId) throws EntityRetrievalException;
	public CertifiedProductTargetedUserDTO createCertifiedProductTargetedUser(CertifiedProductTargetedUserDTO toCreate) throws EntityCreationException;
	public CertifiedProductTargetedUserDTO deleteCertifiedProductTargetedUser(Long id) throws EntityRetrievalException;

}
