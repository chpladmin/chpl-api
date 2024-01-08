package gov.healthit.chpl.subscription.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionSearchResponse implements Serializable {
    private static final long serialVersionUID = 513041021765198329L;
    private Integer recordCount;
    private Integer pageSize;
    private Integer pageNumber;
    private List<SubscriptionSearchResult> results = new ArrayList<SubscriptionSearchResult>();
}
