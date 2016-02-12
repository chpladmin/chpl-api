package gov.healthit.chpl.dto;


import gov.healthit.chpl.entity.CertifiedProductEntity;
import gov.healthit.chpl.entity.CertifiedProductQmsStandardEntity;

import java.util.Date;

import org.springframework.util.StringUtils;

public class CertifiedProductQmsStandardDTO {
	private Long id;
	private Long certifiedProductId;
	private Long qmsStandardId;
	private String qmsStandardName;
	private String qmsModification;
	private String applicableCriteria;
	
	public CertifiedProductQmsStandardDTO(){}
	
	public CertifiedProductQmsStandardDTO(CertifiedProductQmsStandardEntity entity){
		this.id = entity.getId();
		this.certifiedProductId = entity.getCertifiedProductId();
		this.qmsStandardId = entity.getQmsStandardId();
		if(entity.getQmsStandard() != null) {
			this.qmsStandardName = entity.getQmsStandard().getName();
		}
		this.qmsModification = entity.getModification();
		this.applicableCriteria = entity.getApplicableCriteria();
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

	public Long getQmsStandardId() {
		return qmsStandardId;
	}

	public void setQmsStandardId(Long qmsStandardId) {
		this.qmsStandardId = qmsStandardId;
	}

	public String getQmsStandardName() {
		return qmsStandardName;
	}

	public void setQmsStandardName(String qmsStandardName) {
		this.qmsStandardName = qmsStandardName;
	}

	public String getQmsModification() {
		return qmsModification;
	}

	public void setQmsModification(String qmsModification) {
		this.qmsModification = qmsModification;
	}

	public String getApplicableCriteria() {
		return applicableCriteria;
	}

	public void setApplicableCriteria(String applicableCriteria) {
		this.applicableCriteria = applicableCriteria;
	}
}
