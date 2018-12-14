package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public class DuplicateReviewResult<T> {
    private List<T> uniqueList = new ArrayList<T>();
    private List<T> duplicateList = new ArrayList<T>();
    private BiPredicate<T,T> predicate;

    public DuplicateReviewResult(BiPredicate<T,T> predicate) {
        this.predicate = predicate;
    }

    public void addObject(final T t) {
        for (T item : uniqueList) {
            if (predicate.test(item, t)) {
                duplicateList.add(t);
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
}
