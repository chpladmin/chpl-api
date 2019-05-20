package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class DecertifiedDeveloperDTO implements Serializable {
    private static final long serialVersionUID = 4328181604320362899L;

    private DeveloperDTO developer;
    private Set<CertificationBodyDTO> acbs = new HashSet<CertificationBodyDTO>();
    private Date decertificationDate;
    public DeveloperDTO getDeveloper() {
        return developer;
    }
    public void setDeveloper(final DeveloperDTO developer) {
        this.developer = developer;
    }
    public Set<CertificationBodyDTO> getAcbs() {
        return acbs;
    }
    public void setAcbs(final Set<CertificationBodyDTO> acbs) {
        this.acbs = acbs;
    }
    public Date getDecertificationDate() {
        return decertificationDate;
    }
    public void setDecertificationDate(final Date decertificationDate) {
        this.decertificationDate = decertificationDate;
    }
}
