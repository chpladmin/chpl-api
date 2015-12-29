package gov.healthit.chpl.manager;

import gov.healthit.chpl.dto.CQMResultAdditionalSoftwareMapDTO;
import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareMapDTO;

public interface AdditionalSoftwareManager {
	
	public CertificationResultAdditionalSoftwareMapDTO addAdditionalSoftwareCertificationResultMapping(Long additionalSoftwareId, Long certificationResultId);
	public CQMResultAdditionalSoftwareMapDTO addAdditionalSoftwareCQMResultMapping(Long additionalSoftwareId, Long cqmResultId);
	public void deleteAdditionalSoftwareCertificationResultMapping(Long additionalSoftwareId, Long certificationResultId);
	public void deleteAdditionalSoftwareCQMResultMapping(Long additionalSoftwareId, Long cqmResultId);
	public void associateAdditionalSoftwareCerifiedProductSelf(Long additionalSoftwareId, Long certifiedProductId);
	
}
