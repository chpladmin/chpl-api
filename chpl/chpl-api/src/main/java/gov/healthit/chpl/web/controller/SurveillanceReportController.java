package gov.healthit.chpl.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.domain.Job;
import gov.healthit.chpl.domain.complaint.Complaint;
import gov.healthit.chpl.domain.surveillance.privileged.PrivilegedSurveillance;
import gov.healthit.chpl.domain.surveillance.report.AnnualReport;
import gov.healthit.chpl.domain.surveillance.report.QuarterlyReport;
import gov.healthit.chpl.domain.surveillance.report.RelevantListing;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.job.JobDTO;
import gov.healthit.chpl.dto.surveillance.report.AnnualReportDTO;
import gov.healthit.chpl.dto.surveillance.report.PrivilegedSurveillanceDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportExclusionDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportRelevantListingDTO;
import gov.healthit.chpl.dto.surveillance.report.SurveillanceOutcomeDTO;
import gov.healthit.chpl.dto.surveillance.report.SurveillanceProcessTypeDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.logging.Loggable;
import gov.healthit.chpl.manager.ComplaintManager;
import gov.healthit.chpl.manager.SurveillanceReportManager;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.web.controller.results.ComplaintResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;

@Api(value = "surveillance-report")
@RestController
@RequestMapping("/surveillance-report")
@Loggable
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

    @ApiOperation(value = "Get all annual surveillance reports this user has access to.",
            notes = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, ROLE_ONC_STAFF, or ROLE_ACB and administrative "
                    + "authority on the ACB associated with the report.")
    @RequestMapping(value = "/annual", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<AnnualReport> getAllAnnualReports() throws AccessDeniedException {
        List<AnnualReportDTO> allReports = reportManager.getAnnualReports();
        List<AnnualReport> response = new ArrayList<AnnualReport>();
        for (AnnualReportDTO currReport : allReports) {
            response.add(new AnnualReport(currReport));
        }
        return response;
    }

    @ApiOperation(value = "Get a specific annual surveillance report by ID.",
            notes = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, ROLE_ONC_STAFF, or ROLE_ACB and administrative "
                    + "authority on the ACB associated with the report.")
    @RequestMapping(value = "/annual/{annualReportId}",
        method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody AnnualReport getAnnualReport(@PathVariable Long annualReportId)
            throws AccessDeniedException, EntityRetrievalException {
        AnnualReportDTO reportDto = reportManager.getAnnualReport(annualReportId);
        return new AnnualReport(reportDto);
    }

    @ApiOperation(value = "Create a new annual surveillance report.",
            notes = "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative "
                    + "authority on the ACB associated with the report.")
    @RequestMapping(value = "/annual", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public synchronized AnnualReport createAnnualReport(
        @RequestBody(required = true) AnnualReport createRequest)
                throws AccessDeniedException, InvalidArgumentsException, EntityCreationException,
                JsonProcessingException, EntityRetrievalException {
        if (createRequest.getAcb() == null || createRequest.getAcb().getId() == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("report.annualSurveillance.missingAcb"));
        }

        //at least one quarterly report must exist to create the annual report
        List<QuarterlyReportDTO> quarterlyReports =
                reportManager.getQuarterlyReports(createRequest.getAcb().getId(), createRequest.getYear());
        if (quarterlyReports == null || quarterlyReports.size() == 0) {
            throw new InvalidArgumentsException(msgUtil.getMessage("report.annualSurveillance.missingQuarterly",
                    createRequest.getYear(), "create"));
        }

        //create the report
        AnnualReportDTO annualReport = new AnnualReportDTO();
        CertificationBodyDTO associatedAcb = new CertificationBodyDTO();
        associatedAcb.setId(createRequest.getAcb().getId());
        annualReport.setAcb(associatedAcb);
        annualReport.setYear(createRequest.getYear());
        annualReport.setFindingsSummary(createRequest.getPriorityChangesFromFindingsSummary());
        annualReport.setObstacleSummary(createRequest.getObstacleSummary());
        AnnualReportDTO createdReport = reportManager.createAnnualReport(annualReport);
        return new AnnualReport(createdReport);
    }

    @ApiOperation(value = "Update an existing annual surveillance report.",
            notes = "The associated ACB and year of the report cannot be changed. "
            + "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative authority "
            + "on the ACB associated with the report.")
    @RequestMapping(value = "/annual", method = RequestMethod.PUT, produces = "application/json; charset=utf-8")
    public synchronized AnnualReport updateAnnualReport(
        @RequestBody(required = true) AnnualReport updateRequest)
    throws AccessDeniedException, InvalidArgumentsException, EntityRetrievalException, JsonProcessingException,
    EntityCreationException {
        if (updateRequest.getId() == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("report.annualSurveillance.missingReportId"));
        }
        AnnualReportDTO reportToUpdate = reportManager.getAnnualReport(updateRequest.getId());
        //above line throws entity retrieval exception if bad id
        reportToUpdate.setFindingsSummary(updateRequest.getPriorityChangesFromFindingsSummary());
        reportToUpdate.setObstacleSummary(updateRequest.getObstacleSummary());
        AnnualReportDTO createdReport = reportManager.updateAnnualReport(reportToUpdate);
        return new AnnualReport(createdReport);
    }

    @ApiOperation(value = "Delete an annual report.",
            notes = "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative authority "
            + "on the ACB associated with the report.")
    @RequestMapping(value = "/annual/{annualReportId}", method = RequestMethod.DELETE,
    produces = "application/json; charset=utf-8")
    public void deleteAnnualReport(@PathVariable Long annualReportId)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
        reportManager.deleteAnnualReport(annualReportId);
    }


    @ApiOperation(value = "Generates an annual report as an XLSX file as a background job "
            + "and emails the report to the logged in user",
            notes = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, ROLE_ONC_STAFF, or ROLE_ACB and administrative authority "
                    + "on the ACB associated with the report.")
    @RequestMapping(value = "/export/annual/{annualReportId}", method = RequestMethod.GET)
    public Job exportAnnualReport(@PathVariable("annualReportId") Long annualReportId,
            HttpServletResponse response) throws EntityRetrievalException, UserRetrievalException,
            EntityCreationException, IOException, InvalidArgumentsException {
        AnnualReportDTO reportToExport = reportManager.getAnnualReport(annualReportId);
        //at least one quarterly report must exist to export the annual report
        List<QuarterlyReportDTO> quarterlyReports =
                reportManager.getQuarterlyReports(reportToExport.getAcb().getId(), reportToExport.getYear());
        if (quarterlyReports == null || quarterlyReports.size() == 0) {
            throw new InvalidArgumentsException(msgUtil.getMessage("report.annualSurveillance.missingQuarterly",
                    reportToExport.getYear(), "export"));
        }

        JobDTO exportJob = reportManager.exportAnnualReportAsBackgroundJob(annualReportId);
        return new Job(exportJob);
    }

    @ApiOperation(value = "Get all quarterly surveillance reports this user has access to.",
            notes = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, ROLE_ONC_STAFF, or ROLE_ACB and administrative "
                    + "authority on the ACB associated with the report.")
    @RequestMapping(value = "/quarterly", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<QuarterlyReport> getAllQuarterlyReports() throws AccessDeniedException {
        List<QuarterlyReportDTO> allReports = reportManager.getQuarterlyReports();
        List<QuarterlyReport> response = new ArrayList<QuarterlyReport>();
        for (QuarterlyReportDTO currReport : allReports) {
            response.add(new QuarterlyReport(currReport));
        }
        return response;
    }

    @ApiOperation(value = "Get a specific quarterly surveillance report by ID.",
            notes = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, ROLE_ONC_STAFF, or ROLE_ACB and administrative "
                    + "authority on the ACB associated with the report.")
    @RequestMapping(value = "/quarterly/{quarterlyReportId}",
        method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody QuarterlyReport getQuarterlyReport(@PathVariable Long quarterlyReportId)
            throws AccessDeniedException, EntityRetrievalException {
        QuarterlyReportDTO reportDto = reportManager.getQuarterlyReport(quarterlyReportId);
        return new QuarterlyReport(reportDto);
    }

    @ApiOperation(value = "Get listings that are relevant to a specific quarterly report. "
            + "These are listings belonging to the ACB associtaed with the report "
            + "that had a status of <TBD>. at any point during the quarter",
            notes = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, ROLE_ONC_STAFF, or ROLE_ACB and administrative "
                    + "authority on the ACB associated with the report.")
    @RequestMapping(value = "/quarterly/{quarterlyReportId}/listings",
        method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<RelevantListing> getRelevantListings(@PathVariable Long quarterlyReportId)
            throws AccessDeniedException, EntityRetrievalException {
        QuarterlyReportDTO reportDto = reportManager.getQuarterlyReport(quarterlyReportId);
        List<QuarterlyReportRelevantListingDTO> relevantListingDtos =
                reportManager.getRelevantListings(reportDto);

        List<RelevantListing> relevantListings = new ArrayList<RelevantListing>();
        if (relevantListingDtos != null && relevantListingDtos.size() > 0) {
            for (QuarterlyReportRelevantListingDTO relevantListingDto : relevantListingDtos) {
                relevantListings.add(new RelevantListing(relevantListingDto));
            }
        }
        return relevantListings;
    }

    @ApiOperation(value = "Get complaints that are relevant to a specific quarterly report. "
            + "These are complaints that were open during the quarter.",
            notes = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, ROLE_ONC_STAFF, or ROLE_ACB and administrative "
                    + "authority on the ACB associated with the report.")
    @RequestMapping(value = "/quarterly/{quarterlyReportId}/complaints",
        method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody ComplaintResults getRelevantComplaints(@PathVariable Long quarterlyReportId)
            throws AccessDeniedException, EntityRetrievalException {
        QuarterlyReportDTO reportDto = reportManager.getQuarterlyReport(quarterlyReportId);
        List<Complaint> relevantComplaints =
                complaintManager.getAllComplaintsBetweenDates(reportDto.getAcb(), reportDto.getStartDate(), reportDto.getEndDate());
        ComplaintResults results = new ComplaintResults();
        results.getResults().addAll(relevantComplaints);
        return results;
    }

    @ApiOperation(value = "Create a new quarterly surveillance report.",
                    notes = "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative "
                            + "authority on the ACB associated with the report.")
    @RequestMapping(value = "/quarterly", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public synchronized QuarterlyReport createQuarterlyReport(
            @RequestBody(required = true) QuarterlyReport createRequest)
    throws AccessDeniedException, InvalidArgumentsException, EntityCreationException,
    JsonProcessingException, EntityRetrievalException {
        if (createRequest.getAcb() == null || createRequest.getAcb().getId() == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("report.quarterlySurveillance.missingAcb"));
        }

        //create the report
        QuarterlyReportDTO quarterlyReport = new QuarterlyReportDTO();
        quarterlyReport.setYear(createRequest.getYear());
        CertificationBodyDTO associatedAcb = new CertificationBodyDTO();
        associatedAcb.setId(createRequest.getAcb().getId());
        quarterlyReport.setAcb(associatedAcb);
        QuarterDTO quarter = new QuarterDTO();
        quarter.setName(createRequest.getQuarter());
        quarterlyReport.setQuarter(quarter);
        quarterlyReport.setActivitiesOutcomesSummary(createRequest.getSurveillanceActivitiesAndOutcomes());
        quarterlyReport.setPrioritizedElementSummary(createRequest.getPrioritizedElementSummary());
        quarterlyReport.setReactiveSummary(createRequest.getReactiveSummary());
        quarterlyReport.setTransparencyDisclosureSummary(createRequest.getTransparencyDisclosureSummary());
        QuarterlyReportDTO createdReport = reportManager.createQuarterlyReport(quarterlyReport);
        return new QuarterlyReport(createdReport);
    }

    @ApiOperation(value = "Updates surveillance data that is tied to the quarterly report. ",
            notes = "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative "
                    + "authority on the ACB associated with the report.")
    @RequestMapping(value = "/quarterly/{quarterlyReportId}/surveillance/{surveillanceId}", method = RequestMethod.PUT, produces = "application/json; charset=utf-8")
    public synchronized PrivilegedSurveillance updatePrivilegedSurveillanceData(
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
        PrivilegedSurveillanceDTO updated =
                reportManager.createOrUpdateQuarterlyReportSurveillanceMap(toUpdate);
        return new PrivilegedSurveillance(updated);
    }

    @ApiOperation(value = "Updates whether a relevant listing is marked as excluded from a quarterly "
            + "report. If it's being excluded then the reason is required.",
            notes = "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative "
                    + "authority on the ACB associated with the report.")
    @RequestMapping(value = "/quarterly/{quarterlyReportId}/listings/{listingId}", method = RequestMethod.PUT, produces = "application/json; charset=utf-8")
    public synchronized RelevantListing updateRelevantListing(@PathVariable Long quarterlyReportId,
            @PathVariable Long listingId,
            @RequestBody(required = true) RelevantListing updateExclusionRequest)
                throws AccessDeniedException, InvalidArgumentsException, EntityRetrievalException, EntityCreationException,
                JsonProcessingException {
        QuarterlyReportDTO quarterlyReport = reportManager.getQuarterlyReport(quarterlyReportId);
        if (updateExclusionRequest.isExcluded() && StringUtils.isEmpty(updateExclusionRequest.getReason())) {
            throw new InvalidArgumentsException(
                    msgUtil.getMessage("report.quarterlySurveillance.exclusion.missingReason", quarterlyReport.getQuarter().getName()));
        }

        //see if a current exclusion exists for this listing to determine if it's being
        //newly created with this request or just updated
        QuarterlyReportExclusionDTO existingExclusion = reportManager.getExclusion(quarterlyReport, listingId);
        if (existingExclusion == null && updateExclusionRequest.isExcluded()) {
            //no existing exclusion - create one
            reportManager.createQuarterlyReportExclusion(quarterlyReport, listingId,
                    updateExclusionRequest.getReason());
        } else if (existingExclusion != null && updateExclusionRequest.isExcluded()) {
            //found existing exclusion for this listing - update the reason
            reportManager.updateQuarterlyReportExclusion(quarterlyReport, listingId, updateExclusionRequest.getReason());
        } else if (existingExclusion != null && !updateExclusionRequest.isExcluded()) {
            reportManager.deleteQuarterlyReportExclusion(quarterlyReportId, listingId);
        }

        //get the relevant listing with its new exclusion fields
        QuarterlyReportRelevantListingDTO updatedRelevantListing =
                reportManager.getRelevantListing(quarterlyReport, listingId);
        RelevantListing result = null;
        if (updatedRelevantListing != null) {
            result = new RelevantListing(updatedRelevantListing);
        }
        return result;
    }

    @ApiOperation(value = "Update an existing quarterly surveillance report.",
            notes = "The associated ACB, year, and quarter of the report cannot be changed. "
            + "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative authority "
            + "on the ACB associated with the report.")
    @RequestMapping(value = "/quarterly", method = RequestMethod.PUT, produces = "application/json; charset=utf-8")
    public synchronized QuarterlyReport updateQuarterlyReport(
        @RequestBody(required = true) QuarterlyReport updateRequest)
    throws AccessDeniedException, InvalidArgumentsException, EntityRetrievalException, JsonProcessingException,
    EntityCreationException {
        if (updateRequest.getId() == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("report.quarterlySurveillance.missingReportId"));
        }
        QuarterlyReportDTO reportToUpdate = reportManager.getQuarterlyReport(updateRequest.getId());
        //above line throws entity retrieval exception if bad id
        reportToUpdate.setActivitiesOutcomesSummary(updateRequest.getSurveillanceActivitiesAndOutcomes());
        reportToUpdate.setPrioritizedElementSummary(updateRequest.getPrioritizedElementSummary());
        reportToUpdate.setReactiveSummary(updateRequest.getReactiveSummary());
        reportToUpdate.setTransparencyDisclosureSummary(updateRequest.getTransparencyDisclosureSummary());
        QuarterlyReportDTO createdReport = reportManager.updateQuarterlyReport(reportToUpdate);
        return new QuarterlyReport(createdReport);
    }

    @ApiOperation(value = "Delete a quarterly report.",
            notes = "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative authority "
            + "on the ACB associated with the report.")
    @RequestMapping(value = "/quarterly/{quarterlyReportId}", method = RequestMethod.DELETE,
    produces = "application/json; charset=utf-8")
    public void deleteQuarterlyReport(@PathVariable Long quarterlyReportId)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
        reportManager.deleteQuarterlyReport(quarterlyReportId);
    }

    @ApiOperation(value = "Generates a quarterly report as an XLSX file as a background job "
            + "and emails the report to the logged in user",
            notes = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, ROLE_ONC_STAFF, or ROLE_ACB and administrative authority "
                    + "on the ACB associated with the report.")
    @RequestMapping(value = "/export/quarterly/{quarterlyReportId}", method = RequestMethod.GET)
    public Job exportQuarterlyReport(@PathVariable("quarterlyReportId") Long quarterlyReportId,
            HttpServletResponse response) throws EntityRetrievalException, UserRetrievalException,
            EntityCreationException, IOException {
        JobDTO exportJob = reportManager.exportQuarterlyReportAsBackgroundJob(quarterlyReportId);
        return new Job(exportJob);
    }
}
