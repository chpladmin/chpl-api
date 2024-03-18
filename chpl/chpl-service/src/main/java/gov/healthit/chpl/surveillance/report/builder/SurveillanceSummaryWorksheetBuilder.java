package gov.healthit.chpl.surveillance.report.builder;

import java.awt.Color;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.BorderExtent;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PropertyTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.surveillance.SurveillanceType;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.surveillance.report.ComplaintSummaryDAO;
import gov.healthit.chpl.surveillance.report.SurveillanceReportManager;
import gov.healthit.chpl.surveillance.report.SurveillanceSummaryDAO;
import gov.healthit.chpl.surveillance.report.domain.PrivilegedSurveillance;
import gov.healthit.chpl.surveillance.report.domain.QuarterlyReport;
import gov.healthit.chpl.surveillance.report.domain.RelevantListing;
import gov.healthit.chpl.surveillance.report.domain.SurveillanceSummary;
import gov.healthit.chpl.util.DateUtil;

@Component
public class SurveillanceSummaryWorksheetBuilder {
    private static final int LAST_DATA_COLUMN = 10;
    private static final int LAST_DATA_ROW = 60;
    private static final String PROC_TYPE_IN_THE_FIELD = "In-the-Field";
    private static final String PROC_TYPE_CONTROLLED = "Controlled/Test Environment";
    private static final String PROC_TYPE_CORRESPONDENCE_WITH_COMP_DEV = "Correspondence with Complainant/Developer";
    private static final String PROC_TYPE_CORRESPONDENCE_WITH_DEV = "Correspondence with Developer";
    private static final String PROC_TYPE_CORRESPONDENCE_WITH_COMP = "Correspondence with Complainant";
    private static final String PROC_TYPE_CORRESPONDENCE_WITH_USER = "Correspondence with End User";
    private static final String PROC_TYPE_REVIEW = "Review of Websites/Written Documentation";
    private static final String PROC_TYPE_OTHER = "Other";

    private static final String OUTCOME_TYPE_NO_NC = "No non-conformity";
    private static final String OUTCOME_TYPE_NC = "Non-conformity substantiated";
    private static final String OUTCOME_TYPE_CAP = "Resolved through corrective action";

    private CertifiedProductDetailsManager detailsManager;
    private SurveillanceReportManager survReportManager;
    private SurveillanceSummaryDAO survSummaryDao;
    private ComplaintSummaryDAO complaintSummaryDao;
    private PropertyTemplate pt;

    @Autowired
    public SurveillanceSummaryWorksheetBuilder(CertifiedProductDetailsManager detailsManager,
            SurveillanceReportManager survReportManager,
            SurveillanceSummaryDAO survSummaryDao,
            ComplaintSummaryDAO complaintSummaryDao) {
        this.detailsManager = detailsManager;
        this.survReportManager= survReportManager;
        this.survSummaryDao = survSummaryDao;
        this.complaintSummaryDao = complaintSummaryDao;
    }

    public int getLastDataColumn() {
        return LAST_DATA_COLUMN;
    }

    public int getLastDataRow() {
        return LAST_DATA_ROW;
    }

    public Sheet buildWorksheet(SurveillanceReportWorkbookWrapper workbook, List<QuarterlyReport> reports, Logger logger) throws IOException {
        pt = new PropertyTemplate();

        //create sheet
        Sheet sheet = workbook.getSheet("Surveillance Summary", new Color(196, 215, 155), getLastDataColumn());

        //set some styling that applies to the whole sheet
        sheet.setDisplayGridlines(false);
        sheet.setDisplayRowColHeadings(false);

        //columns B, C, D, and E need a certain width to match the document format
        sheet.setColumnWidth(1, workbook.getColumnWidth(65));
        int colWidth = workbook.getColumnWidth(12);
        sheet.setColumnWidth(2,  colWidth);
        sheet.setColumnWidth(3, colWidth);
        sheet.setColumnWidth(4, colWidth);

        //column G needs a certain width to match the document format
        sheet.setColumnWidth(6, workbook.getColumnWidth(40));

        addSurveillanceCounts(workbook, sheet, reports, logger);
        addComplaintsCounts(workbook, sheet, reports);

        //apply the borders after the sheet has been created
        pt.applyBorders(sheet);
        return sheet;
    }

