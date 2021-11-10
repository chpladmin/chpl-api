package gov.healthit.chpl.scheduler.job.onetime.teststandardconversion;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.TestStandard;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandard;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
public class TestStandard2OptionalStandardsMapping {
    @Setter(AccessLevel.NONE)
    private Pair<CertificationCriterion, TestStandard> key;
    private CertificationCriterion criterion;
    private TestStandard testStandard;
    private List<OptionalStandard> optionalStandards;

    public TestStandard2OptionalStandardsMapping(CertificationCriterion criterion, TestStandard testStandard, List<OptionalStandard> optionalStandards) {
        this.criterion = criterion;
        this.testStandard = testStandard;
        this.optionalStandards = optionalStandards;

        this.key = new ImmutablePair<>(criterion, testStandard);
    }

    @Override
    public String toString() {
        return String.format("%s %s | %s | {%s}",
                criterion.getNumber(),
                criterion.getTitle(),
                testStandard.getName(),
                optionalStandards.stream()
                        .map(os -> os.getCitation())
                        .collect(Collectors.joining(",")));
    }
}
