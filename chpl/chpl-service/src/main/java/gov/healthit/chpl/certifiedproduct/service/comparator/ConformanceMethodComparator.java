package gov.healthit.chpl.certifiedproduct.service.comparator;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethod;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ConformanceMethodComparator implements Comparator<ConformanceMethod> {

    @Override
    public int compare(ConformanceMethod cm1, ConformanceMethod cm2) {
        if (!StringUtils.isEmpty(cm1.getName()) && !StringUtils.isEmpty(cm2.getName())) {
            return cm1.getName().compareTo(cm2.getName());
        } else if (cm1.getId() != null && cm2.getId() != null) {
            return cm1.getId().compareTo(cm2.getId());
        }
        return 0;
    }
}
