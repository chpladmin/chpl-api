package gov.healthit.chpl.surveillance.report.builder;

import java.awt.Color;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
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

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.compliance.surveillance.SurveillanceManager;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceResultType;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.surveillance.report.PrivilegedSurveillanceDAO;
import gov.healthit.chpl.surveillance.report.SurveillanceReportManager;
import gov.healthit.chpl.surveillance.report.dto.PrivilegedSurveillanceDTO;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportDTO;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportRelevantListingDTO;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class ActivitiesAndOutcomesWorksheetBuilder {
    private static final int LAST_DATA_COLUMN = 37;

    private static final int COL_CHPL_ID = 1;
    private static final int COL_SURV_ID = 2;
    private static final int COL_SURV_ACTIVITY_TRACKER = 3;
    private static final int COL_RELATED_COMPLAINT = 4;
    private static final int COL_Q1 = 5;
    private static final int COL_Q2 = 6;
    private static final int COL_Q3 = 7;
    private static final int COL_Q4 = 8;
    private static final int COL_DEVELOPER_NAME = 9;
    private static final int COL_PRODUCT_NAME = 10;
    private static final int COL_PRODUCT_VERSION = 11;
    private static final int COL_K1_REVIEWED = 12;
    private static final int COL_SURV_TYPE = 13;
    private static final int COL_SURV_LOCATION_COUNT = 14;
    private static final int COL_SURV_BEGIN = 15;
    private static final int COL_SURV_END = 16;
    private static final int COL_SURV_OUTCOME = 17;
    private static final int COL_SURV_OUTCOME_OTHER = 18;
    private static final int COL_NONCONFORMITY_TYPES_RESULTANT = 19;
    private static final int COL_CERT_STATUS_RESULTANT = 20;
    private static final int COL_SUSPENDED = 21;
    private static final int COL_SURV_PROCESS_TYPE = 22;
    private static final int COL_SURV_PROCESS_TYPE_OTHER = 23;
    private static final int COL_SURV_GROUNDS = 24;
    private static final int COL_NONCONFORMITY_CAUSES = 25;
    private static final int COL_NONCONFORMITY_NATURES = 26;
    private static final int COL_SURV_STEPS = 27;
    private static final int COL_ENGAGEMENT_STEPS = 28;
    private static final int COL_ADDITIONAL_COSTS = 29;
    private static final int COL_LIMITATIONS_EVAL = 30;
    private static final int COL_NONDISCLOSURE_EVAL = 31;
    private static final int COL_DEV_RESOLUTION = 32;
    private static final int COL_COMPLETED_CAP = 33;
    private static final int[] HIDDEN_COLS =
        {COL_SURV_ACTIVITY_TRACKER, COL_Q1, COL_Q2, COL_Q3, COL_Q4};

    private SurveillanceReportManager reportManager;
    private CertifiedProductDetailsManager detailsManager;
    private SurveillanceManager survManager;
    private PrivilegedSurveillanceDAO privilegedSurvDao;
    private int lastDataRow;
    private DateTimeFormatter dateFormatter;
    private PropertyTemplate pt;

    @Autowired
    public ActivitiesAndOutcomesWorksheetBuilder(SurveillanceReportManager reportManager,
            CertifiedProductDetailsManager detailsManager,
            SurveillanceManager survManager, PrivilegedSurveillanceDAO privilegedSurvDao) {
        this.reportManager = reportManager;
        this.detailsManager = detailsManager;
        this.survManager = survManager;
        this.privilegedSurvDao = privilegedSurvDao;
        dateFormatter = DateTimeFormatter.ofPattern("MM/dd/uuuu");
    }

    protected abstract String getGroundsForInitiatingSurveillanceDescription();
    protected abstract String getStepsToSurveilDescription();
    protected abstract String getAdditionalCostsEvaluationDescription();
    protected abstract String getLimitationsEvaluationDescription();
    protected abstract String getNonDisclosureEvaluationDescription();

    public int getLastDataColumn() {
        return LAST_DATA_COLUMN;
    }

    public int getLastDataRow() {
        return lastDataRow <= 1 ? 2 : lastDataRow;
    }

    public Sheet buildWorksheet(SurveillanceReportWorkbookWrapper workbook, List<QuarterlyReportDTO> quarterlyReports)
            throws IOException {
        lastDataRow = 0;
        pt = new PropertyTemplate();
        XSSFDataValidationHelper dvHelper = null;

        //create sheet
        Sheet sheet = workbook.getSheet("Activities and Outcomes", new Color(141, 180, 226), getLastDataColumn());
        if (sheet instanceof XSSFSheet) {
            XSSFSheet xssfSheet = (XSSFSheet) sheet;
            dvHelper = new XSSFDataValidationHelper(xssfSheet);
        }

        //set some styling that applies to the whole sheet
        sheet.setDisplayGridlines(false);
        sheet.setDisplayRowColHeadings(false);

        //all columns need a certain width to match the document format
        int sharedColWidth = workbook.getColumnWidth(11.78);
        sheet.setColumnWidth(COL_CHPL_ID, sharedColWidth);
        sheet.setColumnWidth(COL_SURV_ID,  sharedColWidth);
        sheet.setColumnWidth(COL_SURV_ACTIVITY_TRACKER, sharedColWidth);
        sheet.setColumnWidth(COL_RELATED_COMPLAINT, sharedColWidth);
        int quarterColWidth = workbook.getColumnWidth(2.22);
        sheet.setColumnWidth(COL_Q1, quarterColWidth);
        sheet.setColumnWidth(COL_Q2, quarterColWidth);
        sheet.setColumnWidth(COL_Q3, quarterColWidth);
        sheet.setColumnWidth(COL_Q4, quarterColWidth);
        sheet.setColumnWidth(COL_DEVELOPER_NAME, sharedColWidth);
        sheet.setColumnWidth(COL_PRODUCT_NAME, sharedColWidth);
        sheet.setColumnWidth(COL_PRODUCT_VERSION, sharedColWidth);
        sheet.setColumnWidth(COL_K1_REVIEWED, sharedColWidth);
        sheet.setColumnWidth(COL_SURV_TYPE, workbook.getColumnWidth(13.67));
        sheet.setColumnWidth(COL_SURV_LOCATION_COUNT, sharedColWidth);
        sheet.setColumnWidth(COL_SURV_BEGIN, sharedColWidth);
        sheet.setColumnWidth(COL_SURV_END, sharedColWidth);
        sheet.setColumnWidth(COL_SURV_OUTCOME, workbook.getColumnWidth(51.44));
        sheet.setColumnWidth(COL_SURV_OUTCOME_OTHER, workbook.getColumnWidth(51.44));
        sheet.setColumnWidth(COL_NONCONFORMITY_TYPES_RESULTANT, workbook.getColumnWidth(27));
        sheet.setColumnWidth(COL_CERT_STATUS_RESULTANT, workbook.getColumnWidth(17.78));
        sheet.setColumnWidth(COL_SUSPENDED, workbook.getColumnWidth(17.78));
        sheet.setColumnWidth(COL_SURV_PROCESS_TYPE, workbook.getColumnWidth(30.67));
        sheet.setColumnWidth(COL_SURV_PROCESS_TYPE_OTHER, workbook.getColumnWidth(30.67));
        int longTextColWidth = workbook.getColumnWidth(59.44);
        sheet.setColumnWidth(COL_SURV_GROUNDS, longTextColWidth);
        sheet.setColumnWidth(COL_NONCONFORMITY_CAUSES, longTextColWidth);
        sheet.setColumnWidth(COL_NONCONFORMITY_NATURES, longTextColWidth);
        sheet.setColumnWidth(COL_SURV_STEPS, longTextColWidth);
        sheet.setColumnWidth(COL_ENGAGEMENT_STEPS, longTextColWidth);
        sheet.setColumnWidth(COL_ADDITIONAL_COSTS, longTextColWidth);
        sheet.setColumnWidth(COL_LIMITATIONS_EVAL, longTextColWidth);
        sheet.setColumnWidth(COL_NONDISCLOSURE_EVAL, longTextColWidth);
        sheet.setColumnWidth(COL_DEV_RESOLUTION, longTextColWidth);
        sheet.setColumnWidth(COL_COMPLETED_CAP, longTextColWidth);

        lastDataRow += addHeadingRow(workbook, sheet);
        lastDataRow += addTableData(workbook, sheet, quarterlyReports);

        //some of the columns have dropdown lists of choices for the user - set those up

        //If referenced as a list of strings, the total sum of characters of a dropdown must be less than 256
        //(meaning if you put all the choices together it has to be less than 256 characters)
        //but if you read those same strings from another set of cells using a formula, it is allowed
        //to be as long as you want. The outcome choices are the only ones that are long enough
        //to run into this problem.
        //names for the list constraints
        Name surveillanceOutcomeNamedCell = workbook.getWorkbook().createName();
        surveillanceOutcomeNamedCell.setNameName("SurveillanceOutcomeList");
        String reference = "Lists!$A$1:$A$8";
        surveillanceOutcomeNamedCell.setRefersToFormula(reference);

        Name processTypeNamedCell = workbook.getWorkbook().createName();
        processTypeNamedCell.setNameName("ProcessTypeList");
        reference = "Lists!$B$1:$B$5";
        processTypeNamedCell.setRefersToFormula(reference);

        Name statusNamedCell = workbook.getWorkbook().createName();
        statusNamedCell.setNameName("StatusList");
        reference = "Lists!$C$1:$C$4";
        statusNamedCell.setRefersToFormula(reference);

        Name booleanNamedCell = workbook.getWorkbook().createName();
        booleanNamedCell.setNameName("BooleanList");
        reference = "Lists!$D$1:$D$2";
        booleanNamedCell.setRefersToFormula(reference);

        //k1 reviewed is a dropdown list of choices
        CellRangeAddressList addressList = new CellRangeAddressList(2, getLastDataRow(), COL_K1_REVIEWED, COL_K1_REVIEWED);
        XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint)
          dvHelper.createFormulaListConstraint("BooleanList");
        XSSFDataValidation validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(false);
        sheet.addValidationData(validation);

        //outcome is a dropdown list of choices
        addressList = new CellRangeAddressList(2, getLastDataRow(), COL_SURV_OUTCOME, COL_SURV_OUTCOME);
        dvConstraint = (XSSFDataValidationConstraint) dvHelper.createFormulaListConstraint("SurveillanceOutcomeList");
        validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(false);
        sheet.addValidationData(validation);

        //certification status is a dropdown list of choices
        addressList = new CellRangeAddressList(2, getLastDataRow(), COL_CERT_STATUS_RESULTANT, COL_CERT_STATUS_RESULTANT);
        dvConstraint = (XSSFDataValidationConstraint) dvHelper.createFormulaListConstraint("StatusList");
        validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(false);
        sheet.addValidationData(validation);

        //process type is a dropdown list of choices
        addressList = new CellRangeAddressList(2, getLastDataRow(), COL_SURV_PROCESS_TYPE, COL_SURV_PROCESS_TYPE);
        dvConstraint = (XSSFDataValidationConstraint) dvHelper.createFormulaListConstraint("ProcessTypeList");
        validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(false);
        sheet.addValidationData(validation);

        //suspended is a dropdown list of choices
        addressList = new CellRangeAddressList(2, getLastDataRow(), COL_SUSPENDED, COL_SUSPENDED);
        dvConstraint = (XSSFDataValidationConstraint) dvHelper.createFormulaListConstraint("BooleanList");
        validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(false);
        sheet.addValidationData(validation);

        //hide some rows the ACBs are not expected to fill out (columns D-I)
        for (int i = 0; i < HIDDEN_COLS.length; i++) {
            sheet.setColumnHidden(HIDDEN_COLS[i], true);
        }

        //apply the borders after the sheet has been created
        pt.drawBorders(new CellRangeAddress(1, getLastDataRow(), 1, LAST_DATA_COLUMN - 1),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        pt.applyBorders(sheet);
        return sheet;
    }

    private int addHeadingRow(SurveillanceReportWorkbookWrapper workbook, Sheet sheet) {
        Row row = workbook.getRow(sheet, 1);
        //row can have 6 lines of text
        row.setHeightInPoints(6 * sheet.getDefaultRowHeightInPoints());

        addHeadingCell(workbook, row, COL_CHPL_ID, "CHPL ID");
        addHeadingCell(workbook, row, COL_SURV_ID, "Surveillance ID");
        addHeadingCell(workbook, row, COL_SURV_ACTIVITY_TRACKER, "Surveillance Activity Tracker");
        addHeadingCell(workbook, row, COL_RELATED_COMPLAINT, "Related Complaint (both if possible)");
        addHeadingCell(workbook, row, COL_Q1, "Q1");
        addHeadingCell(workbook, row, COL_Q2, "Q2");
        addHeadingCell(workbook, row, COL_Q3, "Q3");
        addHeadingCell(workbook, row, COL_Q4, "Q4");
        addHeadingCell(workbook, row, COL_DEVELOPER_NAME, "Developer Name");
        addHeadingCell(workbook, row, COL_PRODUCT_NAME, "Product Name");
        addHeadingCell(workbook, row, COL_PRODUCT_VERSION, "Product Version");
        addHeadingCell(workbook, row, COL_K1_REVIEWED, "§170.523(k)(1) Reviewed");
        addHeadingCell(workbook, row, COL_SURV_TYPE, "Type of Surveillance");
        addHeadingCell(workbook, row, COL_SURV_LOCATION_COUNT, "Number of Locations Surveilled");
        addHeadingCell(workbook, row, COL_SURV_BEGIN, "Surveillance Began");
        addHeadingCell(workbook, row, COL_SURV_END, "Surveillance Ended");
        addHeadingCell(workbook, row, COL_SURV_OUTCOME, "Outcome of Surveillance");
        addHeadingCell(workbook, row, COL_SURV_OUTCOME_OTHER, "Outcome of Surveillance - Other Explanation");
        addHeadingCell(workbook, row, COL_NONCONFORMITY_TYPES_RESULTANT,
                "Non-Conformity Type(s) Resultant of Surveillance (i.e. \"170.xxx (x)(x)\")");
        addHeadingCell(workbook, row, COL_CERT_STATUS_RESULTANT, "Certification Status Resultant of Surveillance");
        addHeadingCell(workbook, row, COL_SUSPENDED, "Suspended During Surveillance?");
        addHeadingCell(workbook, row, COL_SURV_PROCESS_TYPE, "Surveillance Process Type");
        addHeadingCell(workbook, row, COL_SURV_PROCESS_TYPE_OTHER, "Surveillance Process Type - Other Explanation");

        //these headings are more complicated with various fonts throughout the cell
        String cellTitle = "Grounds for Initiating Surveillance";
        String cellSubtitle = getGroundsForInitiatingSurveillanceDescription();
        addRichTextHeadingCell(workbook, row, COL_SURV_GROUNDS, cellTitle, cellSubtitle);

        cellTitle = "Potential Causes of Non-Conformities or Suspected Non-Conformities";
        cellSubtitle = "What were the substantial factors that, in the ONC-ACB’s assessment, "
                + "caused or contributed to the suspected non-conformity or non-conformities "
                + "(e.g., implementation problem, user error, limitations on the use of capabilities "
                + "in the field, a failure to disclose known material information, etc.)?";
        addRichTextHeadingCell(workbook, row, COL_NONCONFORMITY_CAUSES, cellTitle, cellSubtitle);

        cellTitle = "Nature of Any Substantiated Non-Conformities";
        cellSubtitle = "Did ONC-ACB substantiate any non-conformities? If so, what was the nature "
                + "of the non-conformity or non-conformities that were substantiated?\n"
                + "Please include specific criteria involved.";
        addRichTextHeadingCell(workbook, row, COL_NONCONFORMITY_NATURES, cellTitle, cellSubtitle);

        cellTitle = "Steps to Surveil and Substantiate";
        cellSubtitle = getStepsToSurveilDescription();
        addRichTextHeadingCell(workbook, row, COL_SURV_STEPS, cellTitle, cellSubtitle);

        cellTitle = "Steps to Engage and Work with Developer and End-Users";
        cellSubtitle = "What steps were taken by ONC-ACB to engage and work with the developer and "
                + "end-users to analyze and determine the causes of any suspected non-conformities and "
                + "related deficiencies?";
        addRichTextHeadingCell(workbook, row, COL_ENGAGEMENT_STEPS, cellTitle, cellSubtitle);

        cellTitle = "Additional Costs Evaluation";
        cellSubtitle = getAdditionalCostsEvaluationDescription();
        addRichTextHeadingCell(workbook, row, COL_ADDITIONAL_COSTS, cellTitle, cellSubtitle);

        cellTitle = "Limitations Evaluation";
        cellSubtitle = getLimitationsEvaluationDescription();
        addRichTextHeadingCell(workbook, row, COL_LIMITATIONS_EVAL, cellTitle, cellSubtitle);

        cellTitle = "Non-Disclosure Evaluation";
        cellSubtitle = getNonDisclosureEvaluationDescription();
        addRichTextHeadingCell(workbook, row, COL_NONDISCLOSURE_EVAL, cellTitle, cellSubtitle);

        cellTitle = "Direction for Developer Resolution";
        cellSubtitle = "If a non-conformity was substantiated, what direction was given to the developer to "
                + "resolve the non-conformity?";
        addRichTextHeadingCell(workbook, row, COL_DEV_RESOLUTION, cellTitle, cellSubtitle);

        cellTitle = "Verification of Completed CAP";
        cellSubtitle = "If an approved Corrective Action Plan was received and completed, "
                + "how did ONC-ACB verify that the developer has completed all requirements "
                + "specified in the Plan?";
        addRichTextHeadingCell(workbook, row, COL_COMPLETED_CAP, cellTitle, cellSubtitle);
        return 1;
    }

    private int addTableData(SurveillanceReportWorkbookWrapper workbook, Sheet sheet,
            final List<QuarterlyReportDTO> quarterlyReports) {
        int addedRows = 0;
        int rowNum = 2;
        //get some details (surveillance and status history) about each relevant listing for each quarterly report
        List<CertifiedProductSearchDetails> relevantListings = getRelevantListingsDetails(quarterlyReports);
        //get all the surveillances relevant to the time period of the report from the listings
        List<Surveillance> relevantSuveillances = new ArrayList<Surveillance>();
        for (CertifiedProductSearchDetails listing : relevantListings) {
            //each listing has 1 or more surveillances
            //but maybe not all are from the periods of time covered by the quarters
            List<Surveillance> listingSurveillances
                = determineRelevantSurveillances(quarterlyReports, listing.getSurveillance());
            relevantSuveillances.addAll(listingSurveillances);
        }
        //sort the relevant surveillances with oldest start date first and newest start date last
        relevantSuveillances.sort(new Comparator<Surveillance>() {
            @Override
            public int compare(final Surveillance o1, final Surveillance o2) {
                if (o1.getStartDay().isBefore(o2.getStartDay())) {
                    return -1;
                } else if (o1.getStartDay().equals(o2.getStartDay())) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        for (Surveillance surv : relevantSuveillances) {
            CertifiedProductSearchDetails listing = null;
            for (CertifiedProductSearchDetails currListing : relevantListings) {
                if (surv.getCertifiedProduct().getId().equals(currListing.getId())) {
                    listing = currListing;
                }
            }
            //get the privileged data for this surveillance
            //it may be different across quarters
            List<PrivilegedSurveillanceDTO> privilegedSurvQuarterlyData =
                    privilegedSurvDao.getBySurveillance(surv.getId());

            Row row = workbook.getRow(sheet, rowNum);
            addDataCell(workbook, row, COL_CHPL_ID, listing.getChplProductNumber());
            addDataCell(workbook, row, COL_SURV_ID, surv.getFriendlyId());
            addDataCell(workbook, row, COL_SURV_ACTIVITY_TRACKER, listing.getChplProductNumber() + surv.getFriendlyId());
            //chpl generated i think once we associate a complaint with surveillance?
            addDataCell(workbook, row, COL_RELATED_COMPLAINT, "");
            if (determineIfSurveillanceHappenedDuringQuarter("Q1", quarterlyReports, surv)) {
                addDataCell(workbook, row, COL_Q1, "X");
            }
            if (determineIfSurveillanceHappenedDuringQuarter("Q2", quarterlyReports, surv)) {
                addDataCell(workbook, row, COL_Q2, "X");
            }
            if (determineIfSurveillanceHappenedDuringQuarter("Q3", quarterlyReports, surv)) {
                addDataCell(workbook, row, COL_Q3, "X");
            }
            if (determineIfSurveillanceHappenedDuringQuarter("Q4", quarterlyReports, surv)) {
                addDataCell(workbook, row, COL_Q4, "X");
            }

            addDataCell(workbook, row, COL_DEVELOPER_NAME, listing.getDeveloper().getName());
            addDataCell(workbook, row, COL_PRODUCT_NAME, listing.getProduct().getName());
            addDataCell(workbook, row, COL_PRODUCT_VERSION, listing.getVersion().getVersion());
            //user has to enter this field
            addDataCell(workbook, row, COL_K1_REVIEWED, generateK1ReviewedValue(quarterlyReports, privilegedSurvQuarterlyData));
            addDataCell(workbook, row, COL_SURV_TYPE, surv.getType().getName());
            addDataCell(workbook, row, COL_SURV_LOCATION_COUNT,
                    surv.getRandomizedSitesUsed() == null ? "" : surv.getRandomizedSitesUsed().toString());
            addDataCell(workbook, row, COL_SURV_BEGIN, dateFormatter.format(surv.getStartDay()));
            addDataCell(workbook, row, COL_SURV_END, surv.getEndDay() == null ? "" : dateFormatter.format(surv.getEndDay()));
            //user has to enter this field
            addDataCell(workbook, row, COL_SURV_OUTCOME,
                    generateSurveillanceOutcomeValue(quarterlyReports, privilegedSurvQuarterlyData));
            addDataCell(workbook, row, COL_SURV_OUTCOME_OTHER,
                    generateSurveillanceOutcomeOtherValue(quarterlyReports, privilegedSurvQuarterlyData));
            addDataCell(workbook, row, COL_NONCONFORMITY_TYPES_RESULTANT, determineNonconformityTypes(surv));
            addDataCell(workbook, row, COL_CERT_STATUS_RESULTANT, determineResultantCertificationStatus(listing, surv));
            addDataCell(workbook, row, COL_SUSPENDED, determineSuspendedStatus(listing, surv));
            //user has to enter this field
            addDataCell(workbook, row, COL_SURV_PROCESS_TYPE,
                    generateSurveillanceProcessTypeValue(quarterlyReports, privilegedSurvQuarterlyData));
            addDataCell(workbook, row, COL_SURV_PROCESS_TYPE_OTHER,
                    generateSurveillanceProcessTypeOtherValue(quarterlyReports, privilegedSurvQuarterlyData));
            addDataCell(workbook, row, COL_SURV_GROUNDS,
                    generateGroundsForInitiatingValue(quarterlyReports, privilegedSurvQuarterlyData));
            addDataCell(workbook, row, COL_NONCONFORMITY_CAUSES,
                    generateNonconformityCausesValue(quarterlyReports, privilegedSurvQuarterlyData));
            addDataCell(workbook, row, COL_NONCONFORMITY_NATURES,
                    generateNonconformityNatureValue(quarterlyReports, privilegedSurvQuarterlyData));
            addDataCell(workbook, row, COL_SURV_STEPS,
                    generateStepsToSurveilValue(quarterlyReports, privilegedSurvQuarterlyData));
            addDataCell(workbook, row, COL_ENGAGEMENT_STEPS,
                    generateStepsToEngageValue(quarterlyReports, privilegedSurvQuarterlyData));
            addDataCell(workbook, row, COL_ADDITIONAL_COSTS,
                    generateAdditionalCostsValue(quarterlyReports, privilegedSurvQuarterlyData));
            addDataCell(workbook, row, COL_LIMITATIONS_EVAL,
                    generateLimitationsValue(quarterlyReports, privilegedSurvQuarterlyData));
            addDataCell(workbook, row, COL_NONDISCLOSURE_EVAL,
                    generateNondisclosureValue(quarterlyReports, privilegedSurvQuarterlyData));
            addDataCell(workbook, row, COL_DEV_RESOLUTION,
                    generateDeveloperResolutionValue(quarterlyReports, privilegedSurvQuarterlyData));
            addDataCell(workbook, row, COL_COMPLETED_CAP,
                    generateCompletedCapValue(quarterlyReports, privilegedSurvQuarterlyData));
            pt.drawBorders(new CellRangeAddress(rowNum, rowNum, 1, LAST_DATA_COLUMN - 1),
                    BorderStyle.HAIR, BorderExtent.HORIZONTAL);
            addedRows++;
            rowNum++;
        }
        return addedRows;
    }

    private List<CertifiedProductSearchDetails> getRelevantListingsDetails(List<QuarterlyReportDTO> quarterlyReports) {
        List<CertifiedProductSearchDetails> relevantListingDetails =
                new ArrayList<CertifiedProductSearchDetails>();
        for (QuarterlyReportDTO currReport : quarterlyReports) {
            //get all of the surveillance details for the listings relevant to this report
            //the details object included on the quarterly report has some of the data that is needed
            //to build activities and outcomes worksheet but not all of it so we need to do
            //some other work to get the necessary data and put it all together
            List<QuarterlyReportRelevantListingDTO> qrRelevantListings = reportManager.getListingsWithRelevantSurveillance(currReport);
            List<QuarterlyReportRelevantListingDTO> missingListingDtos = new ArrayList<QuarterlyReportRelevantListingDTO>();
            for (QuarterlyReportRelevantListingDTO listingFromReport : qrRelevantListings) {
                boolean alreadyGotDetails = false;
                for (CertifiedProductSearchDetails existingDetails : relevantListingDetails) {
                    if (listingFromReport.getId() != null && existingDetails.getId() != null
                            && listingFromReport.getId().longValue() == existingDetails.getId().longValue()) {
                        alreadyGotDetails = true;
                    }
                }
                if (!alreadyGotDetails) {
                    missingListingDtos.add(listingFromReport);
                }
            }
            //some listings will be relevant across multiple quarters so make sure
            //we don't take the extra time to get their details multiple times.
            relevantListingDetails.addAll(getRelevantListingDetails(missingListingDtos));
        }
        return relevantListingDetails;
    }

    /**
     * The relevant listings objects in the quarterly report has some information about the listing
     * itself but not everything that is needed to build the reports.
     * This method queries for certification status events as well as relevant surveillance
     * (surveillance that occurred during the quarter) and adds it into a new details object.
     * The returned objects should have all of the fields needed to fill out
     * the Activities and Outcomes worksheet.
     */
    private List<CertifiedProductSearchDetails> getRelevantListingDetails(
            final List<QuarterlyReportRelevantListingDTO> listingDtos) {
        List<CertifiedProductSearchDetails> relevantListingDetails =
                new ArrayList<CertifiedProductSearchDetails>();
        for (QuarterlyReportRelevantListingDTO listingDetails : listingDtos) {
            LOGGER.debug("Creating CertifiedProductSearchDetails for listing " + listingDetails.getChplProductNumber());
            CertifiedProductSearchDetails completeListingDetails = new CertifiedProductSearchDetails();
            completeListingDetails.setId(listingDetails.getId());
            completeListingDetails.setChplProductNumber(listingDetails.getChplProductNumber());
            Developer dev = new Developer();
            dev.setId(listingDetails.getDeveloper().getId());
            dev.setName(listingDetails.getDeveloper().getName());
            completeListingDetails.setDeveloper(dev);
            Product prod = new Product();
            prod.setId(listingDetails.getProduct().getId());
            prod.setName(listingDetails.getProduct().getName());
            completeListingDetails.setProduct(prod);
            ProductVersion ver = new ProductVersion();
            ver.setId(listingDetails.getVersion().getId());
            ver.setVersion(listingDetails.getVersion().getVersion());
            completeListingDetails.setVersion(ver);

            try {
                LOGGER.debug("Getting certification status events for listing " + listingDetails.getChplProductNumber());
                List<CertificationStatusEvent> certStatusEvents =
                        detailsManager.getCertificationStatusEvents(listingDetails.getId());
                completeListingDetails.setCertificationEvents(certStatusEvents);
                LOGGER.debug("Got " + completeListingDetails.getCertificationEvents().size()
                        + " certification status events for listing " + listingDetails.getChplProductNumber());
            } catch (EntityRetrievalException ex) {
                LOGGER.error("Could not get certification status events for listing " + listingDetails.getId());
            }

            LOGGER.debug("Getting surveillances for listing " + listingDetails.getChplProductNumber());
            List<Surveillance> surveillances = survManager.getByCertifiedProduct(listingDetails.getId());
            completeListingDetails.setSurveillance(surveillances);
            LOGGER.debug("Got " + completeListingDetails.getSurveillance().size()
                    + " surveillances for listing " + listingDetails.getChplProductNumber());
            relevantListingDetails.add(completeListingDetails);
        }
        return relevantListingDetails;
    }

    /**
     * A surveillance is relevant if its dates occur within any of the quarterlyReports passed in.
     */
    private List<Surveillance> determineRelevantSurveillances(List<QuarterlyReportDTO> quarterlyReports,
            List<Surveillance> allSurveillances) {
        List<Surveillance> relevantSurveillances = new ArrayList<Surveillance>();
        for (Surveillance currSurv : allSurveillances) {
            boolean isRelevantToAtLeastOneQuarter = false;
            for (QuarterlyReportDTO quarterlyReport : quarterlyReports) {
                if (surveillanceOccursDuringQuarterlyReportTime(currSurv, quarterlyReport)) {
                    isRelevantToAtLeastOneQuarter = true;
                }
            }
            if (isRelevantToAtLeastOneQuarter) {
                relevantSurveillances.add(currSurv);
            }
        }
        return relevantSurveillances;
    }

    private boolean surveillanceOccursDuringQuarterlyReportTime(Surveillance surv, QuarterlyReportDTO quarterlyReport) {
        return surv.getStartDay().compareTo(quarterlyReport.getEndDate()) <= 0
                && (surv.getEndDay() == null
                    || surv.getEndDay().compareTo(quarterlyReport.getStartDate()) >= 0);
    }

    private boolean determineIfSurveillanceHappenedDuringQuarter(String quarterName,
            List<QuarterlyReportDTO> quarterlyReports, Surveillance surv) {
        QuarterlyReportDTO quarterlyReport = null;
        for (QuarterlyReportDTO currReport : quarterlyReports) {
            if (currReport.getQuarter().getName().equals(quarterName)) {
                quarterlyReport = currReport;
            }
        }
        boolean result = false;
        if (quarterlyReport != null) {
            result = surveillanceOccursDuringQuarterlyReportTime(surv, quarterlyReport);
        }
        return result;
    }

    private String generateK1ReviewedValue(List<QuarterlyReportDTO> quarterlyReports,
            List<PrivilegedSurveillanceDTO> privilegedSurvData) {
        String result = "";
        if (quarterlyReports.size() == 1) {
            //find the privileged surv data for the single report
            QuarterlyReportDTO report = quarterlyReports.get(0);
            for (PrivilegedSurveillanceDTO currSurv : privilegedSurvData) {
                if (currSurv.getQuarterlyReport().getId().longValue() == report.getId().longValue()
                        && currSurv.getK1Reviewed() != null) {
                    result = (currSurv.getK1Reviewed().booleanValue() ? "Yes" : "No");
                }
            }
        } else {
            //there are multiple reports... combine the values for this field in a nice way for the user
            //key is k1reviewed yes/no and value is applicable quarter name(s) like 'Q1, Q2'
            Map<String, ArrayList<String>> valueMap = new LinkedHashMap<String, ArrayList<String>>();
            for (QuarterlyReportDTO currReport : quarterlyReports) {
                for (PrivilegedSurveillanceDTO currSurv : privilegedSurvData) {
                    if (currSurv.getQuarterlyReport().getId().longValue() == currReport.getId().longValue()
                            && currSurv.getK1Reviewed() != null) {
                        String k1Val = (currSurv.getK1Reviewed().booleanValue() ? "Yes" : "No");
                        if (valueMap.get(k1Val) != null) {
                            valueMap.get(k1Val).add(currReport.getQuarter().getName());
                        } else {
                            ArrayList<String> quarterNameList = new ArrayList<String>();
                            quarterNameList.add(currReport.getQuarter().getName());
                            valueMap.put(k1Val, quarterNameList);
                        }
                    }
                }
            }
            result = MultiQuarterWorksheetBuilderUtil.buildStringFromMap(valueMap);
        }
        return result;
    }

    private String generateSurveillanceOutcomeValue(List<QuarterlyReportDTO> quarterlyReports,
            List<PrivilegedSurveillanceDTO> privilegedSurvData) {
        String result = "";
        if (quarterlyReports.size() == 1) {
            //find the privileged surv data for the single report
            QuarterlyReportDTO report = quarterlyReports.get(0);
            for (PrivilegedSurveillanceDTO currSurv : privilegedSurvData) {
                if (currSurv.getQuarterlyReport().getId().longValue() == report.getId().longValue()
                        && currSurv.getSurveillanceOutcome() != null) {
                    result = currSurv.getSurveillanceOutcome().getName();
                }
            }
        } else {
            //there are multiple reports... combine the values for this field in a nice way for the user
            //key is surveillance outcome name and value is applicable quarter name(s) like 'Q1, Q2'
            Map<String, ArrayList<String>> valueMap = new LinkedHashMap<String, ArrayList<String>>();
            for (QuarterlyReportDTO currReport : quarterlyReports) {
                for (PrivilegedSurveillanceDTO currSurv : privilegedSurvData) {
                    if (currSurv.getQuarterlyReport().getId().longValue() == currReport.getId().longValue()
                            && currSurv.getSurveillanceOutcome() != null) {
                        String survOutcomeVal = currSurv.getSurveillanceOutcome().getName();
                        if (valueMap.get(survOutcomeVal) != null) {
                            valueMap.get(survOutcomeVal).add(currReport.getQuarter().getName());
                        } else {
                            ArrayList<String> quarterNameList = new ArrayList<String>();
                            quarterNameList.add(currReport.getQuarter().getName());
                            valueMap.put(survOutcomeVal, quarterNameList);
                        }
                    }
                }
            }
            result = MultiQuarterWorksheetBuilderUtil.buildStringFromMap(valueMap);
        }
        return result;
    }

    private String generateSurveillanceOutcomeOtherValue(List<QuarterlyReportDTO> quarterlyReports,
            List<PrivilegedSurveillanceDTO> privilegedSurvData) {
        String result = "";
        if (quarterlyReports.size() == 1) {
            //find the privileged surv data for the single report
            QuarterlyReportDTO report = quarterlyReports.get(0);
            for (PrivilegedSurveillanceDTO currSurv : privilegedSurvData) {
                if (currSurv.getQuarterlyReport().getId().longValue() == report.getId().longValue()
                        && !StringUtils.isEmpty(currSurv.getSurveillanceOutcomeOther())) {
                    result = currSurv.getSurveillanceOutcomeOther();
                }
            }
        } else {
            //there are multiple reports... combine the values for this field in a nice way for the user
            //key is surveillance outcome name and value is applicable quarter name(s) like 'Q1, Q2'
            Map<String, ArrayList<String>> valueMap = new LinkedHashMap<String, ArrayList<String>>();
            for (QuarterlyReportDTO currReport : quarterlyReports) {
                for (PrivilegedSurveillanceDTO currSurv : privilegedSurvData) {
                    if (currSurv.getQuarterlyReport().getId().longValue() == currReport.getId().longValue()
                            && !StringUtils.isEmpty(currSurv.getSurveillanceOutcomeOther())) {
                        String survOutcomeVal = currSurv.getSurveillanceOutcomeOther();
                        if (valueMap.get(survOutcomeVal) != null) {
                            valueMap.get(survOutcomeVal).add(currReport.getQuarter().getName());
                        } else {
                            ArrayList<String> quarterNameList = new ArrayList<String>();
                            quarterNameList.add(currReport.getQuarter().getName());
                            valueMap.put(survOutcomeVal, quarterNameList);
                        }
                    }
                }
            }
            result = MultiQuarterWorksheetBuilderUtil.buildStringFromMap(valueMap);
        }
        return result;
    }

    private String generateSurveillanceProcessTypeValue(List<QuarterlyReportDTO> quarterlyReports,
            List<PrivilegedSurveillanceDTO> privilegedSurvData) {
        String result = "";
        if (quarterlyReports.size() == 1) {
            //find the privileged surv data for the single report
            QuarterlyReportDTO report = quarterlyReports.get(0);
            for (PrivilegedSurveillanceDTO currSurv : privilegedSurvData) {
                if (currSurv.getQuarterlyReport().getId().longValue() == report.getId().longValue()
                        && currSurv.getSurveillanceProcessType() != null) {
                    result = currSurv.getSurveillanceProcessType().getName();
                }
            }
        } else {
            //there are multiple reports... combine the values for this field in a nice way for the user
            //key is surveillance process type name and value is applicable quarter name(s) like 'Q1, Q2'
            Map<String, ArrayList<String>> valueMap = new LinkedHashMap<String, ArrayList<String>>();
            for (QuarterlyReportDTO currReport : quarterlyReports) {
                for (PrivilegedSurveillanceDTO currSurv : privilegedSurvData) {
                    if (currSurv.getQuarterlyReport().getId().longValue() == currReport.getId().longValue()
                            && currSurv.getSurveillanceProcessType() != null) {
                        String survProcTypeVal = currSurv.getSurveillanceProcessType().getName();
                        if (valueMap.get(survProcTypeVal) != null) {
                            valueMap.get(survProcTypeVal).add(currReport.getQuarter().getName());
                        } else {
                            ArrayList<String> quarterNameList = new ArrayList<String>();
                            quarterNameList.add(currReport.getQuarter().getName());
                            valueMap.put(survProcTypeVal, quarterNameList);
                        }
                    }
                }
            }
            result = MultiQuarterWorksheetBuilderUtil.buildStringFromMap(valueMap);
        }
        return result;
    }

    private String generateSurveillanceProcessTypeOtherValue(List<QuarterlyReportDTO> quarterlyReports,
            List<PrivilegedSurveillanceDTO> privilegedSurvData) {
        String result = "";
        if (quarterlyReports.size() == 1) {
            //find the privileged surv data for the single report
            QuarterlyReportDTO report = quarterlyReports.get(0);
            for (PrivilegedSurveillanceDTO currSurv : privilegedSurvData) {
                if (currSurv.getQuarterlyReport().getId().longValue() == report.getId().longValue()
                        && !StringUtils.isEmpty(currSurv.getSurveillanceProcessTypeOther())) {
                    result = currSurv.getSurveillanceProcessTypeOther();
                }
            }
        } else {
            //there are multiple reports... combine the values for this field in a nice way for the user
            //key is surveillance process type name and value is applicable quarter name(s) like 'Q1, Q2'
            Map<String, ArrayList<String>> valueMap = new LinkedHashMap<String, ArrayList<String>>();
            for (QuarterlyReportDTO currReport : quarterlyReports) {
                for (PrivilegedSurveillanceDTO currSurv : privilegedSurvData) {
                    if (currSurv.getQuarterlyReport().getId().longValue() == currReport.getId().longValue()
                            && !StringUtils.isEmpty(currSurv.getSurveillanceProcessTypeOther())) {
                        String survProcTypeVal = currSurv.getSurveillanceProcessTypeOther();
                        if (valueMap.get(survProcTypeVal) != null) {
                            valueMap.get(survProcTypeVal).add(currReport.getQuarter().getName());
                        } else {
                            ArrayList<String> quarterNameList = new ArrayList<String>();
                            quarterNameList.add(currReport.getQuarter().getName());
                            valueMap.put(survProcTypeVal, quarterNameList);
                        }
                    }
                }
            }
            result = MultiQuarterWorksheetBuilderUtil.buildStringFromMap(valueMap);
        }
        return result;
    }

    private String generateGroundsForInitiatingValue(List<QuarterlyReportDTO> quarterlyReports,
            List<PrivilegedSurveillanceDTO> privilegedSurvData) {
        String result = "";
        if (quarterlyReports.size() == 1) {
            //find the privileged surv data for the single report
            QuarterlyReportDTO report = quarterlyReports.get(0);
            for (PrivilegedSurveillanceDTO currSurv : privilegedSurvData) {
                if (currSurv.getQuarterlyReport().getId().longValue() == report.getId().longValue()
                        && currSurv.getGroundsForInitiating() != null) {
                    result = currSurv.getGroundsForInitiating();
                }
            }
        } else {
            //there are multiple reports... combine the values for this field in a nice way for the user
            //key is grounds for initiating str and value is applicable quarter name(s) like 'Q1, Q2'
            Map<String, ArrayList<String>> valueMap = new LinkedHashMap<String, ArrayList<String>>();
            for (QuarterlyReportDTO currReport : quarterlyReports) {
                for (PrivilegedSurveillanceDTO currSurv : privilegedSurvData) {
                    if (currSurv.getQuarterlyReport().getId().longValue() == currReport.getId().longValue()
                            && currSurv.getGroundsForInitiating() != null) {
                        String survGroundsVal = currSurv.getGroundsForInitiating();
                        if (valueMap.get(survGroundsVal) != null) {
                            valueMap.get(survGroundsVal).add(currReport.getQuarter().getName());
                        } else {
                            ArrayList<String> quarterNameList = new ArrayList<String>();
                            quarterNameList.add(currReport.getQuarter().getName());
                            valueMap.put(survGroundsVal, quarterNameList);
                        }
                    }
                }
            }
            result = MultiQuarterWorksheetBuilderUtil.buildStringFromMap(valueMap);
        }
        return result;
    }

    private String generateNonconformityCausesValue(List<QuarterlyReportDTO> quarterlyReports,
            List<PrivilegedSurveillanceDTO> privilegedSurvData) {
        String result = "";
        if (quarterlyReports.size() == 1) {
            //find the privileged surv data for the single report
            QuarterlyReportDTO report = quarterlyReports.get(0);
            for (PrivilegedSurveillanceDTO currSurv : privilegedSurvData) {
                if (currSurv.getQuarterlyReport().getId().longValue() == report.getId().longValue()
                        && currSurv.getNonconformityCauses() != null) {
                    result = currSurv.getNonconformityCauses();
                }
            }
        } else {
            //there are multiple reports... combine the values for this field in a nice way for the user
            //key is the causes str and value is applicable quarter name(s) like 'Q1, Q2'
            Map<String, ArrayList<String>> valueMap = new LinkedHashMap<String, ArrayList<String>>();
            for (QuarterlyReportDTO currReport : quarterlyReports) {
                for (PrivilegedSurveillanceDTO currSurv : privilegedSurvData) {
                    if (currSurv.getQuarterlyReport().getId().longValue() == currReport.getId().longValue()
                            && currSurv.getNonconformityCauses() != null) {
                        String causesVal = currSurv.getNonconformityCauses();
                        if (valueMap.get(causesVal) != null) {
                            valueMap.get(causesVal).add(currReport.getQuarter().getName());
                        } else {
                            ArrayList<String> quarterNameList = new ArrayList<String>();
                            quarterNameList.add(currReport.getQuarter().getName());
                            valueMap.put(causesVal, quarterNameList);
                        }
                    }
                }
            }
            result = MultiQuarterWorksheetBuilderUtil.buildStringFromMap(valueMap);
        }
        return result;
    }

    private String generateNonconformityNatureValue(List<QuarterlyReportDTO> quarterlyReports,
            List<PrivilegedSurveillanceDTO> privilegedSurvData) {
        String result = "";
        if (quarterlyReports.size() == 1) {
            //find the privileged surv data for the single report
            QuarterlyReportDTO report = quarterlyReports.get(0);
            for (PrivilegedSurveillanceDTO currSurv : privilegedSurvData) {
                if (currSurv.getQuarterlyReport().getId().longValue() == report.getId().longValue()
                        && currSurv.getNonconformityNature() != null) {
                    result = currSurv.getNonconformityNature();
                }
            }
        } else {
            //there are multiple reports... combine the values for this field in a nice way for the user
            //key is the nature str and value is applicable quarter name(s) like 'Q1, Q2'
            Map<String, ArrayList<String>> valueMap = new LinkedHashMap<String, ArrayList<String>>();
            for (QuarterlyReportDTO currReport : quarterlyReports) {
                for (PrivilegedSurveillanceDTO currSurv : privilegedSurvData) {
                    if (currSurv.getQuarterlyReport().getId().longValue() == currReport.getId().longValue()
                            && currSurv.getNonconformityNature() != null) {
                        String natureVal = currSurv.getNonconformityNature();
                        if (valueMap.get(natureVal) != null) {
                            valueMap.get(natureVal).add(currReport.getQuarter().getName());
                        } else {
                            ArrayList<String> quarterNameList = new ArrayList<String>();
                            quarterNameList.add(currReport.getQuarter().getName());
                            valueMap.put(natureVal, quarterNameList);
                        }
                    }
                }
            }
            result = MultiQuarterWorksheetBuilderUtil.buildStringFromMap(valueMap);
        }
        return result;
    }

    private String generateStepsToSurveilValue(List<QuarterlyReportDTO> quarterlyReports,
            List<PrivilegedSurveillanceDTO> privilegedSurvData) {
        String result = "";
        if (quarterlyReports.size() == 1) {
            //find the privileged surv data for the single report
            QuarterlyReportDTO report = quarterlyReports.get(0);
            for (PrivilegedSurveillanceDTO currSurv : privilegedSurvData) {
                if (currSurv.getQuarterlyReport().getId().longValue() == report.getId().longValue()
                        && currSurv.getStepsToSurveil() != null) {
                    result = currSurv.getStepsToSurveil();
                }
            }
        } else {
            //there are multiple reports... combine the values for this field in a nice way for the user
            //key is the steps to surveil str and value is applicable quarter name(s) like 'Q1, Q2'
            Map<String, ArrayList<String>> valueMap = new LinkedHashMap<String, ArrayList<String>>();
            for (QuarterlyReportDTO currReport : quarterlyReports) {
                for (PrivilegedSurveillanceDTO currSurv : privilegedSurvData) {
                    if (currSurv.getQuarterlyReport().getId().longValue() == currReport.getId().longValue()
                            && currSurv.getStepsToSurveil() != null) {
                        String stepsVal = currSurv.getStepsToSurveil();
                        if (valueMap.get(stepsVal) != null) {
                            valueMap.get(stepsVal).add(currReport.getQuarter().getName());
                        } else {
                            ArrayList<String> quarterNameList = new ArrayList<String>();
                            quarterNameList.add(currReport.getQuarter().getName());
                            valueMap.put(stepsVal, quarterNameList);
                        }
                    }
                }
            }
            result = MultiQuarterWorksheetBuilderUtil.buildStringFromMap(valueMap);
        }
        return result;
    }

    private String generateStepsToEngageValue(List<QuarterlyReportDTO> quarterlyReports,
            List<PrivilegedSurveillanceDTO> privilegedSurvData) {
        String result = "";
        if (quarterlyReports.size() == 1) {
            //find the privileged surv data for the single report
            QuarterlyReportDTO report = quarterlyReports.get(0);
            for (PrivilegedSurveillanceDTO currSurv : privilegedSurvData) {
                if (currSurv.getQuarterlyReport().getId().longValue() == report.getId().longValue()
                        && currSurv.getStepsToEngage() != null) {
                    result = currSurv.getStepsToEngage();
                }
            }
        } else {
            //there are multiple reports... combine the values for this field in a nice way for the user
            //key is the steps to engage str and value is applicable quarter name(s) like 'Q1, Q2'
            Map<String, ArrayList<String>> valueMap = new LinkedHashMap<String, ArrayList<String>>();
            for (QuarterlyReportDTO currReport : quarterlyReports) {
                for (PrivilegedSurveillanceDTO currSurv : privilegedSurvData) {
                    if (currSurv.getQuarterlyReport().getId().longValue() == currReport.getId().longValue()
                            && currSurv.getStepsToEngage() != null) {
                        String stepsVal = currSurv.getStepsToEngage();
                        if (valueMap.get(stepsVal) != null) {
                            valueMap.get(stepsVal).add(currReport.getQuarter().getName());
                        } else {
                            ArrayList<String> quarterNameList = new ArrayList<String>();
                            quarterNameList.add(currReport.getQuarter().getName());
                            valueMap.put(stepsVal, quarterNameList);
                        }
                    }
                }
            }
            result = MultiQuarterWorksheetBuilderUtil.buildStringFromMap(valueMap);
        }
        return result;
    }

    private String generateAdditionalCostsValue(List<QuarterlyReportDTO> quarterlyReports,
            List<PrivilegedSurveillanceDTO> privilegedSurvData) {
        String result = "";
        if (quarterlyReports.size() == 1) {
            //find the privileged surv data for the single report
            QuarterlyReportDTO report = quarterlyReports.get(0);
            for (PrivilegedSurveillanceDTO currSurv : privilegedSurvData) {
                if (currSurv.getQuarterlyReport().getId().longValue() == report.getId().longValue()
                        && currSurv.getAdditionalCostsEvaluation() != null) {
                    result = currSurv.getAdditionalCostsEvaluation();
                }
            }
        } else {
            //there are multiple reports... combine the values for this field in a nice way for the user
            //key is the additional costs str and value is applicable quarter name(s) like 'Q1, Q2'
            Map<String, ArrayList<String>> valueMap = new LinkedHashMap<String, ArrayList<String>>();
            for (QuarterlyReportDTO currReport : quarterlyReports) {
                for (PrivilegedSurveillanceDTO currSurv : privilegedSurvData) {
                    if (currSurv.getQuarterlyReport().getId().longValue() == currReport.getId().longValue()
                            && currSurv.getAdditionalCostsEvaluation() != null) {
                        String costsVal = currSurv.getAdditionalCostsEvaluation();
                        if (valueMap.get(costsVal) != null) {
                            valueMap.get(costsVal).add(currReport.getQuarter().getName());
                        } else {
                            ArrayList<String> quarterNameList = new ArrayList<String>();
                            quarterNameList.add(currReport.getQuarter().getName());
                            valueMap.put(costsVal, quarterNameList);
                        }
                    }
                }
            }
            result = MultiQuarterWorksheetBuilderUtil.buildStringFromMap(valueMap);
        }
        return result;
    }

    private String generateLimitationsValue(List<QuarterlyReportDTO> quarterlyReports,
            List<PrivilegedSurveillanceDTO> privilegedSurvData) {
        String result = "";
        if (quarterlyReports.size() == 1) {
            //find the privileged surv data for the single report
            QuarterlyReportDTO report = quarterlyReports.get(0);
            for (PrivilegedSurveillanceDTO currSurv : privilegedSurvData) {
                if (currSurv.getQuarterlyReport().getId().longValue() == report.getId().longValue()
                        && currSurv.getLimitationsEvaluation() != null) {
                    result = currSurv.getLimitationsEvaluation();
                }
            }
        } else {
            //there are multiple reports... combine the values for this field in a nice way for the user
            //key is the limitations str and value is applicable quarter name(s) like 'Q1, Q2'
            Map<String, ArrayList<String>> valueMap = new LinkedHashMap<String, ArrayList<String>>();
            for (QuarterlyReportDTO currReport : quarterlyReports) {
                for (PrivilegedSurveillanceDTO currSurv : privilegedSurvData) {
                    if (currSurv.getQuarterlyReport().getId().longValue() == currReport.getId().longValue()
                            && currSurv.getLimitationsEvaluation() != null) {
                        String limitationsVal = currSurv.getLimitationsEvaluation();
                        if (valueMap.get(limitationsVal) != null) {
                            valueMap.get(limitationsVal).add(currReport.getQuarter().getName());
                        } else {
                            ArrayList<String> quarterNameList = new ArrayList<String>();
                            quarterNameList.add(currReport.getQuarter().getName());
                            valueMap.put(limitationsVal, quarterNameList);
                        }
                    }
                }
            }
            result = MultiQuarterWorksheetBuilderUtil.buildStringFromMap(valueMap);
        }
        return result;
    }

    private String generateNondisclosureValue(List<QuarterlyReportDTO> quarterlyReports,
            List<PrivilegedSurveillanceDTO> privilegedSurvData) {
        String result = "";
        if (quarterlyReports.size() == 1) {
            //find the privileged surv data for the single report
            QuarterlyReportDTO report = quarterlyReports.get(0);
            for (PrivilegedSurveillanceDTO currSurv : privilegedSurvData) {
                if (currSurv.getQuarterlyReport().getId().longValue() == report.getId().longValue()
                        && currSurv.getNondisclosureEvaluation() != null) {
                    result = currSurv.getNondisclosureEvaluation();
                }
            }
        } else {
            //there are multiple reports... combine the values for this field in a nice way for the user
            //key is the nondisclosure str and value is applicable quarter name(s) like 'Q1, Q2'
            Map<String, ArrayList<String>> valueMap = new LinkedHashMap<String, ArrayList<String>>();
            for (QuarterlyReportDTO currReport : quarterlyReports) {
                for (PrivilegedSurveillanceDTO currSurv : privilegedSurvData) {
                    if (currSurv.getQuarterlyReport().getId().longValue() == currReport.getId().longValue()
                            && currSurv.getNondisclosureEvaluation() != null) {
                        String nondisclosureVal = currSurv.getNondisclosureEvaluation();
                        if (valueMap.get(nondisclosureVal) != null) {
                            valueMap.get(nondisclosureVal).add(currReport.getQuarter().getName());
                        } else {
                            ArrayList<String> quarterNameList = new ArrayList<String>();
                            quarterNameList.add(currReport.getQuarter().getName());
                            valueMap.put(nondisclosureVal, quarterNameList);
                        }
                    }
                }
            }
            result = MultiQuarterWorksheetBuilderUtil.buildStringFromMap(valueMap);
        }
        return result;
    }

    private String generateDeveloperResolutionValue(List<QuarterlyReportDTO> quarterlyReports,
            List<PrivilegedSurveillanceDTO> privilegedSurvData) {
        String result = "";
        if (quarterlyReports.size() == 1) {
            //find the privileged surv data for the single report
            QuarterlyReportDTO report = quarterlyReports.get(0);
            for (PrivilegedSurveillanceDTO currSurv : privilegedSurvData) {
                if (currSurv.getQuarterlyReport().getId().longValue() == report.getId().longValue()
                        && currSurv.getDirectionDeveloperResolution() != null) {
                    result = currSurv.getDirectionDeveloperResolution();
                }
            }
        } else {
            //there are multiple reports... combine the values for this field in a nice way for the user
            //key is the developer resolution str and value is applicable quarter name(s) like 'Q1, Q2'
            Map<String, ArrayList<String>> valueMap = new LinkedHashMap<String, ArrayList<String>>();
            for (QuarterlyReportDTO currReport : quarterlyReports) {
                for (PrivilegedSurveillanceDTO currSurv : privilegedSurvData) {
                    if (currSurv.getQuarterlyReport().getId().longValue() == currReport.getId().longValue()
                            && currSurv.getDirectionDeveloperResolution() != null) {
                        String devResolutionVal = currSurv.getDirectionDeveloperResolution();
                        if (valueMap.get(devResolutionVal) != null) {
                            valueMap.get(devResolutionVal).add(currReport.getQuarter().getName());
                        } else {
                            ArrayList<String> quarterNameList = new ArrayList<String>();
                            quarterNameList.add(currReport.getQuarter().getName());
                            valueMap.put(devResolutionVal, quarterNameList);
                        }
                    }
                }
            }
            result = MultiQuarterWorksheetBuilderUtil.buildStringFromMap(valueMap);
        }
        return result;
    }

    private String generateCompletedCapValue(List<QuarterlyReportDTO> quarterlyReports,
            List<PrivilegedSurveillanceDTO> privilegedSurvData) {
        String result = "";
        if (quarterlyReports.size() == 1) {
            //find the privileged surv data for the single report
            QuarterlyReportDTO report = quarterlyReports.get(0);
            for (PrivilegedSurveillanceDTO currSurv : privilegedSurvData) {
                if (currSurv.getQuarterlyReport().getId().longValue() == report.getId().longValue()
                        && currSurv.getCompletedCapVerification() != null) {
                    result = currSurv.getCompletedCapVerification();
                }
            }
        } else {
            //there are multiple reports... combine the values for this field in a nice way for the user
            //key is the completed cap str and value is applicable quarter name(s) like 'Q1, Q2'
            Map<String, ArrayList<String>> valueMap = new LinkedHashMap<String, ArrayList<String>>();
            for (QuarterlyReportDTO currReport : quarterlyReports) {
                for (PrivilegedSurveillanceDTO currSurv : privilegedSurvData) {
                    if (currSurv.getQuarterlyReport().getId().longValue() == currReport.getId().longValue()
                            && currSurv.getCompletedCapVerification() != null) {
                        String completedCapVal = currSurv.getCompletedCapVerification();
                        if (valueMap.get(completedCapVal) != null) {
                            valueMap.get(completedCapVal).add(currReport.getQuarter().getName());
                        } else {
                            ArrayList<String> quarterNameList = new ArrayList<String>();
                            quarterNameList.add(currReport.getQuarter().getName());
                            valueMap.put(completedCapVal, quarterNameList);
                        }
                    }
                }
            }
            result = MultiQuarterWorksheetBuilderUtil.buildStringFromMap(valueMap);
        }
        return result;
    }

    private String determineNonconformityTypes(Surveillance surv) {
        return surv.getRequirements().stream()
                .filter(req -> req.getResult() != null  && req.getResult().getName().equalsIgnoreCase(SurveillanceResultType.NON_CONFORMITY))
                .flatMap(req -> req.getNonconformities().stream())
                .filter(survNC -> survNC.getType() != null)
                .map(nc -> nc.getType().getFormattedTitle())
                .collect(Collectors.joining(" and "));
    }

    /**
     * Figures out the certification status of the listing on the date surveillance ended.
     * @param listing
     * @param surv
     * @return
     */
    private String determineResultantCertificationStatus(CertifiedProductSearchDetails listing,
            Surveillance surv) {
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

    /**
     * Determines if a listing was in a suspended status at any point during the surveillance.
     * @param listing
     * @param surv
     * @return
     */
    private String determineSuspendedStatus(CertifiedProductSearchDetails listing,
            final Surveillance surv) {
        String result = "No";
        for (CertificationStatusEvent statusEvent : listing.getCertificationEvents()) {
            if (statusEvent.getStatus().getName().equals(CertificationStatusType.SuspendedByAcb.getName())
                    || statusEvent.getStatus().getName().equals(CertificationStatusType.SuspendedByOnc.getName())) {
                //the suspended status occurred after the surv start and either the surv hasn't
                //ended yet or the end date occurrs after the suspended status.
                LocalDate statusEventDay = DateUtil.toLocalDate(statusEvent.getEventDate().longValue());
                if (statusEventDay.compareTo(surv.getStartDay()) >= 0
                        && (surv.getEndDay() == null || surv.getEndDay().compareTo(statusEventDay) >= 0)) {
                    result = "Yes";
                }
            }
        }
        return result;
    }

    private Cell addHeadingCell(SurveillanceReportWorkbookWrapper workbook, Row row, int cellNum, String cellText) {
        Cell cell = workbook.createCell(row, cellNum, workbook.getWrappedTableHeadingStyle());
        cell.setCellValue(cellText);
        return cell;
    }

    private Cell addRichTextHeadingCell(SurveillanceReportWorkbookWrapper workbook, Row row, int cellNum,
            String cellHeading, String cellSubHeading) {
        Cell cell = workbook.createCell(row, cellNum, workbook.getWrappedTableHeadingStyle());
        RichTextString richTextTitle = new XSSFRichTextString(cellHeading + "\n" + cellSubHeading);
        richTextTitle.applyFont(0, cellHeading.length(), workbook.getBoldSmallFont());
        richTextTitle.applyFont(cellHeading.length() + 1, richTextTitle.length(), workbook.getItalicSmallFont());
        cell.setCellValue(richTextTitle);
        return cell;
    }

    private Cell addDataCell(SurveillanceReportWorkbookWrapper workbook, Row row, int cellNum, String cellText) {
        Cell cell = workbook.createCell(row, cellNum);
        cell.setCellValue(cellText);
        return cell;
    }
}
