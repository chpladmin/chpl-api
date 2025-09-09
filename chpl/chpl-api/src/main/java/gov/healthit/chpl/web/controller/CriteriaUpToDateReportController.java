package gov.healthit.chpl.web.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.report.ReportDataManager;
import gov.healthit.chpl.report.criteriauptodate.CriteriaUpToDateReport;
import gov.healthit.chpl.report.criteriauptodate.ListingNotUpToDateReport;
import gov.healthit.chpl.util.LogMethodUsage;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Tag(name = "report-data/criteria-up-to-date",
    description = "Allows retrieval of data about attested criteria being up-to-date.")
@RestController
@RequestMapping("/report-data/criteria-up-to-date")
public class CriteriaUpToDateReportController {
    private CertificationBodyManager acbManager;
    private ReportDataManager reportDataManager;

    @Autowired
    public CriteriaUpToDateReportController(CertificationBodyManager acbManager,
            ReportDataManager reportDataManager) {
        this.acbManager = acbManager;
        this.reportDataManager = reportDataManager;
    }

    @Operation(summary = "Retrieves the data used to generate the Criteria Up-To-Date Report",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CriteriaUpToDateReport> getCriteriaUpToDateReports() {
        List<CertificationBody> allAcbs = acbManager.getAll();
        List<Long> acbIds = allAcbs.stream()
                    .map(acb -> acb.getId())
                    .collect(Collectors.toList());
        return reportDataManager.getCriteriaAttributeUpToDateService().getAllCriteriaUpToDateReports(acbIds);
    }

    @Operation(summary = "Retrieves the data used to generate the Criteria up-to-date counts monthly for the past year",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "/monthly", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CriteriaUpToDateReport> getMonthlyCriteriaUpToDateReports() {        List<CertificationBody> allAcbs = acbManager.getAll();
        List<Long> acbIds = allAcbs.stream()
                .map(acb -> acb.getId())
                .collect(Collectors.toList());
        return reportDataManager.getCriteriaAttributeUpToDateService().getMonthlyCriteriaUpToDateReports(acbIds);
    }

    @Operation(summary = "Retrieves all listing and attested criteria combinations that were not up-to-date on the "
            + "most recent day data was available.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "/listings", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<ListingNotUpToDateReport> getCriteriaUpToDateListings() {
        return reportDataManager.getCriteriaAttributeUpToDateService().getAllListingNotUpToDateReports();
    }
}
