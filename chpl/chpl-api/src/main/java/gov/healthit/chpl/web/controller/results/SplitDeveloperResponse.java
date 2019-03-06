package gov.healthit.chpl.web.controller.results;

import gov.healthit.chpl.domain.Developer;

public class SplitDeveloperResponse {
    private Developer oldDeveloper;
    private Developer newDeveloper;

    public Developer getOldDeveloper() {
        return oldDeveloper;
    }
    public void setOldDeveloper(final Developer oldDeveloper) {
        this.oldDeveloper = oldDeveloper;
    }
    public Developer getNewDeveloper() {
        return newDeveloper;
    }
    public void setNewDeveloper(final Developer newDeveloper) {
        this.newDeveloper = newDeveloper;
    }

}
