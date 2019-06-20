package gov.healthit.chpl.dto;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.BeanUtils;

import gov.healthit.chpl.domain.complaint.Complaint;
import gov.healthit.chpl.domain.complaint.ComplaintCriterionMap;
import gov.healthit.chpl.domain.complaint.ComplaintListingMap;
import gov.healthit.chpl.entity.ComplaintCriterionMapEntity;
import gov.healthit.chpl.entity.ComplaintEntity;
import gov.healthit.chpl.entity.ComplaintListingMapEntity;

public class ComplaintDTO {
    private Long id;
    private CertificationBodyDTO certificationBody;
    private ComplaintTypeDTO complaintType;
    private ComplaintStatusTypeDTO complaintStatusType;
    private String oncComplaintId;
    private String acbComplaintId;
    private Date receivedDate;
    private String summary;
    private String actions;
    private boolean complainantContacted;
    private boolean developerContacted;
    private boolean oncAtlContacted;
    private boolean flagForOncReview;
    private Date closedDate;
    private Date creationDate;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    private Boolean deleted;
    private Set<ComplaintListingMapDTO> listings = new HashSet<ComplaintListingMapDTO>();
    private Set<ComplaintCriterionMapDTO> criteria = new HashSet<ComplaintCriterionMapDTO>();

    public ComplaintDTO() {

    }

    public ComplaintDTO(ComplaintEntity entity) {
        BeanUtils.copyProperties(entity, this);

        listings = new HashSet<ComplaintListingMapDTO>();
        for (ComplaintListingMapEntity clEntity : entity.getListings()) {
            listings.add(new ComplaintListingMapDTO(clEntity));
        }

        criteria = new HashSet<ComplaintCriterionMapDTO>();
        for (ComplaintCriterionMapEntity criteriaEntity : entity.getCriteria()) {
            criteria.add(new ComplaintCriterionMapDTO(criteriaEntity));
        }

        if (entity.getCertificationBody() != null) {
            this.certificationBody = new CertificationBodyDTO(entity.getCertificationBody());
        }
        if (entity.getComplaintType() != null) {
            this.complaintType = new ComplaintTypeDTO(entity.getComplaintType());
        }
        if (entity.getComplaintStatusType() != null) {
            this.complaintStatusType = new ComplaintStatusTypeDTO(entity.getComplaintStatusType());
        }
    }

    public ComplaintDTO(Complaint domain) {
        BeanUtils.copyProperties(domain, this);

        listings = new HashSet<ComplaintListingMapDTO>();
        for (ComplaintListingMap cl : domain.getListings()) {
            listings.add(new ComplaintListingMapDTO(cl));
        }

        criteria = new HashSet<ComplaintCriterionMapDTO>();
        for (ComplaintCriterionMap cc : domain.getCriteria()) {
            criteria.add(new ComplaintCriterionMapDTO(cc));
        }

        if (domain.getCertificationBody() != null) {
            this.certificationBody = new CertificationBodyDTO(domain.getCertificationBody());
        }
        if (domain.getComplaintType() != null) {
            this.complaintType = new ComplaintTypeDTO(domain.getComplaintType());
        }
        if (domain.getComplaintStatusType() != null) {
            this.complaintStatusType = new ComplaintStatusTypeDTO(domain.getComplaintStatusType());
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public CertificationBodyDTO getCertificationBody() {
        return certificationBody;
    }

    public void setCertificationBody(CertificationBodyDTO certificationBody) {
        this.certificationBody = certificationBody;
    }

    public ComplaintTypeDTO getComplaintType() {
        return complaintType;
    }

    public void setComplaintType(final ComplaintTypeDTO complaintType) {
        this.complaintType = complaintType;
    }

    public ComplaintStatusTypeDTO getComplaintStatusType() {
        return complaintStatusType;
    }

    public void setComplaintStatusType(final ComplaintStatusTypeDTO complaintStatusType) {
        this.complaintStatusType = complaintStatusType;
    }

    public String getOncComplaintId() {
        return oncComplaintId;
    }

    public void setOncComplaintId(final String oncComplaintId) {
        this.oncComplaintId = oncComplaintId;
    }

    public String getAcbComplaintId() {
        return acbComplaintId;
    }

    public void setAcbComplaintId(final String acbComplaintId) {
        this.acbComplaintId = acbComplaintId;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(final Date receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(final String summary) {
        this.summary = summary;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(final String actions) {
        this.actions = actions;
    }

    public boolean isComplainantContacted() {
        return complainantContacted;
    }

    public void setComplainantContacted(final boolean complainantContacted) {
        this.complainantContacted = complainantContacted;
    }

    public boolean isDeveloperContacted() {
        return developerContacted;
    }

    public void setDeveloperContacted(final boolean developerContacted) {
        this.developerContacted = developerContacted;
    }

    public boolean isOncAtlContacted() {
        return oncAtlContacted;
    }

    public void setOncAtlContacted(final boolean oncAtlContacted) {
        this.oncAtlContacted = oncAtlContacted;
    }

    public boolean isFlagForOncReview() {
        return flagForOncReview;
    }

    public void setFlagForOncReview(final boolean flagForOncReview) {
        this.flagForOncReview = flagForOncReview;
    }

    public Date getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(final Date closedDate) {
        this.closedDate = closedDate;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Set<ComplaintListingMapDTO> getListings() {
        return listings;
    }

    public void setListings(Set<ComplaintListingMapDTO> listings) {
        this.listings = listings;
    }

    public Set<ComplaintCriterionMapDTO> getCriteria() {
        return criteria;
    }

    public void setCriteria(Set<ComplaintCriterionMapDTO> criteria) {
        this.criteria = criteria;
    }

}
