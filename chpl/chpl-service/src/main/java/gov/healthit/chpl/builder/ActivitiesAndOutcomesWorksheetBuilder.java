package gov.healthit.chpl.builder;

import java.awt.Color;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderExtent;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.PropertyTemplate;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;
import gov.healthit.chpl.domain.SurveillanceRequirement;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.validation.surveillance.SurveillanceValidator;

public class ActivitiesAndOutcomesWorksheetBuilder extends XlsxWorksheetBuilder {
    private static final int LAST_DATA_COLUMN = 35;

    private static final int COL_CHPL_ID = 1;
    private static final int COL_SURV_ID = 2;
    //cols 3-8 get hidden
    private static final int COL_SURV_ACTIVITY_TRACKER = 3;
    private static final int COL_RELATED_COMPLAINT = 4;
    private static final int COL_Q1 = 5;
    private static final int COL_Q2 = 6;
    private static final int COL_Q3 = 7;
    private static final int COL_Q4 = 8;
    private static final int COL_CERT_EDITION = 9;
    private static final int COL_DEVELOPER_NAME = 10;
    private static final int COL_PRODUCT_NAME = 11;
    private static final int COL_PRODUCT_VERSION = 12;
    private static final int COL_K1_REVIEWED = 13;
    private static final int COL_SURV_TYPE = 14;
    private static final int COL_SURV_LOCATION_COUNT = 15;
    private static final int COL_SURV_BEGIN = 16;
    private static final int COL_SURV_END = 17;
    private static final int COL_SURV_OUTCOME = 18;
    private static final int COL_NONCONFORMITY_TYPES_RESULTANT = 19;
    private static final int COL_CERT_STATUS_RESULTANT = 20;
    private static final int COL_SUSPENDED = 21;
    private static final int COL_SURV_PROCESS_TYPE = 22;
    private static final int COL_SURV_GROUNDS = 23;
    private static final int COL_NONCONFORMITY_CAUSES = 24;
    private static final int COL_NONCONFORMITY_NATURES = 25;
    private static final int COL_SURV_STEPS = 26;
    private static final int COL_ENGAGEMENT_STEPS = 27;
    private static final int COL_ADDITIONAL_COSTS = 28;
    private static final int COL_LIMITATIONS_EVAL = 29;
    private static final int COL_NONDISCLOSURE_EVAL = 30;
    private static final int COL_DEV_RESOLUTION = 31;
    private static final int COL_COMPLETED_CAP = 32;

    private int lastDataRow;
    private SimpleDateFormat dateFormatter;
    private PropertyTemplate pt;

