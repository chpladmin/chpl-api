package gov.healthit.chpl.manager;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceUploadResult;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ObjectMissingValidationException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.impl.SurveillanceAuthorityAccessDeniedException;

public interface PendingSurveillanceManager {
    List<Surveillance> getAllPendingSurveillances();

    SurveillanceUploadResult uploadPendingSurveillance(final MultipartFile file)
            throws ValidationException, EntityCreationException, EntityRetrievalException;

    void rejectPendingSurveillance(final Long id)
            throws ObjectMissingValidationException, JsonProcessingException, EntityRetrievalException,
            EntityCreationException;

    Surveillance confirmPendingSurveillance(final Surveillance survToInsert)
            throws ValidationException, EntityRetrievalException, UserPermissionRetrievalException,
            SurveillanceAuthorityAccessDeniedException, EntityCreationException, JsonProcessingException;
}
