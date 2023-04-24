package gov.healthit.chpl.search.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.domain.IdNamePair;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import gov.healthit.chpl.util.NullSafeEvaluator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ListingSearchResult implements Serializable {

    private static final long serialVersionUID = -254739051764841038L;
    public static final String SMILEY_SPLIT_CHAR = "\u263A";
    public static final String FROWNEY_SPLIT_CHAR = "\u2639";

    private Long id;
    private String chplProductNumber;
    private Set<String> previousChplProductNumbers;
    private IdNamePair edition;
    private IdNamePair certificationBody;
    private String acbCertificationId;
    private IdNamePair practiceType;
    private DeveloperSearchResult developer;
    private IdNamePair product;
    private IdNamePair version;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate certificationDate;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate decertificationDate;
    private IdNamePair certificationStatus;
    private Boolean curesUpdate;
    private Long surveillanceCount;
    private Long openSurveillanceNonConformityCount;
    private Long closedSurveillanceNonConformityCount;
    @Builder.Default
    private Integer directReviewCount = 0;
    @Builder.Default
    private Integer openDirectReviewNonConformityCount = 0;
    @Builder.Default
    private Integer closedDirectReviewNonConformityCount = 0;
    private Long openSurveillanceCount;
    private Long closedSurveillanceCount;
    private PromotingInteroperabilitySearchResult promotingInteroperability;
    private String mandatoryDisclosures;
    private Set<IdNamePair> previousDevelopers;
    private Set<CertificationCriterionSearchResult> criteriaMet;
    private Set<CQMSearchResult> cqmsMet;
    private Set<DateRangeSearchResult> surveillanceDateRanges;
    private Set<StatusEventSearchResult> statusEvents;
    private Set<CertificationCriterionSearchResultWithStringField> apiDocumentation;
    private CertificationCriterionSearchResultWithStringField serviceBaseUrlList;
    private Set<CertificationCriterionSearchResultWithLongField> svaps;
    private String rwtPlansUrl;
    private String rwtResultsUrl;
    private String svapNoticeUrl;

    public ListingSearchResult() {
        this.setDirectReviewCount(0);
        this.setSurveillanceCount(0L);
        this.setOpenDirectReviewNonConformityCount(0);
        this.setClosedDirectReviewNonConformityCount(0);
        this.setOpenSurveillanceCount(0L);
        this.setClosedSurveillanceCount(0L);
        this.setOpenSurveillanceNonConformityCount(0L);
        this.setClosedSurveillanceNonConformityCount(0L);
        previousChplProductNumbers = new LinkedHashSet<String>();
        previousDevelopers = new HashSet<IdNamePair>();
        criteriaMet = new HashSet<CertificationCriterionSearchResult>();
        cqmsMet = new HashSet<CQMSearchResult>();
        surveillanceDateRanges = new HashSet<DateRangeSearchResult>();
        statusEvents = new HashSet<StatusEventSearchResult>();
        apiDocumentation = new HashSet<CertificationCriterionSearchResultWithStringField>();
        svaps = new HashSet<CertificationCriterionSearchResultWithLongField>();
    }

    @Override
    public boolean equals(Object another) {
        if (another == null) {
            return false;
        }
        if (!(another instanceof ListingSearchResult)) {
            return false;
        }
        ListingSearchResult anotherSearchResult = (ListingSearchResult) another;
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
        return getEdition().getName() + (BooleanUtils.isTrue(getCuresUpdate()) ? CertificationEdition.CURES_SUFFIX : "");
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeveloperSearchResult implements Serializable {
        private static final long serialVersionUID = 2613618482034013795L;
        private Long id;
        private String name;
        private IdNamePair status;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromotingInteroperabilitySearchResult implements Serializable {
        private static final long serialVersionUID = 2278077507370451530L;
        private Long userCount;
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @JsonSerialize(using = LocalDateSerializer.class)
        private LocalDate userDate;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificationCriterionSearchResult implements Serializable  {
        private static final long serialVersionUID = -3239646505785162609L;
        private Long id;
        private String number;
        private String title;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificationCriterionSearchResultWithStringField implements Serializable  {
        private static final long serialVersionUID = 2228742866328063730L;
        private CertificationCriterionSearchResult criterion;
        private String value;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificationCriterionSearchResultWithLongField implements Serializable  {
        private static final long serialVersionUID = 2216242866328063730L;
        private CertificationCriterionSearchResult criterion;
        private Long value;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CQMSearchResult implements Serializable {
        private static final long serialVersionUID = 4266643022213089438L;
        private Long id;
        private String number;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusEventSearchResult implements Serializable {
        private static final long serialVersionUID = -6553219041130182281L;
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @JsonSerialize(using = LocalDateSerializer.class)
        private LocalDate statusStart;
        private IdNamePair status;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DateRangeSearchResult implements Serializable {
        private static final long serialVersionUID = 3820451684223011046L;
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @JsonSerialize(using = LocalDateSerializer.class)
        private LocalDate start;
        @JsonDeserialize(using = LocalDateDeserializer.class)
        @JsonSerialize(using = LocalDateSerializer.class)
        private LocalDate end;
    }

    @JsonIgnore
    public boolean isCertificateActive() {
        String currentStatus = NullSafeEvaluator.eval(() -> certificationStatus.getName(), "");
        return  currentStatus.equalsIgnoreCase(CertificationStatusType.Active.getName())
                || currentStatus.equalsIgnoreCase(CertificationStatusType.SuspendedByAcb.getName())
                || currentStatus.equalsIgnoreCase(CertificationStatusType.SuspendedByOnc.getName());
    }

}
