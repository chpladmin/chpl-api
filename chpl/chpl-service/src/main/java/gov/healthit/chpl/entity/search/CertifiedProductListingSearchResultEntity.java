package gov.healthit.chpl.entity.search;

import java.time.LocalDate;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Immutable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Immutable
public class CertifiedProductListingSearchResultEntity {
    private static final long serialVersionUID = -2928445796550377509L;

    @Id
    @Column(name = "unique_id")
    private int uniqueId;

    @Column(name = "certified_product_id", nullable = false)
    private Long id;

    @Column(name = "chpl_product_number")
    private String chplProductNumber;

    @Column(name = "certification_status_name")
    private String certificationStatus;

    @Column(name = "promoting_interoperability_user_count")
    private Long promotingInteroperabilityUserCount;

    @Column(name = "promoting_interoperability_user_count_date")
    private LocalDate promotingInteroperabilityUserCountDate;

    @Column(name = "mandatory_disclosures")
    private String mandatoryDisclosures;

    @Deprecated
    @Column(name = "year")
    private String edition;

    @Deprecated
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

    @Column(name = "vendor_name")
    private String developer;

    @Column(name = "prev_vendor")
    private String previousDeveloperOwner;

    @Column(name = "certification_date")
    private Date certificationDate;

    @Column(name = "decertification_date")
    private Date decertificationDate;

    @Column(name = "count_surveillance_activities")
    private Integer countSurveillance;

    @Column(name = "count_open_nonconformities")
    private Integer countOpenSurveillanceNonconformities;

    @Column(name = "count_closed_nonconformities")
    private Integer countClosedSurveillanceNonconformities;

    @Column(name = "cert_number")
    private String cert;

    @Column(name = "cqm_number")
    private String cqm;
}
