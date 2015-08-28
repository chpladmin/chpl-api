package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.dto.CertifiedProductDTO;

public interface CertifiedProductManager {

	CertifiedProductDTO getById(Long certifiedProductId);
	List<CertifiedProductDTO> getByProductVersion(Long productVersionId);
	

	
}
