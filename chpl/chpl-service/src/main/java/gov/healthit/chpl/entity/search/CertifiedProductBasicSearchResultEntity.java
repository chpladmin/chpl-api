package gov.healthit.chpl.entity.search;

import java.time.LocalDate;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Deprecated
@Data
@NoArgsConstructor
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

    @Column(name = "cures_update")
    private Boolean curesUpdate;

    @Column(name = "certification_body_name")
    private String acbName;

    @Column(name = "acb_certification_id")
    private String acbCertificationId;

    @Column(name = "practice_type_name")
    private String practiceTypeName;

    @Column(name = "product_version")
    private String version;

    @Column(name = "product_name")
    private String product;

    @Column(name = "vendor_id")
    private Long developerId;

    @Column(name = "vendor_name")
    private String developer;

    @Column(name = "vendor_status_name")
    private String developerStatus;

    @Column(name = "owner_history")
    private String previousDevelopers;

    @Column(name = "certification_date")
    private Date certificationDate;

    @Column(name = "certification_status_name")
    private String certificationStatus;

    @Column(name = "decertification_date")
    private Date decertificationDate;

    @Column(name = "mandatory_disclosures")
    private String mandatoryDisclosures;

    @Column(name = "api_documentation")
    private String apiDocumentation;

    @Column(name = "service_base_url_list")
    private String serviceBaseUrlList;

    @Column(name = "surveillance_count")
    private Long surveillanceCount;

    @Column(name = "open_surveillance_count")
    private Long openSurveillanceCount;

    @Column(name = "closed_surveillance_count")
    private Long closedSurveillanceCount;

    @Column(name = "open_nonconformity_count")
    private Long openSurveillanceNonConformityCount;

    @Column(name = "closed_nonconformity_count")
    private Long closedSurveillanceNonConformityCount;

    @Column(name = "surv_dates")
    private String surveillanceDates;

    @Column(name = "status_events")
    private String statusEvents;

    @Column(name = "promoting_interoperability_user_count")
    private Long promotingInteroperabilityUserCount;

    @Column(name = "promoting_interoperability_user_count_date")
    private LocalDate promotingInteroperabilityUserCountDate;

    @Column(name = "rwt_plans_url")
    private String rwtPlansUrl;

    @Column(name = "rwt_results_url")
    private String rwtResultsUrl;

    @Column(name = "certs")
    private String certs; // comma-separated list of all certification criteria met by the certified product

    @Column(name = "cqms")
    private String cqms; // comma-separated list of all cqms met by the certified product

    @Column(name = "parent")
    private String parent; // comma-separated list of all parents

    @Column(name = "child")
    private String child; // comma-separated list of all children
}
