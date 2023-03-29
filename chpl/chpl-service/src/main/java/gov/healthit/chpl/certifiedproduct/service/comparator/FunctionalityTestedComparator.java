package gov.healthit.chpl.certifiedproduct.service.comparator;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.functionalityTested.FunctionalityTested;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class FunctionalityTestedComparator implements Comparator<FunctionalityTested> {

    @Override
    public int compare(FunctionalityTested func1, FunctionalityTested func2) {
        if (!StringUtils.isEmpty(func1.getName()) && !StringUtils.isEmpty(func2.getName())) {
            return func1.getName().compareTo(func2.getName());
        } else if (func1.getId() != null && func2.getId() != null) {
            return func1.getId().compareTo(func2.getId());
        }
        return 0;
    }
}
