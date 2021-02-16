package gov.healthit.chpl.job;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dto.job.JobDTO;
import gov.healthit.chpl.entity.job.JobStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.surveillance.report.SurveillanceReportManager;
import gov.healthit.chpl.surveillance.report.builder.QuarterlyReportBuilderXlsx;
import gov.healthit.chpl.surveillance.report.builder.ReportBuilderFactory;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
@Scope("prototype") // tells spring to make a new instance of this class every
// time it is needed
public class ExportQuarterlySurveillanceReportJob extends RunnableJob {
    private static final Logger LOGGER = LogManager.getLogger(ExportQuarterlySurveillanceReportJob.class);
    private ErrorMessageUtil errorMessageUtil;
    private SurveillanceReportManager reportManager;
    private ReportBuilderFactory reportBuilderFactory;

    @Autowired
    public ExportQuarterlySurveillanceReportJob(ErrorMessageUtil errorMessageUtil,
            SurveillanceReportManager reportManager,
            ReportBuilderFactory reportBuilderFactory) {
        this.errorMessageUtil = errorMessageUtil;
        this.reportManager = reportManager;
        this.reportBuilderFactory = reportBuilderFactory;
    }

    public ExportQuarterlySurveillanceReportJob() {
        LOGGER.debug("Created new Export Quarterly Report Job Job");
    }

    public ExportQuarterlySurveillanceReportJob(JobDTO job) {
        LOGGER.debug("Created new Export Quarterly Report Job");
        this.job = job;
    }

    @Override
    @Transactional
    public void run() {
        super.run();

        File writtenFile = null;
        String quarterlyReportIdStr = job.getData();
        Long quarterlyReportId = null;
        try {
            quarterlyReportId = new Long(quarterlyReportIdStr);
        } catch (NumberFormatException ex) {
            String msg = errorMessageUtil.getMessage("report.quarterlySurveillance.export.badIdFormat", quarterlyReportIdStr);
            LOGGER.error(msg);
            addJobMessage(msg);
            updateStatus(100, JobStatusType.Error);
        }

        QuarterlyReportDTO report = null;
        Workbook workbook = null;
        try {
            report = reportManager.getQuarterlyReport(quarterlyReportId);
            if (report != null) {
                QuarterlyReportBuilderXlsx reportBuilder = reportBuilderFactory.getReportBuilder(report);
                if (reportBuilder != null) {
                    workbook = reportBuilder.buildXlsx(report);
                } else {
                    String msg = errorMessageUtil.getMessage("report.quarterlySurveillance.builderNotFound");
                    LOGGER.error(msg + " Report id " + quarterlyReportId);
                    addJobMessage(msg);
                    updateStatus(100, JobStatusType.Error);
                }
            }
        } catch (EntityRetrievalException ex) {
            String msg = errorMessageUtil.getMessage("report.quarterlySurveillance.export.badId", quarterlyReportId);
            LOGGER.error(msg);
            addJobMessage(msg);
            updateStatus(100, JobStatusType.Error);
        } catch (IOException io) {
            String msg = errorMessageUtil.getMessage("report.quarterlySurveillance.export.builder.buildError");
            LOGGER.error(msg);
            addJobMessage(msg);
            updateStatus(100, JobStatusType.Error);
        } catch (Exception general) {
            //catch any other type of exception
            String msg = errorMessageUtil.getMessage("report.annualSurveillance.export.builder.buildError");
            LOGGER.error(msg);
            addJobMessage(msg);
            updateStatus(100, JobStatusType.Error);
        }

        if (workbook != null && report != null) {
            updateStatus(75, JobStatusType.In_Progress);
            String filename = report.getQuarter().getName() + "-" + report.getYear()
                        + "-" + report.getAcb().getName() + "-quarterly-report";
            //write out the workbook contents to this file
            OutputStream outputStream = null;
            try {
                writtenFile = File.createTempFile(filename, ".xlsx");
                outputStream = new FileOutputStream(writtenFile);
                LOGGER.info("Writing quarterly report file to " + writtenFile.getAbsolutePath());
                workbook.write(outputStream);
                updateStatus(90, JobStatusType.In_Progress);
            } catch (final Exception ex) {
                String msg = errorMessageUtil.getMessage("report.quarterlySurveillance.export.writeError");
                LOGGER.error(msg);
                addJobMessage(msg);
                updateStatus(100, JobStatusType.Error);
            } finally {
                try { outputStream.flush(); } catch (Exception ignore) {}
                try { outputStream.close(); } catch (Exception ignore) {}
            }
        } else {
            updateStatus(100, JobStatusType.Error);
        }

        List<File> fileAttachments = new ArrayList<File>();
        if (writtenFile != null) {
            fileAttachments.add(writtenFile);
        }
        this.complete(fileAttachments);
    }

    public SurveillanceReportManager getReportManager() {
        return reportManager;
    }

    public void setReportManager(final SurveillanceReportManager reportManager) {
        this.reportManager = reportManager;
    }
}
