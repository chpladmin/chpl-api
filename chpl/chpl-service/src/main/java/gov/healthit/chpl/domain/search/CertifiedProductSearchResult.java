package gov.healthit.chpl.domain.search;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonView;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CertifiedProductSearchResult implements Serializable {
    private static final long serialVersionUID = -2547390525592841034L;

    @JsonView({
            SearchViews.Default.class
    })
    private Long id;

    @JsonView({
            SearchViews.Default.class
    })
    private String chplProductNumber;

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
    private Long openSurveillanceNonconformityCount;

    @JsonView({
            SearchViews.Default.class
    })
    private Long closedSurveillanceNonconformityCount;

    @JsonView({
        SearchViews.Default.class
    })
    private Long directReviewCount;

    @JsonView({
            SearchViews.Default.class
    })
    private Long openDirectReviewNonconformityCount;

    @JsonView({
            SearchViews.Default.class
    })
    private Long closedDirectReviewNonconformityCount;

    private Long openSurveillanceCount;
    private Long closedSurveillanceCount;
    private Long decertificationDate;
    private Long numMeaningfulUse;
    private Long numMeaningfulUseDate;
    private String transparencyAttestationUrl;

    public CertifiedProductSearchResult(CertifiedProductSearchResult other) {
        this.id = other.getId();
        this.chplProductNumber = other.getChplProductNumber();
        this.edition = other.getEdition();
        this.acb = other.getAcb();
        this.acbCertificationId = other.getAcbCertificationId();
        this.practiceType = other.getPracticeType();
        this.developer = other.getDeveloper();
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
        this.openSurveillanceNonconformityCount = other.getOpenSurveillanceNonconformityCount();
        this.closedSurveillanceNonconformityCount = other.getClosedSurveillanceNonconformityCount();
        this.directReviewCount = other.getDirectReviewCount();
        this.openDirectReviewNonconformityCount = other.getOpenDirectReviewNonconformityCount();
        this.closedDirectReviewNonconformityCount = other.getClosedDirectReviewNonconformityCount();
        this.numMeaningfulUse = other.getNumMeaningfulUse();
        this.numMeaningfulUseDate = other.getNumMeaningfulUseDate();
        this.transparencyAttestationUrl = other.getTransparencyAttestationUrl();
    }
}
