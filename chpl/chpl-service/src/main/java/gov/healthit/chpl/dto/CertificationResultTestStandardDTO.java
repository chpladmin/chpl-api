package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.listing.CertificationResultTestStandardEntity;

public class CertificationResultTestStandardDTO implements Serializable {
	private static final long serialVersionUID = -1877751077243323497L;
	private Long id;
	private Long certificationResultId;
	private Long testStandardId;
	private String testStandardDescription;
	private String testStandardName;
	private Boolean deleted;

	public CertificationResultTestStandardDTO() {}

	public CertificationResultTestStandardDTO(CertificationResultTestStandardEntity entity) {
		this.id = entity.getId();
		this.certificationResultId = entity.getCertificationResultId();
		this.testStandardId = entity.getTestStandardId();
		if(entity.getTestStandard() != null) {
			this.testStandardDescription = entity.getTestStandard().getDescription();
			this.testStandardName = entity.getTestStandard().getName();
		}
		this.deleted = entity.getDeleted();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCertificationResultId() {
		return certificationResultId;
	}

	public void setCertificationResultId(Long certificationResultId) {
		this.certificationResultId = certificationResultId;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public Long getTestStandardId() {
		return testStandardId;
	}

	public void setTestStandardId(Long testStandardId) {
		this.testStandardId = testStandardId;
	}

	public String getTestStandardDescription() {
		return testStandardDescription;
	}

	public void setTestStandardDescription(String testStandardDescription) {
		this.testStandardDescription = testStandardDescription;
	}

	public String getTestStandardName() {
		return testStandardName;
	}

	public void setTestStandardName(String testStandardName) {
		this.testStandardName = testStandardName;
	}
}
