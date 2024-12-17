package gov.healthit.chpl.scheduler.job;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingReport;
import gov.healthit.chpl.realworldtesting.manager.RealWorldTestingReportService;
import gov.healthit.chpl.report.realworldtesting.RealWorldTestingPlanSummaryReportDao;
import gov.healthit.chpl.report.realworldtesting.RealWorldTestingSummaryReport;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RealWorldTestingSummaryReportCreatorJob extends  QuartzJob {

    @Autowired
    private RealWorldTestingReportService rwtReportService;

    @Autowired
    private RealWorldTestingPlanSummaryReportDao realWorldTestingPlanSummaryReportDao;

    @Autowired
    private CertificationBodyManager certificationBodyManager;
    
    @Autowired
    private JpaTransactionManager txManager;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Real World Report Creator job for " + context.getMergedJobDataMap().getString("email") + " *********");
        try {
            List<Long> activeAcbIds = certificationBodyManager.getAllActive().stream()
                    .map(acb -> acb.getId())
                    .toList();

            List<RealWorldTestingReport> reportRows = rwtReportService.getRealWorldTestingReports(activeAcbIds, LOGGER);
            
            TransactionTemplate txTemplate = new TransactionTemplate(txManager);
            txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            txTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    processRwtPlanCounts(reportRows);
                }
            });

        } catch (Exception e) {
            LOGGER.catching(e);
        } finally {
            LOGGER.info("********* Completed the Real World Report Creator job. *********");
        }

    }

    private void processRwtPlanCounts(List<RealWorldTestingReport> reportRows) {
        Integer rwtEligibilityYear = LocalDate.now().getYear() + 1;

        if (!isDateInPlansSubmissionWindow(LocalDate.now(), rwtEligibilityYear)) {
            LOGGER.info("Out side the RWT Plan submission window.  Not collecting data.");
            return;
        }

        List<RealWorldTestingSummaryReport> rwtSummaryReports = new ArrayList<RealWorldTestingSummaryReport>();

        rwtReportService.getPlansStartDate(rwtEligibilityYear).datesUntil(LocalDate.now()).forEach(reportDate -> {
            certificationBodyManager.getAllActive().forEach(acb -> {
                rwtSummaryReports.add(RealWorldTestingSummaryReport.builder()
                        .reportDate(reportDate)
                        .realWorldTestingYear(rwtEligibilityYear.longValue())
                        .certificationBody(acb)
                        .checkedDate(reportDate)
                        .checkedCount(calculate(reportRows, rwtEligibilityYear, acb, reportDate).longValue())
                        .build());
            });
        });

        rwtSummaryReports.sort(Comparator.comparing(RealWorldTestingSummaryReport::getCheckedDate)
                .thenComparing((o1, o2) -> o1.getCertificationBody().getId().compareTo(o2.getCertificationBody().getId())));

        rwtSummaryReports.forEach(value -> {
            LOGGER.info("{} - {} - {}", value.getCheckedCount(), value.getCheckedDate(), value.getCertificationBody().getName());
            try {
                realWorldTestingPlanSummaryReportDao.save(value);
            } catch (Exception e) {
                LOGGER.error("Could not save RealWorldTestingSummaryReport: {}", value.toString(), e);
            }
        });
    }

    private Integer calculate(List<RealWorldTestingReport> reports, Integer rwtYear, CertificationBody acb, LocalDate checkedDate) {
        return reports.stream()
                .filter(report -> report.getAcbName().equals(acb.getName())
                        && DateUtil.isDateBetweenInclusive(Pair.of(rwtReportService.getPlansStartDate(rwtYear), checkedDate),
                                report.getRwtPlansCheckDate()))
                .toList()
                .size();

    }

    private boolean isDateInPlansSubmissionWindow(LocalDate dateToTest, Integer rwtYear) {
        return DateUtil.isDateBetweenInclusive(Pair.of(rwtReportService.getPlansStartDate(rwtYear), rwtReportService.getPlansLateDate(rwtYear)), dateToTest);
    }
}
