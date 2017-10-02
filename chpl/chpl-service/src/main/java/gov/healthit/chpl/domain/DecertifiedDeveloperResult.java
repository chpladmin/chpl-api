package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import gov.healthit.chpl.dto.DeveloperDTO;

public class DecertifiedDeveloperResult implements Serializable {
    private static final long serialVersionUID = 7463932788024776464L;
    private Developer developer;
    private List<CertificationBody> certifyingBody;
    private Date decertificationDate;
    private Long estimatedUsers;

    public DecertifiedDeveloperResult() {
    }

    public DecertifiedDeveloperResult(Developer developer, List<CertificationBody> certifyingBody,
            Date decertificationDate, Long estimatedUsers) {
        this.developer = developer;
        this.certifyingBody = certifyingBody;
        this.decertificationDate = decertificationDate;
        this.estimatedUsers = estimatedUsers;
    }

    public DecertifiedDeveloperResult(DeveloperDTO developerDTO, List<CertificationBody> certifyingBody,
            Date decertificationDate, Long estimatedUsers) {
        this.developer = new Developer(developerDTO);
        this.certifyingBody = certifyingBody;
        this.decertificationDate = decertificationDate;
        this.estimatedUsers = estimatedUsers;
    }

    public Developer getDeveloper() {
        return developer;
    }

    public void setDeveloper(Developer developer) {
        this.developer = developer;
    }

    public List<CertificationBody> getCertifyingBody() {
        return certifyingBody;
    }

    public void setCertifyingBody(List<CertificationBody> certifyingBody) {
        this.certifyingBody = certifyingBody;
    }

    public Long getEstimatedUsers() {
        return estimatedUsers;
    }

    public void setEstimatedUsers(Long estimatedUsers) {
        this.estimatedUsers = estimatedUsers;
    }

    public Date getDecertificationDate() {
        return decertificationDate;
    }

    public void setDecertificationDate(Date decertificationDate) {
        this.decertificationDate = decertificationDate;
    };
}
