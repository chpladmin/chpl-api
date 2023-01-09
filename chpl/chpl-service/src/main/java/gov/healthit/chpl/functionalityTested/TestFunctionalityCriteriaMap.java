package gov.healthit.chpl.functionalityTested;

import java.util.Date;

import gov.healthit.chpl.domain.CertificationCriterion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestFunctionalityCriteriaMap {
    private Long id;
    private CertificationCriterion criterion;
    private TestFunctionality functionalityTested;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
}
