package gov.healthit.chpl.testtool;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.lang3.builder.CompareToBuilder;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CertificationResultTestToolComparator implements Serializable, Comparator<CertificationResultTestTool> {

    private static final long serialVersionUID = -1538957930853930928L;

    @Override
    public int compare(CertificationResultTestTool tt1, CertificationResultTestTool tt2) {
        return new CompareToBuilder()
                .append(tt1.getTestTool().getValue(), tt2.getTestTool().getValue())
                .append(tt1.getVersion(), tt2.getVersion())
                .append(tt1.getId(), tt2.getId())
                .toComparison();
    }
}
