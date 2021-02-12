package gov.healthit.chpl.surveillance.report.builder2019;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderExtent;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PropertyTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.SurveillanceTypeDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.surveillance.report.ComplaintSummaryDAO;
import gov.healthit.chpl.surveillance.report.PrivilegedSurveillanceDAO;
import gov.healthit.chpl.surveillance.report.SurveillanceSummaryDAO;
import gov.healthit.chpl.surveillance.report.dto.PrivilegedSurveillanceDTO;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportDTO;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportRelevantListingDTO;
import gov.healthit.chpl.surveillance.report.dto.SurveillanceOutcomeDTO;
import gov.healthit.chpl.surveillance.report.dto.SurveillanceProcessTypeDTO;
import gov.healthit.chpl.surveillance.report.dto.SurveillanceSummaryDTO;

@Component
public class SurveillanceSummaryWorksheetBuilder {
    private static final int LAST_DATA_COLUMN = 10;
    private static final int LAST_DATA_ROW = 60;
    private static final String PROC_TYPE_IN_THE_FIELD = "In-the-Field";
    private static final String PROC_TYPE_CONTROLLED = "Controlled/Test Environment";
    private static final String PROC_TYPE_CORRESPONDENCE = "Correspondence with Complainant/Developer";
    private static final String PROC_TYPE_REVIEW = "Review of Websites/Written Documentation";
    private static final String PROC_TYPE_OTHER = "Other";

    private static final String OUTCOME_TYPE_NO_NC = "No non-conformity";
    private static final String OUTCOME_TYPE_NC = "Non-conformity substantiated";
    private static final String OUTCOME_TYPE_CAP = "Resolved through corrective action";

    private CertifiedProductDetailsManager detailsManager;
    private SurveillanceSummaryDAO survSummaryDao;
    private ComplaintSummaryDAO complaintSummaryDao;
    private PrivilegedSurveillanceDAO privSurvDao;
    private PropertyTemplate pt;

    @Autowired
    public SurveillanceSummaryWorksheetBuilder(CertifiedProductDetailsManager detailsManager,
            SurveillanceSummaryDAO survSummaryDao,
            PrivilegedSurveillanceDAO privSurvDao,
            ComplaintSummaryDAO complaintSummaryDao) {
        this.detailsManager = detailsManager;
        this.survSummaryDao = survSummaryDao;
        this.privSurvDao = privSurvDao;
        this.complaintSummaryDao = complaintSummaryDao;
    }

    public int getLastDataColumn() {
        return LAST_DATA_COLUMN;
    }

    public int getLastDataRow() {
        return LAST_DATA_ROW;
    }

    public Sheet buildWorksheet(SurveillanceReportWorkbookWrapper workbook, List<QuarterlyReportDTO> reports) throws IOException {
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

        addSurveillanceCounts(workbook, sheet, reports);
        addComplaintsCounts(workbook, sheet, reports);

        //apply the borders after the sheet has been created
        pt.applyBorders(sheet);
        return sheet;
    }

