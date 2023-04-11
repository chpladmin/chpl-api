package gov.healthit.chpl.exception;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.collections.api.set.ImmutableSet;

public class ValidationException extends Exception {
    protected Set<String> errorMessages;
    protected Set<String> businessErrorMessages;
    protected Set<String> dataErrorMessages;
    protected Set<String> warningMessages;

    private static final long serialVersionUID = 1L;

    public ValidationException() {
        super();
        errorMessages = new HashSet<String>();
        businessErrorMessages = new HashSet<>();
        dataErrorMessages = new HashSet<>();
        warningMessages = new HashSet<String>();
    }

    public ValidationException(String message) {
        super(message);
        errorMessages = new HashSet<String>();
        errorMessages.add(message);
        businessErrorMessages = new HashSet<>();
        dataErrorMessages = new HashSet<>();
        warningMessages = new HashSet<String>();
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        errorMessages = new HashSet<String>();
        errorMessages.add(message);
        businessErrorMessages = new HashSet<>();
        dataErrorMessages = new HashSet<>();
        warningMessages = new HashSet<String>();
    }

    public ValidationException(Throwable cause) {
        super(cause);
        errorMessages = new HashSet<String>();
        businessErrorMessages = new HashSet<>();
        dataErrorMessages = new HashSet<>();
        warningMessages = new HashSet<String>();
    }

    public ValidationException(Set<String> errorMessages) {
        super();
        this.errorMessages = errorMessages;
        businessErrorMessages = new HashSet<>();
        dataErrorMessages = new HashSet<>();
        this.warningMessages = new HashSet<String>();
    }

    public ValidationException(Set<String> errorMessages, Set<String> warningMessages) {
        super();
        this.errorMessages = errorMessages;
        businessErrorMessages = new HashSet<>();
        dataErrorMessages = new HashSet<>();
        this.warningMessages = warningMessages;
    }

    public ValidationException(ImmutableSet<String> errorMessages, ImmutableSet<String> businessErrorMessages, ImmutableSet<String> dataErrorMessages, Set<String> warningMessages) {
        super();
        this.errorMessages = errorMessages.castToSet();
        this.businessErrorMessages = businessErrorMessages.castToSet();
        this.dataErrorMessages = dataErrorMessages.castToSet();
        this.warningMessages = warningMessages;
    }

    public Set<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(final Set<String> errorMessages) {
        this.errorMessages = errorMessages;
    }

    public Set<String> getWarningMessages() {
        return warningMessages;
    }

    public void setWarningMessages(final Set<String> warningMessages) {
        this.warningMessages = warningMessages;
    }

    public Set<String> getBusinessErrorMessages() {
        return businessErrorMessages;
    }

    public Set<String> getDataErrorMessages() {
        return dataErrorMessages;
    }
}
