package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.developer.DeveloperStatusEventEntity;

public class DeveloperStatusEventDTO implements Serializable {
	private static final long serialVersionUID = -2492374479266782228L;

	private Long id;
	private Long developerId;
	private DeveloperStatusDTO status;
	private Date statusDate;

	public DeveloperStatusEventDTO(){
	}

	public DeveloperStatusEventDTO(DeveloperStatusEventEntity entity){
		this();
		this.id = entity.getId();
		this.developerId = entity.getDeveloperId();
		this.status = new DeveloperStatusDTO(entity.getDeveloperStatus());
		this.statusDate = entity.getStatusDate();
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public Long getDeveloperId() {
		return developerId;
	}

	public void setDeveloperId(Long developerId) {
		this.developerId = developerId;
	}

	public Date getStatusDate() {
		return statusDate;
	}

	public void setStatusDate(Date statusDate) {
		this.statusDate = statusDate;
	}

	public DeveloperStatusDTO getStatus() {
		return status;
	}

	public void setStatus(DeveloperStatusDTO status) {
		this.status = status;
	}

}
