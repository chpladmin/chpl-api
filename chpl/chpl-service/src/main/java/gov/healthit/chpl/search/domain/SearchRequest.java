package gov.healthit.chpl.search.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchRequest implements Serializable {
    private static final long serialVersionUID = 1179207628667101580L;
    public static final String CERTIFICATION_DATE_SEARCH_FORMAT = "yyyy-MM-dd";
    public static final int MAX_LISTING_IDS = 100;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    private String searchTerm;
    @Builder.Default
    @JsonIgnore
    @XmlTransient
    private Set<String> listingIdStrings = new HashSet<String>();
    @Builder.Default
    private Set<Long> listingIds = new HashSet<Long>();
    @Builder.Default
    private Set<String> certificationStatuses = new HashSet<String>();
    @Builder.Default
    private Set<String> derivedCertificationEditions = new HashSet<String>();
    @Builder.Default
    private Set<String> certificationEditions = new HashSet<String>();
    @Builder.Default
    @JsonIgnore
    @XmlTransient
    private Set<String> certificationCriteriaIdStrings = new HashSet<String>();
    @Builder.Default
    private Set<Long> certificationCriteriaIds = new HashSet<Long>();
    @JsonIgnore
    @XmlTransient
    private String certificationCriteriaOperatorString;
    private SearchSetOperator certificationCriteriaOperator;
    @Builder.Default
    private Set<String> cqms = new HashSet<String>();
    @JsonIgnore
    @XmlTransient
    private String cqmsOperatorString;
    private SearchSetOperator cqmsOperator;
    @Builder.Default
    private Set<String> certificationBodies = new HashSet<String>();

    private String developer;
    private Long developerId;
    private String product;
    private String version;
    private String practiceType;
    private String certificationDateStart;
    private String certificationDateEnd;
    private String decertificationDateStart;
    private String decertificationDateEnd;
    @Builder.Default
    private ComplianceSearchFilter complianceActivity = new ComplianceSearchFilter();

    @JsonIgnore
    @XmlTransient
    @Builder.Default
    private Set<String> rwtOptionsStrings = new HashSet<String>();
    @Builder.Default
    private Set<RwtSearchOptions> rwtOptions = new HashSet<RwtSearchOptions>();
    @JsonIgnore
    @XmlTransient
    private String rwtOperatorString;
    private SearchSetOperator rwtOperator;

    @Builder.Default
    private Boolean hasSvapNoticeUrl = null;
    @Builder.Default
    private Boolean hasAnySvap = null;

    @Builder.Default
    @JsonIgnore
    @XmlTransient
    private Set<String> svapIdStrings = new HashSet<String>();
    @Builder.Default
    private Set<Long> svapIds = new HashSet<Long>();
    @JsonIgnore
    @XmlTransient
    private String svapOperatorString;
    private SearchSetOperator svapOperator;

    @JsonIgnore
    @XmlTransient
    private String orderByString;
    private OrderByOption orderBy;
    @Builder.Default
    private Boolean sortDescending = false;
    @Builder.Default
    private Integer pageNumber = 0;
    @Builder.Default
    private Integer pageSize = DEFAULT_PAGE_SIZE;
}
