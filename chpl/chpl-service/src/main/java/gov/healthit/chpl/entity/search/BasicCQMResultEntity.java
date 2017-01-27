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

import gov.healthit.chpl.entity.CQMCriterionEntity;

@Entity
@Table(name = "cqm_result")
public class BasicCQMResultEntity  implements Serializable {
	private static final long serialVersionUID = -9050374846030566947L;

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cqm_result_id", nullable = false)
	private Long id;
	
    @Column(name = "certified_product_id", nullable = false)
	private Long certifiedProductId;
    
	@Column(name = "cqm_criterion_id")
	private Long cqmCriterionId;
	
	@Basic(optional = true)
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cqm_criterion_id", unique=true, nullable = true, insertable=false, updatable= false)
	private CQMCriterionEntity cqmCriterion;
	
	@Column(name = "success")
	private Boolean success;

	@Column(name = "deleted")
	private Boolean deleted;
	
	public BasicCQMResultEntity() {
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

	public Long getCqmCriterionId() {
		return cqmCriterionId;
	}

	public void setCqmCriterionId(Long cqmCriterionId) {
		this.cqmCriterionId = cqmCriterionId;
	}

	public CQMCriterionEntity getCqmCriterion() {
		return cqmCriterion;
	}

	public void setCqmCriterion(CQMCriterionEntity cqmCriterion) {
		this.cqmCriterion = cqmCriterion;
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	} 
}
