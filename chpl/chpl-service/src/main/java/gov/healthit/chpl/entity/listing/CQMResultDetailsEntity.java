package gov.healthit.chpl.entity.listing;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "cqm_result_details", schema = "openchpl")
public class CQMResultDetailsEntity {

    @Id
    @Column(name = "cqm_result_id", nullable = false)
    private Long id;

    @Column(name = "certified_product_id")
    private Long certifiedProductId;

    @Basic(optional = false)
    @Column(name = "success", nullable = false)
    private Boolean success;

    @Basic(optional = false)
    @Column(name = "cqm_criterion_id", nullable = false)
    private Long cqmCriterionId;

    @Column(name = "number")
    private String number;

    @Column(name = "cms_id")
    private String cmsId;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "nqf_number")
    private String nqfNumber;

    @Column(name = "cqm_criterion_type_id")
    private Long cqmCriterionTypeId;

    @Basic(optional = true)
    @Column(name = "cqm_version_id", nullable = true)
    private Long cqmVersionId;

    @Basic(optional = true)
    @Column(name = "cqm_domain", nullable = true)
    private String domain;

    @Basic(optional = true)
    @Column(name = "version")
    private String version;

    @Column(name = "cqm_id")
    private String cqmId;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(final Boolean success) {
        this.success = success;
    }

    public Long getCqmCriterionId() {
        return cqmCriterionId;
    }

    public void setCqmCriterionId(final Long cqmCriterionId) {
        this.cqmCriterionId = cqmCriterionId;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(final String number) {
        this.number = number;
    }

    public String getCmsId() {
        return cmsId;
    }

    public void setCmsId(final String cmsId) {
        this.cmsId = cmsId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getNqfNumber() {
        return nqfNumber;
    }

    public void setNqfNumber(final String nqfNumber) {
        this.nqfNumber = nqfNumber;
    }

    public Long getCqmCriterionTypeId() {
        return cqmCriterionTypeId;
    }

    public void setCqmCriterionTypeId(final Long cqmCriterionTypeId) {
        this.cqmCriterionTypeId = cqmCriterionTypeId;
    }

    public Long getCqmVersionId() {
        return cqmVersionId;
    }

    public void setCqmVersionId(final Long cqmVersionId) {
        this.cqmVersionId = cqmVersionId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(final String domain) {
        this.domain = domain;
    }

    public String getCqmId() {
        return cqmId;
    }

    public void setCqmId(final String cqmId) {
        this.cqmId = cqmId;
    }

    public Long getCertifiedProductId() {
        return certifiedProductId;
    }

    public void setCertifiedProductId(final Long certifiedProductId) {
        this.certifiedProductId = certifiedProductId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}
