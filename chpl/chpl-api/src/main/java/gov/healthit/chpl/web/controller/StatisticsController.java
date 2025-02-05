package gov.healthit.chpl.web.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.annotation.DeprecatedApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;

@Deprecated
@Tag(name = "statistics", description = "Gets statistics.")
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/statistics/nonconformity_criteria_count",
            message = "This endpoint is deprecated and will be removed in a future release. Please use /report-data/non-conformities/types as a replacement.",
            removalDate = "2025-07-01")
    @Operation(summary = "Get count of non-conformities by criteria.",
            description = "Retrieves and returns the counts.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/nonconformity_criteria_count", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public NonconformityTypeStatisticsResult getNonconformityCountByCriteria() {
        NonconformityTypeStatisticsResult response = new NonconformityTypeStatisticsResult();
        return response;
    }

    @Deprecated
    private static class NonconformityTypeStatisticsResult {

        private List<NonconformityTypeStatistics> nonconformityStatisticsResult;

        public NonconformityTypeStatisticsResult() {
            this.nonconformityStatisticsResult = new ArrayList<NonconformityTypeStatistics>();
        }

        public List<NonconformityTypeStatistics> getNonconformityStatisticsResult() {
            return nonconformityStatisticsResult;
        }

        public void setNonconformityStatisticsResult(
                final List<NonconformityTypeStatistics> nonconformityStatisticsResult) {
            this.nonconformityStatisticsResult = nonconformityStatisticsResult;
        }
    }

    @Deprecated
    @Data
    private static class NonconformityTypeStatistics {
        private Long id;
        private Long nonconformityCount;
        private String nonconformityType;
        private CertificationCriterion criterion;
        private Boolean deleted;
        private Long lastModifiedUser;
        private Date creationDate;
        private Date lastModifiedDate;
    }
}
