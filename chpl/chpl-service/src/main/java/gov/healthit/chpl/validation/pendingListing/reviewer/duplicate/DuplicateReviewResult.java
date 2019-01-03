package gov.healthit.chpl.validation.pendingListing.reviewer.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public class DuplicateReviewResult<T> {
    private List<T> uniqueList = new ArrayList<T>();
    private List<T> duplicateList = new ArrayList<T>();
    private BiPredicate<T, T> predicate;

    public DuplicateReviewResult(final BiPredicate<T, T> predicate) {
        this.predicate = predicate;
    }

    public void addObject(final T t) {
        for (T item : uniqueList) {
            if (predicate.test(item, t)) {
                duplicateList.add(t);
                return;
            }
        }
        uniqueList.add(t);
    }

    public List<T> getUniqueList() {
        return uniqueList;
    }

    public List<T> getDuplicateList() {
        return duplicateList;
    }

    public Boolean duplicatesExist() {
        return getDuplicateList().size() > 0;
    }
}
