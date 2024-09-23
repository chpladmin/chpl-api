package gov.healthit.chpl.activity.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivitySearchResponse implements Serializable {
    private static final long serialVersionUID = 5304244711725198329L;
    private Long recordCount;
    private Integer pageSize;
    private Integer pageNumber;
    private List<ActivitySearchResult> results = new ArrayList<ActivitySearchResult>();
}
