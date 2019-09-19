package gov.healthit.chpl.changerequest.manager;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;

public interface ChangeRequestManager {
    Set<KeyValueModel> getChangeRequestTypes();

    Set<KeyValueModel> getChangeRequestStatusTypes();

    ChangeRequest createChangeRequest(final ChangeRequest cr)
            throws EntityRetrievalException, ValidationException, EntityCreationException, JsonProcessingException;

    ChangeRequest getChangeRequest(final Long changeRequestId) throws EntityRetrievalException;

    List<ChangeRequest> getAllChangeRequestsForUser() throws EntityRetrievalException;

    ChangeRequest updateChangeRequest(final ChangeRequest cr)
            throws EntityRetrievalException, EntityCreationException, ValidationException, EntityCreationException,
            JsonProcessingException;
}
