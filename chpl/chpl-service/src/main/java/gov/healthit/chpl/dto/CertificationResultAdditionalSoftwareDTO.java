package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.entity.listing.CertificationResultAdditionalSoftwareEntity;

public class CertificationResultAdditionalSoftwareDTO implements Serializable {
	private static final long serialVersionUID = 4034572440468231407L;
	private Long id;
	private Long certificationResultId;
	private String name;
	private String version;
	private Long certifiedProductId;
	private String certifiedProductNumber;
	private String justification;
	private String grouping;
	
	private Date creationDate;
	private Boolean deleted;
	
	public CertificationResultAdditionalSoftwareDTO(){}
	
	public CertificationResultAdditionalSoftwareDTO(CertificationResultAdditionalSoftwareEntity entity){		
		this.id = entity.getId();
		this.certificationResultId = entity.getCertificationResultId();
		this.name = entity.getName();
		this.version = entity.getVersion();
		this.certifiedProductId = entity.getCertifiedProductId();
		this.justification = entity.getJustification();
		this.grouping = entity.getGrouping();
		if(this.certifiedProductId != null && entity.getCertifiedProduct() != null) {
			CertifiedProductDetailsDTO detailsDto = new CertifiedProductDetailsDTO(entity.getCertifiedProduct());
			CertifiedProduct cp = new CertifiedProduct(detailsDto);
			this.certifiedProductNumber = cp.getChplProductNumber();
		}
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

	public String getJustification() {
		return justification;
	}

	public void setJustification(String justification) {
		this.justification = justification;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Long getCertifiedProductId() {
		return certifiedProductId;
	}

	public void setCertifiedProductId(Long certifiedProductId) {
		this.certifiedProductId = certifiedProductId;
	}

	public String getGrouping() {
		return grouping;
	}

	public void setGrouping(String grouping) {
		this.grouping = grouping;
	}

	public String getCertifiedProductNumber() {
		return certifiedProductNumber;
	}

	public void setCertifiedProductNumber(String certifiedProductNumber) {
		this.certifiedProductNumber = certifiedProductNumber;
	}
}