    private void addSurveillanceCounts(SurveillanceReportWorkbookWrapper workbook, Sheet sheet, List<QuarterlyReportDTO> reports) {
        //the reports must all be for the same ACB so just take the acb in the first one
        Long acbId = reports.get(0).getAcb().getId();
        //find the date range encompassing all the reports
        Date startDate = reports.get(0).getStartDate();
        Date endDate = reports.get(0).getEndDate();
        for (QuarterlyReportDTO report : reports) {
            if (report.getStartDate().getTime() < startDate.getTime()) {
                startDate = report.getStartDate();
            }
            if (report.getEndDate().getTime() > endDate.getTime()) {
                endDate = report.getEndDate();
            }
        }

        Row row = workbook.getRow(sheet, 1);
        Cell cell = workbook.createCell(row, 1, workbook.getRightAlignedTableHeadingStyle());
        cell.setCellValue("");
        cell = workbook.createCell(row, 2, workbook.getRightAlignedTableHeadingStyle());
        cell.setCellValue("Reactive");
        cell = workbook.createCell(row, 3, workbook.getRightAlignedTableHeadingStyle());
        cell.setCellValue("Randomized");
        cell = workbook.createCell(row, 4, workbook.getRightAlignedTableHeadingStyle());
        cell.setCellValue("Total");

        createSurveillanceCountsSubheadingRow(workbook, sheet, "Surveillance Counts", 2);
        //number of listings that had an open surveillance during the period of time the reports cover
        SurveillanceSummaryDTO listingSummary =
                survSummaryDao.getCountOfListingsSurveilledByType(acbId, startDate, endDate);
        createSurveillanceCountsDataRow(workbook, sheet, "Number of Certificates Surveilled",
                listingSummary.getReactiveCount(), listingSummary.getRandomizedCount(),
                listingSummary.getReactiveCount() + listingSummary.getRandomizedCount(), 3);

        //number of surveillances open during the period of time the reports cover using the listed process
        //a surveillance could potentially have one process during one report period
        //and a different process during another report period so in that case
        //the surveillance would be counted in more than one row
        createSurveillanceCountsSubheadingRow(workbook, sheet, "Primary Surveillance Processes", 4);
        List<SurveillanceProcessTypeDTO> allProcTypes = privSurvDao.getSurveillanceProcessTypes();
        List<SurveillanceProcessTypeDTO> procTypes = new ArrayList<SurveillanceProcessTypeDTO>();
        for (SurveillanceProcessTypeDTO procType : allProcTypes) {
            if (procType.getName().equals(PROC_TYPE_IN_THE_FIELD)) {
                procTypes.add(procType);
            }
        }
        SurveillanceSummaryDTO procTypeSummary =
                survSummaryDao.getCountOfSurveillanceProcessTypesBySurveillanceType(acbId, procTypes, startDate, endDate);
        createSurveillanceCountsDataRow(workbook, sheet, PROC_TYPE_IN_THE_FIELD,
                procTypeSummary.getReactiveCount(), procTypeSummary.getRandomizedCount(),
                procTypeSummary.getReactiveCount() + procTypeSummary.getRandomizedCount(), 5);

        procTypes.clear();
        for (SurveillanceProcessTypeDTO procType : allProcTypes) {
            if (procType.getName().equals(PROC_TYPE_CONTROLLED)) {
                procTypes.add(procType);
            }
        }
        procTypeSummary =
                survSummaryDao.getCountOfSurveillanceProcessTypesBySurveillanceType(acbId, procTypes, startDate, endDate);
        createSurveillanceCountsDataRow(workbook, sheet, PROC_TYPE_CONTROLLED,
                procTypeSummary.getReactiveCount(), procTypeSummary.getRandomizedCount(),
                procTypeSummary.getReactiveCount() + procTypeSummary.getRandomizedCount(), 6);

        procTypes.clear();
        for (SurveillanceProcessTypeDTO procType : allProcTypes) {
            if (procType.getName().equals(PROC_TYPE_CORRESPONDENCE)) {
                procTypes.add(procType);
            }
        }
        procTypeSummary =
                survSummaryDao.getCountOfSurveillanceProcessTypesBySurveillanceType(acbId, procTypes, startDate, endDate);
        createSurveillanceCountsDataRow(workbook, sheet, PROC_TYPE_CORRESPONDENCE,
                procTypeSummary.getReactiveCount(), procTypeSummary.getRandomizedCount(),
                procTypeSummary.getReactiveCount() + procTypeSummary.getRandomizedCount(), 7);

        procTypes.clear();
        for (SurveillanceProcessTypeDTO procType : allProcTypes) {
            if (procType.getName().equals(PROC_TYPE_REVIEW)) {
                procTypes.add(procType);
            }
        }
        procTypeSummary =
                survSummaryDao.getCountOfSurveillanceProcessTypesBySurveillanceType(acbId, procTypes, startDate, endDate);
        createSurveillanceCountsDataRow(workbook, sheet, PROC_TYPE_REVIEW,
                procTypeSummary.getReactiveCount(), procTypeSummary.getRandomizedCount(),
                procTypeSummary.getReactiveCount() + procTypeSummary.getRandomizedCount(), 8);

        procTypes.clear();
        for (SurveillanceProcessTypeDTO procType : allProcTypes) {
            if (procType.getName().startsWith(PROC_TYPE_OTHER)) {
                procTypes.add(procType);
            }
        }
        procTypeSummary =
                survSummaryDao.getCountOfSurveillanceProcessTypesBySurveillanceType(acbId, procTypes, startDate, endDate);
        createSurveillanceCountsDataRow(workbook, sheet, PROC_TYPE_OTHER,
                procTypeSummary.getReactiveCount(), procTypeSummary.getRandomizedCount(),
                procTypeSummary.getReactiveCount() + procTypeSummary.getRandomizedCount(), 9);

        //number of surveillances open during the period of time the reports cover with the listed outcome
        //a surveillance could potentially have one outcome during one report period
        //and a different outcome during another report period so in that case
        //the surveillance would be counted in more than one row
        createSurveillanceCountsSubheadingRow(workbook, sheet, "Outcome of the Surveillance", 10);

        List<SurveillanceOutcomeDTO> allOutcomes = privSurvDao.getSurveillanceOutcomes();
        List<SurveillanceOutcomeDTO> outcomes = new ArrayList<SurveillanceOutcomeDTO>();

        //outcome = No non-conformity
        for (SurveillanceOutcomeDTO outcome : allOutcomes) {
            if (outcome.getName().equals(OUTCOME_TYPE_NO_NC)) {
                outcomes.add(outcome);
            }
        }
        SurveillanceSummaryDTO outcomeSummary =
                survSummaryDao.getCountOfSurveillanceOutcomesBySurveillanceType(acbId, outcomes, startDate, endDate);
        createSurveillanceCountsDataRow(workbook, sheet, "Number of Surveillance with No Non-Conformities Found",
                outcomeSummary.getReactiveCount(), outcomeSummary.getRandomizedCount(),
                outcomeSummary.getReactiveCount() + outcomeSummary.getRandomizedCount(), 11);

        //outcome = Non-conformity substantiated*
        outcomes.clear();
        for (SurveillanceOutcomeDTO outcome : allOutcomes) {
            if (outcome.getName().startsWith(OUTCOME_TYPE_NC)) {
                outcomes.add(outcome);
            }
        }
        outcomeSummary =
                survSummaryDao.getCountOfSurveillanceOutcomesBySurveillanceType(acbId, outcomes, startDate, endDate);
        createSurveillanceCountsDataRow(workbook, sheet, "Number of Surveillance with Non-Conformities Found",
                outcomeSummary.getReactiveCount(), outcomeSummary.getRandomizedCount(),
                outcomeSummary.getReactiveCount() + outcomeSummary.getRandomizedCount(), 12);

        //outcome = *Resolved through corrective action
        outcomes.clear();
        for (SurveillanceOutcomeDTO outcome : allOutcomes) {
            if (outcome.getName().endsWith(OUTCOME_TYPE_CAP)) {
                outcomes.add(outcome);
            }
        }
        outcomeSummary =
                survSummaryDao.getCountOfSurveillanceOutcomesBySurveillanceType(acbId, outcomes, startDate, endDate);
        createSurveillanceCountsDataRow(workbook, sheet, "Number of Corrective Action Plans",
                outcomeSummary.getReactiveCount(), outcomeSummary.getRandomizedCount(),
                outcomeSummary.getReactiveCount() + outcomeSummary.getRandomizedCount(), 13);

        //number of listings with open surveillance during the period of time the reports
        //cover with the listed resultant status
        //a listing could have one resultant status during one report period
        //and a different resultant status during another report period so in that case
        //the listing would be counted in more than one row
        createSurveillanceCountsSubheadingRow(workbook, sheet, "Certification Status Resultant of Surveillance", 14);

        //get the listings with relevant surveillance of each type
        SurveillanceTypeDTO reactiveType = new SurveillanceTypeDTO();
        reactiveType.setName("Reactive");
        SurveillanceTypeDTO randomizedType = new SurveillanceTypeDTO();
        randomizedType.setName("Randomized");
        List<QuarterlyReportRelevantListingDTO> listingsWithReactive =
                survSummaryDao.getListingsBySurveillanceType(acbId, reactiveType, startDate, endDate);
        List<QuarterlyReportRelevantListingDTO> listingsWithRandomized =
                survSummaryDao.getListingsBySurveillanceType(acbId, randomizedType, startDate, endDate);

        //need to get the certification status events of each listing
        //to determine it's status during the surveillance
        List<CertificationStatusType> listingStatuses = new ArrayList<CertificationStatusType>();
        listingStatuses.add(CertificationStatusType.Active);
        long reactiveSurvActiveStatusCount = 0;
        for (QuarterlyReportRelevantListingDTO listing : listingsWithReactive) {
            if (determineIfListingHadStatusDuringRelevantSurveillance(listing, listingStatuses)) {
                reactiveSurvActiveStatusCount++;
            }
        }
        long randomizedSurvActiveStatusCount = 0;
        for (QuarterlyReportRelevantListingDTO listing : listingsWithRandomized) {
            if (determineIfListingHadStatusDuringRelevantSurveillance(listing, listingStatuses)) {
                randomizedSurvActiveStatusCount++;
            }
        }
        createSurveillanceCountsDataRow(workbook, sheet, "Number of Certificates Active",
                reactiveSurvActiveStatusCount, randomizedSurvActiveStatusCount,
                reactiveSurvActiveStatusCount + randomizedSurvActiveStatusCount, 15);

        listingStatuses.clear();
        listingStatuses.add(CertificationStatusType.SuspendedByAcb);
        listingStatuses.add(CertificationStatusType.SuspendedByOnc);
        long reactiveSurvSuspendedStatusCount = 0;
        for (QuarterlyReportRelevantListingDTO listing : listingsWithReactive) {
            if (determineIfListingHadStatusDuringRelevantSurveillance(listing, listingStatuses)) {
                reactiveSurvSuspendedStatusCount++;
            }
        }
        long randomizedSurvSuspendedStatusCount = 0;
        for (QuarterlyReportRelevantListingDTO listing : listingsWithRandomized) {
            if (determineIfListingHadStatusDuringRelevantSurveillance(listing, listingStatuses)) {
                randomizedSurvSuspendedStatusCount++;
            }
        }
        createSurveillanceCountsDataRow(workbook, sheet, "Number of Certificates Suspended",
                reactiveSurvSuspendedStatusCount, randomizedSurvSuspendedStatusCount,
                reactiveSurvSuspendedStatusCount + randomizedSurvSuspendedStatusCount, 16);

        listingStatuses.clear();
        listingStatuses.add(CertificationStatusType.WithdrawnByDeveloper);
        listingStatuses.add(CertificationStatusType.WithdrawnByDeveloperUnderReview);
        long reactiveSurvWithdrawnStatusCount = 0;
        for (QuarterlyReportRelevantListingDTO listing : listingsWithReactive) {
            if (determineIfListingHadStatusDuringRelevantSurveillance(listing, listingStatuses)) {
                reactiveSurvWithdrawnStatusCount++;
            }
        }
        long randomizedSurvWithdrawnStatusCount = 0;
        for (QuarterlyReportRelevantListingDTO listing : listingsWithRandomized) {
            if (determineIfListingHadStatusDuringRelevantSurveillance(listing, listingStatuses)) {
                randomizedSurvWithdrawnStatusCount++;
            }
        }
        createSurveillanceCountsDataRow(workbook, sheet, "Number of Certificates Withdrawn by Developer",
                reactiveSurvWithdrawnStatusCount, randomizedSurvWithdrawnStatusCount,
                reactiveSurvWithdrawnStatusCount + randomizedSurvWithdrawnStatusCount, 17);

        listingStatuses.clear();
        listingStatuses.add(CertificationStatusType.WithdrawnByAcb);
        long reactiveSurvWithdrawnAcbStatusCount = 0;
        for (QuarterlyReportRelevantListingDTO listing : listingsWithReactive) {
            if (determineIfListingHadStatusDuringRelevantSurveillance(listing, listingStatuses)) {
                reactiveSurvWithdrawnAcbStatusCount++;
            }
        }
        long randomizedSurvWithdrawnAcbStatusCount = 0;
        for (QuarterlyReportRelevantListingDTO listing : listingsWithRandomized) {
            if (determineIfListingHadStatusDuringRelevantSurveillance(listing, listingStatuses)) {
                randomizedSurvWithdrawnAcbStatusCount++;
            }
        }
        createSurveillanceCountsDataRow(workbook, sheet, "Number of Certificates Withdrawn by ONC-ACB",
                reactiveSurvWithdrawnAcbStatusCount, randomizedSurvWithdrawnAcbStatusCount,
                reactiveSurvWithdrawnAcbStatusCount + randomizedSurvWithdrawnAcbStatusCount, 18);

        pt.drawBorders(new CellRangeAddress(1, 18, 1, 4),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
    }

    private boolean determineIfListingHadStatusDuringRelevantSurveillance(QuarterlyReportRelevantListingDTO listing,
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
        for (PrivilegedSurveillanceDTO surv : listing.getSurveillances()) {
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
            PrivilegedSurveillanceDTO surv) {
        String result = "";
        if (surv.getEndDate() == null) {
            result = listing.getCurrentStatus().getStatus().getName();
        } else {
            CertificationStatusEvent statusEvent = listing.getStatusOnDate(surv.getEndDate());
            if (statusEvent != null) {
                result = statusEvent.getStatus().getName();
            }
        }
        return result;
    }

    private void addComplaintsCounts(SurveillanceReportWorkbookWrapper workbook, Sheet sheet,
            List<QuarterlyReportDTO> reports) {
        //the reports must all be for the same ACB so just take the acb in the first one
        Long acbId = reports.get(0).getAcb().getId();
        //find the date range encompassing all the reports
        Date startDate = reports.get(0).getStartDate();
        Date endDate = reports.get(0).getEndDate();
        for (QuarterlyReportDTO report : reports) {
            if (report.getStartDate().getTime() < startDate.getTime()) {
                startDate = report.getStartDate();
            }
            if (report.getEndDate().getTime() > endDate.getTime()) {
                endDate = report.getEndDate();
            }
        }

        Row row = workbook.getRow(sheet, 1);
        Cell cell = workbook.createCell(row, 6, workbook.getRightAlignedTableHeadingStyle());
        cell.setCellValue("");
        cell = workbook.createCell(row, 7, workbook.getRightAlignedTableHeadingStyle());
        cell.setCellValue("Total");

        createComplaintCountsSubheadingRow(workbook, sheet, "Complaint Counts", 2);
        Long totalComplaintsReceived = complaintSummaryDao.getTotalComplaints(acbId, startDate, endDate);
        createComplainCountsDataRow(workbook, sheet, "Total Number Received", totalComplaintsReceived, 3);
        Long totalComplaintsFromOnc = complaintSummaryDao.getTotalComplaintsFromOnc(acbId, startDate, endDate);
        createComplainCountsDataRow(workbook, sheet, "Total Number Referred by ONC", totalComplaintsFromOnc, 4);
        createComplaintCountsSubheadingRow(workbook, sheet, "Surveillance Activities Trigged by Complaints", 5);
        Long totalComplaintsWithSurv = complaintSummaryDao.getTotalComplaintsResultingInSurveillance(acbId, startDate, endDate);
        createComplainCountsDataRow(workbook, sheet, "Number that Led to Surveillance Activities", totalComplaintsWithSurv, 6);
        Long totalSurvFromComplaints = complaintSummaryDao.getTotalSurveillanceRelatedToComplaints(acbId, startDate, endDate);
        createComplainCountsDataRow(workbook, sheet, "Number of Surveillance Activities", totalSurvFromComplaints, 7);
        createComplaintCountsSubheadingRow(workbook, sheet, "Non-Conformities Found by Complaints", 8);
        Long totalComplaintsResultingInNonconformities =
                complaintSummaryDao.getTotalComplaintsResultingInNonconformities(acbId, startDate, endDate);
        createComplainCountsDataRow(workbook, sheet, "Number that Led to Non-conformities", totalComplaintsResultingInNonconformities, 9);
        Long totalNonconformitiesFromComplaints =
                complaintSummaryDao.getTotalNonconformitiesRelatedToComplaints(acbId, startDate, endDate);
        createComplainCountsDataRow(workbook, sheet, "Number of Non-conformities", totalNonconformitiesFromComplaints, 10);

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

    private void createComplainCountsDataRow(SurveillanceReportWorkbookWrapper workbook,
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
}
