package gov.healthit.chpl.web.controller;

import java.util.List;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.report.ReportDataManager;
import gov.healthit.chpl.report.listingattribute.AccessibilityStandardListingReport;
import gov.healthit.chpl.report.listingattribute.AccessibilityStandardReport;
import gov.healthit.chpl.report.listingattribute.MeasureListingReport;
import gov.healthit.chpl.report.listingattribute.MeasureReport;
import gov.healthit.chpl.report.listingattribute.QmsStandardListingReport;
import gov.healthit.chpl.report.listingattribute.QmsStandardReport;
import gov.healthit.chpl.util.LogMethodUsage;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Tag(name = "report-data/listing-attributes", description = "Allows retrieval of data used by Listing Attribute report.")
@RestController
@RequestMapping("/report-data/listing-attributes")
public class ListingAttributeReportController {
    private ReportDataManager reportDataManager;
    private FF4j ff4j;

    @Autowired
    public ListingAttributeReportController(ReportDataManager reportDataManager,
            FF4j ff4j) {
        this.reportDataManager = reportDataManager;
        this.ff4j = ff4j;
    }

    @Operation(summary = "Retrieves the data used to generate the QMS Standard Listing Attribute Summary report.",
            description = "Retrieves the data used to generate the QMS Standard Listing Attribute Summary report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "/qms-standards", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<QmsStandardReport> getQmsStandardsReports() {
        return reportDataManager.getListingAttributeService().getQmsStandardReports();
    }

    @Operation(summary = "Retrieves the data used to generate the QMS Standard Listing Attribute Listings report.",
            description = "Retrieves the data used to generate the QMS Standard Listing Attribute Listings report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "/qms-standards/listings", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<QmsStandardListingReport> getQmsStandardsListingReports() {
        return reportDataManager.getListingAttributeService().getQmsStandardListingReports();
    }

    @Operation(summary = "Retrieves the data used to generate the Accessibility Standard Listing Attribute Summary report.",
            description = "Retrieves the data used to generate the Accessibility Standard Listing Summary report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "/accessibility-standards", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<AccessibilityStandardReport> geAccessibilitytStandardReports() {
        return reportDataManager.getListingAttributeService().getAccessibilityStandardReports();
    }

    @Operation(summary = "Retrieves the data used to generate the Accessibility Standard Listing Attribute Listings report.",
            description = "Retrieves the data used to generate the Accessibility Standard Listing Attribute Listings report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "/accessibility-standards/listings", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<AccessibilityStandardListingReport> getAccessibilityStandardListingReports() {
        return reportDataManager.getListingAttributeService().getAccessibilityStandardListingReports();
    }

    @Operation(summary = "Retrieves the data used to generate the Measures Listing Attribute Summary report.",
            description = "Retrieves the data used to generate the Measures Listing Attribute Summary report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "/measures", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<MeasureReport> getMeasureReports() {
        if (ff4j.check(FeatureList.HTI_5_2027_01_01)) {
            return List.of();
        }
        return reportDataManager.getListingAttributeService().getMeasureReports();
    }

    @Operation(summary = "Retrieves the data used to generate the Measures Listing Attribute Listings report.",
            description = "Retrieves the data used to generate the Measures Listing Attribute Listings report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "/measures/listings", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<MeasureListingReport> getMeasureListingReports() {
        if (ff4j.check(FeatureList.HTI_5_2027_01_01)) {
            return List.of();
        }
        return reportDataManager.getListingAttributeService().getMeasureListingReports();
    }
}