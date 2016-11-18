package gov.healthit.chpl.manager;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.security.access.AccessDeniedException;

import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.dto.CertificationBodyDTO;

public interface SurveillanceManager {
	public void validate(Surveillance surveillance);
	public void sendSuspiciousActivityEmail(Surveillance questionableSurv);
	
	public Long createSurveillance(Long abcId, Surveillance surv);
	public void updateSurveillance(Long abcId, Surveillance surv);
	public Surveillance getById(Long survId) throws EntityNotFoundException;
	public Surveillance getByFriendlyIdAndProduct(Long certifiedProductId, String survFriendlyId);
	public List<Surveillance> getByCertifiedProduct(Long cpId);
	public void deleteSurveillance(Long acbId, Long survId);
	
	public List<Surveillance> getPendingByAcb(Long acbId);
	public Surveillance getPendingById(Long acbId, Long survId) throws EntityNotFoundException;
	public Long createPendingSurveillance(Long acbId, Surveillance surv);
	public void deletePendingSurveillance(Long acbId, Long survId);
	public void deletePendingSurveillance(List<CertificationBodyDTO> userAcbs, Long survId)
			throws EntityNotFoundException, AccessDeniedException;
}
