package gov.healthit.chpl.changerequest.search;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangeRequestSearchRequest implements Serializable {
    private static final long serialVersionUID = 1816207628667101580L;
    public static final String TIMESTAMP_SEARCH_FORMAT = "yyyy-MM-ddTHH:mm:ss";
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MIN_PAGE_SIZE = 1;
    public static final int MAX_PAGE_SIZE = 250;

    private String searchTerm;
    @JsonIgnore
    private String developerIdString;
    private Long developerId;
    @JsonIgnore
    @Builder.Default
    private Set<Long> acbIds = new HashSet<Long>();
    @Builder.Default
    private Set<String> currentStatusNames = new HashSet<String>();
    @Builder.Default
    private Set<String> typeNames = new HashSet<String>();
    private String currentStatusChangeDateTimeStart;
    private String currentStatusChangeDateTimeEnd;
    private String submittedDateTimeStart;
    private String submittedDateTimeEnd;
    @JsonIgnore
    private String orderByString;
    private OrderByOption orderBy;
    @Builder.Default
    private Boolean sortDescending = false;
    @JsonIgnore
    private String pageNumberString;
    @Builder.Default
    private Integer pageNumber = 0;
    @JsonIgnore
    private String pageSizeString;
    @Builder.Default
    private Integer pageSize = DEFAULT_PAGE_SIZE;
}
