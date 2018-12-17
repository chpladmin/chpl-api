package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.util.Util;

public class DecertifiedDeveloperResult implements Serializable {
    private static final long serialVersionUID = 7463932788024776464L;
    private Developer developer;
    private List<CertificationBody> certifyingBody;
    private Date decertificationDate;
    private Long estimatedUsers;
    private Long earliestMeaningfulUseDate;
    private Long latestMeaningfulUseDate;

    public DecertifiedDeveloperResult() {
    }

    public DecertifiedDeveloperResult(Developer developer, List<CertificationBody> certifyingBody,
            Date decertificationDate, Long estimatedUsers, Date earliestMeaningfulUseDate,
            Date latestMeaningfulUseDate) {
        this.developer = developer;
        this.certifyingBody = certifyingBody;
        this.decertificationDate = decertificationDate;
        this.estimatedUsers = estimatedUsers;
        this.earliestMeaningfulUseDate = earliestMeaningfulUseDate.getTime();
        this.latestMeaningfulUseDate = latestMeaningfulUseDate.getTime();
    }

    public DecertifiedDeveloperResult(DeveloperDTO developerDTO, List<CertificationBody> certifyingBody,
            Date decertificationDate, Long estimatedUsers, Date earliestMeaningfulUseDate,
            Date latestMeaningfulUseDate) {
        this.developer = new Developer(developerDTO);
        this.certifyingBody = certifyingBody;
        this.decertificationDate = decertificationDate;
        this.estimatedUsers = estimatedUsers;
        this.earliestMeaningfulUseDate = earliestMeaningfulUseDate.getTime();
        this.latestMeaningfulUseDate = latestMeaningfulUseDate.getTime();
    }

    public Developer getDeveloper() {
        return developer;
    }

    public void setDeveloper(final Developer developer) {
        this.developer = developer;
    }

    public List<CertificationBody> getCertifyingBody() {
        return certifyingBody;
    }

    public void setCertifyingBody(final List<CertificationBody> certifyingBody) {
        this.certifyingBody = certifyingBody;
    }

    public Long getEstimatedUsers() {
        return estimatedUsers;
    }

    public void setEstimatedUsers(final Long estimatedUsers) {
        this.estimatedUsers = estimatedUsers;
    }

    public Date getDecertificationDate() {
        return Util.getNewDate(decertificationDate);
    }

    public void setDecertificationDate(final Date decertificationDate) {
        this.decertificationDate = Util.getNewDate(decertificationDate);
    }

    public Long getEarliestMeaningfulUseDate() {
        return earliestMeaningfulUseDate;
    }

    public void setEarliestMeaningfulUseDate(Long earliestMeaningfulUseDate) {
        this.earliestMeaningfulUseDate = earliestMeaningfulUseDate;
    }

    public Long getLatestMeaningfulUseDate() {
        return latestMeaningfulUseDate;
    }

    public void setLatestMeaningfulUseDate(Long latestMeaningfulUseDate) {
        this.latestMeaningfulUseDate = latestMeaningfulUseDate;
    };
}
