package gov.healthit.chpl.dao;


import gov.healthit.chpl.dto.CertificationResultDetailsDTO;

import java.util.List;

public interface CertificationResultDetailsDAO {
	
	public List<CertificationResultDetailsDTO> getCertificationResultDetailsByCertifiedProductId(Long certifiedProductId) throws EntityRetrievalException;

}
