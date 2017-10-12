package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CertificationResultDetailsDTO;

public interface CertificationResultDetailsDAO {

    List<CertificationResultDetailsDTO> getCertificationResultDetailsByCertifiedProductId(
            Long certifiedProductId) throws EntityRetrievalException;

}
