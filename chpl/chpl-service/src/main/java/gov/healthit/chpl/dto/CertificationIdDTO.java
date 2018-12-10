package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.CertificationIdEntity;
import gov.healthit.chpl.util.Util;

public class CertificationIdDTO implements Serializable {
    private static final long serialVersionUID = 1338639051071192137L;
    private Long id;
    private String certificationId;
    private String year;
    private Long practiceTypeId;
    private String key;

    private Date creationDate;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    public CertificationIdDTO() {
    }

    public CertificationIdDTO(CertificationIdEntity entity) {

        this.id = entity.getId();
        this.creationDate = entity.getCreationDate();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
        this.certificationId = entity.getCertificationId();
        this.year = entity.getYear();
        this.practiceTypeId = entity.getPracticeTypeId();
        this.key = entity.getKey();
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public void setCertificationId(final String certId) {
        this.certificationId = certId;
    }

    public String getCertificationId() {
        return this.certificationId;
    }

    public void setYear(final String year) {
        this.year = year;
    }

    public String getYear() {
        return this.year;
    }

    public void setPracticeTypeId(final Long practiceTypeId) {
        this.practiceTypeId = practiceTypeId;
    }

    public Long getPracticeTypeId() {
        return this.practiceTypeId;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

    /**
     * Provides toString implementation.
     *
     * @see java.lang.Object#toString()
     * @return String representation of this class.
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("creationDate: " + this.getCreationDate() + ", ");
        sb.append("id: " + this.getId() + ", ");
        sb.append("key: " + this.getKey() + ", ");
        sb.append("lastModifiedDate: " + this.getLastModifiedDate() + ", ");
        sb.append("lastModifiedUser: " + this.getLastModifiedUser() + ", ");
        sb.append("certificationId: " + this.getCertificationId() + ", ");
        sb.append("year: " + this.getYear() + ", ");
        sb.append("practiceTypeId: " + this.getPracticeTypeId() + ", ");
        return sb.toString();
    }
}
