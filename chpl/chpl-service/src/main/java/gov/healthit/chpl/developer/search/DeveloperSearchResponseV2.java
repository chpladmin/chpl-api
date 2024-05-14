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
@Deprecated
public class DeveloperSearchResponseV2 implements Serializable {
    private static final long serialVersionUID = 5130901471765198329L;
    private Integer recordCount;
    private Integer pageSize;
    private Integer pageNumber;
    private List<DeveloperSearchResultV2> results = new ArrayList<DeveloperSearchResultV2>();
}
