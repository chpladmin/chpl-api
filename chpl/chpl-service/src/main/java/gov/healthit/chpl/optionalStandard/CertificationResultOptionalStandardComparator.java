package gov.healthit.chpl.optionalStandard;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.optionalStandard.domain.CertificationResultOptionalStandard;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CertificationResultOptionalStandardComparator implements Serializable, Comparator<CertificationResultOptionalStandard> {

    private static final long serialVersionUID = -1994250530139142187L;

    @Override
    public int compare(CertificationResultOptionalStandard os1, CertificationResultOptionalStandard os2) {
        if (!StringUtils.isEmpty(os1.getCitation()) && !StringUtils.isEmpty(os2.getCitation())) {
            return os1.getCitation().compareTo(os2.getCitation());
        } else if (os1.getId() != null && os2.getId() != null) {
            return os1.getId().compareTo(os2.getId());
        }
        return 0;
    }
}
