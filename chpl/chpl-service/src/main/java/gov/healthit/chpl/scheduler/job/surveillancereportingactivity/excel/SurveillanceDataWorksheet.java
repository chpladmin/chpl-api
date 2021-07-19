package gov.healthit.chpl.scheduler.job.surveillancereportingactivity.excel;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import gov.healthit.chpl.scheduler.job.surveillancereportingactivity.SurveillanceData;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "surveillanceActivityReportJobLogger")
public class SurveillanceDataWorksheet {
    private static final Integer RECORD_STATUS_C_INDEX = 0;
    private static final Integer UNIQUE_CHPL_ID_C_INDEX = 1;
    private static final Integer URL_INDEX = 2;
    private static final Integer ACB_NAME_INDEX = 3;
    private static final Integer CERTIFICATION_STATUS_INDEX = 4;
    private static final Integer DEVELOPER_NAME_INDEX = 5;
    private static final Integer PRODUCT_NAME_INDEX = 6;
    private static final Integer PRODUCT_VERSION_INDEX = 7;
    private static final Integer SURVEILLANCE_ID_INDEX = 8;
    private static final Integer SURVEILLANCE_BEGAN_INDEX = 9;
    private static final Integer SURVEILLANCE_ENDED_INDEX = 10;
    private static final Integer SURVEILLANCE_TYPE_INDEX = 11;
    private static final Integer RANDOMIZED_SITES_USED_INDEX = 12;
    private static final Integer SURVEILLED_REQUIREMENT_TYPE_INDEX = 13;
    private static final Integer SURVEILLED_REQUIREMENT_INDEX = 14;
    private static final Integer SURVEILLANCE_RESULT_INDEX = 15;
    private static final Integer NON_CONFORMITY_TYPE_INDEX = 16;
    private static final Integer NON_CONFORMITY_STATUS_INDEX = 17;
    private static final Integer NON_CONFORMITY_CLOSE_DATE_INDEX = 18;
    private static final Integer DATE_OF_DETERMINATION_INDEX = 19;
    private static final Integer CAP_APPROVAL_DATE_INDEX = 20;
    private static final Integer ACTION_BEGAN_DATE_INDEX = 21;
    private static final Integer MUST_COMPLETE_DATE_INDEX = 22;
    private static final Integer WAS_COMPLETE_DATE_INDEX = 23;
    private static final Integer NON_CONFORMITY_SUMMARY_INDEX = 24;
    private static final Integer NON_CONFORMITY_FINDINGS_INDEX = 25;
    private static final Integer SITES_PASSED_INDEX = 26;
    private static final Integer TOTAL_SITES_INDEX = 27;
    private static final Integer DEVELOPER_EXPLANATION_INDEX = 28;
    private static final Integer RESOLUTION_DESCRIPTION_INDEX = 29;
    private static final Integer DURATION_OF_CLOSED_SURVEILLANCE_INDEX = 30;
    private static final Integer TIME_TO_ASSESS_CONFORMITY_INDEX = 31;
    private static final Integer TIME_T0_APPROVE_CAP_INDEX = 32;
    private static final Integer MUST_COMPLETE_MET_INDEX = 33;
    private static final Integer DURATION_OF_CAP_INDEX = 34;
    private static final Integer TIME_FROM_CAP_APPROVAL_TO_SURVEILLANCE_CLOSE_INDEX = 35;
    private static final Integer TIME_FROM_CAP_CLOSE_TO_SURVEILLANCE_CLOSE_INDEX = 36;

    private Map<String, Integer> headers = new HashMap<String, Integer>();
    private Workbook workbook;
    private CellStyle dateCellStyle;
    private CellStyle integerCellStyle;

