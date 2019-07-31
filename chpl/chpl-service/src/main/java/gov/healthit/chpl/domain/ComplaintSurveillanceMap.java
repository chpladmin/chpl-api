package gov.healthit.chpl.domain;

import java.io.Serializable;

import org.springframework.beans.BeanUtils;

import gov.healthit.chpl.domain.surveillance.SurveillanceBasic;
import gov.healthit.chpl.dto.ComplaintSurveillanceMapDTO;

public class ComplaintSurveillanceMap implements Serializable {
    private static final long serialVersionUID = -751810206635865021L;

    private Long id;
    private Long complaintId;
    private Long surveillanceId;
    private SurveillanceBasic surveillance;

    public ComplaintSurveillanceMap() {

    }

    public ComplaintSurveillanceMap(ComplaintSurveillanceMapDTO dto) {
        BeanUtils.copyProperties(dto, this);
        this.surveillance = new SurveillanceBasic(dto.getSurveillance());
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

    public SurveillanceBasic getSurveillance() {
        return surveillance;
    }

    public void setSurveillance(SurveillanceBasic surveillance) {
        this.surveillance = surveillance;
    }

    @Override
    public String toString() {
        return "ComplaintSurveillanceMap [id=" + id + ", complaintId=" + complaintId + ", surveillanceId="
                + surveillanceId + ", surveillance=" + surveillance + "]";
    }
}
