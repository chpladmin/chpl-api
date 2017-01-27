package gov.healthit.chpl.entity.search;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import gov.healthit.chpl.entity.CertificationCriterionEntity;

@Entity
@Table(name = "certification_result")
public class BasicCertificationResultEntity  implements Serializable {
	private static final long serialVersionUID = -9050374846030066947L;

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "certification_result_id", nullable = false)
	private Long id;
	
    @Column(name = "certified_product_id", nullable = false)
	private Long certifiedProductId;
    
	@Column(name = "certification_criterion_id")
	private Long certificationCriterionId;
	
	@Basic(optional = true)
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "certification_criterion_id", unique=true, nullable = true, insertable=false, updatable= false)
	private CertificationCriterionEntity certificationCriterion;
	
	@Column(name = "success")
	private Boolean success;
	
	@Column(name = "deleted")
	private Boolean deleted;

	public BasicCertificationResultEntity() {
	}

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

	public CertificationCriterionEntity getCertificationCriterion() {
		return certificationCriterion;
	}

	public void setCertificationCriterion(CertificationCriterionEntity certificationCriterion) {
		this.certificationCriterion = certificationCriterion;
	}

	public Long getCertifiedProductId() {
		return certifiedProductId;
	}

	public void setCertifiedProductId(Long certifiedProductId) {
		this.certifiedProductId = certifiedProductId;
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	} 
}