    public SurveillanceDataWorksheet(Workbook workbook) {
        this.workbook = workbook;
        dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("m/d/yyy"));
        integerCellStyle = workbook.createCellStyle();
        integerCellStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("0"));

        headers.put("RECORD_STATUS__C", RECORD_STATUS_C_INDEX);
        headers.put("UNIQUE_CHPL_ID__C", UNIQUE_CHPL_ID_C_INDEX);
        headers.put("URL", URL_INDEX);
        headers.put("ACB_NAME", ACB_NAME_INDEX);
        headers.put("CERTIFICATION_STATUS", CERTIFICATION_STATUS_INDEX);
        headers.put("DEVELOPER_NAME", DEVELOPER_NAME_INDEX);
        headers.put("PRODUCT_NAME", PRODUCT_NAME_INDEX);
        headers.put("PRODUCT_VERSION", PRODUCT_VERSION_INDEX);
        headers.put("SURVEILLANCE_ID", SURVEILLANCE_ID_INDEX);
        headers.put("SURVEILLANCE_BEGAN", SURVEILLANCE_BEGAN_INDEX);
        headers.put("SURVEILLANCE_ENDED", SURVEILLANCE_ENDED_INDEX);
        headers.put("SURVEILLANCE_TYPE", SURVEILLANCE_TYPE_INDEX);
        headers.put("RANDOMIZED_SITES_USED", RANDOMIZED_SITES_USED_INDEX);
        headers.put("SURVEILLED_REQUIREMENT_TYPE", SURVEILLED_REQUIREMENT_TYPE_INDEX);
        headers.put("SURVEILLED_REQUIREMENT", SURVEILLED_REQUIREMENT_INDEX);
        headers.put("SURVEILLANCE_RESULT", SURVEILLANCE_RESULT_INDEX);
        headers.put("NON_CONFORMITY_TYPE", NON_CONFORMITY_TYPE_INDEX);
        headers.put("NON_CONFORMITY_STATUS", NON_CONFORMITY_STATUS_INDEX);
        headers.put("NON_CONFORMITY_CLOSE_DATE", NON_CONFORMITY_CLOSE_DATE_INDEX);
        headers.put("DATE_OF_DETERMINATION", DATE_OF_DETERMINATION_INDEX);
        headers.put("CAP_APPROVAL_DATE", CAP_APPROVAL_DATE_INDEX);
        headers.put("ACTION_BEGAN_DATE", ACTION_BEGAN_DATE_INDEX);
        headers.put("MUST_COMPLETE_DATE", MUST_COMPLETE_DATE_INDEX);
        headers.put("WAS_COMPLETE_DATE", WAS_COMPLETE_DATE_INDEX);
        headers.put("NON_CONFORMITY_SUMMARY", NON_CONFORMITY_SUMMARY_INDEX);
        headers.put("NON_CONFORMITY_FINDINGS", NON_CONFORMITY_FINDINGS_INDEX);
        headers.put("SITES_PASSED", SITES_PASSED_INDEX);
        headers.put("TOTAL_SITES", TOTAL_SITES_INDEX);
        headers.put("DEVELOPER_EXPLANATION", DEVELOPER_EXPLANATION_INDEX);
        headers.put("RESOLUTION_DESCRIPTION", RESOLUTION_DESCRIPTION_INDEX);
        headers.put("Duration of Closed Surveillance (days)", DURATION_OF_CLOSED_SURVEILLANCE_INDEX);
        headers.put("Time to Assess Conformity (days)", TIME_TO_ASSESS_CONFORMITY_INDEX);
        headers.put("Time to Approve CAP (days)", TIME_T0_APPROVE_CAP_INDEX);
        headers.put("MUST_COMPLETE_MET?", MUST_COMPLETE_MET_INDEX);
        headers.put("Duration of CAP (days)", DURATION_OF_CAP_INDEX);
        headers.put("Time from CAP Approval to Surveillance Close (days)", TIME_FROM_CAP_APPROVAL_TO_SURVEILLANCE_CLOSE_INDEX);
        headers.put("Time from CAP Close to Surveillance Close (days)", TIME_FROM_CAP_CLOSE_TO_SURVEILLANCE_CLOSE_INDEX);
    }

    public Sheet generateWorksheet(List<SurveillanceData> surveillances) {
        try {
            LOGGER.info("Starting to build the Surveillance Data worksheet.");
            Sheet sheet = workbook.createSheet("Surveillance Data");
            sheet = populateSheet(sheet, surveillances);
            return sheet;
        } finally {
            LOGGER.info("Completed the Surveillance Data worksheet.");
        }
    }

    private Sheet populateSheet(Sheet sheet, List<SurveillanceData> surveillances) {
        populateHeaderRow(sheet.createRow(0));

        for (int rowNum = 1; rowNum <= surveillances.size(); ++rowNum) {
            SurveillanceData surveillance = surveillances.get(rowNum - 1);
            populateSurveillanceRow(sheet.createRow(rowNum), surveillance);
        }

        return sheet;
    }

    private Row populateSurveillanceRow(Row row, SurveillanceData surveillance) {
        row.createCell(RECORD_STATUS_C_INDEX).setCellValue(surveillance.getRecordType().toString());
        setCellValueAsString(row.createCell(UNIQUE_CHPL_ID_C_INDEX), surveillance.getChplProductNumber());
        setCellValueAsString(row.createCell(URL_INDEX), surveillance.getUrl());
        setCellValueAsString(row.createCell(ACB_NAME_INDEX), surveillance.getAcbName());
        setCellValueAsString(row.createCell(CERTIFICATION_STATUS_INDEX), surveillance.getCertificationStatus());
        setCellValueAsString(row.createCell(DEVELOPER_NAME_INDEX), surveillance.getDeveloperName());
        setCellValueAsString(row.createCell(PRODUCT_NAME_INDEX), surveillance.getProductName());
        setCellValueAsString(row.createCell(PRODUCT_VERSION_INDEX), surveillance.getVersion());
        setCellValueAsString(row.createCell(SURVEILLANCE_ID_INDEX), surveillance.getSurveillanceId());
        setCellValueAsLocalDate(row.createCell(SURVEILLANCE_BEGAN_INDEX), surveillance.getSurveillanceBegan());
        setCellValueAsLocalDate(row.createCell(SURVEILLANCE_ENDED_INDEX), surveillance.getSurveillanceEnded());
        setCellValueAsString(row.createCell(SURVEILLANCE_TYPE_INDEX), surveillance.getSurveillanceType());
        setCellValueAsInteger(row.createCell(RANDOMIZED_SITES_USED_INDEX), surveillance.getRandonmizedSitesUsed());
        setCellValueAsString(row.createCell(SURVEILLED_REQUIREMENT_TYPE_INDEX), surveillance.getSurveilledRequirementType());
        setCellValueAsString(row.createCell(SURVEILLED_REQUIREMENT_INDEX), surveillance.getSurveilledRequirement());
        setCellValueAsString(row.createCell(SURVEILLANCE_RESULT_INDEX), surveillance.getSurveillanceResult());
        setCellValueAsString(row.createCell(NON_CONFORMITY_TYPE_INDEX), surveillance.getNonconformityType());
        setCellValueAsString(row.createCell(NON_CONFORMITY_STATUS_INDEX), surveillance.getNonconformityCloseDate() == null ? "Open" : "Closed");
        setCellValueAsLocalDate(row.createCell(NON_CONFORMITY_CLOSE_DATE_INDEX), surveillance.getNonconformityCloseDate());
        setCellValueAsLocalDate(row.createCell(DATE_OF_DETERMINATION_INDEX), surveillance.getDateOfDetermination());
        setCellValueAsLocalDate(row.createCell(CAP_APPROVAL_DATE_INDEX), surveillance.getCapApprovalDate());
        setCellValueAsLocalDate(row.createCell(ACTION_BEGAN_DATE_INDEX), surveillance.getActionBeganDate());
        setCellValueAsLocalDate(row.createCell(MUST_COMPLETE_DATE_INDEX), surveillance.getMustCompleteDate());
        setCellValueAsLocalDate(row.createCell(WAS_COMPLETE_DATE_INDEX), surveillance.getWasCompleteDate());
        setCellValueAsString(row.createCell(NON_CONFORMITY_SUMMARY_INDEX), surveillance.getNonconformitySummary());
        setCellValueAsString(row.createCell(NON_CONFORMITY_FINDINGS_INDEX), surveillance.getNonconformityFindings());
        setCellValueAsInteger(row.createCell(SITES_PASSED_INDEX), surveillance.getSitesPassed());
        setCellValueAsInteger(row.createCell(TOTAL_SITES_INDEX), surveillance.getTotalSites());
        setCellValueAsString(row.createCell(DEVELOPER_EXPLANATION_INDEX), surveillance.getDeveloperExplanation());
        setCellValueAsString(row.createCell(RESOLUTION_DESCRIPTION_INDEX), surveillance.getResolutionDescription());
        setCellValueAsInteger(row.createCell(DURATION_OF_CLOSED_SURVEILLANCE_INDEX), surveillance.getDurationOfClosedSurveillance());
        setCellValueAsInteger(row.createCell(TIME_TO_ASSESS_CONFORMITY_INDEX), surveillance.getTimeToAssessConformity());
        setCellValueAsInteger(row.createCell(TIME_T0_APPROVE_CAP_INDEX), surveillance.getTimeToApproveCap());
        setCellValueAsString(row.createCell(MUST_COMPLETE_MET_INDEX), "");
        setCellValueAsInteger(row.createCell(DURATION_OF_CAP_INDEX), surveillance.getDurationOfCap());
        setCellValueAsInteger(row.createCell(TIME_FROM_CAP_APPROVAL_TO_SURVEILLANCE_CLOSE_INDEX), surveillance.getTimeFromCapApprovalToSurveillanceClose());
        setCellValueAsInteger(row.createCell(TIME_FROM_CAP_CLOSE_TO_SURVEILLANCE_CLOSE_INDEX), surveillance.getTimeFromCapCloseToSurveillanceClose());

        return row;
    }

    private Cell setCellValueAsString(Cell cell, String value) {
        return CellHelper.setCellValueAsString(cell, value, "");
    }

    private Cell setCellValueAsLocalDate(Cell cell, LocalDate value) {
        return CellHelper.setCellValueAsLocalDate(cell, value, "", dateCellStyle);
    }

    private Cell setCellValueAsInteger(Cell cell, Integer value) {
        return CellHelper.setCellValueAsInteger(cell, value, "", integerCellStyle);
    }

    private Row populateHeaderRow(Row row) {
        List<String> sortedHeaders = headers.entrySet().stream()
                .sorted((e1, e2) -> e1.getValue() - e2.getValue())
                .map(e -> e.getKey())
                .collect(Collectors.toList());

        int cellNum = 0;
        for (String header : sortedHeaders) {
            row.createCell(cellNum).setCellValue(header);
            cellNum++;
        }
        return row;
    }
}
