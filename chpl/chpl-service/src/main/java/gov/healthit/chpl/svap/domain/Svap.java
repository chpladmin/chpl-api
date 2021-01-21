package gov.healthit.chpl.svap.domain;

import gov.healthit.chpl.svap.entity.SvapEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Svap {

    private Long svapId;
    private String regulatoryTextCitation;
    private String approvedStandardVersion;
    private boolean replaced;

    public Svap(SvapEntity entity) {
        this.svapId = entity.getSvapId();
        this.regulatoryTextCitation = entity.getRegulatoryTextCitation();
        this.approvedStandardVersion = entity.getApprovedStandardVersion();
        this.replaced = entity.getReplaced();
    }
}
