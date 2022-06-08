package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRequestSearchResponse implements Serializable {
    private static final long serialVersionUID = 5130424476125198329L;
    private Integer recordCount;
    private Integer pageSize;
    private Integer pageNumber;
    private List<ChangeRequestSearchResult> results = new ArrayList<ChangeRequestSearchResult>();
}