    public ActivitiesAndOutcomesWorksheetBuilder(final Workbook workbook) {
        super(workbook);
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
     * @param reportListingMap a mapping of quarterly reports to the listing details
     * (including surveillance that occurred during the quarter)
     * @return
     * @throws IOException
     */
    public Sheet buildWorksheet(final List<QuarterlyReportDTO> quarterlyReports,
            final List<CertifiedProductSearchDetails> relevantListings)
            throws IOException {
        XSSFDataValidationHelper dvHelper = null;

        //create sheet
        Sheet sheet = getSheet("Activities and Outcomes", new Color(141, 180, 226));
        if (sheet instanceof XSSFSheet) {
            XSSFSheet xssfSheet = (XSSFSheet) sheet;
            dvHelper = new XSSFDataValidationHelper(xssfSheet);
        }

        //set some styling that applies to the whole sheet
        sheet.setDisplayGridlines(false);
        sheet.setDisplayRowColHeadings(false);

        //all columns need a certain width to match the document format
        int sharedColWidth = getColumnWidth(11.78);
        sheet.setColumnWidth(COL_CHPL_ID, sharedColWidth);
        sheet.setColumnWidth(COL_SURV_ID,  sharedColWidth);
        sheet.setColumnWidth(COL_SURV_ACTIVITY_TRACKER, sharedColWidth);
        sheet.setColumnWidth(COL_RELATED_COMPLAINT, sharedColWidth);
        int quarterColWidth = getColumnWidth(2.22);
        sheet.setColumnWidth(COL_Q1, quarterColWidth);
        sheet.setColumnWidth(COL_Q2, quarterColWidth);
        sheet.setColumnWidth(COL_Q3, quarterColWidth);
        sheet.setColumnWidth(COL_Q4, quarterColWidth);
        sheet.setColumnWidth(COL_CERT_EDITION, sharedColWidth);
        sheet.setColumnWidth(COL_DEVELOPER_NAME, sharedColWidth);
        sheet.setColumnWidth(COL_PRODUCT_NAME, sharedColWidth);
        sheet.setColumnWidth(COL_PRODUCT_VERSION, sharedColWidth);
        sheet.setColumnWidth(COL_K1_REVIEWED, sharedColWidth);
        sheet.setColumnWidth(COL_SURV_TYPE, getColumnWidth(13.67));
        sheet.setColumnWidth(COL_SURV_LOCATION_COUNT, sharedColWidth);
        sheet.setColumnWidth(COL_SURV_BEGIN, sharedColWidth);
        sheet.setColumnWidth(COL_SURV_END, sharedColWidth);
        sheet.setColumnWidth(COL_SURV_OUTCOME, getColumnWidth(51.44));
        sheet.setColumnWidth(COL_NONCONFORMITY_TYPES_RESULTANT, getColumnWidth(27));
        sheet.setColumnWidth(COL_CERT_STATUS_RESULTANT, getColumnWidth(17.78));
        sheet.setColumnWidth(COL_SUSPENDED, getColumnWidth(17.78));
        sheet.setColumnWidth(COL_SURV_PROCESS_TYPE, getColumnWidth(30.67));
        int longTextColWidth = getColumnWidth(59.44);
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

        lastDataRow += addHeadingRow(sheet);
        lastDataRow += addTableData(sheet, quarterlyReports, relevantListings);

        //some of the columns have dropdown lists of choices for the user - set those up

        //If referenced as a list of strings, the total sum of characters of a dropdown must be less than 256
        //(meaning if you put all the choices together it has to be less than 256 characters)
        //but if you read those same strings from another set of cells using a formula, it is allowed
        //to be as long as you want. The outcome choices are the only ones that are long enough 
        //to run into this problem.
        //names for the list constraints
        Name surveillanceOutcomeNamedCell = workbook.createName();
        surveillanceOutcomeNamedCell.setNameName("SurveillanceOutcomeList");
        String reference = "Lists!$A$1:$A$8";
        surveillanceOutcomeNamedCell.setRefersToFormula(reference);

        Name processTypeNamedCell = workbook.createName();
        processTypeNamedCell.setNameName("ProcessTypeList");
        reference = "Lists!$B$1:$B$5";
        processTypeNamedCell.setRefersToFormula(reference);

        Name statusNamedCell = workbook.createName();
        statusNamedCell.setNameName("StatusList");
        reference = "Lists!$C$1:$C$4";
        statusNamedCell.setRefersToFormula(reference);

        Name booleanNamedCell = workbook.createName();
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
        for (int i = 3; i < 9; i++) {
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
        row.setHeightInPoints(6 * sheet.getDefaultRowHeightInPoints());

        addHeadingCell(row, COL_CHPL_ID, "CHPL ID");
        addHeadingCell(row, COL_SURV_ID, "Surveillance ID");
        addHeadingCell(row, COL_SURV_ACTIVITY_TRACKER, "Surveillance Activity Tracker");
        addHeadingCell(row, COL_RELATED_COMPLAINT, "Related Complaint (both if possible)");
        addHeadingCell(row, COL_Q1, "Q1");
        addHeadingCell(row, COL_Q2, "Q2");
        addHeadingCell(row, COL_Q3, "Q3");
        addHeadingCell(row, COL_Q4, "Q4");
        addHeadingCell(row, COL_CERT_EDITION, "Certification Edition");
        addHeadingCell(row, COL_DEVELOPER_NAME, "Developer Name");
        addHeadingCell(row, COL_PRODUCT_NAME, "Product Name");
        addHeadingCell(row, COL_PRODUCT_VERSION, "Product Version");
        addHeadingCell(row, COL_K1_REVIEWED, "§170.523(k)(1) Reviewed?");
        addHeadingCell(row, COL_SURV_TYPE, "Type of Surveillance");
        addHeadingCell(row, COL_SURV_LOCATION_COUNT, "Number of Locations Surveilled");
        addHeadingCell(row, COL_SURV_BEGIN, "Surveillance Began");
        addHeadingCell(row, COL_SURV_END, "Surveillance Ended");
        addHeadingCell(row, COL_SURV_OUTCOME, "Outcome of Surveillance");
        addHeadingCell(row, COL_NONCONFORMITY_TYPES_RESULTANT,
                "Non-Conformity Type(s) Resultant of Surveillance (i.e. \"170.xxx (x)(x)\")");
        addHeadingCell(row, COL_CERT_STATUS_RESULTANT, "Certification Status Resultant of Surveillance");
        addHeadingCell(row, COL_SUSPENDED, "Suspended During Surveillance?");
        addHeadingCell(row, COL_SURV_PROCESS_TYPE, "Surveillance Process Type");

        //these headings are more complicated with various fonts throughout the cell
        String cellTitle = "Grounds for Initiating Surveillance";
        String cellSubtitle = "On what grounds did the ONC-ACB initiate surveillance "
                + "(i.e., the particular facts and circumstances from which a reasonable person would "
                + "have had grounds to question the continued conformity of the Complete EHR or "
                + "Health IT Module)? For randomized surveillance, it is acceptable to state it was chosen randomly.";
        addRichTextHeadingCell(row, COL_SURV_GROUNDS, cellTitle, cellSubtitle);

        cellTitle = "Potential Causes of Non-Conformities or Suspected Non-Conformities";
        cellSubtitle = "What were the substantial factors that, in the ONC-ACB’s assessment, "
                + "caused or contributed to the suspected non-conformity or non-conformities "
                + "(e.g., implementation problem, user error, limitations on the use of capabilities "
                + "in the field, a failure to disclose known material information, etc.)?";
        addRichTextHeadingCell(row, COL_NONCONFORMITY_CAUSES, cellTitle, cellSubtitle);

        cellTitle = "Nature of Any Substantiated Non-Conformities";
        cellSubtitle = "Did ONC-ACB substantiate any non-conformities? If so, what was the nature "
                + "of the non-conformity or non-conformities that were substantiated?\n"
                + "Please include specific criteria involved.";
        addRichTextHeadingCell(row, COL_NONCONFORMITY_NATURES, cellTitle, cellSubtitle);

        cellTitle = "Steps to Surveil and Substantiate";
        cellSubtitle = "What steps did the ONC-ACB take to surveil the Complete EHR or Health "
                + "IT Module, to analyze evidence, and to substantiate the non-conformity or non-conformities?";
        addRichTextHeadingCell(row, COL_SURV_STEPS, cellTitle, cellSubtitle);

        cellTitle = "Steps to Engage and Work with Developer and End-Users";
        cellSubtitle = "What steps were taken by ONC-ACB to engage and work with the developer and "
                + "end-users to analyze and determine the causes of any suspected non-conformities and "
                + "related deficiencies?";
        addRichTextHeadingCell(row, COL_ENGAGEMENT_STEPS, cellTitle, cellSubtitle);

        cellTitle = "Additional Costs Evaluation";
        cellSubtitle = "If a suspected non-conformity resulted from additional types of costs "
                + "that a user was required to pay in order to implement or use the Complete EHR "
                + "or Health IT Module's certified capabilities, how did ONC-ACB evaluate that "
                + "suspected non-conformity?";
        addRichTextHeadingCell(row, COL_ADDITIONAL_COSTS, cellTitle, cellSubtitle);

        cellTitle = "Limitations Evaluation";
        cellSubtitle = "If a suspected non-conformity resulted from limitations that a user "
                + "encountered in the course of implementing and using the Complete EHR or "
                + "Health IT Module's certified capabilities, how did ONC-ACB evaluate that "
                + "suspected non-conformity?";
        addRichTextHeadingCell(row, COL_LIMITATIONS_EVAL, cellTitle, cellSubtitle);

        cellTitle = "Non-Disclosure Evaluation";
        cellSubtitle = "If a suspected non-conformity resulted from the non-disclosure of material "
                + "information by the developer about limitations or additional types of costs associated "
                + "with the Complete EHR or Health IT Module, how did the ONC-ACB evaluate the suspected "
                + "non-conformity?";
        addRichTextHeadingCell(row, COL_NONDISCLOSURE_EVAL, cellTitle, cellSubtitle);

        cellTitle = "Direction for Developer Resolution";
        cellSubtitle = "If a non-conformity was substantiated, what direction was given to the developer to "
                + "resolve the non-conformity?";
        addRichTextHeadingCell(row, COL_DEV_RESOLUTION, cellTitle, cellSubtitle);

        cellTitle = "Verification of Completed CAP";
        cellSubtitle = "If an approved Corrective Action Plan was received and completed, "
                + "how did ONC-ACB verify that the developer has completed all requirements "
                + "specified in the Plan?";
        addRichTextHeadingCell(row, COL_COMPLETED_CAP, cellTitle, cellSubtitle);
        return 1;
    }

    /**
     * Adds all of the surveillance data to this worksheet. 
     * Returns the number of rows added.
     * @param sheet
     * @param reportListingMap
     */
    private int addTableData(final Sheet sheet,
            final List<QuarterlyReportDTO> quarterlyReports, final List<CertifiedProductSearchDetails> listings) {
        int addedRows = 0;
        int rowNum = 2;
        //get all the surveillances relevant to the time period of the report from the listings
        List<Surveillance> relevantSuveillances = new ArrayList<Surveillance>();
        for (CertifiedProductSearchDetails listing : listings) {
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
                if (o1.getStartDate().getTime() < o2.getStartDate().getTime()) {
                    return -1;
                } else if (o1.getStartDate().getTime() == o2.getStartDate().getTime()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        for (Surveillance surv : relevantSuveillances) {
            CertifiedProductSearchDetails listing = null;
            for (CertifiedProductSearchDetails currListing : listings) {
                if (surv.getCertifiedProduct().getId().equals(currListing.getId())) {
                    listing = currListing;
                }
            }
            Row row = getRow(sheet, rowNum);
            addDataCell(row, COL_CHPL_ID, listing.getChplProductNumber());
            addDataCell(row, COL_SURV_ID, surv.getFriendlyId());
            addDataCell(row, COL_SURV_ACTIVITY_TRACKER, listing.getChplProductNumber() + surv.getFriendlyId());
            //chpl generated i think once we associate a complaint with surveillance?
            addDataCell(row, COL_RELATED_COMPLAINT, "");
            if (determineIfSurveillanceHappenedDuringQuarter("Q1", quarterlyReports, surv)) {
                addDataCell(row, COL_Q1, "X");
            }
            if (determineIfSurveillanceHappenedDuringQuarter("Q2", quarterlyReports, surv)) {
                addDataCell(row, COL_Q2, "X");
            }
            if (determineIfSurveillanceHappenedDuringQuarter("Q3", quarterlyReports, surv)) {
                addDataCell(row, COL_Q3, "X");
            }
            if (determineIfSurveillanceHappenedDuringQuarter("Q4", quarterlyReports, surv)) {
                addDataCell(row, COL_Q4, "X");
            }
            addDataCell(row, COL_CERT_EDITION, listing.getCertificationEdition().get("name").toString());
            addDataCell(row, COL_DEVELOPER_NAME, listing.getDeveloper().getName());
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

    /**
     * A surveillance is relevant if its dates occur within any of the quarterlyReports passed in.
     * @param quarterlyReports
     * @param allSurveillances
     * @return
     */
    private List<Surveillance> determineRelevantSurveillances(final List<QuarterlyReportDTO> quarterlyReports, 
            final List<Surveillance> allSurveillances) {
        List<Surveillance> relevantSurveillances = new ArrayList<Surveillance>();
        for (Surveillance currSurv : allSurveillances) {
            boolean isRelevantToAtLeastOneQuarter = false;
            for (QuarterlyReportDTO quarterlyReport : quarterlyReports) {
                if (currSurv.getStartDate().getTime() <= quarterlyReport.getEndDate().getTime()
                        && (currSurv.getEndDate() == null
                        || currSurv.getEndDate().getTime() >= quarterlyReport.getStartDate().getTime())) {
                    isRelevantToAtLeastOneQuarter = true;
                }
            }
            if (isRelevantToAtLeastOneQuarter) {
                relevantSurveillances.add(currSurv);
            }
        }
        return relevantSurveillances;
    }

    private boolean determineIfSurveillanceHappenedDuringQuarter(final String quarterName,
            final List<QuarterlyReportDTO> quarterlyReports, final Surveillance surv) {
        QuarterlyReportDTO quarterlyReport = null;
        for (QuarterlyReportDTO currReport : quarterlyReports) {
            if (currReport.getQuarter().getName().equals(quarterName)) {
                quarterlyReport = currReport;
            }
        }
        boolean result = false;
        if (quarterlyReport != null) {
            if (surv.getStartDate().getTime() <= quarterlyReport.getEndDate().getTime()
                    && surv.getEndDate() == null) {
                result = true;
            } else if (surv.getStartDate().getTime() <= quarterlyReport.getEndDate().getTime()
                    && surv.getEndDate() != null && surv.getEndDate().getTime() >= quarterlyReport.getStartDate().getTime()) {
                result = true;
            }
        }
        return result;
    }

    private String determineNonconformityTypes(final Surveillance surv) {
        Set<String> nonconformityTypes = new HashSet<String>();
        //get unique set of nonconformity types
        for (SurveillanceRequirement req : surv.getRequirements()) {
            if (req.getResult() != null &&
                    req.getResult().getName().equalsIgnoreCase(SurveillanceValidator.HAS_NON_CONFORMITY)) {
                for (SurveillanceNonconformity nc : req.getNonconformities()) {
                    if (!StringUtils.isEmpty(nc.getNonconformityType())) {
                        nonconformityTypes.add(nc.getNonconformityType());
                    }
                }
            }
        }

        //write out the nc types to a string
        StringBuffer buf = new StringBuffer();
        int i = 0;
        for (String ncType : nonconformityTypes) {
            buf.append(ncType);
            if (nonconformityTypes.size() == 2 && i == 0) {
                buf.append(" and ");
            }
            if (nonconformityTypes.size() > 2 && i < (nonconformityTypes.size()-2)) {
                buf.append(", ");
            } else if (nonconformityTypes.size() > 2 && i == (nonconformityTypes.size()-2)) {
                buf.append(", and ");
            }
            i++;
        }
        return buf.toString();
    }

    /**
     * Figures out the certification status of the listing on the date surveillance ended.
     * @param listing
     * @param surv
     * @return
     */
    private String determineResultantCertificationStatus(final CertifiedProductSearchDetails listing,
            final Surveillance surv) {
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

    /**
     * Determines if a listing was in a suspended status at any point during the surveillance.
     * @param listing
     * @param surv
     * @return
     */
    private String determineSuspendedStatus(final CertifiedProductSearchDetails listing,
            final Surveillance surv) {
        String result = "No";
        for (CertificationStatusEvent statusEvent : listing.getCertificationEvents()) {
            if (statusEvent.getStatus().getName().equals(CertificationStatusType.SuspendedByAcb.getName())
                    || statusEvent.getStatus().getName().equals(CertificationStatusType.SuspendedByOnc.getName())) {
                //the suspended status occurred after the surv start and either the surv hasn't
                //ended yet or the end date occurrs after the suspended status.
                if (statusEvent.getEventDate().longValue() >= surv.getStartDate().getTime()
                        && (surv.getEndDate() == null || surv.getEndDate().getTime() >= statusEvent.getEventDate().longValue())) {
                    result = "Yes";
                }
            }
        }
        return result;
    }

    private Cell addHeadingCell(final Row row, final int cellNum, final String cellText) {
        Cell cell = createCell(row, cellNum);
        cell.setCellStyle(wrappedTableHeadingStyle);
        cell.setCellValue(cellText);
        return cell;
    }

    private Cell addRichTextHeadingCell(final Row row, final int cellNum,
            final String cellHeading, final String cellSubHeading) {
        Cell cell = createCell(row, cellNum);
        cell.setCellStyle(wrappedTableHeadingStyle);
        RichTextString richTextTitle = new XSSFRichTextString(cellHeading + "\n" + cellSubHeading);
        richTextTitle.applyFont(0, cellHeading.length(), boldSmallFont);
        richTextTitle.applyFont(cellHeading.length()+1, richTextTitle.length(), italicSmallFont);
        cell.setCellValue(richTextTitle);
        return cell;
    }

    private Cell addDataCell(final Row row, final int cellNum, final String cellText) {
        Cell cell = createCell(row, cellNum);
        cell.setCellStyle(smallStyle);
        cell.setCellValue(cellText);
        return cell;
    }
}
