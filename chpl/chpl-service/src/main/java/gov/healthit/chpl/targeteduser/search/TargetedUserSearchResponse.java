package gov.healthit.chpl.targeteduser.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.targeteduser.TargetedUserWithUsage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TargetedUserSearchResponse implements Serializable {
    private static final long serialVersionUID = 4130414472721108329L;
    private Integer recordCount;
    private Integer pageSize;
    private Integer pageNumber;
    private List<TargetedUserWithUsage> results = new ArrayList<TargetedUserWithUsage>();
}
