package gov.healthit.chpl.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.developer.search.DeveloperSearchResult;
import gov.healthit.chpl.report.ReportDataManager;
import gov.healthit.chpl.report.attestation.AttestationSubmissionStatistics;
import gov.healthit.chpl.scheduler.job.report.attestation.AttestationReport;
import gov.healthit.chpl.scheduler.job.report.attestation.AttestationReportDeveloper;
import gov.healthit.chpl.util.LogMethodUsage;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "report-data/attestations", description = "Provides reporting data related to attestations")
@RestController
@RequestMapping("/report-data/attestations")
public class AttestationReportDataController {

    private ReportDataManager reportDataManager;

    @Autowired
    public AttestationReportDataController(ReportDataManager reportDataManager) {
        this.reportDataManager = reportDataManager;
    }

    @Operation(summary = "Retrieves the data used to generate the Attestations report.",
            description = "Retrieves the data used to generate the Attestations report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
    })
    @LogMethodUsage
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<AttestationReport> getAttestationReports() {
        return reportDataManager.getAttestationReportService().getAttestationReports();
    }

    @Operation(summary = "Retrieves the data used to generate the Attestations report.",
            description = "Retrieves the data used to generate the Attestations report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
    })
    @LogMethodUsage
    @RequestMapping(value = "/developers", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<AttestationReportDeveloper> getAttestationReportDevelopers() {
        return reportDataManager.getAttestationReportService().getAttestationReportDevelopers();
    }

    @Operation(summary = "Retrieves the data used to generate a report about attestation submission statistics during the current submission window (if any).",
            description = "Retrieves the data used to generate a report about attestation submission statistics during the current submission window (if any).",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
    })
    @LogMethodUsage
    @RequestMapping(value = "/statistics", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody AttestationSubmissionStatistics getAttestationSubmissionStatistics() {
        return reportDataManager.getAttestationReportService().getAttestationSubmissionStatistics();
    }

    @Operation(summary = "Retrieves a list of developers that should be submitting attestations during the current period that have not yet",
            description = "Retrieves a list of developers that should be submitting attestations during the current period that have not yet.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
    })
    @LogMethodUsage
    @RequestMapping(value = "/developers-not-submitted", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<DeveloperSearchResult> getDevelopersNotSubmitted() {
        return reportDataManager.getAttestationReportService().getDevelopersNotSubmitted();
    }

    @Operation(summary = "Retrieves a list of developers who's attesations for the current period have been submitted but not yet published",
            description = "Retrieves a list of developers who's attesations for the current period have been submitted but not yet published",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
    })
    @LogMethodUsage
    @RequestMapping(value = "/developers-not-published", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<DeveloperSearchResult> getDevelopersSubmittedAndNotPublished() {
        return reportDataManager.getAttestationReportService().getDevelopersSubmittedAndNotPublished();
    }
}
