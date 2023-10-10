package gov.healthit.chpl.domain.comparator;

import java.util.Comparator;

import org.apache.commons.lang3.builder.CompareToBuilder;

import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CertifiedProductQmsStandardComparator implements Comparator<CertifiedProductQmsStandard> {

    @Override
    public int compare(CertifiedProductQmsStandard qms1, CertifiedProductQmsStandard qms2) {
        return new CompareToBuilder()
                .append(qms1.getQmsStandardName(), qms2.getQmsStandardName())
                .append(qms1.getQmsModification(), qms2.getQmsModification())
                .append(qms1.getApplicableCriteria(), qms2.getApplicableCriteria())
                .append(qms1.getQmsStandardId(), qms2.getQmsStandardId())
                .toComparison();
    }
}
