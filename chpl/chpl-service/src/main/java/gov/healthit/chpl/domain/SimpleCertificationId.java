package gov.healthit.chpl.domain;

import java.util.Date;

import gov.healthit.chpl.dto.CertificationIdDTO;

public class SimpleCertificationId {
	private String certificationId;
	private Date created;
	
	public SimpleCertificationId() {}
	
	public SimpleCertificationId(CertificationIdDTO dto) {
		this.certificationId = dto.getCertificationId();
		this.created = dto.getCreationDate();
	}
	
	public String getCertificationId() {
		return certificationId;
	}
	public void setCertificationId(String certificationId) {
		this.certificationId = certificationId;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	
	
}
