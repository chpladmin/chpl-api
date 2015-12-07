package gov.healthit.chpl.dto;

import java.util.Date;

import gov.healthit.chpl.entity.SurveillanceEntity;

public class SurveillanceDTO {

	private Long id;
	private Long certifiedProductId;
	private Date startDate;
	private Date endDate;

	public SurveillanceDTO() {
	}
	
	public SurveillanceDTO(SurveillanceEntity entity) {
		setId(entity.getId());
		setCertifiedProductId(entity.getCertifiedProductId());
		setStartDate(entity.getStartDate());
		setEndDate(entity.getEndDate());
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCertifiedProductId() {
		return certifiedProductId;
	}

	public void setCertifiedProductId(Long certifiedProductId) {
		this.certifiedProductId = certifiedProductId;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
}
