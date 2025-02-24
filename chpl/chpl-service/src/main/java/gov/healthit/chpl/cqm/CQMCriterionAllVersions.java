package gov.healthit.chpl.cqm;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CQMCriterionAllVersions implements Serializable, Comparable<CQMCriterionAllVersions> {
    private static final long serialVersionUID = -4748525240792675076L;

    private String cmsId;
    private String nqfNumber;
    private String domain;

    //Note that the description is for the highest-numbered version. Other versions may have different descriptions.
    private String description;

    //Note that the title is for the highest-numbered version. Other versions may have different titles.
    private String title;

    private List<String> versions;

    @Override
    public int compareTo(CQMCriterionAllVersions other) {
        return this.getCmsId().compareTo(other.getCmsId());
    }
}
