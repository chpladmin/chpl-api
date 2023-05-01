package gov.healthit.chpl.svap.domain;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.svap.entity.SvapEntity;
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

    public Svap(SvapEntity entity) {
        this.svapId = entity.getSvapId();
        this.regulatoryTextCitation = entity.getRegulatoryTextCitation();
        this.approvedStandardVersion = entity.getApprovedStandardVersion();
        this.replaced = entity.getReplaced();
        this.criteria = entity.getCriteria().stream()
                .map(crit -> crit.toDomain())
                .collect(Collectors.toList());
    }
}
