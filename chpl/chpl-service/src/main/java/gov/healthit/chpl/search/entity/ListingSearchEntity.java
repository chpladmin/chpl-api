package gov.healthit.chpl.search.entity;

import java.time.LocalDate;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Immutable
@Table(name = "listing_search")
public class ListingSearchEntity {
    public static final String SMILEY_SPLIT_CHAR = "\u263A";
    public static final String FROWNEY_SPLIT_CHAR = "\u2639";

    @Id
    @Column(name = "certified_product_id", nullable = false)
    private Long id;

    @Column(name = "chpl_product_number")
    private String chplProductNumber;

    @Column(name = "certification_edition_id")
    private Long certificationEditionId;

    @Column(name = "year")
    private String certificationEditionYear;

    @Column(name = "cures_update")
    private Boolean curesUpdate;

    @Column(name = "certification_body_id")
    private Long certificationBodyId;

    @Column(name = "certification_body_name")
    private String certificationBodyName;

    @Column(name = "acb_certification_id")
    private String acbCertificationId;

    @Column(name = "practice_type_id")
    private Long practiceTypeId;

    @Column(name = "practice_type_name")
    private String practiceTypeName;

    @Column(name = "version_id")
    private Long versionId;

    @Column(name = "version_name")
    private String version;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name")
    private String product;

    @Column(name = "developer_id")
    private Long developerId;

    @Column(name = "developer_name")
    private String developer;

    @Column(name = "developer_status_name")
    private String developerStatus;

    @Column(name = "developer_status_id")
    private Long developerStatusId;

    @Column(name = "product_owner_history")
    private String previousDevelopers;

    @Column(name = "certification_date")
    private Date certificationDate;

    @Column(name = "certification_status_id")
    private Long certificationStatusId;

    @Column(name = "certification_status_name")
    private String certificationStatus;

    @Column(name = "decertification_date")
    private Date decertificationDate;

    @Column(name = "mandatory_disclosures")
    private String mandatoryDisclosures;

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

    @Column(name = "surv_date_ranges")
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

    @Column(name = "certification_criteria_met")
    private String certificationCriteriaMet;

    @Column(name = "criteria_with_api_documentation")
    private String criteriaWithApiDocumentation;

    @Column(name = "criteria_with_service_base_url")
    private String criteriaWithServiceBaseUrl;

    @Column(name = "cqms_met")
    private String cqmsMet;

    @Column(name = "parents")
    private String parents;

    @Column(name = "children")
    private String children;
}
