package gov.healthit.chpl.domain;

import java.io.Serializable;

import org.springframework.beans.BeanUtils;

import gov.healthit.chpl.dto.ComplaintSurveillanceMapDTO;

public class ComplaintSurveillanceMap implements Serializable {
    private static final long serialVersionUID = -751810206635865021L;

    private Long id;
    private Long complaintId;
    private Long surveillanceId;
    private SurveillanceLite surveillance;

    public ComplaintSurveillanceMap() {

    }

    public ComplaintSurveillanceMap(ComplaintSurveillanceMapDTO dto) {
        BeanUtils.copyProperties(dto, this);
        this.surveillance = new SurveillanceLite(dto.getSurveillance());
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(final Long complaintId) {
        this.complaintId = complaintId;
    }

    public Long getSurveillanceId() {
        return surveillanceId;
    }

    public void setSurveillanceId(final Long surveillanceId) {
        this.surveillanceId = surveillanceId;
    }

    public SurveillanceLite getSurveillance() {
        return surveillance;
    }

    public void setSurveillance(SurveillanceLite surveillance) {
        this.surveillance = surveillance;
    }

    @Override
    public String toString() {
        return "ComplaintSurveillanceMap [id=" + id + ", complaintId=" + complaintId + ", surveillanceId="
                + surveillanceId + ", surveillance=" + surveillance + "]";
    }
}
