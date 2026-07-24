package gov.healthit.chpl.scheduler.job;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.ff4j.FF4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.developer.search.ActiveListingSearchOptions;
import gov.healthit.chpl.developer.search.DeveloperSearchRequest;
import gov.healthit.chpl.developer.search.DeveloperSearchResult;
import gov.healthit.chpl.developer.search.DeveloperSearchService;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingReport;
import gov.healthit.chpl.realworldtesting.manager.RealWorldTestingReportService;
import gov.healthit.chpl.report.realworldtesting.RealWorldTestingPlanSummaryReportDao;
import gov.healthit.chpl.report.realworldtesting.RealWorldTestingResultsSummaryReportDao;
import gov.healthit.chpl.report.realworldtesting.RealWorldTestingSummaryByAcbReport;
import gov.healthit.chpl.report.realworldtesting.RealWorldTestingSummaryByDeveloperReport;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "realWorldTestingSummaryReportCreatorJobLogger")
public class RealWorldTestingSummaryReportCreatorJob extends QuartzJob {

    @Autowired
    private RealWorldTestingReportService rwtReportService;

    @Autowired
    private RealWorldTestingPlanSummaryReportDao realWorldTestingPlanSummaryReportDao;

    @Autowired
    private RealWorldTestingResultsSummaryReportDao realWorldTestingResultsSummaryReportDao;

    @Autowired
    private CertificationBodyManager certificationBodyManager;

    @Autowired
    private DeveloperSearchService developerSearchService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private FF4j ff4j;

    @Autowired
    private CertificationCriterionService criteriaService;

    private CertificationCriterion g7, g9, g10;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Real World Testing Summary Report Creator job *********");
        g7 = criteriaService.get(Criteria2015.G_7);
        g9 = criteriaService.get(Criteria2015.G_9_CURES);
        g10 = criteriaService.get(Criteria2015.G_10);

        try {
            List<Long> activeAcbIds = certificationBodyManager.getAllActive().stream()
                    .map(acb -> acb.getId())
                    .toList();

            List<RealWorldTestingReport> rwtPlansReports = rwtReportService.getRealWorldTestingReports(activeAcbIds, LOGGER).stream()
                    .collect(Collectors.toList());

            List<RealWorldTestingReport> rwtResultReports = rwtPlansReports.stream()
                    .filter(report -> isRwtResultsRequired(report))
                    .collect(Collectors.toList());

            TransactionOperations transactionOperations = new TransactionTemplate(transactionManager,
                    new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
            transactionOperations.executeWithoutResult(status -> {
                    processRwtPlanCountsByAcb(rwtPlansReports);
                    processRwtResultsCountsByAcb(rwtResultReports);
                    processRwtResultsCountsByDeveloper(rwtResultReports);
            });
        } catch (Exception e) {
            LOGGER.catching(e);
        } finally {
            LOGGER.info("********* Completed the Real World Testing Summary Report Creator job. *********");
        }

    }

    private boolean isRwtResultsRequired(RealWorldTestingReport rwtReport) {
        //RWT Results requirement is only enforced for listings with g7, g9, or g10 pre HTI-5.
        //Post HTI-5 listings with any g-criteria or non-g-criteria using svap have the RWT Results requirement.
        if (ff4j.check(FeatureList.HTI_5_ERD)) {
            return rwtReport.getCriterionAndSvapData().stream()
                .filter(item -> item.isAttested() && (item.isGCriterion() || item.isUsesSvap()))
                .findAny()
                .isPresent();
        } else {
            return rwtReport.getCriterionAndSvapData().stream()
                    .filter(item -> item.getCriterion().getId().equals(g7.getId())
                            || item.getCriterion().getId().equals(g9.getId())
                            || item.getCriterion().getId().equals(g10.getId()))
                    .findAny()
                    .isPresent();
        }
    }

