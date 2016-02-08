package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.AdditionalSoftware;
import gov.healthit.chpl.dto.AdditionalSoftwareDTO;
import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareMapDTO;

public interface AdditionalSoftwareManager {
	
	public AdditionalSoftwareDTO createAdditionalSoftware(AdditionalSoftwareDTO toCreate) throws EntityCreationException;
	public CertificationResultAdditionalSoftwareMapDTO addAdditionalSoftwareCertificationResultMapping(Long additionalSoftwareId, Long certificationResultId) throws EntityCreationException;
	public void deleteAdditionalSoftwareCertificationResultMapping(Long additionalSoftwareId, Long certificationResultId);
	public void associateAdditionalSoftwareCertifiedProductSelf(Long additionalSoftwareId, Long certifiedProductId) throws EntityRetrievalException;
	public List<AdditionalSoftware> getAdditionalSoftwareByCertificationResultId(Long id) throws EntityRetrievalException;
	
}
