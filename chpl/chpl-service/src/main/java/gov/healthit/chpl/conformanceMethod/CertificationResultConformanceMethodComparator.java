package gov.healthit.chpl.conformanceMethod;

import java.util.Comparator;

import org.apache.commons.lang3.builder.CompareToBuilder;

import gov.healthit.chpl.conformanceMethod.domain.CertificationResultConformanceMethod;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CertificationResultConformanceMethodComparator implements Comparator<CertificationResultConformanceMethod> {

    @Override
    public int compare(CertificationResultConformanceMethod cm1, CertificationResultConformanceMethod cm2) {
        return new CompareToBuilder()
            .append(cm1.getConformanceMethod().getId(), cm2.getConformanceMethod().getId())
            .append(cm1.getConformanceMethodVersion(), cm2.getConformanceMethodVersion())
            .append(cm1.getId(), cm2.getId())
            .toComparison();
    }
}
