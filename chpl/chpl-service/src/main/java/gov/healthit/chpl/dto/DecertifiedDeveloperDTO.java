package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import gov.healthit.chpl.util.Util;

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
        return Util.getNewDate(decertificationDate);
    }

    public void setDecertificationDate(final Date decertificationDate) {
        this.decertificationDate = Util.getNewDate(decertificationDate);
    }

    public Date getEarliestNumMeaningfulUseDate() {
        return Util.getNewDate(earliestNumMeaningfulUseDate);
    }

    public void setEarliestNumMeaningfulUseDate(Date earliestNumMeaningfulUseDate) {
        this.earliestNumMeaningfulUseDate = Util.getNewDate(earliestNumMeaningfulUseDate);
    }

    public Date getLatestNumMeaningfulUseDate() {
        return Util.getNewDate(latestNumMeaningfulUseDate);
    }

    public void setLatestNumMeaningfulUseDate(Date latestNumMeaningfulUseDate) {
        this.latestNumMeaningfulUseDate = Util.getNewDate(latestNumMeaningfulUseDate);
    }

}
