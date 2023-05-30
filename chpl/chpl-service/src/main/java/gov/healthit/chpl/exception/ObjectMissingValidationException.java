package gov.healthit.chpl.exception;

import java.util.Date;

import org.eclipse.collections.api.factory.SortedSets;

import gov.healthit.chpl.domain.auth.User;
import lombok.Data;

@Data
public class ObjectMissingValidationException extends ValidationException {
    private static final long serialVersionUID = -6542978782670873229L;

    private String objectId;
    private User user;
    private Date startDate;
    private Date endDate;

    public ObjectMissingValidationException() {
        super();
    }

    public ObjectMissingValidationException(String message, User user, String objectId) {
        super();
        errorMessages = SortedSets.immutable.of(message);
        setUser(user);
        this.objectId = objectId;
    }
}
