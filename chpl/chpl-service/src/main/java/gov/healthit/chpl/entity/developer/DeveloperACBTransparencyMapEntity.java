package gov.healthit.chpl.entity.developer;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import gov.healthit.chpl.entity.AttestationType;

@Entity
@Table(name = "acb_developer_transparency_mappings", schema="openchpl")
public class DeveloperACBTransparencyMapEntity {

	@Id
	@Basic( optional = false )
    @Column(name = "id")
    private Long id;

	@Column(name = "vendor_id")
	private Long developerId;

	@Column(name = "certification_body_id")
	private Long certificationBodyId;

    @Column(name = "acb_name")
    private String acbName;

    @Column(name = "developer_name")
    private String developerName;

	@Column(name = "transparency_attestation")
	@Type(type = "gov.healthit.chpl.entity.PostgresAttestationType" , parameters = {@org.hibernate.annotations.Parameter(name = "enumClassName",value = "gov.healthit.chpl.entity.AttestationType")} )
	private AttestationType transparencyAttestation;

	public DeveloperACBTransparencyMapEntity() {
		// Default constructor
	}

    public Long getId() {
        return id;
    }

	public void setId(Long id) {
		this.id = id;
	}

	public Long getDeveloperId() {
		return developerId;
	}

	public void setDeveloperId(Long developerId) {
		this.developerId = developerId;
	}

	public Long getCertificationBodyId() {
		return certificationBodyId;
	}

	public void setCertificationBodyId(Long certificationBodyId) {
		this.certificationBodyId = certificationBodyId;
	}

    public String getAcbName() {
        return acbName;
    }

    public void setAcbName(String acbName) {
        this.acbName = acbName;
    }

    public String getDeveloperName() {
        return developerName;
    }

    public void setDeveloperName(String developerName) {
        this.developerName = developerName;
    }

	public AttestationType getTransparencyAttestation() {
		return transparencyAttestation;
	}

	public void setTransparencyAttestation(AttestationType transparencyAttestation) {
		this.transparencyAttestation = transparencyAttestation;
	}
}
