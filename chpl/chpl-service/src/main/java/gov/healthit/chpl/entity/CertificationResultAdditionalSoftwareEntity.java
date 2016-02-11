package gov.healthit.chpl.entity;

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


@Entity
@Table(name = "certification_result_additional_software")
public class CertificationResultAdditionalSoftwareEntity extends AuditedEntity {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic( optional = false )
	@Column(name = "certification_result_additional_software_id")
	private Long id;
	
	@Basic( optional = false )
	@Column( name = "certification_result_id", nullable = false  )
	private Long certificationResultId;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "version")
	private String version;
	
	@Column(name = "certified_product_id")
	private Long certifiedProductId;
	
	@Column(name = "justification")
	private String justification;

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

	
}
