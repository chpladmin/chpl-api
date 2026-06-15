package gov.healthit.chpl.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.report.ReportDataManager;
import gov.healthit.chpl.report.ReportMetadata;
import gov.healthit.chpl.util.LogMethodUsage;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.annotation.DeprecatedApiResponseFields;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Tag(name = "report-metadata",
    description = "Allows retrieval of report metadata, such as the URL to a particular report.")
@RestController
@RequestMapping("/report-metadata")
public class ReportMetadataController {
    private ReportDataManager reportDataManager;

    @Autowired
    public ReportMetadataController(ReportDataManager reportDataManager) {
        this.reportDataManager = reportDataManager;
    }

    @Operation(summary = "Retrieves the report metadata for all reports this user is allowed to see.",
            description = "Retrieves the report metadata for all reports this user is alloewd to see.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
    })
    @LogMethodUsage
    @DeprecatedApiResponseFields(friendlyUrl = "/report-metadata}", httpMethod = "GET", responseClass = ReportMetadata.class)
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<ReportMetadata> getReportMetadataForUser() {
        return reportDataManager.getReportMetadata(null);
    }

    @Operation(summary = "Retrieves the report metadata for all reports this user is allowed to see.",
            description = "Retrieves the report metadata for all reports this user is alloewd to see.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
    })
    @LogMethodUsage
    @DeprecatedApiResponseFields(friendlyUrl = "/report-metadata/{reportGroup}", httpMethod = "GET", responseClass = ReportMetadata.class)
    @RequestMapping(value = "/{reportGroup:^[a-zA-Z0-9\\-]+$}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<ReportMetadata> getReportMetadataForUserAndGroup(
            @PathVariable(name = "reportGroup", required = true) String reportGroup) {
        return reportDataManager.getReportMetadata(reportGroup);
    }
}
