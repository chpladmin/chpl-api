package gov.healthit.chpl.domain.comparator;

import java.util.Comparator;

import gov.healthit.chpl.domain.TestParticipant;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TestParticipantComparator implements Comparator<TestParticipant> {
    @Override
    public int compare(TestParticipant tp1, TestParticipant tp2) {
        if (tp1.getId() != null && tp2.getId() != null) {
            return tp1.getId().compareTo(tp2.getId());
        }
        return 0;
    }
}
