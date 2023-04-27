package gov.healthit.chpl.functionalityTested;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CertificationResultFunctionalityTestedComparator implements Comparator<CertificationResultFunctionalityTested> {

    @Override
    public int compare(CertificationResultFunctionalityTested func1, CertificationResultFunctionalityTested func2) {
        if (!StringUtils.isEmpty(func1.getName()) && !StringUtils.isEmpty(func2.getName())) {
            return func1.getName().compareTo(func2.getName());
        } else if (func1.getFunctionalityTestedId() != null && func2.getFunctionalityTestedId() != null) {
            return func1.getFunctionalityTestedId().compareTo(func2.getFunctionalityTestedId());
        }
        return 0;
    }
}
