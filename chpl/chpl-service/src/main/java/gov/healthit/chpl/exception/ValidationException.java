package gov.healthit.chpl.exception;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;
import org.eclipse.collections.impl.factory.SortedSets;

public class ValidationException extends Exception {
    protected ImmutableSortedSet<String> errorMessages;
    protected ImmutableSortedSet<String> businessErrorMessages;
    protected ImmutableSortedSet<String> dataErrorMessages;
    protected Set<String> warningMessages;

    private static final long serialVersionUID = 1L;

    public ValidationException() {
        super();
        errorMessages = SortedSets.immutable.empty();
        businessErrorMessages = SortedSets.immutable.empty();
        dataErrorMessages = SortedSets.immutable.empty();
        warningMessages = new HashSet<String>();
    }

    public ValidationException(String message) {
        super(message);
        errorMessages = SortedSets.immutable.of(message);
        businessErrorMessages = SortedSets.immutable.empty();
        dataErrorMessages = SortedSets.immutable.empty();
        warningMessages = new HashSet<String>();
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        errorMessages = SortedSets.immutable.of(message);
        businessErrorMessages = SortedSets.immutable.empty();
        dataErrorMessages = SortedSets.immutable.empty();
        warningMessages = new HashSet<String>();
    }

    public ValidationException(Throwable cause) {
        super(cause);
        errorMessages = SortedSets.immutable.empty();
        businessErrorMessages = SortedSets.immutable.empty();
        dataErrorMessages = SortedSets.immutable.empty();
        warningMessages = new HashSet<String>();
    }

    public ValidationException(Set<String> errorMessages) {
        super();
        this.errorMessages = SortedSets.immutable.ofAll(errorMessages);
        businessErrorMessages = SortedSets.immutable.empty();
        dataErrorMessages = SortedSets.immutable.empty();
        this.warningMessages = new HashSet<String>();
    }

    public ValidationException(Set<String> errorMessages, Set<String> warningMessages) {
        super();
        this.errorMessages = SortedSets.immutable.ofAll(errorMessages);
        businessErrorMessages = SortedSets.immutable.empty();
        dataErrorMessages = SortedSets.immutable.empty();
        this.warningMessages = warningMessages;
    }

    public ValidationException(ImmutableSortedSet<String> errorMessages, ImmutableSortedSet<String> businessErrorMessages, ImmutableSortedSet<String> dataErrorMessages, Set<String> warningMessages) {
        super();
        this.errorMessages = errorMessages;
        this.businessErrorMessages = businessErrorMessages;
        this.dataErrorMessages = dataErrorMessages;
        this.warningMessages = warningMessages;
    }

    public Set<String> getErrorMessages() {
        return errorMessages.castToSortedSet();
    }

    public void setErrorMessages(Set<String> errorMessages) {
        this.errorMessages = SortedSets.immutable.ofAll(errorMessages);
    }

    public Set<String> getWarningMessages() {
        return warningMessages;
    }

    public void setWarningMessages(Set<String> warningMessages) {
        this.warningMessages = warningMessages;
    }

    public Set<String> getBusinessErrorMessages() {
        return businessErrorMessages.castToSortedSet();
    }

    public Set<String> getDataErrorMessages() {
        return dataErrorMessages.castToSortedSet();
    }
}
