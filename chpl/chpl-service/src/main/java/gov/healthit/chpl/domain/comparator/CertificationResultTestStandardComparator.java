package gov.healthit.chpl.domain.comparator;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.lang3.builder.CompareToBuilder;

import gov.healthit.chpl.domain.CertificationResultTestStandard;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CertificationResultTestStandardComparator implements Serializable, Comparator<CertificationResultTestStandard> {

    private static final long serialVersionUID = 6135388873793519271L;

    @Override
    public int compare(CertificationResultTestStandard ts1, CertificationResultTestStandard ts2) {
        return new CompareToBuilder()
                .append(ts1.getTestStandardId(), ts2.getTestStandardId())
                .append(ts1.getId(), ts2.getId())
                .toComparison();
    }
}
