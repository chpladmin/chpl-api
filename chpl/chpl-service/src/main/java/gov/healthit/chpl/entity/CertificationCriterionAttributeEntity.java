package gov.healthit.chpl.entity;

import java.util.Date;

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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "certification_criterion_attribute")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CertificationCriterionAttributeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "criterion_id", insertable = false, updatable = false)
    private CertificationCriterionEntity criterion;

    @Column(name = "conformance_method")
    private Boolean conformanceMethod;

    @Column(name = "functionality_tested")
    private Boolean functionalityTested;

    @Column(name = "optional_standard")
    private Boolean optionalStandard;

    @Column(name = "privacy_security_framework")
    private Boolean privacySecurityFramework;

    @Column(name = "service_base_url_list")
    private Boolean serviceBaseUrlList;

    @Column(name = "svap")
    private Boolean svap;

    @Column(name = "test_procedure")
    private Boolean testProcedure;

    @Column(name = "test_tool")
    private Boolean testTool;

    @Column(name = "test_data")
    private Boolean testData;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false, insertable = false, updatable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false, insertable = false, updatable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;
}
