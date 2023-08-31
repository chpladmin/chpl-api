package gov.healthit.chpl.functionalitytested;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CertificationResultFunctionalityTestedComparator implements Comparator<CertificationResultFunctionalityTested> {

    @Override
    public int compare(CertificationResultFunctionalityTested func1, CertificationResultFunctionalityTested func2) {
        if (!StringUtils.isEmpty(func1.getFunctionalityTested().getValue()) && !StringUtils.isEmpty(func2.getFunctionalityTested().getValue())) {
            return func1.getFunctionalityTested().getValue().compareTo(func2.getFunctionalityTested().getValue());
        } else if (func1.getFunctionalityTested().getId() != null && func2.getFunctionalityTested().getId() != null) {
            return func1.getFunctionalityTested().getId().compareTo(func2.getFunctionalityTested().getId());
        }
        return 0;
    }
}
