package gov.healthit.chpl.search.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListingSearchResponse implements Serializable {
    private static final long serialVersionUID = 5130424471765198329L;
    @Schema(description = "The total number of records in the system matching all filters in the search request.")
    private Integer recordCount;
    @Schema(description = "The total number of records in the system matching the 'searchTerm' filter only. "
            + "If the search request has no 'searchTerm' filter supplied this value is not applicable to the response and will be null.")
    private Integer searchTermRecordCount;
    @Schema(description = "The maximum number of records returned for this page of search results.")
    private Integer pageSize;
    @Schema(description = "The page number for this set of search results, 0-based.")
    private Integer pageNumber;
    @Schema(description = "Whether or not direct reviews are available. "
            + "This dictates whether non-conformity filters can be expected to have correct results.")
    private Boolean directReviewsAvailable;
    @Schema(description = "The set of records matching all filters and paging parameters in the search request.")
    private List<ListingSearchResult> results = new ArrayList<ListingSearchResult>();
}
