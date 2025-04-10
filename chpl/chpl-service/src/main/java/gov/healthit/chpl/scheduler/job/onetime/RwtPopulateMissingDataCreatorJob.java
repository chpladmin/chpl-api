package gov.healthit.chpl.scheduler.job.onetime;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
import gov.healthit.chpl.report.realworldtesting.RealWorldTestingResultsSummaryReportDao;
import gov.healthit.chpl.report.realworldtesting.RealWorldTestingSummaryReport;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "rwtPopulateMissingDataCreatorJobLogger")
public class RwtPopulateMissingDataCreatorJob extends QuartzJob {

        @Autowired
        private RealWorldTestingReportService rwtReportService;

        @Autowired
        private RealWorldTestingResultsSummaryReportDao realWorldTestingResultsSummaryReportDao;

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
                        processRwtResultsCounts(reportRows);
                    }
                });

            } catch (Exception e) {
                LOGGER.catching(e);
            } finally {
                LOGGER.info("********* Completed the Real World Report Creator job. *********");
            }

        }

        private void processRwtResultsCounts(List<RealWorldTestingReport> reportRows) {
            Integer rwtEligibilityYear = LocalDate.now().getYear() - 1;
            LocalDate lastDayOfResultsSubmissionWindow = rwtReportService.getResultsLateDate(rwtEligibilityYear);

            if (!isDateInResultsSubmissionWindow(lastDayOfResultsSubmissionWindow, rwtEligibilityYear)) {
                LOGGER.info("Outside the RWT Results submission window.  Not collecting data.");
                return;
            }

            List<RealWorldTestingSummaryReport> rwtSummaryReports = new ArrayList<RealWorldTestingSummaryReport>();

            rwtReportService.getResultsStartDate(rwtEligibilityYear).datesUntil(lastDayOfResultsSubmissionWindow.plusDays(1)).forEach(reportDate -> {
                certificationBodyManager.getAllActive().forEach(acb -> {
                    Long eligibleListingCountForAcb = reportRows.stream()
                            .filter(row -> row.getAcbName().equals(acb.getName())
                                    && row.getRwtEligibilityYear() != null
                                    && isListingValidAsOfDate(row.getCertificationDate(), reportDate))
                            .collect(Collectors.counting());

                    rwtSummaryReports.add(RealWorldTestingSummaryReport.builder()
                            .realWorldTestingYear(rwtEligibilityYear.longValue())
                            .certificationBody(acb)
                            .checkedDate(reportDate)
                            .checkedCount(calculateResulltsCount(reportRows, rwtEligibilityYear, acb, reportDate).longValue())
                            .requiresCheckCount(eligibleListingCountForAcb)
                            .build());
                });
            });

            rwtSummaryReports.sort(Comparator.comparing(RealWorldTestingSummaryReport::getCheckedDate)
                    .thenComparing((o1, o2) -> o1.getCertificationBody().getId().compareTo(o2.getCertificationBody().getId())));

            rwtSummaryReports.forEach(value -> {
                LOGGER.info("{} - {} - {}", value.getCheckedCount(), value.getCheckedDate(), value.getCertificationBody().getName());
                try {
                    realWorldTestingResultsSummaryReportDao.save(value);
                } catch (Exception e) {
                    LOGGER.error("Could not save RealWorldTestingSummaryReport: {}", value.toString(), e);
                }
            });
            LOGGER.info("Completed gathering RWT Results submissions.");
        }

       private Integer calculateResulltsCount(List<RealWorldTestingReport> reports, Integer rwtYear, CertificationBody acb, LocalDate checkedDate) {
            return reports.stream()
                    .filter(report -> report.getAcbName().equals(acb.getName())
                            && report.getRwtEligibilityYear() != null
                            && DateUtil.isDateBetweenInclusive(Pair.of(rwtReportService.getResultsStartDate(rwtYear), checkedDate),
                                    report.getRwtResultsCheckDate()))
                    .toList()
                    .size();
        }

        private boolean isDateInResultsSubmissionWindow(LocalDate dateToTest, Integer rwtYear) {
            return DateUtil.isDateBetweenInclusive(Pair.of(rwtReportService.getResultsStartDate(rwtYear), rwtReportService.getResultsLateDate(rwtYear)), dateToTest);
        }

        private Boolean isListingValidAsOfDate(LocalDate listingCertificationDate, LocalDate date) {
            return date.isAfter(listingCertificationDate)
                    || date.equals(listingCertificationDate);
        }

}
