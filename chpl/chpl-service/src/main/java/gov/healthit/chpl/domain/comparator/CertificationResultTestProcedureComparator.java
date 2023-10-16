package gov.healthit.chpl.domain.comparator;

import java.util.Comparator;

import org.apache.commons.lang3.builder.CompareToBuilder;

import gov.healthit.chpl.domain.CertificationResultTestProcedure;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CertificationResultTestProcedureComparator implements Comparator<CertificationResultTestProcedure> {

    @Override
    public int compare(CertificationResultTestProcedure tp1, CertificationResultTestProcedure tp2) {
        return new CompareToBuilder()
                .append(tp1.getTestProcedure().getId(), tp2.getTestProcedure().getId())
                .append(tp1.getTestProcedureVersion(), tp2.getTestProcedureVersion())
                .append(tp1.getId(), tp2.getId())
                .toComparison();
    }
}
