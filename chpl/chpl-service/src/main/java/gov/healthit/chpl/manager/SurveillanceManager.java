package gov.healthit.chpl.manager;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

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
import gov.healthit.chpl.web.controller.exception.ValidationException;

public interface SurveillanceManager {
    static final String HEADING_CELL_INDICATOR = "RECORD_STATUS__C";
    static final String NEW_SURVEILLANCE_BEGIN_INDICATOR = "New";
    static final String UPDATE_SURVEILLANCE_BEGIN_INDICATOR = "Update";
    static final String SUBELEMENT_INDICATOR = "Subelement";
    
    File getDownloadFile(String filename) throws IOException;

    File getProtectedDownloadFile(String filename) throws IOException;

    void validate(Surveillance surveillance);

    Long createSurveillance(Long abcId, Surveillance surv)
            throws UserPermissionRetrievalException, SurveillanceAuthorityAccessDeniedException;

    Long addDocumentToNonconformity(Long acbId, Long nonconformityId, SurveillanceNonconformityDocument doc)
            throws EntityRetrievalException;

    void updateSurveillance(Long acbId, Surveillance surv) throws EntityRetrievalException,
            UserPermissionRetrievalException, SurveillanceAuthorityAccessDeniedException;

    Surveillance getById(Long survId) throws EntityRetrievalException;

    Surveillance getByFriendlyIdAndProduct(Long certifiedProductId, String survFriendlyId);

    List<Surveillance> getByCertifiedProduct(Long cpId);

    SurveillanceNonconformityDocument getDocumentById(Long docId, boolean getFileContents)
            throws EntityRetrievalException;

    void deleteSurveillance(Long acbId, Surveillance surv)
            throws EntityRetrievalException, SurveillanceAuthorityAccessDeniedException;

    void deleteNonconformityDocument(Long acbId, Long documentId) throws EntityRetrievalException;

    List<Surveillance> getPendingByAcb(Long acbId);

    Surveillance getPendingById(Long acbId, Long survId, boolean includeDeleted) throws EntityRetrievalException;

    Long createPendingSurveillance(Long acbId, Surveillance surv);
    
    public int countSurveillanceRecords(MultipartFile file) throws ValidationException;
    public List<Surveillance> parseUploadFile(MultipartFile file) throws ValidationException;
    
    void deletePendingSurveillance(Long acbId, Long survId, boolean isConfirmed)
            throws ObjectMissingValidationException, JsonProcessingException, EntityRetrievalException,
            EntityCreationException;

    void deletePendingSurveillance(List<CertificationBodyDTO> userAcbs, Long survId, boolean isConfirmed)
            throws EntityNotFoundException, AccessDeniedException, ObjectMissingValidationException,
            JsonProcessingException, EntityRetrievalException, EntityCreationException;

    boolean isPendingSurveillanceAvailableForUpdate(Long acbId, Long pendingSurvId)
            throws EntityRetrievalException, ObjectMissingValidationException;

    boolean isPendingSurveillanceAvailableForUpdate(Long acbId, PendingSurveillanceEntity pendingSurv)
            throws EntityRetrievalException, ObjectMissingValidationException;

}
