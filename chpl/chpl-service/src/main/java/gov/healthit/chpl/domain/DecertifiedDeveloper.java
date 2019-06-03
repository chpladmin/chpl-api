package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class DecertifiedDeveloper implements Serializable {
    private static final long serialVersionUID = -6684356941321177859L;
    private Long developerId;
    private String developerName;
    private Date decertificationDate;
    private Set<String> acbNames = new HashSet<String>();

    public DecertifiedDeveloper() {
    }

    public Long getDeveloperId() {
        return developerId;
    }

    public void setDeveloperId(final Long developerId) {
        this.developerId = developerId;
    }

    public String getDeveloperName() {
        return developerName;
    }

    public void setDeveloperName(final String developerName) {
        this.developerName = developerName;
    }

    public Date getDecertificationDate() {
        return decertificationDate;
    }

    public void setDecertificationDate(final Date decertificationDate) {
        this.decertificationDate = decertificationDate;
    }

    public Set<String> getAcbNames() {
        return acbNames;
    }

    public void setAcbNames(final Set<String> acbNames) {
        this.acbNames = acbNames;
    }
}
