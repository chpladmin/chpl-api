package gov.healthit.chpl.report.criteriamigrationreport;

import java.time.LocalDate;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CriteriaMigrationReportDenormalized {
    private CertificationCriterion originalCriterion;
    private CertificationCriterion updatedCriterion;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate reportDate;

    private Integer newCertificationCount;
    private Integer upgradedCertificationCount;
    private Integer requiresUpdateCount;
    private Double percentUpdated;
}
