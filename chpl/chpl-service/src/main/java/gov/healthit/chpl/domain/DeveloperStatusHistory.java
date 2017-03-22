package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.dto.DeveloperStatusHistoryDTO;

public class DeveloperStatusHistory implements Serializable {
	private static final long serialVersionUID = -7303257499336378800L;
	private Long id;
	private Long developerId;
	private DeveloperStatus status;
	private Date statusDate;
	
	public DeveloperStatusHistory() {
	}
	
	public DeveloperStatusHistory(DeveloperStatusHistoryDTO dto) {
		this.id = dto.getId();
		this.developerId = dto.getDeveloperId();
		this.status = new DeveloperStatus(dto.getStatus());
		this.statusDate = dto.getStatusDate();
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

	public DeveloperStatus getStatus() {
		return status;
	}

	public void setStatus(DeveloperStatus status) {
		this.status = status;
	}

	public Date getStatusDate() {
		return statusDate;
	}

	public void setStatusDate(Date statusDate) {
		this.statusDate = statusDate;
	}
}
