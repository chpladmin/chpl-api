package gov.healthit.chpl.domain.comparator;

import java.util.Comparator;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.domain.CertificationResultTestData;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CertificationResultTestDataComparator implements Comparator<CertificationResultTestData> {

    @Override
    public int compare(CertificationResultTestData td1, CertificationResultTestData td2) {
        if (ObjectUtils.allNotNull(td1.getTestData(), td2.getTestData())
                && !StringUtils.isEmpty(td1.getTestData().getName())
                && !StringUtils.isEmpty(td2.getTestData().getName())) {
            return td1.getTestData().getName().compareTo(td2.getTestData().getName());
        } else if (td1.getId() != null && td2.getId() != null) {
            return td1.getId().compareTo(td2.getId());
        }
        return 0;
    }
}
