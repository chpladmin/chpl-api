package gov.healthit.chpl.domain.comparator;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.domain.CertificationResultTestTool;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CertificationResultTestToolComparator implements Comparator<CertificationResultTestTool> {

    @Override
    public int compare(CertificationResultTestTool tt1, CertificationResultTestTool tt2) {
        if (!StringUtils.isEmpty(tt1.getValue())
                && !StringUtils.isEmpty(tt2.getValue())) {
            return tt1.getValue().compareTo(tt2.getValue());
        } else if (tt1.getTestToolId() != null && tt2.getTestToolId() != null) {
            return tt1.getTestToolId().compareTo(tt2.getTestToolId());
        }
        return 0;
    }
}
