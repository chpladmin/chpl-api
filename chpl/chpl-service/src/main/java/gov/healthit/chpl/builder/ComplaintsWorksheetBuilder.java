package gov.healthit.chpl.builder;

import java.awt.Color;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.BorderExtent;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.PropertyTemplate;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.domain.complaint.Complaint;
import gov.healthit.chpl.domain.complaint.ComplaintListingMap;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportRelevantListingDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.ComplaintManager;
import gov.healthit.chpl.manager.SurveillanceManager;
import gov.healthit.chpl.manager.SurveillanceReportManager;
import gov.healthit.chpl.validation.surveillance.SurveillanceValidator;

@Component
public class ComplaintsWorksheetBuilder extends XlsxWorksheetBuilder {
    private static final Logger LOGGER = LogManager.getLogger(ComplaintsWorksheetBuilder.class);
    private static final int LAST_DATA_COLUMN = 18;

    private static final int COL_COMPLAINT_DATE = 1;
    private static final int COL_ACB_COMPLAINT_ID = 2;
    private static final int COL_ONC_COMPLAINT_ID = 3;
    private static final int COL_SUMMARY = 4;
    private static final int COL_ACTIONS_RESPONSE = 5;
    private static final int COL_COMPLAINANT_TYPE = 6;
    private static final int COL_CHPL_ID = 7;
    private static final int COL_SURV_ID = 8;
    //columns 9 - 12 get hidden
    private static final int COL_DEVELOPER = 9;
    private static final int COL_PRODUCT = 10;
    private static final int COL_VERSION = 11;
    private static final int COL_SURV_OUTCOME = 12;
    private static final int COL_COMPLAINANT_CONTACTED = 13;
    private static final int COL_DEVELOPER_CONTACTED = 14;
    private static final int COL_ATL_CONTACTED = 15;
    private static final int COL_COMPLAINT_STATUS = 16;

    private ComplaintManager complaintManager;
    private int lastDataRow;
    private SimpleDateFormat dateFormatter;
    private PropertyTemplate pt;

    @Autowired
    public ComplaintsWorksheetBuilder(final ComplaintManager complaintManager) {
        super();
        this.complaintManager = complaintManager;
        dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
        pt = new PropertyTemplate();
    }

    @Override
    public int getLastDataColumn() {
        return LAST_DATA_COLUMN;
    }

    @Override
    public int getLastDataRow() {
        return lastDataRow <= 1 ? 2 : lastDataRow;
    }

