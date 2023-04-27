package gov.healthit.chpl.conformanceMethod;

import java.util.Comparator;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.conformanceMethod.domain.CertificationResultConformanceMethod;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CertificationResultConformanceMethodComparator implements Comparator<CertificationResultConformanceMethod> {

    @Override
    public int compare(CertificationResultConformanceMethod cm1, CertificationResultConformanceMethod cm2) {
        if (ObjectUtils.allNotNull(cm1.getConformanceMethod(), cm2.getConformanceMethod())
                && !StringUtils.isEmpty(cm1.getConformanceMethod().getName())
                && !StringUtils.isEmpty(cm2.getConformanceMethod().getName())) {
            return cm1.getConformanceMethod().getName().compareTo(cm2.getConformanceMethod().getName());
        } else if (cm1.getId() != null && cm2.getId() != null) {
            return cm1.getId().compareTo(cm2.getId());
        }
        return 0;
    }
}
