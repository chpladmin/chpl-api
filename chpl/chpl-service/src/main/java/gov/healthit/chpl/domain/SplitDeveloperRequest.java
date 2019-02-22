package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.List;

public class SplitDeveloperRequest implements Serializable {
    private static final long serialVersionUID = -5814366900559692235L;

    private Developer newDeveloper;
    private List<Long> newDeveloperProductIds;
    private Developer oldDeveloper;
    private List<Long> oldDeveloperProductIds;
    public Developer getNewDeveloper() {
        return newDeveloper;
    }
    public void setNewDeveloper(final Developer newDeveloper) {
        this.newDeveloper = newDeveloper;
    }
    public List<Long> getNewDeveloperProductIds() {
        return newDeveloperProductIds;
    }
    public void setNewDeveloperProductIds(final List<Long> newDeveloperProductIds) {
        this.newDeveloperProductIds = newDeveloperProductIds;
    }
    public Developer getOldDeveloper() {
        return oldDeveloper;
    }
    public void setOldDeveloper(final Developer oldDeveloper) {
        this.oldDeveloper = oldDeveloper;
    }
    public List<Long> getOldDeveloperProductIds() {
        return oldDeveloperProductIds;
    }
    public void setOldDeveloperProductIds(final List<Long> oldDeveloperProductIds) {
        this.oldDeveloperProductIds = oldDeveloperProductIds;
    }

}
