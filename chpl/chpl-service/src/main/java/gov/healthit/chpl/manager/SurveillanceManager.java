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
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceEntity;
import gov.healthit.chpl.manager.impl.SurveillanceAuthorityAccessDeniedException;
import gov.healthit.chpl.web.controller.exception.ObjectMissingValidationException;

public interface SurveillanceManager extends QuestionableActivityHandler {
	public File getDownloadFile(String filename) throws IOException;
	public File getProtectedDownloadFile(String filename) throws IOException;
	public void validate(Surveillance surveillance);

	public Long createSurveillance(Long abcId, Surveillance surv) throws UserPermissionRetrievalException, SurveillanceAuthorityAccessDeniedException;
	public Long addDocumentToNonconformity(Long acbId, Long nonconformityId, SurveillanceNonconformityDocument doc) throws EntityRetrievalException;
	public void updateSurveillance(Long acbId, Surveillance surv) throws EntityRetrievalException, UserPermissionRetrievalException, SurveillanceAuthorityAccessDeniedException;
	public Surveillance getById(Long survId) throws EntityRetrievalException;
	public Surveillance getByFriendlyIdAndProduct(Long certifiedProductId, String survFriendlyId);
	public List<Surveillance> getByCertifiedProduct(Long cpId);
	public SurveillanceNonconformityDocument getDocumentById(Long docId, boolean getFileContents) throws EntityRetrievalException;
	public void deleteSurveillance(Long acbId, Surveillance surv) throws EntityRetrievalException, SurveillanceAuthorityAccessDeniedException;
	public void deleteNonconformityDocument(Long acbId, Long documentId) throws EntityRetrievalException;

	public List<Surveillance> getPendingByAcb(Long acbId);
	public Surveillance getPendingById(Long acbId, Long survId, boolean includeDeleted) throws EntityRetrievalException;
	public Long createPendingSurveillance(Long acbId, Surveillance surv);
	public void deletePendingSurveillance(Long acbId, Long survId, boolean isConfirmed) throws ObjectMissingValidationException, JsonProcessingException,
	EntityRetrievalException, EntityCreationException;
	public void deletePendingSurveillance(List<CertificationBodyDTO> userAcbs, Long survId, boolean isConfirmed)
			throws EntityNotFoundException, AccessDeniedException, ObjectMissingValidationException, JsonProcessingException, EntityRetrievalException, EntityCreationException;
	public boolean isPendingSurveillanceAvailableForUpdate(Long acbId, Long pendingSurvId)
			throws EntityRetrievalException, ObjectMissingValidationException;
	boolean isPendingSurveillanceAvailableForUpdate(Long acbId, PendingSurveillanceEntity pendingSurv)
			throws EntityRetrievalException, ObjectMissingValidationException;

}
