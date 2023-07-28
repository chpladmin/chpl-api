package gov.healthit.chpl.search.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data
@Deprecated
public class CertifiedProductSearchResult implements Serializable {
    private static final long serialVersionUID = -2547390525592841123L;

    @JsonView({
            SearchViews.Default.class
    })
    private Long id;

    @JsonView({
            SearchViews.Default.class
    })
    private String chplProductNumber;

    @Deprecated
    @DeprecatedResponseField(message = "The certification edition will be removed.", removalDate = "2024-02-01")
    @JsonView({
            SearchViews.Default.class
    })
    private String edition;

    @JsonView({
            SearchViews.Default.class
    })
    private String acb;

    @JsonView({
            SearchViews.Default.class
    })
    private String acbCertificationId;

    @JsonView({
            SearchViews.Default.class
    })
    private String practiceType;

    @JsonView({
        SearchViews.Default.class
    })
    private Long developerId;

    @JsonView({
            SearchViews.Default.class
    })
    private String developer;

    @JsonView({
        SearchViews.Default.class
    })
    private String developerStatus;

    @JsonView({
            SearchViews.Default.class
    })
    private String product;

    @JsonView({
            SearchViews.Default.class
    })
    private String version;

    @JsonView({
            SearchViews.Default.class
    })
    private Long certificationDate;

    @JsonView({
            SearchViews.Default.class
    })
    private String certificationStatus;

    @JsonView({
        SearchViews.Default.class
    })
    private Boolean curesUpdate;

    @JsonView({
            SearchViews.Default.class
    })
    private Long surveillanceCount;

    @JsonView({
            SearchViews.Default.class
    })
    private Long openSurveillanceNonConformityCount;

    @JsonView({
            SearchViews.Default.class
    })
    private Long closedSurveillanceNonConformityCount;

    @JsonView({
        SearchViews.Default.class
    })
    @Builder.Default
    private Integer directReviewCount = 0;

    @JsonView({
            SearchViews.Default.class
    })
    @Builder.Default
    private Integer openDirectReviewNonConformityCount = 0;

    @JsonView({
            SearchViews.Default.class
    })
    @Builder.Default
    private Integer closedDirectReviewNonConformityCount = 0;

    private Long openSurveillanceCount;
    private Long closedSurveillanceCount;
    private Long decertificationDate;

    private Long promotingInteroperabilityUserCount;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate promotingInteroperabilityUserDate;
    private String mandatoryDisclosures;

    public CertifiedProductSearchResult(CertifiedProductSearchResult other) {
        this.id = other.getId();
        this.chplProductNumber = other.getChplProductNumber();
        this.edition = other.getEdition();
        this.acb = other.getAcb();
        this.acbCertificationId = other.getAcbCertificationId();
        this.practiceType = other.getPracticeType();
        this.developer = other.getDeveloper();
        this.developerId = other.getDeveloperId();
        this.developerStatus = other.getDeveloperStatus();
        this.product = other.getProduct();
        this.version = other.getVersion();
        this.certificationDate = other.getCertificationDate();
        this.certificationStatus = other.getCertificationStatus();
        this.curesUpdate = other.getCuresUpdate();
        this.decertificationDate = other.getDecertificationDate();
        this.surveillanceCount = other.getSurveillanceCount();
        this.openSurveillanceCount = other.getOpenSurveillanceCount();
        this.closedSurveillanceCount = other.getClosedSurveillanceCount();
        this.openSurveillanceNonConformityCount = other.getOpenSurveillanceNonConformityCount();
        this.closedSurveillanceNonConformityCount = other.getClosedSurveillanceNonConformityCount();
        this.directReviewCount = other.getDirectReviewCount();
        this.openDirectReviewNonConformityCount = other.getOpenDirectReviewNonConformityCount();
        this.closedDirectReviewNonConformityCount = other.getClosedDirectReviewNonConformityCount();
        this.promotingInteroperabilityUserCount = other.getPromotingInteroperabilityUserCount();
        this.promotingInteroperabilityUserDate = other.getPromotingInteroperabilityUserDate();
        this.mandatoryDisclosures = other.getMandatoryDisclosures();
    }

    @Override
    public boolean equals(Object another) {
        if (another == null) {
            return false;
        }
        if (!(another instanceof CertifiedProductSearchResult)) {
            return false;
        }
        CertifiedProductSearchResult anotherSearchResult = (CertifiedProductSearchResult) another;
        if (ObjectUtils.allNotNull(this, anotherSearchResult, this.getId(), anotherSearchResult.getId())) {
            return Objects.equals(this.getId(), anotherSearchResult.getId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (this.getId() == null) {
            return -1;
        }
        return this.getId().hashCode();
    }

    @JsonIgnore
    public String getDerivedEdition() {
        return getEdition() + (BooleanUtils.isTrue(getCuresUpdate()) ? CertificationEdition.CURES_SUFFIX : "");
    }
}
