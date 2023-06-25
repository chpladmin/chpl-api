package gov.healthit.chpl.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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

import gov.healthit.chpl.complaint.ComplaintManager;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
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
import gov.healthit.chpl.surveillance.report.dto.PrivilegedSurveillanceDTO;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportDTO;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportExclusionDTO;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportRelevantListingDTO;
import gov.healthit.chpl.surveillance.report.dto.SurveillanceOutcomeDTO;
import gov.healthit.chpl.surveillance.report.dto.SurveillanceProcessTypeDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.annotation.DeprecatedApiResponseFields;
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
    private ComplaintManager complaintManager;

    @Autowired
    public SurveillanceReportController(ErrorMessageUtil msgUtil,
            SurveillanceReportManager reportManager,
            ComplaintManager complaintManager) {
        this.msgUtil = msgUtil;
        this.reportManager = reportManager;
        this.complaintManager = complaintManager;
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
    public AnnualReport createAnnualReport(
            @RequestBody(required = true) AnnualReport createRequest)
            throws AccessDeniedException, InvalidArgumentsException, EntityCreationException,
            JsonProcessingException, EntityRetrievalException {
        if (createRequest.getAcb() == null || createRequest.getAcb().getId() == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("report.annualSurveillance.missingAcb"));
        }

        // at least one quarterly report must exist to create the annual report
        List<QuarterlyReportDTO> quarterlyReports = reportManager.getQuarterlyReports(createRequest.getAcb().getId(), createRequest.getYear());
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
    public AnnualReport updateAnnualReport(
            @RequestBody(required = true) AnnualReport updateRequest)
            throws AccessDeniedException, InvalidArgumentsException, EntityRetrievalException, JsonProcessingException,
            EntityCreationException {
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
    public void deleteAnnualReport(@PathVariable Long annualReportId)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
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
        List<QuarterlyReportDTO> quarterlyReports = reportManager.getQuarterlyReports(reportToExport.getAcb().getId(), reportToExport.getYear());
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
    @DeprecatedApiResponseFields(friendlyUrl = "/surveillance-report/quarterly", responseClass = QuarterlyReport.class)
    public @ResponseBody List<QuarterlyReport> getAllQuarterlyReports() throws AccessDeniedException {
        List<QuarterlyReportDTO> allReports = reportManager.getQuarterlyReports();
        List<QuarterlyReport> response = new ArrayList<QuarterlyReport>();
        for (QuarterlyReportDTO currReport : allReports) {
            response.add(new QuarterlyReport(currReport));
        }
        return response;
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
    @DeprecatedApiResponseFields(friendlyUrl = "/surveillance-report/quarterly/{quarterlyReportId}",
        responseClass = QuarterlyReport.class)
    public @ResponseBody QuarterlyReport getQuarterlyReport(@PathVariable Long quarterlyReportId)
            throws AccessDeniedException, EntityRetrievalException {
        QuarterlyReportDTO reportDto = reportManager.getQuarterlyReport(quarterlyReportId);
        return new QuarterlyReport(reportDto);
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
        QuarterlyReportDTO reportDto = reportManager.getQuarterlyReport(quarterlyReportId);
        List<QuarterlyReportRelevantListingDTO> relevantListingDtos = reportManager.getRelevantListings(reportDto);

        List<RelevantListing> relevantListings = new ArrayList<RelevantListing>();
        if (relevantListingDtos != null && relevantListingDtos.size() > 0) {
            for (QuarterlyReportRelevantListingDTO relevantListingDto : relevantListingDtos) {
                relevantListings.add(new RelevantListing(relevantListingDto));
            }
        }
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
    @DeprecatedApiResponseFields(friendlyUrl = "/surveillance-report/quarterly", httpMethod = "POST",
        responseClass = QuarterlyReport.class)
    public QuarterlyReport createQuarterlyReport(
            @RequestBody(required = true) QuarterlyReport createRequest)
            throws AccessDeniedException, InvalidArgumentsException, EntityCreationException,
            JsonProcessingException, EntityRetrievalException, ValidationException {
        if (createRequest.getAcb() == null || createRequest.getAcb().getId() == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("report.quarterlySurveillance.missingAcb"));
        }

        QuarterlyReportDTO createdReport = reportManager.createQuarterlyReport(createRequest);
        return new QuarterlyReport(createdReport);
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
            EntityCreationException, JsonProcessingException {
        QuarterlyReportDTO quarterlyReport = reportManager.getQuarterlyReport(quarterlyReportId);
        PrivilegedSurveillanceDTO toUpdate = new PrivilegedSurveillanceDTO();
        toUpdate.setQuarterlyReport(quarterlyReport);
        toUpdate.setId(surveillanceId);
        toUpdate.setCertifiedProductId(updateRequest.getCertifiedProductId());
        toUpdate.setK1Reviewed(updateRequest.getK1Reviewed());
        toUpdate.setGroundsForInitiating(updateRequest.getGroundsForInitiating());
        toUpdate.setNonconformityCauses(updateRequest.getNonconformityCauses());
        toUpdate.setNonconformityNature(updateRequest.getNonconformityNature());
        toUpdate.setStepsToSurveil(updateRequest.getStepsToSurveil());
        toUpdate.setStepsToEngage(updateRequest.getStepsToEngage());
        toUpdate.setAdditionalCostsEvaluation(updateRequest.getAdditionalCostsEvaluation());
        toUpdate.setLimitationsEvaluation(updateRequest.getLimitationsEvaluation());
        toUpdate.setNondisclosureEvaluation(updateRequest.getNondisclosureEvaluation());
        toUpdate.setDirectionDeveloperResolution(updateRequest.getDirectionDeveloperResolution());
        toUpdate.setCompletedCapVerification(updateRequest.getCompletedCapVerification());
        if (updateRequest.getSurveillanceOutcome() != null) {
            SurveillanceOutcomeDTO survOutcome = new SurveillanceOutcomeDTO();
            survOutcome.setId(updateRequest.getSurveillanceOutcome().getId());
            toUpdate.setSurveillanceOutcome(survOutcome);
        }
        toUpdate.setSurveillanceOutcomeOther(updateRequest.getSurveillanceOutcomeOther());
        if (updateRequest.getSurveillanceProcessType() != null) {
            SurveillanceProcessTypeDTO processType = new SurveillanceProcessTypeDTO();
            processType.setId(updateRequest.getSurveillanceProcessType().getId());
            toUpdate.setSurveillanceProcessType(processType);
        }
        toUpdate.setSurveillanceProcessTypeOther(updateRequest.getSurveillanceProcessTypeOther());
        PrivilegedSurveillanceDTO updated = reportManager.createOrUpdateQuarterlyReportSurveillanceMap(toUpdate);
        return new PrivilegedSurveillance(updated);
    }

    @Operation(summary = "Updates whether a relevant listing is marked as excluded from a quarterly "
            + "report. If it's being excluded then the reason is required.",
            description = "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative "
                    + "authority on the ACB associated with the report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/quarterly/{quarterlyReportId}/listings/{listingId}", method = RequestMethod.PUT, produces = "application/json; charset=utf-8")
    public RelevantListing updateRelevantListing(@PathVariable Long quarterlyReportId,
            @PathVariable Long listingId,
            @RequestBody(required = true) RelevantListing updateExclusionRequest)
            throws AccessDeniedException, InvalidArgumentsException, EntityRetrievalException, EntityCreationException,
            JsonProcessingException {
        QuarterlyReportDTO quarterlyReport = reportManager.getQuarterlyReport(quarterlyReportId);
        if (updateExclusionRequest.isExcluded() && StringUtils.isEmpty(updateExclusionRequest.getReason())) {
            throw new InvalidArgumentsException(
                    msgUtil.getMessage("report.quarterlySurveillance.exclusion.missingReason", quarterlyReport.getQuarter().getName()));
        }

        // see if a current exclusion exists for this listing to determine if
        // it's being
        // newly created with this request or just updated
        QuarterlyReportExclusionDTO existingExclusion = reportManager.getExclusion(quarterlyReport, listingId);
        if (existingExclusion == null && updateExclusionRequest.isExcluded()) {
            // no existing exclusion - create one
            reportManager.createQuarterlyReportExclusion(quarterlyReport, listingId,
                    updateExclusionRequest.getReason());
        } else if (existingExclusion != null && updateExclusionRequest.isExcluded()) {
            // found existing exclusion for this listing - update the reason
            reportManager.updateQuarterlyReportExclusion(quarterlyReport, listingId, updateExclusionRequest.getReason());
        } else if (existingExclusion != null && !updateExclusionRequest.isExcluded()) {
            reportManager.deleteQuarterlyReportExclusion(quarterlyReportId, listingId);
        }

        // get the relevant listing with its new exclusion fields
        QuarterlyReportRelevantListingDTO updatedRelevantListing = reportManager.getRelevantListing(quarterlyReport, listingId);
        RelevantListing result = null;
        if (updatedRelevantListing != null) {
            result = new RelevantListing(updatedRelevantListing);
        }
        return result;
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
    @DeprecatedApiResponseFields(friendlyUrl = "/surveillance-report/quarterly", httpMethod = "PUT",
        responseClass = QuarterlyReport.class)
    public QuarterlyReport updateQuarterlyReport(
            @RequestBody(required = true) QuarterlyReport updateRequest)
            throws AccessDeniedException, InvalidArgumentsException, EntityRetrievalException, JsonProcessingException,
            EntityCreationException, ValidationException {
        if (updateRequest.getId() == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("report.quarterlySurveillance.missingReportId"));
        }
        QuarterlyReport createdReport = reportManager.updateQuarterlyReport(updateRequest);
        return createdReport;
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
    public void deleteQuarterlyReport(@PathVariable Long quarterlyReportId)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
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
