package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.statistics.IncumbentDevelopersStatisticsEntity;
import gov.healthit.chpl.util.Util;

/**
 * Incumbent Developers data transfer object.
 * @author alarned
 *
 */
public class IncumbentDevelopersStatisticsDTO implements Serializable {

    private static final long serialVersionUID = -1536844909545189801L;

    private Long id;
    private Long newCount;
    private Long incumbentCount;
    private Long oldCertificationEditionId;
    private Long newCertificationEditionId;
    private CertificationEditionDTO oldCertificationEdition;
    private CertificationEditionDTO newCertificationEdition;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    /**
     * Default constructor.
     */
    public IncumbentDevelopersStatisticsDTO() {
    }

    /**
     * Constructor that will populate the created object based on the entity
     * that is passed in as a parameter.
     * @param entity IncumbentDevelopersStatisticsEntity entity
     */
    public IncumbentDevelopersStatisticsDTO(final IncumbentDevelopersStatisticsEntity entity) {
        this.id = entity.getId();
        this.newCount = entity.getNewCount();
        this.incumbentCount = entity.getIncumbentCount();
        this.oldCertificationEditionId = entity.getOldCertificationEditionId();
        this.newCertificationEditionId = entity.getNewCertificationEditionId();
        if (entity.getOldCertificationEdition() != null) {
            this.oldCertificationEdition = new CertificationEditionDTO(entity.getOldCertificationEdition());
        }
        if (entity.getNewCertificationEdition() != null) {
            this.newCertificationEdition = new CertificationEditionDTO(entity.getNewCertificationEdition());
        }
        this.creationDate = entity.getCreationDate();
        this.deleted = entity.getDeleted();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
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

    public Long getNewCount() {
        return newCount;
    }

    public void setNewCount(final Long newCount) {
        this.newCount = newCount;
    }

    public Long getIncumbentCount() {
        return incumbentCount;
    }

    public void setIncumbentCount(final Long incumbentCount) {
        this.incumbentCount = incumbentCount;
    }

    public Long getOldCertificationEditionId() {
        return oldCertificationEditionId;
    }

    public void setOldCertificationEditionId(final Long oldCertificationEditionId) {
        this.oldCertificationEditionId = oldCertificationEditionId;
    }

    public Long getNewCertificationEditionId() {
        return newCertificationEditionId;
    }

    public void setNewCertificationEditionId(final Long newCertificationEditionId) {
        this.newCertificationEditionId = newCertificationEditionId;
    }

    public CertificationEditionDTO getOldCertificationEdition() {
        return oldCertificationEdition;
    }

    public void setOldCertificationEdition(final CertificationEditionDTO oldCertificationEdition) {
        this.oldCertificationEdition = oldCertificationEdition;
    }

    public CertificationEditionDTO getNewCertificationEdition() {
        return newCertificationEdition;
    }

    public void setNewCertificationEdition(final CertificationEditionDTO newCertificationEdition) {
        this.newCertificationEdition = newCertificationEdition;
    }

    @Override
    public String toString() {
        return "Incumbent Developers Statistics DTO ["
                + "[New: " + this.newCount + "]"
                + "[Incumbent: " + this.incumbentCount + "]"
                + "[Old Edition: " + this.oldCertificationEditionId.toString() + "]"
                + "[New Edition: " + this.newCertificationEditionId.toString() + "]"
                + "]";
    }
}
