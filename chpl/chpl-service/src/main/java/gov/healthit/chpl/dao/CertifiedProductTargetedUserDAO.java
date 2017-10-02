package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CertifiedProductTargetedUserDTO;

public interface CertifiedProductTargetedUserDAO {

    public List<CertifiedProductTargetedUserDTO> getTargetedUsersByCertifiedProductId(Long certifiedProductId)
            throws EntityRetrievalException;

    public CertifiedProductTargetedUserDTO lookupMapping(Long certifiedProductId, Long tuId)
            throws EntityRetrievalException;

    public CertifiedProductTargetedUserDTO createCertifiedProductTargetedUser(CertifiedProductTargetedUserDTO toCreate)
            throws EntityCreationException;

    public CertifiedProductTargetedUserDTO deleteCertifiedProductTargetedUser(Long id) throws EntityRetrievalException;

}
