package gov.healthit.chpl.entity.listing;

import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Where;

import gov.healthit.chpl.entity.CertificationCriterionEntity;
import gov.healthit.chpl.svap.entity.CertificationResultSvapEntity;
import lombok.Data;

@Data
@Immutable
@Entity
@Table(name = "certification_result_details")
public class CertificationResultDetailsEntity {
    private static final long serialVersionUID = -2928065796550377879L;

    @Id
    @Basic(optional = false)
    @Column(name = "certification_result_id", nullable = false)
    private Long id;

    @Column(name = "certification_criterion_id")
    private Long certificationCriterionId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_criterion_id", insertable = false, updatable = false)
    private CertificationCriterionEntity certificationCriterion;

    @Column(name = "certified_product_id")
    private Long certifiedProductId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certified_product_id", insertable = false, updatable = false)
    private CertifiedProductDetailsEntity listing;

    @Column(name = "success")
    private Boolean success;

    @Column(name = "number")
    private String number;

    @Column(name = "title")
    private String title;

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

    @Column(name = "deleted")
    private Boolean deleted;

    @Basic(optional = true)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "certificationResultId")
    @Column(name = "certification_result_id")
    @Where(clause = "deleted <> 'true'")
    private Set<CertificationResultTestDataEntity> certificationResultTestData;

    @Basic(optional = true)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "certificationResultId")
    @Column(name = "certification_result_id")
    @Where(clause = "deleted <> 'true'")
    private Set<CertificationResultTestFunctionalityEntity> certificationResultTestFunctionalities;

    @Basic(optional = true)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "certificationResultId")
    @Column(name = "certification_result_id")
    @Where(clause = "deleted <> 'true'")
    private Set<CertificationResultTestProcedureEntity> certificationResultTestProcedures;

    @Basic(optional = true)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "certificationResultId")
    @Column(name = "certification_result_id")
    @Where(clause = "deleted <> 'true'")
    private Set<CertificationResultTestStandardEntity> certificationResultTestStandards;

    @Basic(optional = true)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "certificationResultId")
    @Column(name = "certification_result_id")
    @Where(clause = "deleted <> 'true'")
    private Set<CertificationResultAdditionalSoftwareEntity> certificationResultAdditionalSoftware;

    @Basic(optional = true)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "certificationResultId")
    @Column(name = "certification_result_id")
    @Where(clause = "deleted <> 'true'")
    private Set<CertificationResultTestToolEntity> certificationResultTestTools;

    @Basic(optional = true)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "certificationResultId")
    @Column(name = "certification_result_id")
    @Where(clause = "deleted <> 'true'")
    private Set<CertificationResultSvapEntity> certificationResultSvaps;

}
