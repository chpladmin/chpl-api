package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DecertifiedDeveloperDTO implements Serializable {
    private static final long serialVersionUID = 5686501038412355764L;
    private Long developerId;
    private List<Long> acbIdList;
    private String developerStatus;
    private Date decertificationDate;
    private Long numMeaningfulUse;
    private Date earliestNumMeaningfulUseDate;
    private Date latestNumMeaningfulUseDate;

    public DecertifiedDeveloperDTO() {
    }

    public DecertifiedDeveloperDTO(Long developerId, List<Long> acbIdList, String developerStatus,
            Date decertificationDate, Long numMeaningfulUse) {
        this.setDeveloperId(developerId);
        this.acbIdList = acbIdList;
        this.developerStatus = developerStatus;
        this.decertificationDate = decertificationDate;
        this.numMeaningfulUse = numMeaningfulUse;
    }

    public String getDeveloperStatus() {
        return developerStatus;
    }

    public void setDeveloperStatus(final String developerStatus) {
        this.developerStatus = developerStatus;
    }

    public Long getNumMeaningfulUse() {
        return numMeaningfulUse;
    }

    public void setNumMeaningfulUse(final Long numMeaningfulUse) {
        this.numMeaningfulUse = numMeaningfulUse;
    }

    public void incrementNumMeaningfulUse(final Long numMeaningfulUse) {
        if (this.numMeaningfulUse == null) {
            this.numMeaningfulUse = numMeaningfulUse;
        } else {
            this.numMeaningfulUse += numMeaningfulUse;
        }
    }

    public void addAcb(Long acbId) {
        if (this.acbIdList != null) {
            this.acbIdList.add(acbId);
        } else {
            this.acbIdList = new ArrayList<Long>();
            this.acbIdList.add(acbId);
        }
    }

    public Long getDeveloperId() {
        return developerId;
    }

    public void setDeveloperId(final Long developerId) {
        this.developerId = developerId;
    }

    public List<Long> getAcbIdList() {
        return this.acbIdList;
    }

    public void setAcbList(final List<Long> acbIdList) {
        this.acbIdList = acbIdList;
    }

    public Date getDecertificationDate() {
        return decertificationDate;
    }

    public void setDecertificationDate(final Date decertificationDate) {
        this.decertificationDate = decertificationDate;
    }

    public Date getEarliestNumMeaningfulUseDate() {
        return earliestNumMeaningfulUseDate;
    }

    public void setEarliestNumMeaningfulUseDate(Date earliestNumMeaningfulUseDate) {
        this.earliestNumMeaningfulUseDate = earliestNumMeaningfulUseDate;
    }

    public Date getLatestNumMeaningfulUseDate() {
        return latestNumMeaningfulUseDate;
    }

    public void setLatestNumMeaningfulUseDate(Date latestNumMeaningfulUseDate) {
        this.latestNumMeaningfulUseDate = latestNumMeaningfulUseDate;
    }

}
