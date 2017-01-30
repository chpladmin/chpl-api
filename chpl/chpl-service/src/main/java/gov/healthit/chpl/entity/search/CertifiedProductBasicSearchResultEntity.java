package gov.healthit.chpl.entity.search;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "certified_product_search")
public class CertifiedProductBasicSearchResultEntity {
	private static final long serialVersionUID = -2928065796550377869L;
	
    @Id 
	@Column(name = "certified_product_id", nullable = false)
	private Long id;
    
    @Column(name = "chpl_product_number")
    private String chplProductNumber;
    
    @Column(name = "year")
    private String edition;
    
    @Column(name = "testing_lab_name")
    private String atlName;
    
    @Column(name = "certification_body_name")
    private String acbName;
    
    @Column(name = "acb_certification_id")
    private String acbCertificationId;

	@Column(name = "practice_type_name")
    private String practiceTypeName;
    
	@Column( name = "product_version")
	private String version;
	
	@Column(name = "product_name")
	private String product;
	
	@Column(name = "vendor_name")
	private String developer;
	
	@Column(name = "owner_history")
	private String previousDevelopers;
	
	@Column( name = "certification_date")
	private Date certificationDate;
	
	@Column( name = "certification_status_name")
	private String certificationStatus;
	
	@Column(name = "has_had_surveillance")
	private Boolean hasHadSurveillance;
	
    @Column(name = "has_open_surveillance")
    private Boolean hasOpenSurveillance;

    @Column(name = "has_open_nonconformities")
    private Boolean hasOpenNonconformities;
  
    @Column(name = "certs")
    private String certs; // comma-separated list of all certification criteria met by the certified product
    
    @Column(name = "cqms")
    private String cqms; // comma-separated list of all cqms met by the certified product
  
	public CertifiedProductBasicSearchResultEntity() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getChplProductNumber() {
		return chplProductNumber;
	}

	public void setChplProductNumber(String chplProductNumber) {
		this.chplProductNumber = chplProductNumber;
	}

	public String getEdition() {
		return edition;
	}

	public void setEdition(String edition) {
		this.edition = edition;
	}

	public String getAtlName() {
		return atlName;
	}

	public void setAtlName(String atlName) {
		this.atlName = atlName;
	}

	public String getAcbName() {
		return acbName;
	}

	public void setAcbName(String acbName) {
		this.acbName = acbName;
	}

	public String getPracticeTypeName() {
		return practiceTypeName;
	}

	public void setPracticeTypeName(String practiceTypeName) {
		this.practiceTypeName = practiceTypeName;
	}

    public String getAcbCertificationId() {
		return acbCertificationId;
	}

	public void setAcbCertificationId(String acbCertificationId) {
		this.acbCertificationId = acbCertificationId;
	}
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getDeveloper() {
		return developer;
	}

	public void setDeveloper(String developer) {
		this.developer = developer;
	}

	public Date getCertificationDate() {
		return certificationDate;
	}

	public void setCertificationDate(Date certificationDate) {
		this.certificationDate = certificationDate;
	}

	public String getCertificationStatus() {
		return certificationStatus;
	}

	public void setCertificationStatus(String certificationStatus) {
		this.certificationStatus = certificationStatus;
	}

	public Boolean getHasHadSurveillance() {
		return hasHadSurveillance;
	}

	public void setHasHadSurveillance(Boolean hasHadSurveillance) {
		this.hasHadSurveillance = hasHadSurveillance;
	}

	public Boolean getHasOpenSurveillance() {
		return hasOpenSurveillance;
	}

	public void setHasOpenSurveillance(Boolean hasOpenSurveillance) {
		this.hasOpenSurveillance = hasOpenSurveillance;
	}

	public Boolean getHasOpenNonconformities() {
		return hasOpenNonconformities;
	}

	public void setHasOpenNonconformities(Boolean hasOpenNonconformities) {
		this.hasOpenNonconformities = hasOpenNonconformities;
	}

	public String getCerts() {
		return certs;
	}

	public void setCerts(String certs) {
		this.certs = certs;
	}

	public String getCqms() {
		return cqms;
	}

	public void setCqms(String cqms) {
		this.cqms = cqms;
	}

	public String getPreviousDevelopers() {
		return previousDevelopers;
	}

	public void setPreviousDevelopers(String previousDevelopers) {
		this.previousDevelopers = previousDevelopers;
	}
}
