package gov.healthit.chpl.manager;

import gov.healthit.chpl.dto.CertifiedProductDTO;

import java.util.List;



public interface CertifiedProductManager {

	List<CertifiedProductDTO> getByVersion(Long versionId);
	
}
