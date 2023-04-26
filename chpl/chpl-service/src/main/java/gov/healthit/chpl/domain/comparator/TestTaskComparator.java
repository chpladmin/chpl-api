package gov.healthit.chpl.domain.comparator;

import java.util.Comparator;

import gov.healthit.chpl.domain.TestTask;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TestTaskComparator implements Comparator<TestTask> {
    @Override
    public int compare(TestTask tt1, TestTask tt2) {
        if (tt1.getId() != null && tt2.getId() != null) {
            return tt1.getId().compareTo(tt2.getId());
        }
        return 0;
    }
}
