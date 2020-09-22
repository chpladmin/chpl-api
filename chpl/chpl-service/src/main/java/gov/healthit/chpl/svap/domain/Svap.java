package gov.healthit.chpl.svap.domain;

import gov.healthit.chpl.svap.entity.SvapEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Svap {

    private Long svaplId;
    private String regulatoryTextCitation;
    private String approvedStandardVersion;

    public Svap(SvapEntity entity) {
        this.svaplId = entity.getSvaplId();
        this.regulatoryTextCitation = entity.getRegulatoryTextCitation();
        this.approvedStandardVersion = entity.getApprovedStandardVersion();
    }
}
