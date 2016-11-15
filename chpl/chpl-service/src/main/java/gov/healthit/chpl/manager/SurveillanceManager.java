package gov.healthit.chpl.manager;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.security.access.AccessDeniedException;

import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.dto.CertificationBodyDTO;

public interface SurveillanceManager {
	public List<Surveillance> getPendingByAcb(Long acbId);
	public Surveillance getPendingById(Long acbId, Long survId) throws EntityNotFoundException;
	public Long createPendingSurveillance(Long acbId, Surveillance surv);
	public void deletePendingSurveillance(Long acbId, Long survId);
	public void deletePendingSurveillance(List<CertificationBodyDTO> userAcbs, Long survId)
			throws EntityNotFoundException, AccessDeniedException;
}
