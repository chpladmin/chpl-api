package gov.healthit.chpl.certifiedproduct.service.comparator;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.domain.CertifiedProductTargetedUser;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CertifiedProductTargetedUserComparator implements Comparator<CertifiedProductTargetedUser> {

    @Override
    public int compare(CertifiedProductTargetedUser tu1, CertifiedProductTargetedUser tu2) {
        if (!StringUtils.isEmpty(tu1.getTargetedUserName()) && !StringUtils.isEmpty(tu2.getTargetedUserName())) {
            return tu1.getTargetedUserName().compareTo(tu2.getTargetedUserName());
        } else if (tu1.getTargetedUserId() != null && tu2.getTargetedUserId() != null) {
            return tu1.getTargetedUserId().compareTo(tu2.getTargetedUserId());
        }
        return 0;
    }
}
