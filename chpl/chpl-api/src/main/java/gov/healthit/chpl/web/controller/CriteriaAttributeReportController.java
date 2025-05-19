package gov.healthit.chpl.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.report.ReportDataManager;
import gov.healthit.chpl.report.criteriaattribute.CodeSetListingReport;
import gov.healthit.chpl.report.criteriaattribute.CodeSetReport;
import gov.healthit.chpl.report.criteriaattribute.ConformanceMethodListingReport;
import gov.healthit.chpl.report.criteriaattribute.ConformanceMethodReport;
import gov.healthit.chpl.report.criteriaattribute.FunctionalityTestedListingReport;
import gov.healthit.chpl.report.criteriaattribute.FunctionalityTestedReport;
import gov.healthit.chpl.report.criteriaattribute.OptionalStandardListingReport;
import gov.healthit.chpl.report.criteriaattribute.OptionalStandardReport;
import gov.healthit.chpl.report.criteriaattribute.PrivacyAndSecurityFrameworkListingReport;
import gov.healthit.chpl.report.criteriaattribute.PrivacyAndSecurityFrameworkReport;
import gov.healthit.chpl.report.criteriaattribute.StandardListingReport;
import gov.healthit.chpl.report.criteriaattribute.StandardReport;
import gov.healthit.chpl.report.criteriaattribute.SvapListingReport;
import gov.healthit.chpl.report.criteriaattribute.SvapReport;
import gov.healthit.chpl.report.criteriaattribute.TestDataListingReport;
import gov.healthit.chpl.report.criteriaattribute.TestDataReport;
import gov.healthit.chpl.report.criteriaattribute.TestToolListingReport;
import gov.healthit.chpl.report.criteriaattribute.TestToolReport;
import gov.healthit.chpl.util.LogMethodUsage;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Tag(name = "report-data/criteria-attributes", description = "Allows retrieval of data used by Criteria Attribute report.")
@RestController
@RequestMapping("/report-data/criteria-attributes")
public class CriteriaAttributeReportController {
    private ReportDataManager reportDataManager;

    @Autowired
    public CriteriaAttributeReportController(ReportDataManager reportDataManager) {
        this.reportDataManager = reportDataManager;
    }

    @Operation(summary = "Retrieves the data used to generate the Test Tool Criteria Attribute Summary report.",
            description = "Retrieves the data used to generate the Test Tool Criteria Attribute Summary report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "/test-tools", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<TestToolReport> getTestToolReports() {
        return reportDataManager.getCriteriaAttributeAttributeService().getTestToolReports();
    }

    @Operation(summary = "Retrieves the data used to generate the Test Tool Criteria Attribute Listing report.",
            description = "Retrieves the data used to generate the Test Tool Criteria Attribute Listing report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "/test-tools/listings", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<TestToolListingReport> getTestToolListingReports() {
        return reportDataManager.getCriteriaAttributeAttributeService().getTestToolListingReports();
    }

    @Operation(summary = "Retrieves the data used to generate the Standard Criteria Attribute Summary report.",
            description = "Retrieves the data used to generate the Standard Criteria Attribute Summary report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "/standards", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<StandardReport> getStandardReports() {
        return reportDataManager.getCriteriaAttributeAttributeService().getStandardReports();
    }

    @Operation(summary = "Retrieves the data used to generate the Standard Criteria Attribute Listing report.",
            description = "Retrieves the data used to generate the Standard Criteria Attribute Listing report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "/standards/listings", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<StandardListingReport> getStandardListingReports() {
        return reportDataManager.getCriteriaAttributeAttributeService().getStandardListingReports();
    }

    @Operation(summary = "Retrieves the data used to generate the Functionality Tested Criteria Attribute Summary report.",
            description = "Retrieves the data used to generate the Functionality Tested Criteria Attribute Summary report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "/functionalities-tested", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<FunctionalityTestedReport> getFunctionalityTestedReports() {
        return reportDataManager.getCriteriaAttributeAttributeService().getFunctionalityTestedReports();
    }

    @Operation(summary = "Retrieves the data used to generate the Functionality Tested Criteria Attribute Listing report.",
            description = "Retrieves the data used to generate the Functionality Tested Criteria Attribute Listing report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "/functionalities-tested/listings", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<FunctionalityTestedListingReport> getFunctionalityTestedListingReports() {
        return reportDataManager.getCriteriaAttributeAttributeService().getFunctionalityTestedListingReports();
    }

