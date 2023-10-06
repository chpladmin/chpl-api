package gov.healthit.chpl.domain.comparator;

import java.util.Comparator;

import org.apache.commons.lang3.builder.CompareToBuilder;

import gov.healthit.chpl.domain.CertificationResultTestData;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CertificationResultTestDataComparator implements Comparator<CertificationResultTestData> {

    @Override
    public int compare(CertificationResultTestData td1, CertificationResultTestData td2) {
        return new CompareToBuilder()
                .append(td1.getTestData().getName(), td2.getTestData().getName())
                .append(td1.getVersion(), td2.getVersion())
                .append(td1.getAlteration(), td2.getAlteration())
                .append(td1.getId(), td2.getId())
                .toComparison();
    }
}
