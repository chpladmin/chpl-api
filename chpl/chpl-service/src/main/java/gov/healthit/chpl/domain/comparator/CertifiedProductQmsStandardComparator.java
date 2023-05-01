package gov.healthit.chpl.domain.comparator;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CertifiedProductQmsStandardComparator implements Comparator<CertifiedProductQmsStandard> {

    @Override
    public int compare(CertifiedProductQmsStandard qms1, CertifiedProductQmsStandard qms2) {
        if (!StringUtils.isEmpty(qms1.getQmsStandardName()) && !StringUtils.isEmpty(qms2.getQmsStandardName())) {
            return qms1.getQmsStandardName().compareTo(qms2.getQmsStandardName());
        } else if (qms1.getQmsStandardId() != null && qms2.getQmsStandardId() != null) {
            return qms1.getQmsStandardId().compareTo(qms2.getQmsStandardId());
        }
        return 0;
    }
}
