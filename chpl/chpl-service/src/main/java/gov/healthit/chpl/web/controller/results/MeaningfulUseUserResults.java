package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.MeaningfulUseUser;

public class MeaningfulUseUserResults implements Serializable {
    private static final long serialVersionUID = 4865758231142816185L;
    private List<MeaningfulUseUser> meaningfulUseUsers;
    private List<MeaningfulUseUser> errors;

    public MeaningfulUseUserResults() {
        setMeaningfulUseUsers(new ArrayList<MeaningfulUseUser>());
        errors = new ArrayList<MeaningfulUseUser>();
    }

    public MeaningfulUseUserResults(List<MeaningfulUseUser> results) {
        this.setMeaningfulUseUsers(results);
    }

    public List<MeaningfulUseUser> getMeaningfulUseUsers() {
        return meaningfulUseUsers;
    }

    public void setMeaningfulUseUsers(final List<MeaningfulUseUser> meaningfulUseUsers) {
        this.meaningfulUseUsers = meaningfulUseUsers;
    }

    public List<MeaningfulUseUser> getErrors() {
        return errors;
    }

    public void setErrors(final List<MeaningfulUseUser> errors) {
        this.errors = errors;
    }
}
