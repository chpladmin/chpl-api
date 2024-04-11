package gov.healthit.chpl.web.controller;

import java.util.List;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.exception.ActivityException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.surveillance.report.SurveillanceReportManager;
import gov.healthit.chpl.surveillance.report.domain.AnnualReport;
import gov.healthit.chpl.surveillance.report.domain.PrivilegedSurveillance;
import gov.healthit.chpl.surveillance.report.domain.QuarterlyReport;
import gov.healthit.chpl.surveillance.report.domain.RelevantListing;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Tag(name = "surveillance-report", description = "Allows management of quarterly and annual surveillance reporting.")
@RestController
@RequestMapping("/surveillance-report")
@Log4j2
public class SurveillanceReportController {

    private ErrorMessageUtil msgUtil;
    private SurveillanceReportManager reportManager;

    @Autowired
    public SurveillanceReportController(ErrorMessageUtil msgUtil,
            SurveillanceReportManager reportManager) {
        this.msgUtil = msgUtil;
        this.reportManager = reportManager;
    }

    @Operation(summary = "Get all annual surveillance reports this user has access to.",
            description = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB and administrative "
                    + "authority on the ACB associated with the report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/annual", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<AnnualReport> getAllAnnualReports() throws AccessDeniedException {
        List<AnnualReport> response = reportManager.getAnnualReports();
        return response;
    }

    @Operation(summary = "Get a specific annual surveillance report by ID.",
            description = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB and administrative "
                    + "authority on the ACB associated with the report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/annual/{annualReportId}",
            method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody AnnualReport getAnnualReport(@PathVariable Long annualReportId)
            throws AccessDeniedException, EntityRetrievalException {
        AnnualReport report = reportManager.getAnnualReport(annualReportId);
        return report;
    }

    @Operation(summary = "Create a new annual surveillance report.",
            description = "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative "
                    + "authority on the ACB associated with the report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/annual", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public AnnualReport createAnnualReport(@RequestBody(required = true) AnnualReport createRequest)
            throws InvalidArgumentsException, EntityCreationException, EntityRetrievalException, ActivityException {

        if (createRequest.getAcb() == null || createRequest.getAcb().getId() == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("report.annualSurveillance.missingAcb"));
        }

        // at least one quarterly report must exist to create the annual report
        List<QuarterlyReport> quarterlyReports = reportManager.getQuarterlyReports(createRequest.getAcb().getId(), createRequest.getYear());
        if (quarterlyReports == null || quarterlyReports.size() == 0) {
            throw new InvalidArgumentsException(msgUtil.getMessage("report.annualSurveillance.missingQuarterly",
                    createRequest.getYear(), "create"));
        }

        // create the report
        AnnualReport annualReport = AnnualReport.builder()
                .acb(CertificationBody.builder()
                        .id(createRequest.getAcb().getId())
                        .build())
                .year(createRequest.getYear())
                .priorityChangesFromFindingsSummary(createRequest.getPriorityChangesFromFindingsSummary())
                .obstacleSummary(createRequest.getObstacleSummary())
                .build();
        AnnualReport createdReport = reportManager.createAnnualReport(annualReport);
        return createdReport;
    }

    @Operation(summary = "Update an existing annual surveillance report.",
            description = "The associated ACB and year of the report cannot be changed. "
                    + "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative authority "
                    + "on the ACB associated with the report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/annual", method = RequestMethod.PUT, produces = "application/json; charset=utf-8")
    public AnnualReport updateAnnualReport(@RequestBody(required = true) AnnualReport updateRequest)
            throws InvalidArgumentsException, EntityRetrievalException, ActivityException {

        if (updateRequest.getId() == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("report.annualSurveillance.missingReportId"));
        }
        AnnualReport reportToUpdate = reportManager.getAnnualReport(updateRequest.getId());
        // above line throws entity retrieval exception if bad id
        reportToUpdate.setPriorityChangesFromFindingsSummary(updateRequest.getPriorityChangesFromFindingsSummary());
        reportToUpdate.setObstacleSummary(updateRequest.getObstacleSummary());
        AnnualReport createdReport = reportManager.updateAnnualReport(reportToUpdate);
        return createdReport;
    }

    @Operation(summary = "Delete an annual report.",
            description = "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative authority "
                    + "on the ACB associated with the report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/annual/{annualReportId}", method = RequestMethod.DELETE,
            produces = "application/json; charset=utf-8")
    public void deleteAnnualReport(@PathVariable Long annualReportId) throws EntityRetrievalException, ActivityException {
        reportManager.deleteAnnualReport(annualReportId);
    }

    @Operation(summary = "Generates an annual report as an XLSX file as a background job "
            + "and emails the report to the logged in user",
            description = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB and administrative authority "
                    + "on the ACB associated with the report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/export/annual/{annualReportId}", method = RequestMethod.GET)
    public ChplOneTimeTrigger exportAnnualReport(@PathVariable("annualReportId") Long annualReportId)
            throws ValidationException, SchedulerException, EntityRetrievalException,
            UserRetrievalException, InvalidArgumentsException {
        AnnualReport reportToExport = reportManager.getAnnualReport(annualReportId);
        // at least one quarterly report must exist to export the annual report
        List<QuarterlyReport> quarterlyReports = reportManager.getQuarterlyReports(reportToExport.getAcb().getId(), reportToExport.getYear());
        if (quarterlyReports == null || quarterlyReports.size() == 0) {
            throw new InvalidArgumentsException(msgUtil.getMessage("report.annualSurveillance.missingQuarterly",
                    reportToExport.getYear(), "export"));
        }

        return reportManager.exportAnnualReportAsBackgroundJob(annualReportId);
    }

    @Operation(summary = "Get all quarterly surveillance reports this user has access to.",
            description = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB and administrative "
                    + "authority on the ACB associated with the report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/quarterly", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<QuarterlyReport> getAllQuarterlyReports() throws AccessDeniedException {
        List<QuarterlyReport> allReports = reportManager.getQuarterlyReports();
        return allReports;
    }

    @Operation(summary = "Get a specific quarterly surveillance report by ID.",
            description = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB and administrative "
                    + "authority on the ACB associated with the report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/quarterly/{quarterlyReportId}",
            method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody QuarterlyReport getQuarterlyReport(@PathVariable Long quarterlyReportId)
            throws AccessDeniedException, EntityRetrievalException {
        QuarterlyReport report = reportManager.getQuarterlyReport(quarterlyReportId);
        return report;
    }

    @Operation(summary = "Get listings that are relevant to a specific quarterly report. "
            + "These are listings belonging to the ACB associated with the report "
            + "that had an active status at any point during the quarter",
            description = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB and administrative "
                    + "authority on the ACB associated with the report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/quarterly/{quarterlyReportId}/listings",
            method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<RelevantListing> getRelevantListings(@PathVariable Long quarterlyReportId)
            throws AccessDeniedException, EntityRetrievalException {
        QuarterlyReport report = reportManager.getQuarterlyReport(quarterlyReportId);
        List<RelevantListing> relevantListings = reportManager.getRelevantListings(report);
        return relevantListings;
    }

    @Operation(summary = "Create a new quarterly surveillance report.",
            description = "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative "
                    + "authority on the ACB associated with the report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/quarterly", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public QuarterlyReport createQuarterlyReport(
            @RequestBody(required = true) QuarterlyReport createRequest)
            throws AccessDeniedException, InvalidArgumentsException, EntityCreationException,
            JsonProcessingException, EntityRetrievalException, ValidationException, ActivityException {
        if (createRequest.getAcb() == null || createRequest.getAcb().getId() == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("report.quarterlySurveillance.missingAcb"));
        }

        Long createdReportId = reportManager.createQuarterlyReport(createRequest);
        return reportManager.getQuarterlyReport(createdReportId);
    }

    @Operation(summary = "Updates surveillance data that is tied to the quarterly report. ",
            description = "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative "
                    + "authority on the ACB associated with the report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/quarterly/{quarterlyReportId}/surveillance/{surveillanceId}", method = RequestMethod.PUT, produces = "application/json; charset=utf-8")
    public PrivilegedSurveillance updatePrivilegedSurveillanceData(
            @PathVariable Long quarterlyReportId,
            @PathVariable Long surveillanceId,
            @RequestBody(required = true) PrivilegedSurveillance updateRequest)
            throws AccessDeniedException, InvalidArgumentsException, EntityRetrievalException,
            EntityCreationException, JsonProcessingException, ActivityException {
        QuarterlyReport quarterlyReport = reportManager.getQuarterlyReport(quarterlyReportId);
        updateRequest.setQuarterlyReport(quarterlyReport);
        updateRequest.setId(surveillanceId);
        return reportManager.createOrUpdateQuarterlyReportSurveillanceMap(updateRequest);
    }

    @Operation(summary = "Update an existing quarterly surveillance report.",
            description = "The associated ACB, year, and quarter of the report cannot be changed. "
                    + "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative authority "
                    + "on the ACB associated with the report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/quarterly", method = RequestMethod.PUT, produces = "application/json; charset=utf-8")
    public QuarterlyReport updateQuarterlyReport(
            @RequestBody(required = true) QuarterlyReport updateRequest)
            throws AccessDeniedException, InvalidArgumentsException, EntityRetrievalException, JsonProcessingException,
            EntityCreationException, ValidationException, ActivityException {
        if (updateRequest.getId() == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("report.quarterlySurveillance.missingReportId"));
        }
        reportManager.updateQuarterlyReport(updateRequest);
        return reportManager.getQuarterlyReport(updateRequest.getId());
    }

    @Operation(summary = "Delete a quarterly report.",
            description = "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative authority "
                    + "on the ACB associated with the report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/quarterly/{quarterlyReportId}", method = RequestMethod.DELETE,
            produces = "application/json; charset=utf-8")
    public void deleteQuarterlyReport(@PathVariable Long quarterlyReportId) throws EntityRetrievalException, ActivityException, JsonProcessingException, EntityCreationException {
        reportManager.deleteQuarterlyReport(quarterlyReportId);
    }

    @Operation(summary = "Generates a quarterly report as an XLSX file as a background job "
            + "and emails the report to the logged in user",
            description = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB and administrative authority "
                    + "on the ACB associated with the report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/export/quarterly/{quarterlyReportId}", method = RequestMethod.GET)
    public ChplOneTimeTrigger exportQuarterlyReport(@PathVariable("quarterlyReportId") Long quarterlyReportId)
            throws ValidationException, SchedulerException, UserRetrievalException {
        return reportManager.exportQuarterlyReportAsBackgroundJob(quarterlyReportId);
    }
}
