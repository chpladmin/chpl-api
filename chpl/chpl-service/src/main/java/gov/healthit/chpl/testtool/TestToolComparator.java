package gov.healthit.chpl.testtool;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TestToolComparator implements Comparator<TestTool> {

    @Override
    public int compare(TestTool tt1, TestTool tt2) {
        if (!StringUtils.isEmpty(tt1.getValue()) && !StringUtils.isEmpty(tt2.getValue())) {
            return tt1.getValue().compareTo(tt2.getValue());
        } else if (tt1.getId() != null && tt2.getId() != null) {
            return tt1.getId().compareTo(tt2.getId());
        }
        return 0;
    }
}
