package gov.healthit.chpl.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.report.ReportDataManager;
import gov.healthit.chpl.report.common.CertificationCriterionWithOrder;
import gov.healthit.chpl.report.criteriaattribute.SvapListingReport;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Tag(name = "report-data/svaps", description = "Allows retrieval of data used by SVAP report.")
@RestController
@RequestMapping("/report-data/svaps")
public class SvapReportController {

    private ReportDataManager reportDataManager;

    @Autowired
    public SvapReportController(ReportDataManager reportDataManager) {
        this.reportDataManager = reportDataManager;
    }

    @Operation(summary = "",
            description = "",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
    })
    @RequestMapping(value = "/listings", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<SvapListingReport> getSvapListingReports() {
        return reportDataManager.getSvapReportService().getSvapListingReports();
    }

    @Operation(summary = "Retrieves the list of Certification Criteria that can be associated with an SVAP.",
            description = "Retrieves the list of Certification Criteria that can be associated with an SVAP.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
    })
    @RequestMapping(value = "/criteria", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CertificationCriterionWithOrder> getCertificationCriteria() {
        return reportDataManager.getSvapReportService().getCertificationCriteria();
    }

}
