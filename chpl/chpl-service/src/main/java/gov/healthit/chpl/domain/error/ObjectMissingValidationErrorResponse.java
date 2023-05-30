package gov.healthit.chpl.domain.error;

import java.io.Serializable;

import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;

import gov.healthit.chpl.domain.auth.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class ObjectMissingValidationErrorResponse implements Serializable {
    private static final long serialVersionUID = -2186304673031903240L;
    private ImmutableSortedSet<String> errorMessages;
    private ImmutableSortedSet<String> warningMessages;
    private String objectId;
    private User contact;

    public ImmutableSortedSet<String> getErrorMessages() {
        return errorMessages;
    }

    public ImmutableSortedSet<String> getWarningMessages() {
        return warningMessages;
    }

    public User getContact() {
        return contact;
    }

    public void setContact(User contact) {
        this.contact = contact;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }
}
