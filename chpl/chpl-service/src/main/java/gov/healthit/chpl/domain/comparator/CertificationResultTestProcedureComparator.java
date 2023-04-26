package gov.healthit.chpl.domain.comparator;

import java.util.Comparator;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CertificationResultTestProcedureComparator implements Comparator<CertificationResultTestProcedure> {

    @Override
    public int compare(CertificationResultTestProcedure tp1, CertificationResultTestProcedure tp2) {
        if (ObjectUtils.allNotNull(tp1.getTestProcedure(), tp2.getTestProcedure())
                && !StringUtils.isEmpty(tp1.getTestProcedure().getName())
                && !StringUtils.isEmpty(tp2.getTestProcedure().getName())) {
            return tp1.getTestProcedure().getName().compareTo(tp2.getTestProcedure().getName());
        } else if (tp1.getId() != null && tp2.getId() != null) {
            return tp1.getId().compareTo(tp2.getId());
        }
        return 0;
    }
}
