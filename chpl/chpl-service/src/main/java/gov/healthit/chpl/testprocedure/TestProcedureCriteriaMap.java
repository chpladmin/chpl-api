package gov.healthit.chpl.testprocedure;

import java.io.Serializable;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestProcedureCriteriaMap implements Serializable {
    private static final long serialVersionUID = -1863384989196377463L;
    private Long id;
    private Long criteriaId;
    private CertificationCriterion criteria;
    private Long testProcedureId;
    private TestProcedure testProcedure;
}
