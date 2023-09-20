package gov.healthit.chpl.svap.domain;

import java.io.Serializable;
import java.util.List;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Svap implements Serializable {
    private static final long serialVersionUID = -346593579303353915L;

    private Long svapId;
    private String regulatoryTextCitation;
    private String approvedStandardVersion;
    private boolean replaced;

    @Singular(value = "criterion")
    private List<CertificationCriterion> criteria;
}
