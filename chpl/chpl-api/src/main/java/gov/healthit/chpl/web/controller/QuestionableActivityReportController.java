package gov.healthit.chpl.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.report.ReportDataManager;
import gov.healthit.chpl.report.questionableActivity.QuestionableActivityReport;
import gov.healthit.chpl.util.LogMethodUsage;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Tag(name = "report-data/questionable-activity", description = "Allows retrieval of data used by Questionable Activity Report.")
@RestController
@RequestMapping("/report-data/questionable-activity")
public class QuestionableActivityReportController {
    private ReportDataManager reportDataManager;

    @Autowired
    public QuestionableActivityReportController(ReportDataManager reportDataManager) {
        this.reportDataManager = reportDataManager;
    }

    @Operation(summary = "Retrieves the data used to generate the Questionable Activity Report.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @LogMethodUsage
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<QuestionableActivityReport> getQuestionableActivityReports() {
        return reportDataManager.getQuestionableActivityService().getQuestionableActivityReports();
    }
}