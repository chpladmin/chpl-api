package gov.healthit.chpl.web.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;
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
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.surveillance.report.AnnualReport;
import gov.healthit.chpl.domain.surveillance.report.QuarterlyReport;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.surveillance.report.AnnualReportDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.manager.SurveillanceReportManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.FileUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "surveillance-report")
@RestController
@RequestMapping("/surveillance-report")
public class SurveillanceReportController {

    private static final Logger LOGGER = LogManager.getLogger(SurveillanceReportController.class);
    private static final String XLSX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @Autowired
    private ErrorMessageUtil msgUtil;
    @Autowired
    private SurveillanceReportManager reportManager;
    @Autowired
    private ResourcePermissions resourcePermissions;
    @Autowired
    private FileUtils fileUtils;
    @Autowired
    private FF4j ff4j;

    @ApiOperation(value = "Get all annual surveillance reports this user has access to.",
            notes = "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative "
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
            notes = "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative "
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
            notes = "An effort will be made to determine which ACB the report belongs to "
                    + "if one is not provided in the request."
                    + "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative "
                    + "authority on the ACB associated with the report.")
    @RequestMapping(value = "/annual", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public synchronized AnnualReport createAnnualReport(
        @RequestBody(required = true) final AnnualReport createRequest)
                throws AccessDeniedException, InvalidArgumentsException, EntityCreationException {
        if (!ff4j.check(FeatureList.SURVEILLANCE_REPORTING)) {
            throw new NotImplementedException();
        }
        CertificationBody acb = determineAcb(createRequest.getAcb());
        createRequest.setAcb(acb);
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

    @ApiOperation(value = "Download an annaul report as an XLSX file.",
            notes = "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative authority "
                    + "on the ACB associated with the report.")
    @RequestMapping(value = "/export/annual/{annualReportId}", method = RequestMethod.GET)
    public void exportAnnualReport(@PathVariable("annualReportId") final Long annualReportId,
            final HttpServletResponse response) throws EntityRetrievalException, IOException {
        if (!ff4j.check(FeatureList.SURVEILLANCE_REPORTING)) {
            throw new NotImplementedException();
        }

        //create the excel workbook object
        Workbook reportXlsx = reportManager.exportAnnualReport(annualReportId);

        //create a new file in the download directory
        AnnualReportDTO report = reportManager.getAnnualReport(annualReportId);
        String filename = report.getYear() + "-" + report.getAcb().getName() + "-annual-report";
        File tempFileToStream = File.createTempFile(filename, ".xlsx");
        //write out the workbook contents to this file
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(tempFileToStream);
            reportXlsx.write(outputStream);
        } catch (final Exception ex) {
            LOGGER.error("Error writing excel document to file output stream.", ex);
        } finally {
            outputStream.flush();
            outputStream.close();
        }

        fileUtils.streamFileAsResponse(tempFileToStream, XLSX_MIME_TYPE, response);
    }

    @ApiOperation(value = "Get all quarterly surveillance reports this user has access to.",
            notes = "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative "
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
            notes = "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative "
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

    @ApiOperation(value = "Create a new quarterly surveillance report.",
                    notes = "An effort will be made to determine which ACB the report belongs to "
                            + "if one is not provided in the request."
                            + "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative "
                            + "authority on the ACB associated with the report.")
    @RequestMapping(value = "/quarterly", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public synchronized QuarterlyReport createQuarterlyReport(
            @RequestBody(required = true) final QuarterlyReport createRequest) 
    throws AccessDeniedException, InvalidArgumentsException, EntityCreationException {
        if (!ff4j.check(FeatureList.SURVEILLANCE_REPORTING)) {
            throw new NotImplementedException();
        }
        CertificationBody acb = determineAcb(createRequest.getAcb());
        createRequest.setAcb(acb);

        if (createRequest.getAcb() == null || createRequest.getAcb().getId() == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("report.quarterlySurveillance.missingAcb"));
        }

        //create the report
        QuarterlyReportDTO quarterlyReport = new QuarterlyReportDTO();
        AnnualReportDTO associatedAnnualReport = new AnnualReportDTO();
        CertificationBodyDTO associatedAcb = new CertificationBodyDTO();
        associatedAcb.setId(createRequest.getAcb().getId());
        associatedAnnualReport.setAcb(associatedAcb);
        associatedAnnualReport.setYear(createRequest.getYear());
        quarterlyReport.setAnnualReport(associatedAnnualReport);
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

    @ApiOperation(value = "Download a quarterly report as an XLSX file.",
            notes = "Security Restrictions: ROLE_ADMIN or ROLE_ACB and administrative authority "
                    + "on the ACB associated with the report.")
    @RequestMapping(value = "/export/quarterly/{quarterlyReportId}", method = RequestMethod.GET)
    public void exportQuarterlyReport(@PathVariable("quarterlyReportId") final Long quarterlyReportId,
            final HttpServletResponse response) throws EntityRetrievalException, IOException {
        if (!ff4j.check(FeatureList.SURVEILLANCE_REPORTING)) {
            throw new NotImplementedException();
        }

        //create the excel workbook object
        Workbook reportXlsx = reportManager.exportQuarterlyReport(quarterlyReportId);

        //create a new file in the download directory
        QuarterlyReportDTO report = reportManager.getQuarterlyReport(quarterlyReportId);
        String filename = report.getQuarter().getName() + "-" + report.getAnnualReport().getYear()
                    + "-" + report.getAnnualReport().getAcb().getName() + "-quarterly-report";
        File tempFileToStream = File.createTempFile(filename, ".xlsx");
        //write out the workbook contents to this file
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(tempFileToStream);
            reportXlsx.write(outputStream);
        } catch (final Exception ex) {
            LOGGER.error("Error writing excel document to file output stream.", ex);
        } finally {
            outputStream.flush();
            outputStream.close();
        }

        fileUtils.streamFileAsResponse(tempFileToStream, XLSX_MIME_TYPE, response);
    }

    private CertificationBody determineAcb(final CertificationBody requestAcb) {
        CertificationBody result = requestAcb;
        if (requestAcb == null || (requestAcb.getId() == null
                && StringUtils.isEmpty(requestAcb.getName()))) {
            //no acb information was provided - check user permissions to see if
            //we can determine which ACB the user has access to (this is fine if they have only one)
            List<CertificationBodyDTO> allowedAcbs = resourcePermissions.getAllAcbsForCurrentUser();
            if (allowedAcbs != null && allowedAcbs.size() == 1) {
                result = new CertificationBody(allowedAcbs.get(0));
            }
        } else if (requestAcb != null && requestAcb.getId() == null
                && !StringUtils.isEmpty(requestAcb.getName())) {
            //just an ACB name was provided - fill in the ACB object if possible
            List<CertificationBodyDTO> allowedAcbs = resourcePermissions.getAllAcbsForCurrentUser();
            for (CertificationBodyDTO allowedAcb : allowedAcbs) {
                if (requestAcb.getName().equals(allowedAcb.getName())) {
                    result = new CertificationBody(allowedAcb);
                }
            }
        }
        return result;
    }
}
