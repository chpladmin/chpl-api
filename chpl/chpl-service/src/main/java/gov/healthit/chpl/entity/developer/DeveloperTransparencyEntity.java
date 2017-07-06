package gov.healthit.chpl.entity.developer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "developers_with_attestations")
public class DeveloperTransparencyEntity {
	private static final long serialVersionUID = -2928065796554477869L;
	
    @Id 
	@Column(name = "vendor_id", nullable = false)
	private Long id;
    
    @Column(name = "vendor_name")
    private String name;
    
    @Column(name = "status_name")
    private String status;
    
    @Column(name = "transparency_attestation_urls")
    private String transparencyAttestationUrls;
    
    @Column(name = "attestations")
    private String acbAttestations;
   
	public DeveloperTransparencyEntity() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTransparencyAttestationUrls() {
		return transparencyAttestationUrls;
	}

	public void setTransparencyAttestationUrls(String transparencyAttestationUrls) {
		this.transparencyAttestationUrls = transparencyAttestationUrls;
	}

	public String getAcbAttestations() {
		return acbAttestations;
	}

	public void setAcbAttestations(String acbAttestations) {
		this.acbAttestations = acbAttestations;
	}
}
