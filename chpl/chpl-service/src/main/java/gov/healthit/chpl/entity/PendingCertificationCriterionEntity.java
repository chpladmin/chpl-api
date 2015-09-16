package gov.healthit.chpl.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="pending_certification_criterion")
public class PendingCertificationCriterionEntity {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pending_certification_criterion_idGenerator")
	@Column( name = "pending_certification_criterion_id", nullable = false  )
	@SequenceGenerator(name = "pending_certification_criterion_idGenerator", 
		sequenceName = "pending_certification_criteri_pending_certification_criteri_seq")
	private Long id;
	
	@Column(name="certification_criterion_id")
	private Long certificationCriterionId;
	
	@Column(name="pending_certified_product_id")
	private Long pendingCertifiedProductId;

	@Column(name = "meets_criteria")
	private Boolean meetsCriteria;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCertificationCriterionId() {
		return certificationCriterionId;
	}

	public void setCertificationCriterionId(Long certificationCriterionId) {
		this.certificationCriterionId = certificationCriterionId;
	}

	public Long getPendingCertifiedProductId() {
		return pendingCertifiedProductId;
	}

	public void setPendingCertifiedProductId(Long pendingCertifiedProductId) {
		this.pendingCertifiedProductId = pendingCertifiedProductId;
	}

	public Boolean getMeetsCriteria() {
		return meetsCriteria;
	}

	public void setMeetsCriteria(Boolean meetsCriteria) {
		this.meetsCriteria = meetsCriteria;
	}

}
