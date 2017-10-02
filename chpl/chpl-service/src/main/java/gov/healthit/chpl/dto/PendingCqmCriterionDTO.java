package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.entity.PendingCqmCertificationCriteriaEntity;
import gov.healthit.chpl.entity.PendingCqmCriterionEntity;

public class PendingCqmCriterionDTO implements Serializable {
	private static final long serialVersionUID = -5341402809549146966L;
	private Long id;
	private Long cqmCriterionId;
	private Long pendingCertifiedProductId;
	private boolean meetsCriteria;

	private String cqmNumber;
	private String title;
	private String cmsId;
	private String nqfNumber;
	private String version; //only valid for CMS
	private Long typeId;
	private String domain;

	private List<PendingCqmCertificationCriterionDTO> certifications;

	public PendingCqmCriterionDTO() {
		certifications = new ArrayList<PendingCqmCertificationCriterionDTO>();
	}

	public PendingCqmCriterionDTO(PendingCqmCriterionEntity entity) {
		this();
		this.setId(entity.getId());
		this.setCqmCriterionId(entity.getMappedCriterion().getId());
		this.setPendingCertifiedProductId(entity.getPendingCertifiedProductId());
		this.setMeetsCriteria(entity.getMeetsCriteria().booleanValue());

		this.setCqmNumber(entity.getMappedCriterion().getNumber());
		this.setTitle(entity.getMappedCriterion().getTitle());
		this.setCmsId(entity.getMappedCriterion().getCmsId());
		this.setNqfNumber(entity.getMappedCriterion().getNqfNumber());
		this.setVersion(entity.getMappedCriterion().getCqmVersion());
		this.setTypeId(entity.getMappedCriterion().getCqmCriterionTypeId());
		this.setDomain(entity.getMappedCriterion().getCqmDomain());

		if(entity.getCertifications() != null && entity.getCertifications().size() > 0) {
			for(PendingCqmCertificationCriteriaEntity certEntity : entity.getCertifications()) {
				PendingCqmCertificationCriterionDTO cert = new PendingCqmCertificationCriterionDTO(certEntity);
				this.certifications.add(cert);
			}
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCqmCriterionId() {
		return cqmCriterionId;
	}

	public void setCqmCriterionId(Long cqmCriterionId) {
		this.cqmCriterionId = cqmCriterionId;
	}

	public Long getPendingCertifiedProductId() {
		return pendingCertifiedProductId;
	}

	public void setPendingCertifiedProductId(Long pendingCertifiedProductId) {
		this.pendingCertifiedProductId = pendingCertifiedProductId;
	}

	public boolean isMeetsCriteria() {
		return meetsCriteria;
	}

	public void setMeetsCriteria(boolean meetsCriteria) {
		this.meetsCriteria = meetsCriteria;
	}

	public String getCqmNumber() {
		return cqmNumber;
	}

	public void setCqmNumber(String cqmNumber) {
		this.cqmNumber = cqmNumber;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCmsId() {
		return cmsId;
	}

	public void setCmsId(String cmsId) {
		this.cmsId = cmsId;
	}

	public String getNqfNumber() {
		return nqfNumber;
	}

	public void setNqfNumber(String nqfNumber) {
		this.nqfNumber = nqfNumber;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Long getTypeId() {
		return typeId;
	}

	public void setTypeId(Long typeId) {
		this.typeId = typeId;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public List<PendingCqmCertificationCriterionDTO> getCertifications() {
		return certifications;
	}

	public void setCertifications(List<PendingCqmCertificationCriterionDTO> certifications) {
		this.certifications = certifications;
	}
}
