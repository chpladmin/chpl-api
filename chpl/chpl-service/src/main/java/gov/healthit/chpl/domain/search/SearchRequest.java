package gov.healthit.chpl.domain.search;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import lombok.Data;

@Data
public class SearchRequest implements Serializable {
    private static final long serialVersionUID = 1179207628667101580L;
    public static final String CERTIFICATION_DATE_SEARCH_FORMAT = "yyyy-MM-dd";
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    private String searchTerm;
    private Set<String> certificationStatuses = new HashSet<String>();
    private Set<String> certificationEditions = new HashSet<String>();
    private Set<Long> certificationCriteriaIds = new HashSet<Long>();
    private SearchSetOperator certificationCriteriaOperator = SearchSetOperator.OR;
    private Set<String> cqms = new HashSet<String>();
    private SearchSetOperator cqmsOperator = SearchSetOperator.OR;
    private Set<String> certificationBodies = new HashSet<String>();

    private String developer;
    private String product;
    private String version;
    private String practiceType;
    private String certificationDateStart;
    private String certificationDateEnd;
    private ComplianceSearchFilter complianceActivity = new ComplianceSearchFilter();
    private OrderByOption orderBy;
    private Boolean sortDescending = false;
    private Integer pageNumber = 0;
    private Integer pageSize = DEFAULT_PAGE_SIZE;
}
