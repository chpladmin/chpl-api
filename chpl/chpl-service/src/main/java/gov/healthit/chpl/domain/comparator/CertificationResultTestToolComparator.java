package gov.healthit.chpl.domain.comparator;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.domain.CertificationResultTestTool;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CertificationResultTestToolComparator implements Comparator<CertificationResultTestTool> {

    @Override
    public int compare(CertificationResultTestTool tt1, CertificationResultTestTool tt2) {
        if (!StringUtils.isEmpty(tt1.getTestTool().getValue())
                && !StringUtils.isEmpty(tt2.getTestTool().getValue())) {
            return tt1.getTestTool().getValue().compareTo(tt2.getTestTool().getValue());
        } else if (tt1.getTestTool().getId() != null && tt2.getTestTool().getId() != null) {
            return tt1.getTestTool().getId().compareTo(tt2.getTestTool().getId());
        }
        return 0;
    }
}
