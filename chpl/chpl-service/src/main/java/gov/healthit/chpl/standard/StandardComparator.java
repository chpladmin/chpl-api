package gov.healthit.chpl.standard;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class StandardComparator implements Comparator<Standard> {
    @Override
    public int compare(Standard std1, Standard std2) {
        if (!StringUtils.isEmpty(std1.getValue()) && !StringUtils.isEmpty(std2.getValue())) {
            return std1.getValue().compareTo(std2.getValue());
        } else if (std1.getId() != null && std2.getId() != null) {
            return std1.getId().compareTo(std2.getId());
        }
        return 0;
    }

}