    private void addSurveillanceCounts(SurveillanceReportWorkbookWrapper workbook, Sheet sheet, List<QuarterlyReport> reports, Logger logger) {
        //the reports must all be for the same ACB so just take the acb in the first one
        Long acbId = reports.get(0).getAcb().getId();
        //find the date range encompassing all the reports
        LocalDate startDate = reports.get(0).getStartDay();
        LocalDate endDate = reports.get(0).getEndDay();
        for (QuarterlyReport report : reports) {
            if (report.getStartDay().isBefore(startDate)) {
                startDate = report.getStartDay();
            }
            if (report.getEndDay().isAfter(endDate)) {
                endDate = report.getEndDay();
            }
        }
        logger.info("Generating Surveillance Summary data for ACB " + acbId + " from " + startDate + " to " + endDate);
        List<RelevantListing> relevantListings = survReportManager.getRelevantListings(reports);
        logger.info("Found " + relevantListings.size() + " relevant listings.");

        Row row = workbook.getRow(sheet, 1);
        Cell cell = workbook.createCell(row, 1, workbook.getRightAlignedTableHeadingStyle());
        cell.setCellValue("");
        cell = workbook.createCell(row, 2, workbook.getRightAlignedTableHeadingStyle());
        cell.setCellValue("Reactive");
        cell = workbook.createCell(row, 3, workbook.getRightAlignedTableHeadingStyle());
        cell.setCellValue("Randomized");
        cell = workbook.createCell(row, 4, workbook.getRightAlignedTableHeadingStyle());
        cell.setCellValue("Total");

        logger.info("Getting count of listings surveilled by surveillance type.");
        createSurveillanceCountsSubheadingRow(workbook, sheet, "Surveillance Counts", 2);
        //number of listings that had an open surveillance during the period of time the reports cover
        SurveillanceSummary listingSummary =
                survSummaryDao.getCountOfListingsSurveilledByType(acbId, startDate, endDate);
        createSurveillanceCountsDataRow(workbook, sheet, "Number of Certificates Surveilled",
                listingSummary.getReactiveCount(), listingSummary.getRandomizedCount(),
                listingSummary.getReactiveCount() + listingSummary.getRandomizedCount(), 3);

        //number of surveillances open during the period of time the reports cover using the listed process
        //a surveillance could potentially have one process during one report period
        //and a different process during another report period so in that case
        //the surveillance would be counted in more than one row
        createSurveillanceCountsSubheadingRow(workbook, sheet, "Primary Surveillance Processes", 4);

        logger.info("Getting count of surveillances using process type " + PROC_TYPE_IN_THE_FIELD);
        Long reactiveCount = getCountOfSurveillanceProcessTypesBySurveillanceType(relevantListings, SurveillanceType.REACTIVE, PROC_TYPE_IN_THE_FIELD);
        Long randomizedCount = getCountOfSurveillanceProcessTypesBySurveillanceType(relevantListings, SurveillanceType.RANDOMIZED, PROC_TYPE_IN_THE_FIELD);
        createSurveillanceCountsDataRow(workbook, sheet, PROC_TYPE_IN_THE_FIELD,
                reactiveCount, randomizedCount, reactiveCount + randomizedCount, 5);

        logger.info("Getting count of surveillances using process type " + PROC_TYPE_CONTROLLED);
        reactiveCount = getCountOfSurveillanceProcessTypesBySurveillanceType(relevantListings, SurveillanceType.REACTIVE, PROC_TYPE_CONTROLLED);
        randomizedCount = getCountOfSurveillanceProcessTypesBySurveillanceType(relevantListings, SurveillanceType.RANDOMIZED, PROC_TYPE_CONTROLLED);
        createSurveillanceCountsDataRow(workbook, sheet, PROC_TYPE_CONTROLLED,
                reactiveCount, randomizedCount, reactiveCount + randomizedCount, 6);

        logger.info("Getting count of surveillances using process type " + PROC_TYPE_CORRESPONDENCE_WITH_COMP_DEV);
        reactiveCount = getCountOfSurveillanceProcessTypesBySurveillanceType(relevantListings, SurveillanceType.REACTIVE, PROC_TYPE_CORRESPONDENCE_WITH_COMP_DEV);
        randomizedCount = getCountOfSurveillanceProcessTypesBySurveillanceType(relevantListings, SurveillanceType.RANDOMIZED, PROC_TYPE_CORRESPONDENCE_WITH_COMP_DEV);
        createSurveillanceCountsDataRow(workbook, sheet, PROC_TYPE_CORRESPONDENCE_WITH_COMP_DEV,
                reactiveCount, randomizedCount, reactiveCount + randomizedCount, 7);

        logger.info("Getting count of surveillances using process type " + PROC_TYPE_CORRESPONDENCE_WITH_COMP);
        reactiveCount = getCountOfSurveillanceProcessTypesBySurveillanceType(relevantListings, SurveillanceType.REACTIVE, PROC_TYPE_CORRESPONDENCE_WITH_COMP);
        randomizedCount = getCountOfSurveillanceProcessTypesBySurveillanceType(relevantListings, SurveillanceType.RANDOMIZED, PROC_TYPE_CORRESPONDENCE_WITH_COMP);
        createSurveillanceCountsDataRow(workbook, sheet, PROC_TYPE_CORRESPONDENCE_WITH_COMP,
                reactiveCount, randomizedCount, reactiveCount + randomizedCount, 8);

        logger.info("Getting count of surveillances using process type " + PROC_TYPE_CORRESPONDENCE_WITH_DEV);
        reactiveCount = getCountOfSurveillanceProcessTypesBySurveillanceType(relevantListings, SurveillanceType.REACTIVE, PROC_TYPE_CORRESPONDENCE_WITH_DEV);
        randomizedCount = getCountOfSurveillanceProcessTypesBySurveillanceType(relevantListings, SurveillanceType.RANDOMIZED, PROC_TYPE_CORRESPONDENCE_WITH_DEV);
        createSurveillanceCountsDataRow(workbook, sheet, PROC_TYPE_CORRESPONDENCE_WITH_DEV,
                reactiveCount, randomizedCount, reactiveCount + randomizedCount, 9);

        logger.info("Getting count of surveillances using process type " + PROC_TYPE_CORRESPONDENCE_WITH_USER);
        reactiveCount = getCountOfSurveillanceProcessTypesBySurveillanceType(relevantListings, SurveillanceType.REACTIVE, PROC_TYPE_CORRESPONDENCE_WITH_USER);
        randomizedCount = getCountOfSurveillanceProcessTypesBySurveillanceType(relevantListings, SurveillanceType.RANDOMIZED, PROC_TYPE_CORRESPONDENCE_WITH_USER);
        createSurveillanceCountsDataRow(workbook, sheet, PROC_TYPE_CORRESPONDENCE_WITH_USER,
                reactiveCount, randomizedCount, reactiveCount + randomizedCount, 10);

        logger.info("Getting count of surveillances using process type " + PROC_TYPE_REVIEW);
        reactiveCount = getCountOfSurveillanceProcessTypesBySurveillanceType(relevantListings, SurveillanceType.REACTIVE, PROC_TYPE_REVIEW);
        randomizedCount = getCountOfSurveillanceProcessTypesBySurveillanceType(relevantListings, SurveillanceType.RANDOMIZED, PROC_TYPE_REVIEW);
        createSurveillanceCountsDataRow(workbook, sheet, PROC_TYPE_REVIEW,
                reactiveCount, randomizedCount, reactiveCount + randomizedCount, 11);

        logger.info("Getting count of surveillances using process type " + PROC_TYPE_OTHER);
        reactiveCount = getCountOfSurveillanceProcessTypesBySurveillanceType(relevantListings, SurveillanceType.REACTIVE, PROC_TYPE_OTHER);
        randomizedCount = getCountOfSurveillanceProcessTypesBySurveillanceType(relevantListings, SurveillanceType.RANDOMIZED, PROC_TYPE_OTHER);
        createSurveillanceCountsDataRow(workbook, sheet, PROC_TYPE_OTHER,
                reactiveCount, randomizedCount, reactiveCount + randomizedCount, 12);

        //number of surveillances open during the period of time the reports cover with the listed outcome
        //a surveillance could potentially have one outcome during one report period
        //and a different outcome during another report period so in that case
        //the surveillance would be counted in more than one row
        createSurveillanceCountsSubheadingRow(workbook, sheet, "Outcome of the Surveillance", 13);

        logger.info("Getting count of surveillances with outcome " + OUTCOME_TYPE_NO_NC);
        reactiveCount = getCountOfSurveillanceOutcomesBySurveillanceType(relevantListings, SurveillanceType.REACTIVE, OUTCOME_TYPE_NO_NC);
        randomizedCount = getCountOfSurveillanceOutcomesBySurveillanceType(relevantListings, SurveillanceType.RANDOMIZED, OUTCOME_TYPE_NO_NC);
        createSurveillanceCountsDataRow(workbook, sheet, "Number of Surveillance with No Non-Conformities Found",
                reactiveCount, randomizedCount, reactiveCount + randomizedCount, 14);


        logger.info("Getting count of surveillances with outcome " + OUTCOME_TYPE_NC);
        reactiveCount = getCountOfSurveillanceOutcomesBySurveillanceType(relevantListings, SurveillanceType.REACTIVE, OUTCOME_TYPE_NC);
        randomizedCount = getCountOfSurveillanceOutcomesBySurveillanceType(relevantListings, SurveillanceType.RANDOMIZED, OUTCOME_TYPE_NC);
        createSurveillanceCountsDataRow(workbook, sheet, "Number of Surveillance with Non-Conformities Found",
                reactiveCount, randomizedCount, reactiveCount + randomizedCount, 15);

        logger.info("Getting count of surveillances with outcome " + OUTCOME_TYPE_CAP);
        reactiveCount = getCountOfSurveillanceOutcomesBySurveillanceType(relevantListings, SurveillanceType.REACTIVE, OUTCOME_TYPE_CAP);
        randomizedCount = getCountOfSurveillanceOutcomesBySurveillanceType(relevantListings, SurveillanceType.RANDOMIZED, OUTCOME_TYPE_CAP);
        createSurveillanceCountsDataRow(workbook, sheet, "Number of Corrective Action Plans",
                reactiveCount, randomizedCount, reactiveCount + randomizedCount, 16);

        //number of listings with open surveillance during the period of time the reports
        //cover with the listed resultant status
        //a listing could have one resultant status during one report period
        //and a different resultant status during another report period so in that case
        //the listing would be counted in more than one row
        createSurveillanceCountsSubheadingRow(workbook, sheet, "Certification Status Resultant of Surveillance", 17);

        //get the listings with relevant surveillance of each type
        List<RelevantListing> listingsWithReactive = getListingsBySurveillanceType(relevantListings, SurveillanceType.REACTIVE);
        List<RelevantListing> listingsWithRandomized = getListingsBySurveillanceType(relevantListings, SurveillanceType.RANDOMIZED);

        //need to get the certification status events of each listing
        //to determine it's status during the surveillance
        List<CertificationStatusType> listingStatuses = new ArrayList<CertificationStatusType>();
        listingStatuses.add(CertificationStatusType.Active);
        long reactiveSurvActiveStatusCount = 0;
        for (RelevantListing listing : listingsWithReactive) {
            if (determineIfListingHadStatusDuringRelevantSurveillance(listing, listingStatuses)) {
                reactiveSurvActiveStatusCount++;
            }
        }
        long randomizedSurvActiveStatusCount = 0;
        for (RelevantListing listing : listingsWithRandomized) {
            if (determineIfListingHadStatusDuringRelevantSurveillance(listing, listingStatuses)) {
                randomizedSurvActiveStatusCount++;
            }
        }
        createSurveillanceCountsDataRow(workbook, sheet, "Number of Certificates Active",
                reactiveSurvActiveStatusCount, randomizedSurvActiveStatusCount,
                reactiveSurvActiveStatusCount + randomizedSurvActiveStatusCount, 18);

        listingStatuses.clear();
        listingStatuses.add(CertificationStatusType.SuspendedByAcb);
        listingStatuses.add(CertificationStatusType.SuspendedByOnc);
        long reactiveSurvSuspendedStatusCount = 0;
        for (RelevantListing listing : listingsWithReactive) {
            if (determineIfListingHadStatusDuringRelevantSurveillance(listing, listingStatuses)) {
                reactiveSurvSuspendedStatusCount++;
            }
        }
        long randomizedSurvSuspendedStatusCount = 0;
        for (RelevantListing listing : listingsWithRandomized) {
            if (determineIfListingHadStatusDuringRelevantSurveillance(listing, listingStatuses)) {
                randomizedSurvSuspendedStatusCount++;
            }
        }
        createSurveillanceCountsDataRow(workbook, sheet, "Number of Certificates Suspended",
                reactiveSurvSuspendedStatusCount, randomizedSurvSuspendedStatusCount,
                reactiveSurvSuspendedStatusCount + randomizedSurvSuspendedStatusCount, 19);

        listingStatuses.clear();
        listingStatuses.add(CertificationStatusType.WithdrawnByDeveloper);
        listingStatuses.add(CertificationStatusType.WithdrawnByDeveloperUnderReview);
        long reactiveSurvWithdrawnStatusCount = 0;
        for (RelevantListing listing : listingsWithReactive) {
            if (determineIfListingHadStatusDuringRelevantSurveillance(listing, listingStatuses)) {
                reactiveSurvWithdrawnStatusCount++;
            }
        }
        long randomizedSurvWithdrawnStatusCount = 0;
        for (RelevantListing listing : listingsWithRandomized) {
            if (determineIfListingHadStatusDuringRelevantSurveillance(listing, listingStatuses)) {
                randomizedSurvWithdrawnStatusCount++;
            }
        }
        createSurveillanceCountsDataRow(workbook, sheet, "Number of Certificates Withdrawn by Developer",
                reactiveSurvWithdrawnStatusCount, randomizedSurvWithdrawnStatusCount,
                reactiveSurvWithdrawnStatusCount + randomizedSurvWithdrawnStatusCount, 20);

        listingStatuses.clear();
        listingStatuses.add(CertificationStatusType.WithdrawnByAcb);
        long reactiveSurvWithdrawnAcbStatusCount = 0;
        for (RelevantListing listing : listingsWithReactive) {
            if (determineIfListingHadStatusDuringRelevantSurveillance(listing, listingStatuses)) {
                reactiveSurvWithdrawnAcbStatusCount++;
            }
        }
        long randomizedSurvWithdrawnAcbStatusCount = 0;
        for (RelevantListing listing : listingsWithRandomized) {
            if (determineIfListingHadStatusDuringRelevantSurveillance(listing, listingStatuses)) {
                randomizedSurvWithdrawnAcbStatusCount++;
            }
        }
        createSurveillanceCountsDataRow(workbook, sheet, "Number of Certificates Withdrawn by ONC-ACB",
                reactiveSurvWithdrawnAcbStatusCount, randomizedSurvWithdrawnAcbStatusCount,
                reactiveSurvWithdrawnAcbStatusCount + randomizedSurvWithdrawnAcbStatusCount, 21);

        pt.drawBorders(new CellRangeAddress(1, 21, 1, 4),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
    }

    private boolean determineIfListingHadStatusDuringRelevantSurveillance(RelevantListing listing,
            List<CertificationStatusType> statusesToCheck) {
        boolean result = false;
        //get the certification events for the listing
        List<CertificationStatusEvent> statusEvents = new ArrayList<CertificationStatusEvent>();
        try {
            statusEvents = detailsManager.getCertificationStatusEvents(listing.getId());
        } catch (EntityRetrievalException ex) {}
        CertifiedProductSearchDetails details = new CertifiedProductSearchDetails();
        details.setId(listing.getId());
        details.setCertificationEvents(statusEvents);

        //check each relevant surveillance to see if the listing's status matches
        //the status to check on the end date of the surveillance
        for (PrivilegedSurveillance surv : listing.getSurveillances()) {
            String resultantStatus = determineResultantCertificationStatus(details, surv);
            for (CertificationStatusType statusToCheck : statusesToCheck) {
                if (statusToCheck.getName().equals(resultantStatus)) {
                    result = true;
                }
            }
        }
        return result;
    }

    private String determineResultantCertificationStatus(CertifiedProductSearchDetails listing,
            PrivilegedSurveillance surv) {
        String result = "";
        if (surv.getEndDay() == null) {
            result = listing.getCurrentStatus().getStatus().getName();
        } else {
            CertificationStatusEvent statusEvent = listing.getStatusOnDate(new Date(DateUtil.toEpochMillisEndOfDay(surv.getEndDay())));
            if (statusEvent != null) {
                result = statusEvent.getStatus().getName();
            }
        }
        return result;
    }

    private void addComplaintsCounts(SurveillanceReportWorkbookWrapper workbook, Sheet sheet,
            List<QuarterlyReport> reports) {
        //the reports must all be for the same ACB so just take the acb in the first one
        Long acbId = reports.get(0).getAcb().getId();
        //find the date range encompassing all the reports
        LocalDate startDate = reports.get(0).getStartDay();
        LocalDate endDate = reports.get(0).getEndDay();
        for (QuarterlyReport report : reports) {
            if (report.getStartDay().isBefore(startDate)) {
                startDate = report.getStartDay();
            }
            if (report.getEndDay().isAfter(endDate)) {
                endDate = report.getEndDay();
            }
        }

        Row row = workbook.getRow(sheet, 1);
        Cell cell = workbook.createCell(row, 6, workbook.getRightAlignedTableHeadingStyle());
        cell.setCellValue("");
        cell = workbook.createCell(row, 7, workbook.getRightAlignedTableHeadingStyle());
        cell.setCellValue("Total");

        createComplaintCountsSubheadingRow(workbook, sheet, "Complaint Counts", 2);
        Long totalComplaintsReceived = complaintSummaryDao.getTotalComplaints(acbId, startDate, endDate);
        createComplaintCountsDataRow(workbook, sheet, "Total Number Received", totalComplaintsReceived, 3);
        Long totalComplaintsFromOnc = complaintSummaryDao.getTotalComplaintsFromOnc(acbId, startDate, endDate);
        createComplaintCountsDataRow(workbook, sheet, "Total Number Referred by ONC", totalComplaintsFromOnc, 4);
        createComplaintCountsSubheadingRow(workbook, sheet, "Surveillance Activities Trigged by Complaints", 5);
        Long totalComplaintsWithSurv = complaintSummaryDao.getTotalComplaintsResultingInSurveillance(acbId, startDate, endDate);
        createComplaintCountsDataRow(workbook, sheet, "Number that Led to Surveillance Activities", totalComplaintsWithSurv, 6);
        Long totalSurvFromComplaints = complaintSummaryDao.getTotalSurveillanceRelatedToComplaints(acbId, startDate, endDate);
        createComplaintCountsDataRow(workbook, sheet, "Number of Surveillance Activities", totalSurvFromComplaints, 7);
        createComplaintCountsSubheadingRow(workbook, sheet, "Non-Conformities Found by Complaints", 8);
        Long totalComplaintsResultingInNonconformities =
                complaintSummaryDao.getTotalComplaintsResultingInNonconformities(acbId, startDate, endDate);
        createComplaintCountsDataRow(workbook, sheet, "Number that Led to Non-conformities", totalComplaintsResultingInNonconformities, 9);
        Long totalNonconformitiesFromComplaints =
                complaintSummaryDao.getTotalNonconformitiesRelatedToComplaints(acbId, startDate, endDate);
        createComplaintCountsDataRow(workbook, sheet, "Number of Non-conformities", totalNonconformitiesFromComplaints, 10);

        //dark outside border around the whole table
        pt.drawBorders(new CellRangeAddress(1, 10, 6, 7),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
    }

    private void createSurveillanceCountsSubheadingRow(SurveillanceReportWorkbookWrapper workbook,
            Sheet sheet, String text, int rowNum) {
        Row row = workbook.getRow(sheet, rowNum);
        Cell cell = workbook.createCell(row, 1, workbook.getTableSubheadingStyle());
        cell.setCellValue(text);
        cell = workbook.createCell(row, 2, workbook.getTableSubheadingStyle());
        cell = workbook.createCell(row, 3, workbook.getTableSubheadingStyle());
        cell = workbook.createCell(row, 4, workbook.getTableSubheadingStyle());
        //light top and bottom borders around each subheading
        pt.drawBorders(new CellRangeAddress(rowNum, rowNum, 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE_HORIZONTAL);
    }

    private void createSurveillanceCountsDataRow(SurveillanceReportWorkbookWrapper workbook,
            Sheet sheet, String name, long reactiveValue, long randomziedValue, long totalValue, int rowNum) {
        Row row = workbook.getRow(sheet, rowNum);
        Cell cell = workbook.createCell(row, 1);
        cell.setCellValue(name);
        cell = workbook.createCell(row, 2);
        cell.setCellValue(reactiveValue);
        cell = workbook.createCell(row, 3);
        cell.setCellValue(randomziedValue);
        cell = workbook.createCell(row, 4);
        cell.setCellValue(totalValue);
        //dotted top and bottom borders around each data field
        pt.drawBorders(new CellRangeAddress(rowNum, rowNum, 1, 4),
                BorderStyle.HAIR, BorderExtent.OUTSIDE_HORIZONTAL);
    }

    private void createComplaintCountsSubheadingRow(SurveillanceReportWorkbookWrapper workbook,
            Sheet sheet, String text, int rowNum) {
        Row row = workbook.getRow(sheet, rowNum);
        Cell cell = workbook.createCell(row, 6, workbook.getTableSubheadingStyle());
        cell.setCellValue(text);
        cell = workbook.createCell(row, 7, workbook.getTableSubheadingStyle());
       //light top and bottom borders around each subheading
        pt.drawBorders(new CellRangeAddress(rowNum, rowNum, 6, 7),
                BorderStyle.THIN, BorderExtent.OUTSIDE_HORIZONTAL);
    }

    private void createComplaintCountsDataRow(SurveillanceReportWorkbookWrapper workbook,
            Sheet sheet, String name, long value, int rowNum) {
        Row row = workbook.getRow(sheet, rowNum);
        Cell cell = workbook.createCell(row, 6);
        cell.setCellValue(name);
        cell = workbook.createCell(row, 7);
        cell.setCellValue(value);
        //dotted top and bottom borders around each data field
        pt.drawBorders(new CellRangeAddress(rowNum, rowNum, 6, 7),
                BorderStyle.HAIR, BorderExtent.OUTSIDE_HORIZONTAL);
    }

    private Long getCountOfSurveillanceProcessTypesBySurveillanceType(List<RelevantListing> listings, String survType, String processType) {
        //There could be more than one privileged surveillance for the same surveillance ID and process type
        //if the surveillance was applied to multiple quarters and this is an annual report.
        //I don't think we want to double-count it for summary purposes, so we have to do some de-duplication here.
        List<PrivilegedSurveillance> survsOfType = listings.stream()
                .flatMap(listing -> listing.getSurveillances().stream())
                .filter(surv -> surv.getSurveillanceType().getName().equals(survType))
                .filter(surv -> hasProcessType(surv, processType))
                .filter(distinctByKey(surv -> surv.getId()))
                .collect(Collectors.toList());
        return survsOfType.stream().count();
    }

    private boolean hasProcessType(PrivilegedSurveillance surv, String processType) {
        if (CollectionUtils.isEmpty(surv.getSurveillanceProcessTypes())) {
            return false;
        }

        return surv.getSurveillanceProcessTypes().stream()
                .filter(procType -> procType.getName().startsWith(processType))
                .findAny().isPresent();
    }

    private Long getCountOfSurveillanceOutcomesBySurveillanceType(List<RelevantListing> listings, String survType, String outcome) {
        List<PrivilegedSurveillance> allSurvs = listings.stream()
                .flatMap(listing -> listing.getSurveillances().stream())
                .filter(surv -> surv.getSurveillanceOutcome() != null)
                .filter(distinctByKey(surv -> surv.getId() + ""
                        + (surv.getSurveillanceOutcome().getName().contains(outcome) ? outcome : surv.getSurveillanceOutcome().getName())))
                .collect(Collectors.toList());
        return allSurvs.stream()
            .filter(surv -> surv.getSurveillanceType().getName().equals(survType)
                        && surv.getSurveillanceOutcome().getName().contains(outcome))
            .count();
    }

    private List<RelevantListing> getListingsBySurveillanceType(List<RelevantListing> listings, String survType) {
        return listings.stream()
            .filter(listing -> doesListingHaveSurveillanceType(listing, survType))
            .collect(Collectors.toList());
    }

    private boolean doesListingHaveSurveillanceType(RelevantListing listing, String survType) {
        return listing.getSurveillances().stream()
                .filter(surv -> surv.getSurveillanceType().getName().equals(survType))
                .findAny().isPresent();
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
