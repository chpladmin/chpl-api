package gov.healthit.chpl.entity.listing;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import gov.healthit.chpl.entity.CertificationCriterionEntity;
import gov.healthit.chpl.util.Util;

@Entity
@Table(name = "certification_result")
public class CertificationResultEntity implements Serializable {

    /** Serial Version UID. */
    private static final long serialVersionUID = -9050374846030066967L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "certification_result_id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "certification_criterion_id", nullable = false)
    private Long certificationCriterionId;

    @Basic(optional = false)
    @Column(name = "certified_product_id", nullable = false)
    private Long certifiedProductId;

    @Column(name = "gap")
    private Boolean gap;

    @Column(name = "sed")
    private Boolean sed;

    @Column(name = "g1_success")
    private Boolean g1Success;

    @Column(name = "g2_success")
    private Boolean g2Success;

    @Column(name = "attestation_answer")
    private Boolean attestationAnswer;

    @Basic(optional = false)
    @Column(name = "success", nullable = false)
    private Boolean success;

    @Column(name = "api_documentation")
    private String apiDocumentation;

    @Column(name = "export_documentation")
    private String exportDocumentation;

    @Column(name = "documentation_url")
    private String documentationUrl;

    @Column(name = "use_cases")
    private String useCases;

    @Column(name = "service_base_url_list")
    private String serviceBaseUrlList;

    @Column(name = "privacy_security_framework")
    private String privacySecurityFramework;

    @Basic(optional = true)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "certificationResult")
    private List<CertificationResultTestToolEntity> certificationResultTestTool;

    @Basic(optional = true)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_result_id", nullable = false, insertable = false, updatable = false)
    private CertificationResultUcdProcessEntity ucdProcesses;

    @Basic(optional = true)
    @ManyToOne
    @JoinColumn(name = "certified_product_id", nullable = false, insertable = false, updatable = false)
    private CertifiedProductEntity certifiedProduct;

    @Basic(optional = false)
    @ManyToOne(targetEntity = CertificationCriterionEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_criterion_id", nullable = false, insertable = false, updatable = false)
    private CertificationCriterionEntity certificationCriterion;

    /**
     * Default constructor, mainly for hibernate use.
     */
    public CertificationResultEntity() {
        // Default constructor
    }

    /**
     * Constructor taking a given ID.
     *
     * @param id
     *            to set
     */
    public CertificationResultEntity(Long id) {
        this.id = id;
    }

    /**
     * Return the type of this class. Useful for when dealing with proxies.
     *
     * @return Defining class.
     */
    @Transient
    public Class<?> getClassType() {
        return CertificationResultEntity.class;
    }

    /**
     * Return the value associated with the column: certificationCriterion.
     *
     * @return A CertificationCriterion object (this.certificationCriterion)
     */
    public Long getCertificationCriterionId() {
        return this.certificationCriterionId;
    }

    /**
     * Set the value related to the column: certificationCriterion.
     * @param certificationCriterionId the certificationCriterion value you wish to set
     */
    public void setCertificationCriterionId(final Long certificationCriterionId) {
        this.certificationCriterionId = certificationCriterionId;
    }

    public void setCertificationCriterion(final CertificationCriterionEntity certificationCriterion) {
        this.certificationCriterion = certificationCriterion;
    }

    /**
     * Return the value associated with the column: certifiedProduct.
     * @return A CertifiedProduct object (this.certifiedProduct)
     */
    public Long getCertifiedProductId() {
        return this.certifiedProductId;
    }

    /**
     * Set the value related to the column: certifiedProduct.
     * @param certifiedProductId the certifiedProduct value you wish to set
     */
    public void setCertifiedProductId(final Long certifiedProductId) {
        this.certifiedProductId = certifiedProductId;
    }

    /**
     * Return the value associated with the column: gap.
     * @return A Boolean object (this.gap)
     */
    public Boolean isGap() {
        return this.gap;
    }

    /**
     * Set the value related to the column: gap.
     * @param gap
     *            the gap value you wish to set
     */
    public void setGap(final Boolean gap) {
        this.gap = gap;
    }

    /**
     * Return the value associated with the column: id.
     *
     * @return A Long object (this.id)
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Set the value related to the column: id.
     *
     * @param id
     *            the id value you wish to set
     */
    public void setId(final Long id) {
        this.id = id;
    }

    /**
     * Return the value associated with the column: successful.
     *
     * @return A Boolean object (this.successful)
     */
    public Boolean isSuccess() {
        return this.success;
    }

    /**
     * Set the value related to the column: successful.
     * @param success the successful value you wish to set
     */
    public void setSuccess(final Boolean success) {
        this.success = success;
    }

    public Boolean getSed() {
        return sed;
    }

    public void setSed(final Boolean sed) {
        this.sed = sed;
    }

    public Boolean getG1Success() {
        return g1Success;
    }

    public void setG1Success(final Boolean g1Success) {
        this.g1Success = g1Success;
    }

    public Boolean getG2Success() {
        return g2Success;
    }

    public void setG2Success(final Boolean g2Success) {
        this.g2Success = g2Success;
    }

    public Boolean getGap() {
        return gap;
    }

    public Boolean getSuccess() {
        return success;
    }

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false, updatable = false, insertable = false)
    protected Date creationDate;

    //marked as updatable false to avoid running the soft delete triggers in the db
    //adding and removing certification results is done through the success flag
    @Basic(optional = false)
    @Column(nullable = false, updatable = false, insertable = false)
    protected Boolean deleted;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false, updatable = false, insertable = false)
    protected Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    protected Long lastModifiedUser;

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public String getApiDocumentation() {
        return apiDocumentation;
    }

    public void setApiDocumentation(final String apiDocumentation) {
        this.apiDocumentation = apiDocumentation;
    }

    public Boolean getAttestationAnswer() {
        return attestationAnswer;
    }

    public void setAttestationAnswer(Boolean attestationAnswer) {
        this.attestationAnswer = attestationAnswer;
    }

    public String getExportDocumentation() {
        return exportDocumentation;
    }

    public void setExportDocumentation(String exportDocumentation) {
        this.exportDocumentation = exportDocumentation;
    }

    public String getDocumentationUrl() {
        return documentationUrl;
    }

    public void setDocumentationUrl(String documentationUrl) {
        this.documentationUrl = documentationUrl;
    }

    public String getUseCases() {
        return useCases;
    }

    public void setUseCases(String useCases) {
        this.useCases = useCases;
    }

    public String getServiceBaseUrlList() {
        return serviceBaseUrlList;
    }

    public void setServiceBaseUrlList(String serviceBaseUrlList) {
        this.serviceBaseUrlList = serviceBaseUrlList;
    }

    public String getPrivacySecurityFramework() {
        return privacySecurityFramework;
    }

    public void setPrivacySecurityFramework(final String privacySecurityFramework) {
        this.privacySecurityFramework = privacySecurityFramework;
    }

    public List<CertificationResultTestToolEntity> getCertificationResultTestTool() {
        return certificationResultTestTool;
    }

    public void setCertificationResultTestTool(final List<CertificationResultTestToolEntity> certificationResultTestTool) {
        this.certificationResultTestTool = certificationResultTestTool;
    }

    public CertifiedProductEntity getCertifiedProduct() {
        return certifiedProduct;
    }

    public void setCertifiedProduct(final CertifiedProductEntity certifiedProduct) {
        this.certifiedProduct = certifiedProduct;
    }
}
