package gov.healthit.chpl.search.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@XmlRootElement(name = "searchResults")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListingSearchResponse implements Serializable {
    private static final long serialVersionUID = 5130424471765198329L;
    private Integer recordCount;
    private Integer pageSize;
    private Integer pageNumber;
    private Boolean directReviewsAvailable;
    private List<ListingSearchResult> results = new ArrayList<ListingSearchResult>();
}
