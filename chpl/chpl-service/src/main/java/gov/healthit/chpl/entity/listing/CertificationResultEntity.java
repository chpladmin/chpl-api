package gov.healthit.chpl.entity.listing;

import java.util.List;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import gov.healthit.chpl.certificationCriteria.CertificationCriterionEntity;
import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.testtool.CertificationResultTestToolEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "certification_result")
public class CertificationResultEntity extends EntityAudit {
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

    @Column(name = "risk_management_summary_information")
    private String riskManagementSummaryInformation;

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

}
