package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CertifiedProductAccessibilityStandardDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface CertifiedProductAccessibilityStandardDAO {

    List<CertifiedProductAccessibilityStandardDTO> getAccessibilityStandardsByCertifiedProductId(
            Long certifiedProductId) throws EntityRetrievalException;

    CertifiedProductAccessibilityStandardDTO lookupMapping(Long certifiedProductId, Long accStdId)
            throws EntityRetrievalException;

    CertifiedProductAccessibilityStandardDTO createCertifiedProductAccessibilityStandard(
            CertifiedProductAccessibilityStandardDTO toCreate) throws EntityCreationException;

    CertifiedProductAccessibilityStandardDTO deleteCertifiedProductAccessibilityStandards(Long id)
            throws EntityRetrievalException;

}
