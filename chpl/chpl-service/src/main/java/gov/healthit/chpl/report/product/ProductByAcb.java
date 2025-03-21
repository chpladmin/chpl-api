package gov.healthit.chpl.report.product;

import gov.healthit.chpl.domain.IdNamePair;
import gov.healthit.chpl.search.domain.ListingSearchResult.DeveloperSearchResult;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class ProductByAcb {
    private IdNamePair product;
    private IdNamePair acb;
    private DeveloperSearchResult developer;
}
