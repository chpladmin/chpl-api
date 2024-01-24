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

import gov.healthit.chpl.certificationCriteria.CertificationCriterionEntity;
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
@Table(name = "certification_criterion_attribute")
public class CertificationCriterionAttributeEntity extends EntityAudit {
    private static final long serialVersionUID = 8715943799930831350L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "criterion_id", insertable = false, updatable = false)
    private CertificationCriterionEntity criterion;

    @Column(name = "additional_software")
    private Boolean additionalSoftware;

    @Column(name = "api_documentation")
    private Boolean apiDocumentation;

    @Column(name = "attestation_answer")
    private Boolean attestationAnswer;

    @Column(name = "conformance_method")
    private Boolean conformanceMethod;

    @Column(name = "documentation_url")
    private Boolean documentationUrl;

    @Column(name = "export_documentation")
    private Boolean exportDocumentation;

    @Column(name = "functionality_tested")
    private Boolean functionalityTested;

    @Column(name = "gap")
    private Boolean gap;

    @Column(name = "g1_success")
    private Boolean g1Success;

    @Column(name = "g2_success")
    private Boolean g2Success;

    @Column(name = "optional_standard")
    private Boolean optionalStandard;

    @Column(name = "privacy_security_framework")
    private Boolean privacySecurityFramework;

    @Column(name = "sed")
    private Boolean sed;

    @Column(name = "service_base_url_list")
    private Boolean serviceBaseUrlList;

    @Column(name = "risk_management_summary_information")
    private Boolean riskManagementSummaryInformation;

    @Column(name = "svap")
    private Boolean svap;

    @Column(name = "test_data")
    private Boolean testData;

    @Column(name = "test_procedure")
    private Boolean testProcedure;

    @Column(name = "test_standard")
    private Boolean testStandard;

    @Column(name = "test_tool")
    private Boolean testTool;

    @Column(name = "use_cases")
    private Boolean useCases;

    @Column(name = "standard")
    private Boolean standard;
}
