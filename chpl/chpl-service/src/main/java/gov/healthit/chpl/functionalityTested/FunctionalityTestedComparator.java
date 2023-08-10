package gov.healthit.chpl.functionalityTested;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class FunctionalityTestedComparator implements Comparator<FunctionalityTested> {

    @Override
    public int compare(FunctionalityTested func1, FunctionalityTested func2) {
        if (!StringUtils.isEmpty(func1.getValue()) && !StringUtils.isEmpty(func2.getValue())) {
            return func1.getValue().compareTo(func2.getValue());
        } else if (func1.getId() != null && func2.getId() != null) {
            return func1.getId().compareTo(func2.getId());
        }
        return 0;
    }
}
