package gov.healthit.chpl.certificationCriteria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@ToString
public class CertificationCriterionWithAttributes extends CertificationCriterion {
    private static final long serialVersionUID = 5732322243571111895L;

    private AllowedAttributes attributes;

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static final class AllowedAttributes {
        private boolean gap;
        private boolean privacySecurityFramework;
        private boolean conformanceMethod;
        private boolean optionalStandard;
        private boolean standardsTested;
        private boolean functionalityTested;
        private boolean apiDocumentation;
        private boolean exportDocumentation;
        private boolean documentationUrl;
        private boolean useCases;
        private boolean g1Success;
        private boolean g2Success;
        private boolean attestationAnswer;
        private boolean additionalSoftware;
        private boolean testTool;
        private boolean testProcedure;
        private boolean testData;
        private boolean sed;
        private boolean serviceBaseUrlList;
        private boolean svap;
        private boolean standard;
    }
}
