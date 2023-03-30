package gov.healthit.chpl.domain.comparator;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.domain.CertificationResultTestStandard;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CertificationResultTestStandardComparator implements Comparator<CertificationResultTestStandard> {

    @Override
    public int compare(CertificationResultTestStandard ts1, CertificationResultTestStandard ts2) {
        if (!StringUtils.isEmpty(ts1.getTestStandardName())
                && !StringUtils.isEmpty(ts2.getTestStandardName())) {
            return ts1.getTestStandardName().compareTo(ts2.getTestStandardName());
        } else if (ts1.getTestStandardId() != null && ts2.getTestStandardId() != null) {
            return ts1.getTestStandardId().compareTo(ts2.getTestStandardId());
        }
        return 0;
    }
}
