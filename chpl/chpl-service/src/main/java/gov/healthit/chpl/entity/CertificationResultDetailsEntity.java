package gov.healthit.chpl.entity;


import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "certification_result_details")
public class CertificationResultDetailsEntity {

	/** Serial Version UID. */
	private static final long serialVersionUID = -2928065796550377879L;
	
    @Id 
	@Basic( optional = false )
	@Column( name = "certification_result_id", nullable = false  )
	private Long id;
    
    @Column(name = "certification_criterion_id")
    private Long certificationCriterionId;
    
    @Column(name = "certified_product_id")
    private Long certifiedProductId;
    
    @Column(name = "success")
    private Boolean success;
    
    @Column(name = "number")
    private String number;
    
    @Column(name = "title")
    private String title;
    
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

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
