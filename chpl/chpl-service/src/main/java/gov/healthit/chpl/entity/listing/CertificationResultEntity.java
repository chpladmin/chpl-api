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
import lombok.Data;

@Data
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
}
