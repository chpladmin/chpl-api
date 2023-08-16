package gov.healthit.chpl.criteriaattribute.testtool;

import java.util.Comparator;

import org.apache.commons.lang3.builder.CompareToBuilder;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CertificationResultTestToolComparator implements Comparator<CertificationResultTestTool> {

    @Override
    public int compare(CertificationResultTestTool tt1, CertificationResultTestTool tt2) {
        return new CompareToBuilder()
                .append(tt1.getTestTool().getValue(), tt2.getTestTool().getValue())
                .append(tt1.getVersion(), tt2.getVersion())
                .toComparison();
    }
}
