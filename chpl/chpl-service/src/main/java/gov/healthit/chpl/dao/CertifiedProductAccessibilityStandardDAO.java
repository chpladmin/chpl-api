package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CertifiedProductAccessibilityStandardDTO;

public interface CertifiedProductAccessibilityStandardDAO {

    public List<CertifiedProductAccessibilityStandardDTO> getAccessibilityStandardsByCertifiedProductId(
            Long certifiedProductId) throws EntityRetrievalException;

    public CertifiedProductAccessibilityStandardDTO lookupMapping(Long certifiedProductId, Long accStdId)
            throws EntityRetrievalException;

    public CertifiedProductAccessibilityStandardDTO createCertifiedProductAccessibilityStandard(
            CertifiedProductAccessibilityStandardDTO toCreate) throws EntityCreationException;

    public CertifiedProductAccessibilityStandardDTO deleteCertifiedProductAccessibilityStandards(Long id)
            throws EntityRetrievalException;

}
