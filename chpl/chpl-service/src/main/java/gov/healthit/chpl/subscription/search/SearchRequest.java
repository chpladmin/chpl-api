package gov.healthit.chpl.subscription.search;

import java.io.Serializable;
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
    private static final long serialVersionUID = 11792092167811580L;
    public static final String TIMESTAMP_SEARCH_FORMAT = "yyyy-MM-ddTHH:mm:ss";
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    //search term could be a subscriber email address, a developer name, product name, or CHPL Product Number
    private String searchTerm;
    private Set<String> subscriptionSubjects;
    private Set<String> subscriptionObjectTypes;
    private Set<String> subscriberRoles;
    private Set<String> subscriberStatuses;
    private String creationDateTimeStart;
    private String creationDateTimeEnd;

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
