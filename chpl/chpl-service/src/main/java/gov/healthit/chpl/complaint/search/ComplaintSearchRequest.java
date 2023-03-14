package gov.healthit.chpl.complaint.search;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import gov.healthit.chpl.util.CommaDelimitedStringToSetOfStrings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ComplaintSearchRequest implements Serializable {
    private static final long serialVersionUID = 1816207628667101580L;
    public static final String DATE_SEARCH_FORMAT = "yyyy-MM-dd";
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MIN_PAGE_SIZE = 1;
    public static final int MAX_PAGE_SIZE = 250;

    //ONC-ACB Complaint ID, ONC Complaint ID, Associated Certified Product, or Associated Criteria
    private String searchTerm;
    @JsonIgnore
    @Builder.Default
    private Set<String> informedOncStrings = new HashSet<String>();
    @JsonDeserialize(using = CommaDelimitedStringToSetOfStrings.class)
    @Builder.Default
    private Set<Boolean> informedOnc = new HashSet<Boolean>();
    @JsonIgnore
    @Builder.Default
    private Set<String> oncAtlContactedStrings = new HashSet<String>();
    @JsonDeserialize(using = CommaDelimitedStringToSetOfStrings.class)
    @Builder.Default
    private Set<Boolean> oncAtlContacted = new HashSet<Boolean>();
    @JsonIgnore
    @Builder.Default
    private Set<String> complainantContactedStrings = new HashSet<String>();
    @JsonDeserialize(using = CommaDelimitedStringToSetOfStrings.class)
    @Builder.Default
    private Set<Boolean> complainantContacted = new HashSet<Boolean>();
    @JsonIgnore
    @Builder.Default
    private Set<String> developerContactedStrings = new HashSet<String>();
    @JsonDeserialize(using = CommaDelimitedStringToSetOfStrings.class)
    @Builder.Default
    private Set<Boolean> developerContacted = new HashSet<Boolean>();
    @JsonDeserialize(using = CommaDelimitedStringToSetOfStrings.class)
    @Builder.Default
    private Set<String> certificationBodyNames = new HashSet<String>();
    @JsonIgnore
    @Builder.Default
    private Set<Long> acbIds = new HashSet<Long>();
    @JsonDeserialize(using = CommaDelimitedStringToSetOfStrings.class)
    @Builder.Default
    private Set<String> complainantTypeNames = new HashSet<String>();
    @JsonDeserialize(using = CommaDelimitedStringToSetOfStrings.class)
    @Builder.Default
    private Set<String> currentStatusNames = new HashSet<String>();
    @JsonDeserialize(using = CommaDelimitedStringToSetOfStrings.class)
    @Builder.Default
    private Set<String> listingIdStrings = new HashSet<String>();
    @JsonIgnore
    @Builder.Default
    private Set<Long> listingIds = new HashSet<Long>();
    @JsonDeserialize(using = CommaDelimitedStringToSetOfStrings.class)
    @Builder.Default
    private Set<String> surveillanceIdStrings = new HashSet<String>();
    @JsonIgnore
    @Builder.Default
    private Set<Long> surveillanceIds = new HashSet<Long>();
    @JsonDeserialize(using = CommaDelimitedStringToSetOfStrings.class)
    @Builder.Default
    private Set<String> certificationCriteriaIdStrings = new HashSet<String>();
    @JsonIgnore
    @Builder.Default
    private Set<Long> certificationCriteriaIds = new HashSet<Long>();
    private String closedDateStart;
    private String closedDateEnd;
    private String receivedDateStart;
    private String receivedDateEnd;
    private String openDuringRangeStart;
    private String openDuringRangeEnd;
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