    private void processRwtResultsCountsByAcb(List<RealWorldTestingReport> reportRows) {
        Integer rwtEligibilityYear = LocalDate.now().getYear() - 1;

        if (!isDateInResultsDataCollectionWindow(LocalDate.now(), rwtEligibilityYear)) {
            LOGGER.info("Outside the RWT Results data collection window.  Not collecting data.");
            return;
        }

        List<RealWorldTestingSummaryByAcbReport> rwtSummaryReports = new ArrayList<RealWorldTestingSummaryByAcbReport>();

        rwtReportService.getResultsStartDate(rwtEligibilityYear).datesUntil(LocalDate.now()).forEach(reportDate -> {
            certificationBodyManager.getAllActive().forEach(acb -> {
                Long eligibleListingCountForAcb = reportRows.stream()
                        .filter(row -> row.getAcbName().equals(acb.getName())
                                && row.getRwtEligibilityYear() != null
                                && isListingValidAsOfDate(row.getCertificationDate(), reportDate))
                        .collect(Collectors.counting());

                rwtSummaryReports.add(RealWorldTestingSummaryByAcbReport.builder()
                        .realWorldTestingYear(rwtEligibilityYear.longValue())
                        .certificationBody(acb)
                        .checkedDate(reportDate)
                        .checkedCount(calculateResultsCount(reportRows, rwtEligibilityYear, acb, reportDate).longValue())
                        .requiresCheckCount(eligibleListingCountForAcb)
                        .build());
            });
        });

        rwtSummaryReports.sort(Comparator.comparing(RealWorldTestingSummaryByAcbReport::getCheckedDate)
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

    private void processRwtResultsCountsByDeveloper(List<RealWorldTestingReport> reportRows) {
        Integer rwtEligibilityYear = LocalDate.now().getYear() - 1;

        if (!isDateInResultsDataCollectionWindow(LocalDate.now(), rwtEligibilityYear)) {
            LOGGER.info("Outside the RWT Results data collection window.  Not collecting data.");
            return;
        }

        List<RealWorldTestingSummaryByDeveloperReport> rwtSummaryReports = new ArrayList<RealWorldTestingSummaryByDeveloperReport>();

        List<DeveloperSearchResult> developersWithActiveListings = developerSearchService.getAllPagesOfSearchResults(DeveloperSearchRequest.builder()
                .activeListingsOptions(Stream.of(ActiveListingSearchOptions.HAS_ANY_ACTIVE).collect(Collectors.toSet()))
                .build(), LOGGER);

        rwtReportService.getResultsStartDate(rwtEligibilityYear).datesUntil(LocalDate.now()).forEach(reportDate -> {
            developersWithActiveListings.stream().forEach(dev -> {
                Long eligibleListingCountForDeveloper = reportRows.stream()
                        .filter(row -> row.getDeveloperId().equals(dev.getId())
                                && row.getRwtEligibilityYear() != null
                                && isListingValidAsOfDate(row.getCertificationDate(), reportDate))
                        .collect(Collectors.counting());

                rwtSummaryReports.add(RealWorldTestingSummaryByDeveloperReport.builder()
                        .realWorldTestingYear(rwtEligibilityYear.longValue())
                        .developerId(dev.getId())
                        .developerName(dev.getName())
                        .checkedDate(reportDate)
                        .checkedCount(calculateResultsCount(reportRows, rwtEligibilityYear, dev, reportDate).longValue())
                        .requiresCheckCount(eligibleListingCountForDeveloper)
                        .build());
            });
        });

        rwtSummaryReports.sort(Comparator.comparing(RealWorldTestingSummaryByDeveloperReport::getCheckedDate)
                .thenComparing((o1, o2) -> o1.getDeveloperId().compareTo(o2.getDeveloperId())));

        rwtSummaryReports.forEach(value -> {
            LOGGER.info("{} - {} - {}", value.getCheckedCount(), value.getCheckedDate(), value.getDeveloperName());
            try {
                realWorldTestingResultsSummaryReportDao.save(value);
            } catch (Exception e) {
                LOGGER.error("Could not save RealWorldTestingSummaryReport: {}", value.toString(), e);
            }
        });
        LOGGER.info("Completed gathering RWT Results submissions.");
    }

    private void processRwtPlanCountsByAcb(List<RealWorldTestingReport> reportRows) {
        Integer rwtEligibilityYear = LocalDate.now().getYear() + 1;

        if (!isDateInPlansSubmissionWindow(LocalDate.now(), rwtEligibilityYear)) {
            LOGGER.info("Outside the RWT Plan submission window.  Not collecting data.");
            return;
        }

        List<RealWorldTestingSummaryByAcbReport> rwtSummaryReports = new ArrayList<RealWorldTestingSummaryByAcbReport>();

        rwtReportService.getPlansStartDate(rwtEligibilityYear).datesUntil(LocalDate.now()).forEach(reportDate -> {
            certificationBodyManager.getAllActive().forEach(acb -> {
                Long eligibleListingCountForAcb = reportRows.stream()
                        .filter(row -> row.getAcbName().equals(acb.getName())
                                && row.getRwtEligibilityYear() != null
                                && isListingValidAsOfDate(row.getCertificationDate(), reportDate))
                        .collect(Collectors.counting());

                rwtSummaryReports.add(RealWorldTestingSummaryByAcbReport.builder()
                        .realWorldTestingYear(rwtEligibilityYear.longValue())
                        .certificationBody(acb)
                        .checkedDate(reportDate)
                        .checkedCount(calculatePlanCount(reportRows, rwtEligibilityYear, acb, reportDate).longValue())
                        .requiresCheckCount(eligibleListingCountForAcb)
                        .build());
            });
        });

        rwtSummaryReports.sort(Comparator.comparing(RealWorldTestingSummaryByAcbReport::getCheckedDate)
                .thenComparing((o1, o2) -> o1.getCertificationBody().getId().compareTo(o2.getCertificationBody().getId())));

        rwtSummaryReports.forEach(value -> {
            LOGGER.info("{} - {} - {}", value.getCheckedCount(), value.getCheckedDate(), value.getCertificationBody().getName());
            try {
                realWorldTestingPlanSummaryReportDao.save(value);
            } catch (Exception e) {
                LOGGER.error("Could not save RealWorldTestingSummaryReport: {}", value.toString(), e);
            }
        });
        LOGGER.info("Completed gathering RWT Plan submissions.");
    }

    private Integer calculatePlanCount(List<RealWorldTestingReport> reports, Integer rwtYear, CertificationBody acb, LocalDate checkedDate) {
        return reports.stream()
                .filter(report -> report.getAcbName().equals(acb.getName())
                        && report.getRwtEligibilityYear() != null
                        && DateUtil.isDateBetweenInclusive(Pair.of(rwtReportService.getPlansStartDate(rwtYear), checkedDate),
                                report.getRwtPlansCheckDate()))
                .toList()
                .size();
    }

    private Integer calculateResultsCount(List<RealWorldTestingReport> reports, Integer rwtYear, CertificationBody acb, LocalDate checkedDate) {
        return reports.stream()
                .filter(report -> report.getAcbName().equals(acb.getName())
                        && report.getRwtEligibilityYear() != null
                        && DateUtil.isDateBetweenInclusive(Pair.of(rwtReportService.getResultsStartDate(rwtYear), checkedDate),
                                report.getRwtResultsCheckDate()))
                .toList()
                .size();
    }

    private Integer calculateResultsCount(List<RealWorldTestingReport> reports, Integer rwtYear, DeveloperSearchResult dev, LocalDate checkedDate) {
        return reports.stream()
                .filter(report -> report.getDeveloperId().equals(dev.getId())
                        && report.getRwtEligibilityYear() != null
                        && DateUtil.isDateBetweenInclusive(Pair.of(rwtReportService.getResultsStartDate(rwtYear), checkedDate),
                                report.getRwtResultsCheckDate()))
                .toList()
                .size();
    }

    private boolean isDateInPlansSubmissionWindow(LocalDate dateToTest, Integer rwtYear) {
        return DateUtil.isDateBetweenInclusive(Pair.of(rwtReportService.getPlansStartDate(rwtYear), rwtReportService.getPlansLateDate(rwtYear)), dateToTest);
    }

    private boolean isDateInResultsDataCollectionWindow(LocalDate dateToTest, Integer rwtYear) {
        return DateUtil.isDateBetweenInclusive(Pair.of(rwtReportService.getResultsStartDate(rwtYear), rwtReportService.getResultsDataGatheringStopDate(rwtYear)), dateToTest);
    }

    private Boolean isListingValidAsOfDate(LocalDate listingCertificationDate, LocalDate date) {
        return date.isAfter(listingCertificationDate)
                || date.equals(listingCertificationDate);
    }
}
