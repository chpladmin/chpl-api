package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface CertificationResultDetailsDAO {

    List<CertificationResultDetailsDTO> getCertificationResultDetailsByCertifiedProductId(
            Long certifiedProductId) throws EntityRetrievalException;
    
    public List<CertificationResultDetailsDTO> getCertificationResultDetailsByCertifiedProductIdSED(
            Long certifiedProductId) throws EntityRetrievalException;

}
