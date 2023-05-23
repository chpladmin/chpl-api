package gov.healthit.chpl.exception;

import java.util.List;
import java.util.Set;

import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;
import org.eclipse.collections.impl.factory.SortedSets;

public class ValidationException extends Exception {
    protected ImmutableSortedSet<String> errorMessages;
    protected ImmutableSortedSet<String> businessErrorMessages;
    protected ImmutableSortedSet<String> dataErrorMessages;
    protected ImmutableSortedSet<String> warningMessages;

    private static final long serialVersionUID = 1L;

    public ValidationException() {
        super();
        errorMessages = SortedSets.immutable.empty();
        businessErrorMessages = SortedSets.immutable.empty();
        dataErrorMessages = SortedSets.immutable.empty();
        warningMessages = SortedSets.immutable.empty();
    }

    public ValidationException(String message) {
        super(message);
        errorMessages = SortedSets.immutable.of(message);
        businessErrorMessages = SortedSets.immutable.empty();
        dataErrorMessages = SortedSets.immutable.empty();
        warningMessages = SortedSets.immutable.empty();
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        errorMessages = SortedSets.immutable.of(message);
        businessErrorMessages = SortedSets.immutable.empty();
        dataErrorMessages = SortedSets.immutable.empty();
        warningMessages = SortedSets.immutable.empty();
    }

    public ValidationException(Throwable cause) {
        super(cause);
        errorMessages = SortedSets.immutable.empty();
        businessErrorMessages = SortedSets.immutable.empty();
        dataErrorMessages = SortedSets.immutable.empty();
        warningMessages = SortedSets.immutable.empty();
    }

    public ValidationException(ImmutableSortedSet<String> errorMessages) {
        super();
        this.errorMessages = errorMessages;
        businessErrorMessages = SortedSets.immutable.empty();
        dataErrorMessages = SortedSets.immutable.empty();
        this.warningMessages = SortedSets.immutable.empty();
    }

    public ValidationException(List<String> errorMessages) {
        super();
        this.errorMessages = SortedSets.immutable.ofAll(errorMessages);
        businessErrorMessages = SortedSets.immutable.empty();
        dataErrorMessages = SortedSets.immutable.empty();
        this.warningMessages = SortedSets.immutable.empty();
    }
    public ValidationException(Set<String> errorMessages) {
        super();
        this.errorMessages = SortedSets.immutable.ofAll(errorMessages);
        businessErrorMessages = SortedSets.immutable.empty();
        dataErrorMessages = SortedSets.immutable.empty();
        this.warningMessages = SortedSets.immutable.empty();
    }
    public ValidationException(Set<String> errorMessages, Set<String> warningMessages) {
        super();
        this.errorMessages = SortedSets.immutable.ofAll(errorMessages);
        businessErrorMessages = SortedSets.immutable.empty();
        dataErrorMessages = SortedSets.immutable.empty();
        this.warningMessages = SortedSets.immutable.ofAll(warningMessages);
    }

    public ValidationException(ImmutableSortedSet<String> errorMessages, ImmutableSortedSet<String> businessErrorMessages, ImmutableSortedSet<String> dataErrorMessages, ImmutableSortedSet<String> warningMessages) {
        super();
        this.errorMessages = errorMessages;
        this.businessErrorMessages = businessErrorMessages;
        this.dataErrorMessages = dataErrorMessages;
        this.warningMessages = warningMessages;
    }

    public ImmutableSortedSet<String> getErrorMessages() {
        return errorMessages;
    }

    public ImmutableSortedSet<String> getWarningMessages() {
        return warningMessages;
    }

    public ImmutableSortedSet<String> getBusinessErrorMessages() {
        return businessErrorMessages;
    }

    public ImmutableSortedSet<String> getDataErrorMessages() {
        return dataErrorMessages;
    }
}
