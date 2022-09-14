package gov.healthit.chpl.developer.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeveloperSearchResponse implements Serializable {
    private static final long serialVersionUID = 5130424471765198329L;
    private Integer recordCount;
    private Integer pageSize;
    private Integer pageNumber;
    private List<DeveloperSearchResult> results = new ArrayList<DeveloperSearchResult>();
}
