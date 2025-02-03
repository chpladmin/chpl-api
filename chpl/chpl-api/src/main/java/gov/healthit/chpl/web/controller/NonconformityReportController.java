package gov.healthit.chpl.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.report.ReportDataManager;
import gov.healthit.chpl.report.nonconformity.NonconformityTypeCount;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Tag(name = "report-data/non-conformities", description = "Allows retrieval of data used by non-conformity reports.")
@RestController
@RequestMapping("/report-data/non-conformities")
public class NonconformityReportController {

    private ReportDataManager reportDataManager;

    @Autowired
    public NonconformityReportController(ReportDataManager reportDataManager) {
        this.reportDataManager = reportDataManager;
    }

    @Operation(summary = "Retrieves the data used to generate the Non-Conformity Type report.",
            description = "Retrieves the data used to generate the Non-Conformity Type report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/types", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<NonconformityTypeCount> getNonconformityTypeCounts() {
        return reportDataManager.getNonconformityReportService().getNonconfomrityCounts();
    }
}
