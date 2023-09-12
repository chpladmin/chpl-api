package gov.healthit.chpl.testtool;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TestToolComparator implements Comparator<TestTool> {

    @Override
    public int compare(TestTool tt1, TestTool tt2) {
        if (!StringUtils.isEmpty(tt1.getName()) && !StringUtils.isEmpty(tt2.getName())) {
            return tt1.getName().compareTo(tt2.getName());
        } else if (tt1.getId() != null && tt2.getId() != null) {
            return tt1.getId().compareTo(tt2.getId());
        }
        return 0;
    }
}