    @Operation(summary = "Retrieves the data used to generate the Optional Standard Criteria Attribute Summary report.",
            description = "Retrieves the data used to generate the Optional Standard Criteria Attribute Summary report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "/optional-standards", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<OptionalStandardReport> getOptionalStandardReports() {
        return reportDataManager.getCriteriaAttributeAttributeService().getOptionalStandardReports();
    }

    @Operation(summary = "Retrieves the data used to generate the Optional Standard Criteria Attribute Listing report.",
            description = "Retrieves the data used to generate the Optional Standard Criteria Attribute Listing report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "/optional-standards/listings", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<OptionalStandardListingReport> getOptionalStandardListingReports() {
        return reportDataManager.getCriteriaAttributeAttributeService().getOptionalStandardListingReports();
    }

    @Operation(summary = "Retrieves the data used to generate the Test Data Criteria Attribute Summary report.",
            description = "Retrieves the data used to generate the Test Data Criteria Attribute Summary report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "/test-data", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<TestDataReport> getTestDataReports() {
        return reportDataManager.getCriteriaAttributeAttributeService().getTestDataReports();
    }

    @Operation(summary = "Retrieves the data used to generate the Test Data Criteria Attribute Listing report.",
            description = "Retrieves the data used to generate the Test Data Criteria Attribute Listing report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "/test-data/listings", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<TestDataListingReport> getTestDataListingReports() {
        return reportDataManager.getCriteriaAttributeAttributeService().getTestDataListingReports();
    }

    @Operation(summary = "Retrieves the data used to generate the SVAP Criteria Attribute Summary report.",
            description = "Retrieves the data used to generate the SVAP Criteria Attribute Summary report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "/svaps", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<SvapReport> getSvapReports() {
        return reportDataManager.getCriteriaAttributeAttributeService().getSvapReports();
    }

    @Operation(summary = "Retrieves the data used to generate the SVAP Criteria Attribute Listing report.",
            description = "Retrieves the data used to generate the SVAP Criteria Attribute Listing report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "/svaps/listings", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<SvapListingReport> getSvapListingReports() {
        return reportDataManager.getCriteriaAttributeAttributeService().getSvapListingReports();
    }

    @Operation(summary = "Retrieves the data used to generate the Privacy & Security Framework Criteria Attribute Summary report.",
            description = "Retrieves the data used to generate the Privacy & Security Framework Criteria Attribute Summary report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "/privacy-and-security-frameworks", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<PrivacyAndSecurityFrameworkReport> getPrivacyAndSecurityFrameworkReports() {
        return reportDataManager.getCriteriaAttributeAttributeService().getPrivacyAndSecurityFrameworkReports();
    }

    @Operation(summary = "Retrieves the data used to generate the Privacy & Security Framework Criteria Attribute Listing report.",
            description = "Retrieves the data used to generate the Privacy & Security Framework Criteria Attribute Listing report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "/privacy-and-security-frameworks/listings", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<PrivacyAndSecurityFrameworkListingReport> getPrivacyAndSecurityFrameworkListingReports() {
        return reportDataManager.getCriteriaAttributeAttributeService().getPrivacyAndSecurityFrameworkListingReports();
    }

    @Operation(summary = "Retrieves the data used to generate the Conformance Method Criteria Attribute Summary report.",
            description = "Retrieves the data used to generate the Conformance Method Criteria Attribute Summary report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "/conformance-methods", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<ConformanceMethodReport> getConformanceMethodReports() {
        return reportDataManager.getCriteriaAttributeAttributeService().getConformnceMethodReports();
    }


    @Operation(summary = "Retrieves the data used to generate the Conformance Method Criteria Attribute Listing report.",
            description = "Retrieves the data used to generate the Conformance Method Criteria Attribute Listing report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "/conformance-methods/listings", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<ConformanceMethodListingReport> getConformanceMethodListingReports() {
        return reportDataManager.getCriteriaAttributeAttributeService().getConformanceMethodListingReports();
    }

    @Operation(summary = "Retrieves the data used to generate the Code Set Criteria Attribute Summary report.",
            description = "Retrieves the data used to generate the Code Set Criteria Attribute Summary report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "/code-sets", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CodeSetReport> getCodeSetReports() {
        return reportDataManager.getCriteriaAttributeAttributeService().getCodeSetReports();
    }


    @Operation(summary = "Retrieves the data used to generate the Code Set Criteria Attribute Listing report.",
            description = "Retrieves the data used to generate the Code SetCriteria Attribute Listing report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "/code-sets/listings", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CodeSetListingReport> getCodeSetListingReports() {
        return reportDataManager.getCriteriaAttributeAttributeService().getCodeSetListingReports();
    }

}
