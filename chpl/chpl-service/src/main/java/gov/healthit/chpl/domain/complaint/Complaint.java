package gov.healthit.chpl.domain.complaint;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.BeanUtils;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.ComplaintSurveillanceMap;
import gov.healthit.chpl.dto.ComplaintCriterionMapDTO;
import gov.healthit.chpl.dto.ComplaintDTO;
import gov.healthit.chpl.dto.ComplaintListingMapDTO;
import gov.healthit.chpl.dto.ComplaintSurveillanceMapDTO;

public class Complaint implements Serializable {
    private static final long serialVersionUID = -7018474294841580851L;

    private Long id;
    private CertificationBody certificationBody;
    private ComplainantType complainantType;
    private String complainantTypeOther;
    private ComplaintStatusType complaintStatusType;
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
    private Set<ComplaintListingMap> listings = new HashSet<ComplaintListingMap>();
    private Set<ComplaintCriterionMap> criteria = new HashSet<ComplaintCriterionMap>();
    private Set<ComplaintSurveillanceMap> surveillances = new HashSet<ComplaintSurveillanceMap>();

    public Complaint() {

    }

    public Complaint(ComplaintDTO dto) {
        BeanUtils.copyProperties(dto, this);

        listings = new HashSet<ComplaintListingMap>();
        for (ComplaintListingMapDTO clDTO : dto.getListings()) {
            listings.add(new ComplaintListingMap(clDTO));
        }

        criteria = new HashSet<ComplaintCriterionMap>();
        for (ComplaintCriterionMapDTO criterionDTO : dto.getCriteria()) {
            criteria.add(new ComplaintCriterionMap(criterionDTO));
        }

        surveillances = new HashSet<ComplaintSurveillanceMap>();
        for (ComplaintSurveillanceMapDTO surveillanceDTO : dto.getSurveillances()) {
            surveillances.add(new ComplaintSurveillanceMap(surveillanceDTO));
        }

        this.certificationBody = new CertificationBody(dto.getCertificationBody());
        this.complaintStatusType = new ComplaintStatusType(dto.getComplaintStatusType());
        this.complainantType = new ComplainantType(dto.getComplainantType());
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public CertificationBody getCertificationBody() {
        return certificationBody;
    }

    public void setCertificationBody(final CertificationBody certificationBody) {
        this.certificationBody = certificationBody;
    }

    public ComplainantType getComplainantType() {
        return complainantType;
    }

    public void setComplainantType(final ComplainantType complainantType) {
        this.complainantType = complainantType;
    }

    public String getComplainantTypeOther() {
        return complainantTypeOther;
    }

    public void setComplainantTypeOther(String complainantTypeOther) {
        this.complainantTypeOther = complainantTypeOther;
    }

    public ComplaintStatusType getComplaintStatusType() {
        return complaintStatusType;
    }

    public void setComplaintStatusType(final ComplaintStatusType complaintStatusType) {
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

    public Set<ComplaintListingMap> getListings() {
        return listings;
    }

    public void setListings(final Set<ComplaintListingMap> listings) {
        this.listings = listings;
    }

    public Set<ComplaintCriterionMap> getCriteria() {
        return criteria;
    }

    public void setCriteria(Set<ComplaintCriterionMap> criteria) {
        this.criteria = criteria;
    }

    public Set<ComplaintSurveillanceMap> getSurveillances() {
        return surveillances;
    }

    public void setSurveillances(Set<ComplaintSurveillanceMap> surveillances) {
        this.surveillances = surveillances;
    }

    @Override
    public String toString() {
        return "Complaint [id=" + id + ", certificationBody=" + certificationBody + ", complainantType="
                + complainantType + ", complainantTypeOther=" + complainantTypeOther + ", complaintStatusType="
                + complaintStatusType + ", oncComplaintId=" + oncComplaintId + ", acbComplaintId=" + acbComplaintId
                + ", receivedDate=" + receivedDate + ", summary=" + summary + ", actions=" + actions
                + ", complainantContacted=" + complainantContacted + ", developerContacted=" + developerContacted
                + ", oncAtlContacted=" + oncAtlContacted + ", flagForOncReview=" + flagForOncReview + ", closedDate="
                + closedDate + ", listings=" + listings + ", criteria=" + criteria + ", surveillances=" + surveillances
                + "]";
    }

}
