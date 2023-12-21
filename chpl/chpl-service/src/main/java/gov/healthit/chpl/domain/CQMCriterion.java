package gov.healthit.chpl.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CQMCriterion implements Serializable {
    private static final long serialVersionUID = -1847517952030827806L;
    private Long criterionId;
    private String cmsId;
    private Long cqmCriterionTypeId;
    private String cqmDomain;
    private Long cqmVersionId;
    private String cqmVersion;
    private String description;
    private String nqfNumber;
    private String number;
    private String title;
}
