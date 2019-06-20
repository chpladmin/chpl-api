package gov.healthit.chpl.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.domain.Job;
import gov.healthit.chpl.domain.surveillance.report.AnnualReport;
import gov.healthit.chpl.domain.surveillance.report.QuarterlyReport;
import gov.healthit.chpl.domain.surveillance.report.QuarterlyReportExclusion;
import gov.healthit.chpl.domain.surveillance.report.RelevantListing;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.job.JobDTO;
import gov.healthit.chpl.dto.surveillance.report.AnnualReportDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportExclusionDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportRelevantListingDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.manager.SurveillanceReportManager;
import gov.healthit.chpl.util.ErrorMessageUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "surveillance-report")
@RestController
@RequestMapping("/surveillance-report")
public class SurveillanceReportController {

    private static final Logger LOGGER = LogManager.getLogger(SurveillanceReportController.class);

    @Autowired
    private ErrorMessageUtil msgUtil;
    @Autowired
    private SurveillanceReportManager reportManager;
    @Autowired
    private FF4j ff4j;

    @ApiOperation(value = "Get all annual surveillance reports this user has access to.",
            notes = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB and administrative "
                    + "authority on the ACB associated with the report.")
    @RequestMapping(value = "/annual", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<AnnualReport> getAllAnnualReports() throws AccessDeniedException {
        if (!ff4j.check(FeatureList.SURVEILLANCE_REPORTING)) {
            throw new NotImplementedException();
        }
        List<AnnualReportDTO> allReports = reportManager.getAnnualReports();
        List<AnnualReport> response = new ArrayList<AnnualReport>();
        for (AnnualReportDTO currReport : allReports) {
            response.add(new AnnualReport(currReport));
        }
        return response;
    }

    @ApiOperation(value = "Get a specific annual surveillance report by ID.",
            notes = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB and administrative "
                    + "authority on the ACB associated with the report.")
    @RequestMapping(value = "/annual/{annualReportId}",
        method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody AnnualReport getAnnualReport(@PathVariable final Long annualReportId)
            throws AccessDeniedException, EntityRetrievalException {
        if (!ff4j.check(FeatureList.SURVEILLANCE_REPORTING)) {
            throw new NotImplementedException();
        }
        AnnualReportDTO reportDto = reportManager.getAnnualReport(annualReportId);
        return new AnnualReport(reportDto);
    }

    @ApiOperation(value = "Create a new annual surveillance report.",
            notes = "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative "
                    + "authority on the ACB associated with the report.")
    @RequestMapping(value = "/annual", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public synchronized AnnualReport createAnnualReport(
        @RequestBody(required = true) final AnnualReport createRequest)
                throws AccessDeniedException, InvalidArgumentsException, EntityCreationException {
        if (!ff4j.check(FeatureList.SURVEILLANCE_REPORTING)) {
            throw new NotImplementedException();
        }
        if (createRequest.getAcb() == null || createRequest.getAcb().getId() == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("report.annualSurveillance.missingAcb"));
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
        @RequestBody(required = true) final AnnualReport updateRequest)
    throws AccessDeniedException, InvalidArgumentsException, EntityRetrievalException {
        if (!ff4j.check(FeatureList.SURVEILLANCE_REPORTING)) {
            throw new NotImplementedException();
        }
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
    public void deleteAnnualReport(@PathVariable final Long annualReportId)
            throws EntityRetrievalException {
        if (!ff4j.check(FeatureList.SURVEILLANCE_REPORTING)) {
            throw new NotImplementedException();
        }
        reportManager.deleteAnnualReport(annualReportId);
    }


    @ApiOperation(value = "Generates an annual report as an XLSX file as a background job "
            + "and emails the report to the logged in user",
            notes = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB and administrative authority "
                    + "on the ACB associated with the report.")
    @RequestMapping(value = "/export/annual/{annualReportId}", method = RequestMethod.GET)
    public Job exportAnnualReport(@PathVariable("annualReportId") final Long annualReportId,
            final HttpServletResponse response) throws EntityRetrievalException, UserRetrievalException,
            EntityCreationException, IOException {
        if (!ff4j.check(FeatureList.SURVEILLANCE_REPORTING)) {
            throw new NotImplementedException();
        }

        JobDTO exportJob = reportManager.exportAnnualReportAsBackgroundJob(annualReportId);
        return new Job(exportJob);
    }

    @ApiOperation(value = "Get all quarterly surveillance reports this user has access to.",
            notes = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB and administrative "
                    + "authority on the ACB associated with the report.")
    @RequestMapping(value = "/quarterly", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<QuarterlyReport> getAllQuarterlyReports() throws AccessDeniedException {
        if (!ff4j.check(FeatureList.SURVEILLANCE_REPORTING)) {
            throw new NotImplementedException();
        }
        List<QuarterlyReportDTO> allReports = reportManager.getQuarterlyReports();
        List<QuarterlyReport> response = new ArrayList<QuarterlyReport>();
        for (QuarterlyReportDTO currReport : allReports) {
            response.add(new QuarterlyReport(currReport));
        }
        return response;
    }

    @ApiOperation(value = "Get a specific quarterly surveillance report by ID.",
            notes = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB and administrative "
                    + "authority on the ACB associated with the report.")
    @RequestMapping(value = "/quarterly/{quarterlyReportId}",
        method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody QuarterlyReport getQuarterlyReport(@PathVariable final Long quarterlyReportId)
            throws AccessDeniedException, EntityRetrievalException {
        if (!ff4j.check(FeatureList.SURVEILLANCE_REPORTING)) {
            throw new NotImplementedException();
        }
        QuarterlyReportDTO reportDto = reportManager.getQuarterlyReport(quarterlyReportId);
        return new QuarterlyReport(reportDto);
    }

    @ApiOperation(value = "Get listings that are relevant to a specific quarterly report. "
            + "These are listings that had surveillance during the quarter.",
            notes = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB and administrative "
                    + "authority on the ACB associated with the report.")
    @RequestMapping(value = "/quarterly/{quarterlyReportId}/listings",
        method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<RelevantListing> getRelevantListings(@PathVariable final Long quarterlyReportId)
            throws AccessDeniedException, EntityRetrievalException {
        if (!ff4j.check(FeatureList.SURVEILLANCE_REPORTING)) {
            throw new NotImplementedException();
        }
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

    @ApiOperation(value = "Create a new quarterly surveillance report.",
                    notes = "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative "
                            + "authority on the ACB associated with the report.")
    @RequestMapping(value = "/quarterly", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public synchronized QuarterlyReport createQuarterlyReport(
            @RequestBody(required = true) final QuarterlyReport createRequest)
    throws AccessDeniedException, InvalidArgumentsException, EntityCreationException {
        if (!ff4j.check(FeatureList.SURVEILLANCE_REPORTING)) {
            throw new NotImplementedException();
        }
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

    @ApiOperation(value = "Marks one of the listings relevant to the specified quarterly surviellance report"
            + "as 'excluded'.",
            notes = "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative "
                    + "authority on the ACB associated with the report.")
    @RequestMapping(value = "/quarterly/{quarterlyReportId}/exclusion", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public synchronized QuarterlyReportExclusion addExclusion(@PathVariable final Long quarterlyReportId,
        @RequestBody(required = true) final QuarterlyReportExclusion createExcludedListingRequest)
                throws AccessDeniedException, InvalidArgumentsException, EntityRetrievalException, EntityCreationException {
        if (!ff4j.check(FeatureList.SURVEILLANCE_REPORTING)) {
            throw new NotImplementedException();
        }
        QuarterlyReportDTO quarterlyReport = reportManager.getQuarterlyReport(quarterlyReportId);
        QuarterlyReportExclusionDTO createdExclusion =
                reportManager.createQuarterlyReportExclusion(quarterlyReport,
                createExcludedListingRequest.getListingId(), createExcludedListingRequest.getReason());
        return new QuarterlyReportExclusion(createdExclusion);
    }

    @ApiOperation(value = "Update an existing quarterly surveillance report.",
            notes = "The associated ACB, year, and quarter of the report cannot be changed. "
            + "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative authority "
            + "on the ACB associated with the report.")
    @RequestMapping(value = "/quarterly", method = RequestMethod.PUT, produces = "application/json; charset=utf-8")
    public synchronized QuarterlyReport updateQuarterlyReport(
        @RequestBody(required = true) final QuarterlyReport updateRequest)
    throws AccessDeniedException, InvalidArgumentsException, EntityRetrievalException {
        if (!ff4j.check(FeatureList.SURVEILLANCE_REPORTING)) {
            throw new NotImplementedException();
        }
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

    @ApiOperation(value = "Updates the exclusion reason for a listing that's already marked as excluded "
            + "from a quarterly surveillance report.",
            notes = "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative "
                    + "authority on the ACB associated with the report.")
    @RequestMapping(value = "/quarterly/{quarterlyReportId}/exclusion", method = RequestMethod.PUT, produces = "application/json; charset=utf-8")
    public synchronized QuarterlyReportExclusion updateExclusion(@PathVariable final Long quarterlyReportId,
        @RequestBody(required = true) final QuarterlyReportExclusion updateExcludedListingRequest)
                throws AccessDeniedException, InvalidArgumentsException, EntityRetrievalException, EntityCreationException {
        if (!ff4j.check(FeatureList.SURVEILLANCE_REPORTING)) {
            throw new NotImplementedException();
        }
        QuarterlyReportDTO quarterlyReport = reportManager.getQuarterlyReport(quarterlyReportId);
        QuarterlyReportExclusionDTO updatedExclusion =
                reportManager.updateQuarterlyReportExclusion(quarterlyReport,
                        updateExcludedListingRequest.getListingId(), updateExcludedListingRequest.getReason());
        return new QuarterlyReportExclusion(updatedExclusion);
    }

    @ApiOperation(value = "Delete a quarterly report.",
            notes = "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative authority "
            + "on the ACB associated with the report.")
    @RequestMapping(value = "/quarterly/{quarterlyReportId}", method = RequestMethod.DELETE,
    produces = "application/json; charset=utf-8")
    public void deleteQuarterlyReport(@PathVariable final Long quarterlyReportId)
            throws EntityRetrievalException {
        if (!ff4j.check(FeatureList.SURVEILLANCE_REPORTING)) {
            throw new NotImplementedException();
        }
        reportManager.deleteQuarterlyReport(quarterlyReportId);
    }

    @ApiOperation(value = "Delete an excluded listing from a quarterly report.",
            notes = "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative authority "
            + "on the ACB associated with the report.")
    @RequestMapping(value = "/quarterly/{quarterlyReportId}/exclusion/{listingId}",
        method = RequestMethod.DELETE,
        produces = "application/json; charset=utf-8")
    public void deleteExclusion(@PathVariable final Long quarterlyReportId, @PathVariable final Long listingId)
            throws EntityRetrievalException {
        if (!ff4j.check(FeatureList.SURVEILLANCE_REPORTING)) {
            throw new NotImplementedException();
        }
        QuarterlyReportDTO quarterlyReport = reportManager.getQuarterlyReport(quarterlyReportId);
        reportManager.deleteQuarterlyReportExclusion(quarterlyReport, listingId);
    }

    @ApiOperation(value = "Generates a quarterly report as an XLSX file as a background job "
            + "and emails the report to the logged in user",
            notes = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB and administrative authority "
                    + "on the ACB associated with the report.")
    @RequestMapping(value = "/export/quarterly/{quarterlyReportId}", method = RequestMethod.GET)
    public Job exportQuarterlyReport(@PathVariable("quarterlyReportId") final Long quarterlyReportId,
            final HttpServletResponse response) throws EntityRetrievalException, UserRetrievalException,
            EntityCreationException, IOException {
        if (!ff4j.check(FeatureList.SURVEILLANCE_REPORTING)) {
            throw new NotImplementedException();
        }

        JobDTO exportJob = reportManager.exportQuarterlyReportAsBackgroundJob(quarterlyReportId);
        return new Job(exportJob);
    }
}