    /**
     * Creates a formatted Excel worksheet with the information in the report.
     * @return
     * @throws IOException
     */
    public Sheet buildWorksheet(final List<QuarterlyReportDTO> quarterlyReports)
            throws IOException {
        XSSFDataValidationHelper dvHelper = null;

        //create sheet
        Sheet sheet = getSheet("Complaints", new Color(141, 180, 226));
        if (sheet instanceof XSSFSheet) {
            XSSFSheet xssfSheet = (XSSFSheet) sheet;
            dvHelper = new XSSFDataValidationHelper(xssfSheet);
        }

        //set some styling that applies to the whole sheet
        sheet.setDisplayGridlines(false);
        sheet.setDisplayRowColHeadings(false);

        //all columns need a certain width to match the document format
        int sharedColWidth = getColumnWidth(11.78);
        sheet.setColumnWidth(COL_COMPLAINT_DATE, sharedColWidth);
        sheet.setColumnWidth(COL_ACB_COMPLAINT_ID,  sharedColWidth);
        sheet.setColumnWidth(COL_ONC_COMPLAINT_ID, sharedColWidth);
        sheet.setColumnWidth(COL_SUMMARY, getColumnWidth(36.78));
        sheet.setColumnWidth(COL_ACTIONS_RESPONSE, getColumnWidth(78));
        sheet.setColumnWidth(COL_COMPLAINANT_TYPE, getColumnWidth(22));
        sheet.setColumnWidth(COL_CHPL_ID, sharedColWidth);
        sheet.setColumnWidth(COL_SURV_ID, sharedColWidth);
        sheet.setColumnWidth(COL_DEVELOPER, sharedColWidth);
        sheet.setColumnWidth(COL_PRODUCT, sharedColWidth);
        sheet.setColumnWidth(COL_VERSION, sharedColWidth);
        sheet.setColumnWidth(COL_SURV_OUTCOME, sharedColWidth);
        sheet.setColumnWidth(COL_COMPLAINANT_CONTACTED, sharedColWidth);
        sheet.setColumnWidth(COL_DEVELOPER_CONTACTED, sharedColWidth);
        sheet.setColumnWidth(COL_ATL_CONTACTED, sharedColWidth);
        sheet.setColumnWidth(COL_COMPLAINT_STATUS, sharedColWidth);

        lastDataRow += addHeadingRow(sheet);
        lastDataRow += addTableData(sheet, quarterlyReports);

        //some of the columns have dropdown lists of choices for the user - set those up

        //If referenced as a list of strings, the total sum of characters of a dropdown must be less than 256
        //(meaning if you put all the choices together it has to be less than 256 characters)
        //but if you read those same strings from another set of cells using a formula, it is allowed
        //to be as long as you want.
        //names for the list constraints
        Name complainantTypeNamedCell = workbook.createName();
        complainantTypeNamedCell.setNameName("ComplainantTypeList");
        String reference = "Lists!$E$1:$E$" + getNumberOfComplainantTypes();
        complainantTypeNamedCell.setRefersToFormula(reference);

        Name complaintStatusTypeNamedCell = workbook.createName();
        complaintStatusTypeNamedCell.setNameName("ComplaintStatusTypeList");
        reference = "Lists!$F$1:$F$" + getNumberOfComplaintStatusTypes();
        complaintStatusTypeNamedCell.setRefersToFormula(reference);

        Name booleanNamedCell = workbook.createName();
        booleanNamedCell.setNameName("BooleanList");
        reference = "Lists!$D$1:$D$2";
        booleanNamedCell.setRefersToFormula(reference);

        //complainant type is a dropdown list of choices
        CellRangeAddressList addressList = new CellRangeAddressList(2, getLastDataRow(), COL_COMPLAINANT_TYPE, COL_COMPLAINANT_TYPE);
        XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper.createFormulaListConstraint("ComplainantTypeList");
        XSSFDataValidation validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(false);
        sheet.addValidationData(validation);

        //complainant contacted? is a dropdown list of choices
        addressList =
                new CellRangeAddressList(2, getLastDataRow(), COL_COMPLAINANT_CONTACTED, COL_COMPLAINANT_CONTACTED);
        dvConstraint = (XSSFDataValidationConstraint)
          dvHelper.createFormulaListConstraint("BooleanList");
        validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(false);
        sheet.addValidationData(validation);

        //developer contacted? is a dropdown list of choices
        addressList =
                new CellRangeAddressList(2, getLastDataRow(), COL_DEVELOPER_CONTACTED, COL_DEVELOPER_CONTACTED);
        dvConstraint = (XSSFDataValidationConstraint)
          dvHelper.createFormulaListConstraint("BooleanList");
        validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(false);
        sheet.addValidationData(validation);

        //atl contacted? is a dropdown list of choices
        addressList =
                new CellRangeAddressList(2, getLastDataRow(), COL_ATL_CONTACTED, COL_ATL_CONTACTED);
        dvConstraint = (XSSFDataValidationConstraint)
          dvHelper.createFormulaListConstraint("BooleanList");
        validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(false);
        sheet.addValidationData(validation);

        //complaint status is a dropdown list of choices
        addressList = new CellRangeAddressList(2, getLastDataRow(), COL_COMPLAINT_STATUS, COL_COMPLAINT_STATUS);
        dvConstraint = (XSSFDataValidationConstraint) dvHelper.createFormulaListConstraint("ComplaintStatusTypeList");
        validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(false);
        sheet.addValidationData(validation);

        //hide some rows the ACBs are not expected to fill out (columns D-I)
        for (int i = 9; i < 12; i++) {
            sheet.setColumnHidden(i, true);
        }

        //apply the borders after the sheet has been created
        pt.drawBorders(new CellRangeAddress(1, getLastDataRow(), 1, LAST_DATA_COLUMN-1),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        pt.applyBorders(sheet);
        return sheet;
    }

    /**
     * Creates the heading for this worksheet.
     * Returns the number of rows added.
     * @param sheet
     * @return
     */
    private int addHeadingRow(final Sheet sheet) {
        Row row = getRow(sheet, 1);
        //row can have 6 lines of text
        row.setHeightInPoints(3 * sheet.getDefaultRowHeightInPoints());

        addHeadingCell(row, COL_COMPLAINT_DATE, "Date Complaint Received");
        addHeadingCell(row, COL_ACB_COMPLAINT_ID, "ONC-ACB Complaint ID");
        addHeadingCell(row, COL_ONC_COMPLAINT_ID, "ONC Complaint ID (if applicable)");
        addHeadingCell(row, COL_SUMMARY, "Complaint Summary");
        addHeadingCell(row, COL_ACTIONS_RESPONSE, "Actions/Response");
        addHeadingCell(row, COL_COMPLAINANT_TYPE, "Type of Complaint");
        addHeadingCell(row, COL_CHPL_ID, "CHPL ID");
        addHeadingCell(row, COL_SURV_ID, "Surveillance ID");
        addHeadingCell(row, COL_DEVELOPER, "Developer");
        addHeadingCell(row, COL_PRODUCT, "Product");
        addHeadingCell(row, COL_VERSION, "Version");
        addHeadingCell(row, COL_SURV_OUTCOME, "Outcome of Surveillance");
        addHeadingCell(row, COL_COMPLAINANT_CONTACTED, "Complainant Contacted?");
        addHeadingCell(row, COL_DEVELOPER_CONTACTED, "Developer Contacted?");
        addHeadingCell(row, COL_ATL_CONTACTED, "ONC-ATL Contacted?");
        addHeadingCell(row, COL_COMPLAINT_STATUS, "Complaint Status");
        return 1;
    }

    /**
     * Adds all of the complaint data to this worksheet. 
     * Returns the number of rows added.
     * @param sheet
     * @param reportListingMap
     */
    private int addTableData(final Sheet sheet,
            final List<QuarterlyReportDTO> quarterlyReports) {
        int addedRows = 0;
        int rowNum = 2;

        List<Complaint> allComplaints = new ArrayList<Complaint>();
        //get the complaints for each quarterly report
        for (QuarterlyReportDTO report : quarterlyReports) {
            allComplaints.addAll(complaintManager.getAllComplaintsBetweenDates(report.getStartDate(), report.getEndDate()));
        }
        //sort the complaints with oldest receied date first
        allComplaints.sort(new Comparator<Complaint>() {
            @Override
            public int compare(final Complaint o1, final Complaint o2) {
                if (o1.getReceivedDate().getTime() < o2.getReceivedDate().getTime()) {
                    return -1;
                } else if (o1.getReceivedDate().getTime() == o2.getReceivedDate().getTime()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });

        for (Complaint complaint : allComplaints) {
            Row row = getRow(sheet, rowNum);
            addDataCell(row, COL_COMPLAINT_DATE, dateFormatter.format(complaint.getReceivedDate()));
            addDataCell(row, COL_ACB_COMPLAINT_ID, complaint.getAcbComplaintId());
            addDataCell(row, COL_ONC_COMPLAINT_ID, complaint.getOncComplaintId());
            addDataCell(row, COL_SUMMARY, complaint.getSummary());
            addDataCell(row, COL_ACTIONS_RESPONSE, complaint.getActions());
            addDataCell(row, COL_COMPLAINANT_TYPE, complaint.getComplainantType().getName());
            addDataCell(row, COL_CHPL_ID, determineChplIds(complaint));
            //TODO: can't fill out until a complaint can be associated with a surveillance
            addDataCell(row, COL_SURV_ID, "");
            //TODO: need answer from heather for these next few
            addDataCell(row, COL_DEVELOPER, "");
            addDataCell(row, COL_PRODUCT, "");
            addDataCell(row, COL_VERSION, "");
            addDataCell(row, COL_SURV_OUTCOME, "");

            addDataCell(row, COL_PRODUCT_NAME, listing.getProduct().getName());
            addDataCell(row, COL_PRODUCT_VERSION, listing.getVersion().getVersion());
            //user has to enter this field
            addDataCell(row, COL_K1_REVIEWED, "");
            addDataCell(row, COL_SURV_TYPE, surv.getType().getName());
            addDataCell(row, COL_SURV_LOCATION_COUNT,
                    surv.getRandomizedSitesUsed() == null ? "" : surv.getRandomizedSitesUsed().toString());
            addDataCell(row, COL_SURV_BEGIN, dateFormatter.format(surv.getStartDate()));
            addDataCell(row, COL_SURV_END, surv.getEndDate() == null ? "" : dateFormatter.format(surv.getEndDate()));
            //user has to enter this field
            addDataCell(row, COL_SURV_OUTCOME, "");
            addDataCell(row, COL_NONCONFORMITY_TYPES_RESULTANT, determineNonconformityTypes(surv));
            addDataCell(row, COL_CERT_STATUS_RESULTANT, determineResultantCertificationStatus(listing, surv));
            addDataCell(row, COL_SUSPENDED, determineSuspendedStatus(listing, surv));
            //user has to enter this field
            addDataCell(row, COL_SURV_PROCESS_TYPE, "");
            pt.drawBorders(new CellRangeAddress(rowNum, rowNum, 1, LAST_DATA_COLUMN - 1),
                    BorderStyle.HAIR, BorderExtent.HORIZONTAL);
            addedRows++;
            rowNum++;
        }
        return addedRows;
    }

    private int getNumberOfComplainantTypes() {
        return complaintManager.getComplainantTypes().size();
    }

    private int getNumberOfComplaintStatusTypes() {
        return complaintManager.getComplaintStatusTypes().size();
    }

    private String determineChplIds(final Complaint complaint) {
        StringBuffer buf = new StringBuffer();
        if (complaint.getListings() != null && complaint.getListings().size() == 0) {
            for (ComplaintListingMap listingMap : complaint.getListings()) {
                if (buf.length() > 0) {
                    buf.append(", ");
                }
                buf.append(listingMap.getChplProductNumber());
            }
        }
        return buf.toString();
    }

    private Cell addHeadingCell(final Row row, final int cellNum, final String cellText) {
        Cell cell = createCell(row, cellNum);
        cell.setCellStyle(wrappedTableHeadingStyle);
        cell.setCellValue(cellText);
        return cell;
    }

    private Cell addDataCell(final Row row, final int cellNum, final String cellText) {
        Cell cell = createCell(row, cellNum);
        cell.setCellStyle(smallStyle);
        cell.setCellValue(cellText);
        return cell;
    }
}
