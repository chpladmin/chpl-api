package gov.healthit.chpl.entity;


import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "cqm_result_details", schema = "openchpl")
public class CQMResultDetailsEntity {
	
	@Id 
	@Column( name = "cqm_result_id", nullable = false )
	private Long id;
	
	@Column(name = "certified_product_id")
    private Long certifiedProductId;
	
	@Basic( optional = false )
	@Column( name = "success", nullable = false )
	private Boolean success;
	
	@Basic( optional = false )
	@Column( name = "cqm_criterion_id", nullable = false )
	private Long cqmCriterionId;
	
	@Column( name = "number")
	private String number;
	
	@Column( name = "cms_id")
	private String cmsId;
	
	@Column( name = "title")
	private String title;
		
	@Column( name = "nqf_number")
	private String nqfNumber;
	
	@Column( name = "cqm_criterion_type_id")
	private Long cqmCriterionTypeId;
	
	@Basic( optional = true )
	@Column(name = "cqm_version_id", nullable = true )
	private Long cqmVersionId;
	
	@Basic(optional = true)
	@Column(name = "cqm_domain", nullable = true)
	private String domain;
	
	@Basic( optional = true )
	@Column(name = "version")
	private String version;
	
	@Column(name = "cqm_id")
	private String cqmId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public Long getCqmCriterionId() {
		return cqmCriterionId;
	}

	public void setCqmCriterionId(Long cqmCriterionId) {
		this.cqmCriterionId = cqmCriterionId;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getCmsId() {
		return cmsId;
	}

	public void setCmsId(String cmsId) {
		this.cmsId = cmsId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getNqfNumber() {
		return nqfNumber;
	}

	public void setNqfNumber(String nqfNumber) {
		this.nqfNumber = nqfNumber;
	}

	public Long getCqmCriterionTypeId() {
		return cqmCriterionTypeId;
	}

	public void setCqmCriterionTypeId(Long cqmCriterionTypeId) {
		this.cqmCriterionTypeId = cqmCriterionTypeId;
	}

	public Long getCqmVersionId() {
		return cqmVersionId;
	}

	public void setCqmVersionId(Long cqmVersionId) {
		this.cqmVersionId = cqmVersionId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	public String getCqmId() {
		return cqmId;
	}

	public void setCqmId(String cqmId) {
		this.cqmId = cqmId;
	}
	
}
