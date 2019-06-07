package gov.healthit.chpl.domain.complaint;

import java.util.Date;

import org.springframework.beans.BeanUtils;

import gov.healthit.chpl.dao.ComplaintDTO;
import gov.healthit.chpl.domain.CertificationBody;

public class Complaint {
    private Long id;
    private CertificationBody certificationBody;
    private ComplaintType complaintType;
    private ComplaintStatusType complaintStatusType;
    private String oncComplaintId;
    private String acbComplaintId;
    private Date receivedDate;
    private String summary;
    private String actions;
    private boolean complainantContacted;
    private boolean developerContacted;
    private boolean oncAtlContacted;
    private Date closedDate;

    public Complaint() {

    }

    public Complaint(ComplaintDTO dto) {
        BeanUtils.copyProperties(dto, this);
        this.certificationBody = new CertificationBody(dto.getCertificationBody());
        this.complaintStatusType = new ComplaintStatusType(dto.getComplaintStatusType());
        this.complaintType = new ComplaintType(dto.getComplaintType());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CertificationBody getCertificationBody() {
        return certificationBody;
    }

    public void setCertificationBody(CertificationBody certificationBody) {
        this.certificationBody = certificationBody;
    }

    public ComplaintType getComplaintType() {
        return complaintType;
    }

    public void setComplaintType(ComplaintType complaintType) {
        this.complaintType = complaintType;
    }

    public ComplaintStatusType getComplaintStatusType() {
        return complaintStatusType;
    }

    public void setComplaintStatusType(ComplaintStatusType complaintStatusType) {
        this.complaintStatusType = complaintStatusType;
    }

    public String getOncComplaintId() {
        return oncComplaintId;
    }

    public void setOncComplaintId(String oncComplaintId) {
        this.oncComplaintId = oncComplaintId;
    }

    public String getAcbComplaintId() {
        return acbComplaintId;
    }

    public void setAcbComplaintId(String acbComplaintId) {
        this.acbComplaintId = acbComplaintId;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
    }

    public boolean isComplainantContacted() {
        return complainantContacted;
    }

    public void setComplainantContacted(boolean complainantContacted) {
        this.complainantContacted = complainantContacted;
    }

    public boolean isDeveloperContacted() {
        return developerContacted;
    }

    public void setDeveloperContacted(boolean developerContacted) {
        this.developerContacted = developerContacted;
    }

    public boolean isOncAtlContacted() {
        return oncAtlContacted;
    }

    public void setOncAtlContacted(boolean oncAtlContacted) {
        this.oncAtlContacted = oncAtlContacted;
    }

    public Date getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(Date closedDate) {
        this.closedDate = closedDate;
    }

}
