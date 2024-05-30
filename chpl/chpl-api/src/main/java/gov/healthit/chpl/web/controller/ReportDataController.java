package gov.healthit.chpl.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.report.ReportDataManager;
import gov.healthit.chpl.report.criteriamigrationreport.CriteriaMigrationReport;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "report-data", description = "Allows retrieval of data used by reports.")
@RestController
@RequestMapping("/report-data")
public class ReportDataController {

    private ReportDataManager reportDataManager;

    @Autowired
    public ReportDataController(ReportDataManager reportDataManager) {
        this.reportDataManager = reportDataManager;
    }

    @Operation(summary = "",
            description = "",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/cures-criteria-migration-report", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody CriteriaMigrationReport getCuresCriteriaMigrationReport() {
        return reportDataManager.getCriteriaMigrationReport();
    }
}
