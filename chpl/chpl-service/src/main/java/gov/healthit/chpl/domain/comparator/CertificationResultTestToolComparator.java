package gov.healthit.chpl.domain.comparator;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.domain.CertificationResultTestTool;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CertificationResultTestToolComparator implements Comparator<CertificationResultTestTool> {

    @Override
    public int compare(CertificationResultTestTool tt1, CertificationResultTestTool tt2) {
        if (!StringUtils.isEmpty(tt1.getTestToolName())
                && !StringUtils.isEmpty(tt2.getTestToolName())) {
            return tt1.getTestToolName().compareTo(tt2.getTestToolName());
        } else if (tt1.getTestToolId() != null && tt2.getTestToolId() != null) {
            return tt1.getTestToolId().compareTo(tt2.getTestToolId());
        }
        return 0;
    }
}
