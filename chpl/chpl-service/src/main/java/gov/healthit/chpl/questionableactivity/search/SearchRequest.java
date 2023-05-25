package gov.healthit.chpl.questionableactivity.search;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchRequest implements Serializable {
    private static final long serialVersionUID = 11792092152701580L;
    public static final String DATE_SEARCH_FORMAT = "yyyy-MM-dd";
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    private String searchTerm;
    @Builder.Default
    private Set<String> triggerIdStrings = new HashSet<String>();
    @Builder.Default
    private Set<Long> triggerIds = new HashSet<Long>();
    private String activityDateStart;
    private String activityDateEnd;
    private String orderByString;
    private OrderByOption orderBy;
    @Builder.Default
    private Boolean sortDescending = false;
    @Builder.Default
    private Integer pageNumber = 0;
    @Builder.Default
    private Integer pageSize = DEFAULT_PAGE_SIZE;
}
