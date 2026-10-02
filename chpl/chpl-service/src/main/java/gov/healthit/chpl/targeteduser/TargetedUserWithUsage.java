package gov.healthit.chpl.targeteduser;

import java.io.Serializable;
import java.util.List;

import gov.healthit.chpl.entity.CertificationStatusType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TargetedUserWithUsage implements Serializable {
    private static final long serialVersionUID = 5819005018143800705L;
    private Long id;
    private String name;
    private List<UsageByCertificationStatus> usage;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static final class UsageByCertificationStatus {
        private CertificationStatusType certificationStatus;
        private Long listingCount;
    }
}
