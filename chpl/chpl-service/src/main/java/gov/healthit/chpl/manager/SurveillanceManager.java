package gov.healthit.chpl.manager;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.security.access.AccessDeniedException;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformityDocument;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.entity.PendingSurveillanceEntity;
import gov.healthit.chpl.manager.impl.SurveillanceAuthorityAccessDeniedException;
import gov.healthit.chpl.web.controller.exception.ObjectMissingValidationException;

public interface SurveillanceManager {
	public File getDownloadFile(String filename) throws IOException;
	public File getProtectedDownloadFile(String filename) throws IOException;
	public void validate(Surveillance surveillance);
	public void sendSuspiciousActivityEmail(Surveillance questionableSurv);
	
	public Long createSurveillance(Long abcId, Surveillance surv) throws UserPermissionRetrievalException, SurveillanceAuthorityAccessDeniedException;
	public Long addDocumentToNonconformity(Long acbId, Long nonconformityId, SurveillanceNonconformityDocument doc);
	public void updateSurveillance(Long acbId, Surveillance surv) throws UserPermissionRetrievalException, SurveillanceAuthorityAccessDeniedException;
	public Surveillance getById(Long survId) throws EntityNotFoundException;
	public Surveillance getByFriendlyIdAndProduct(Long certifiedProductId, String survFriendlyId);
	public List<Surveillance> getByCertifiedProduct(Long cpId);
	public SurveillanceNonconformityDocument getDocumentById(Long docId, boolean getFileContents);
	public void deleteSurveillance(Long acbId, Surveillance surv) throws SurveillanceAuthorityAccessDeniedException;
	public void deleteNonconformityDocument(Long acbId, Long documentId);
	
	public List<Surveillance> getPendingByAcb(Long acbId);
	public Surveillance getPendingById(Long acbId, Long survId) throws EntityNotFoundException;
	public Long createPendingSurveillance(Long acbId, Surveillance surv);
	public void deletePendingSurveillance(Long acbId, Long survId) throws ObjectMissingValidationException, JsonProcessingException, EntityRetrievalException, EntityCreationException;
	public void deletePendingSurveillance(List<CertificationBodyDTO> userAcbs, Long survId)
			throws EntityNotFoundException, AccessDeniedException, ObjectMissingValidationException, JsonProcessingException, EntityRetrievalException, EntityCreationException;
	boolean isPendingSurveillanceAvailableForUpdate(Long acbId, PendingSurveillanceEntity pendingSurv)
			throws EntityRetrievalException, ObjectMissingValidationException;
}
