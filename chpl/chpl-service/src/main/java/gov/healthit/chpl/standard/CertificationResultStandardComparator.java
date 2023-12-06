package gov.healthit.chpl.standard;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

public class CertificationResultStandardComparator implements Comparator<CertificationResultStandard> {
    @Override
    public int compare(CertificationResultStandard standard1, CertificationResultStandard standard2) {
        if (!StringUtils.isEmpty(standard1.getStandard().getValue()) && !StringUtils.isEmpty(standard2.getStandard().getValue())) {
            return standard1.getStandard().getValue().compareTo(standard2.getStandard().getValue());
        } else if (standard1.getStandard().getId() != null && standard2.getStandard().getId() != null) {
            return standard1.getStandard().getId().compareTo(standard2.getStandard().getId());
        }
        return 0;
    }
}
