package gov.healthit.chpl.surveillance.report.builder;

import java.awt.Color;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderExtent;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.PropertyTemplate;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.complaint.ComplaintManager;
import gov.healthit.chpl.complaint.domain.Complaint;
import gov.healthit.chpl.complaint.domain.ComplaintCriterionMap;
import gov.healthit.chpl.complaint.domain.ComplaintListingMap;
import gov.healthit.chpl.complaint.domain.ComplaintType;
import gov.healthit.chpl.compliance.surveillance.SurveillanceManager;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ComplaintSurveillanceMap;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.surveillance.report.PrivilegedSurveillanceDAO;
import gov.healthit.chpl.surveillance.report.domain.PrivilegedSurveillance;
import gov.healthit.chpl.surveillance.report.domain.QuarterlyReport;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ComplaintsWorksheetBuilder {
    private static final int LAST_DATA_COLUMN = 24;

    private static final String BOOLEAN_YES = "Yes";
    private static final String BOOLEAN_NO = "No";

    private static final int COL_COMPLAINT_DATE = 1;
    private static final int COL_COMPLAINT_CLOSED_DATE = 2;
    private static final int COL_ACB_COMPLAINT_ID = 3;
    private static final int COL_ONC_COMPLAINT_ID = 4;
    private static final int COL_SUMMARY = 5;
    private static final int COL_ACTIONS_RESPONSE = 6;
    private static final int COL_COMPLAINT_TYPES = 7;
    private static final int COL_COMPLAINT_TYPE_OTHER = 8;
    private static final int COL_COMPLAINANT_TYPE = 9;
    private static final int COL_COMPLAINANT_TYPE_OTHER = 10;
    private static final int COL_CRITERIA_ID = 11;
    private static final int COL_CHPL_ID = 12;
    private static final int COL_SURV_ID = 13;
    private static final int COL_DEVELOPER = 14;
    private static final int COL_PRODUCT = 15;
    private static final int COL_VERSION = 16;
    private static final int COL_SURV_NONCONFORMITY_COUNT = 17;
    private static final int COL_SURV_NONCONFORMITY_TYPE = 18;
    private static final int COL_SURV_OUTCOME = 19;
    private static final int COL_COMPLAINANT_CONTACTED = 20;
    private static final int COL_DEVELOPER_CONTACTED = 21;
    private static final int COL_ATL_CONTACTED = 22;
    private static final int COL_FLAGGED_FOR_ONC = 23;
    private static final int COL_COMPLAINT_STATUS = 24;
    private static final int[] HIDDEN_COLS = {
            COL_DEVELOPER, COL_PRODUCT, COL_VERSION
    };

    private ComplaintManager complaintManager;
    private CertifiedProductDetailsManager cpdManager;
    private PrivilegedSurveillanceDAO privilegedSurvDao;
    private SurveillanceManager survManager;
    private int lastDataRow;
    private DateTimeFormatter dateFormatter;
    private PropertyTemplate pt;

    @Autowired
    public ComplaintsWorksheetBuilder(ComplaintManager complaintManager,
            CertifiedProductDetailsManager cpdManager, PrivilegedSurveillanceDAO privilegedSurvDao,
            SurveillanceManager survManager) {
        this.complaintManager = complaintManager;
        this.cpdManager = cpdManager;
        this.privilegedSurvDao = privilegedSurvDao;
        this.survManager = survManager;
        dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
    }

    public int getLastDataColumn() {
        return LAST_DATA_COLUMN;
    }

    public int getLastDataRow() {
        return lastDataRow <= 1 ? 2 : lastDataRow;
    }

    public Sheet buildWorksheet(SurveillanceReportWorkbookWrapper workbook, List<QuarterlyReport> quarterlyReports)
            throws IOException {
        pt = new PropertyTemplate();
        lastDataRow = 0;
        XSSFDataValidationHelper dvHelper = null;

        // create sheet
        Sheet sheet = workbook.getSheet("Complaints", new Color(141, 180, 226), getLastDataColumn());
        if (sheet instanceof XSSFSheet) {
            XSSFSheet xssfSheet = (XSSFSheet) sheet;
            dvHelper = new XSSFDataValidationHelper(xssfSheet);
        }

        // set some styling that applies to the whole sheet
        sheet.setDisplayGridlines(false);
        sheet.setDisplayRowColHeadings(false);

        // all columns need a certain width to match the document format
        int sharedColWidth = workbook.getColumnWidth(11.78);
        sheet.setColumnWidth(COL_COMPLAINT_DATE, sharedColWidth);
        sheet.setColumnWidth(COL_COMPLAINT_CLOSED_DATE, sharedColWidth);
        sheet.setColumnWidth(COL_ACB_COMPLAINT_ID, sharedColWidth);
        sheet.setColumnWidth(COL_ONC_COMPLAINT_ID, sharedColWidth);
        sheet.setColumnWidth(COL_SUMMARY, workbook.getColumnWidth(36.78));
        sheet.setColumnWidth(COL_ACTIONS_RESPONSE, workbook.getColumnWidth(78));
        sheet.setColumnWidth(COL_COMPLAINT_TYPES, workbook.getColumnWidth(22));
        sheet.setColumnWidth(COL_COMPLAINT_TYPE_OTHER, workbook.getColumnWidth(22));
        sheet.setColumnWidth(COL_COMPLAINANT_TYPE, workbook.getColumnWidth(22));
        sheet.setColumnWidth(COL_COMPLAINANT_TYPE_OTHER, workbook.getColumnWidth(22));
        sheet.setColumnWidth(COL_CRITERIA_ID, workbook.getColumnWidth(22));
        sheet.setColumnWidth(COL_CHPL_ID, sharedColWidth);
        sheet.setColumnWidth(COL_SURV_ID, sharedColWidth);
        sheet.setColumnWidth(COL_DEVELOPER, sharedColWidth);
        sheet.setColumnWidth(COL_PRODUCT, sharedColWidth);
        sheet.setColumnWidth(COL_VERSION, sharedColWidth);
        sheet.setColumnWidth(COL_SURV_NONCONFORMITY_COUNT, sharedColWidth);
        sheet.setColumnWidth(COL_SURV_NONCONFORMITY_TYPE, sharedColWidth);
        sheet.setColumnWidth(COL_SURV_OUTCOME, sharedColWidth);
        sheet.setColumnWidth(COL_COMPLAINANT_CONTACTED, sharedColWidth);
        sheet.setColumnWidth(COL_DEVELOPER_CONTACTED, sharedColWidth);
        sheet.setColumnWidth(COL_ATL_CONTACTED, sharedColWidth);
        sheet.setColumnWidth(COL_FLAGGED_FOR_ONC, sharedColWidth);
        sheet.setColumnWidth(COL_COMPLAINT_STATUS, sharedColWidth);

        lastDataRow += addHeadingRow(workbook, sheet);
        lastDataRow += addTableData(workbook, sheet, quarterlyReports);

        // some of the columns have dropdown lists of choices for the user - set those up

        // If referenced as a list of strings, the total sum of characters of a dropdown must be less than 256
        // (meaning if you put all the choices together it has to be less than 256 characters)
        // but if you read those same strings from another set of cells using a formula, it is allowed
        // to be as long as you want.
        // names for the list constraints
        Name complainantTypeNamedCell = workbook.getWorkbook().createName();
        complainantTypeNamedCell.setNameName("ComplainantTypeList");
        String reference = "Lists!$E$1:$E$" + getNumberOfComplainantTypes();
        complainantTypeNamedCell.setRefersToFormula(reference);

        Name complaintStatusTypeNamedCell = workbook.getWorkbook().createName();
        complaintStatusTypeNamedCell.setNameName("ComplaintStatusTypeList");
        reference = "Lists!$F$1:$F$" + Complaint.NUMBER_OF_STATES;
        complaintStatusTypeNamedCell.setRefersToFormula(reference);

        Name booleanNamedCell = workbook.getWorkbook().createName();
        booleanNamedCell.setNameName("ComplaintSheetBooleanList");
        reference = "Lists!$D$1:$D$2";
        booleanNamedCell.setRefersToFormula(reference);

        // complainant type is a dropdown list of choices
        CellRangeAddressList addressList = new CellRangeAddressList(2, getLastDataRow(),
                COL_COMPLAINANT_TYPE, COL_COMPLAINANT_TYPE);
        XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper
                .createFormulaListConstraint("ComplainantTypeList");
        XSSFDataValidation validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(false);
        sheet.addValidationData(validation);

        // complainant contacted? is a dropdown list of choices
        addressList = new CellRangeAddressList(2, getLastDataRow(), COL_COMPLAINANT_CONTACTED, COL_COMPLAINANT_CONTACTED);
        dvConstraint = (XSSFDataValidationConstraint) dvHelper.createFormulaListConstraint("ComplaintSheetBooleanList");
        validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(false);
        sheet.addValidationData(validation);

        // developer contacted? is a dropdown list of choices
        addressList = new CellRangeAddressList(2, getLastDataRow(), COL_DEVELOPER_CONTACTED, COL_DEVELOPER_CONTACTED);
        dvConstraint = (XSSFDataValidationConstraint) dvHelper.createFormulaListConstraint("ComplaintSheetBooleanList");
        validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(false);
        sheet.addValidationData(validation);

        // atl contacted? is a dropdown list of choices
        addressList = new CellRangeAddressList(2, getLastDataRow(), COL_ATL_CONTACTED, COL_ATL_CONTACTED);
        dvConstraint = (XSSFDataValidationConstraint) dvHelper.createFormulaListConstraint("ComplaintSheetBooleanList");
        validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(false);
        sheet.addValidationData(validation);

        // complaint status is a dropdown list of choices
        addressList = new CellRangeAddressList(2, getLastDataRow(), COL_COMPLAINT_STATUS, COL_COMPLAINT_STATUS);
        dvConstraint = (XSSFDataValidationConstraint) dvHelper.createFormulaListConstraint("ComplaintStatusTypeList");
        validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(false);
        sheet.addValidationData(validation);

        // hide some rows the ACBs are not expected to fill out
        for (int i = 0; i < HIDDEN_COLS.length; i++) {
            sheet.setColumnHidden(HIDDEN_COLS[i], true);
        }

        // apply the borders after the sheet has been created
        pt.drawBorders(new CellRangeAddress(1, getLastDataRow(), 1, LAST_DATA_COLUMN - 1),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        pt.applyBorders(sheet);
        return sheet;
    }

    private int addHeadingRow(SurveillanceReportWorkbookWrapper workbook, Sheet sheet) {
        Row row = workbook.getRow(sheet, 1);
        // row can have 6 lines of text
        row.setHeightInPoints(3 * sheet.getDefaultRowHeightInPoints());

        addHeadingCell(workbook, row, COL_COMPLAINT_DATE, "Date Complaint Received");
        addHeadingCell(workbook, row, COL_COMPLAINT_CLOSED_DATE, "Date Complaint Closed");
        addHeadingCell(workbook, row, COL_ACB_COMPLAINT_ID, "ONC-ACB Complaint ID");
        addHeadingCell(workbook, row, COL_ONC_COMPLAINT_ID, "ONC Complaint ID (if applicable)");
        addHeadingCell(workbook, row, COL_SUMMARY, "Complaint Summary");
        addHeadingCell(workbook, row, COL_ACTIONS_RESPONSE, "Actions/Response");
        addHeadingCell(workbook, row, COL_COMPLAINT_TYPES, "Complaint Type(s)");
        addHeadingCell(workbook, row, COL_COMPLAINT_TYPE_OTHER, "Complaint Type(s) - Other");
        addHeadingCell(workbook, row, COL_COMPLAINANT_TYPE, "Type of Complainant");
        addHeadingCell(workbook, row, COL_COMPLAINANT_TYPE_OTHER, "Type of Complainant - Other");
        addHeadingCell(workbook, row, COL_CRITERIA_ID, "Associated Criteria");
        addHeadingCell(workbook, row, COL_CHPL_ID, "Associated Certified Products");
        addHeadingCell(workbook, row, COL_SURV_ID, "Associated Surveillance Activities");
        addHeadingCell(workbook, row, COL_DEVELOPER, "Developer");
        addHeadingCell(workbook, row, COL_PRODUCT, "Product");
        addHeadingCell(workbook, row, COL_VERSION, "Version");
        addHeadingCell(workbook, row, COL_SURV_NONCONFORMITY_COUNT, "Count of Non-Conformities");
        addHeadingCell(workbook, row, COL_SURV_NONCONFORMITY_TYPE, "Non-Conformity Type");
        addHeadingCell(workbook, row, COL_SURV_OUTCOME, "Outcome of Surveillance");
        addHeadingCell(workbook, row, COL_COMPLAINANT_CONTACTED, "Complainant Contacted");
        addHeadingCell(workbook, row, COL_DEVELOPER_CONTACTED, "Developer Contacted");
        addHeadingCell(workbook, row, COL_ATL_CONTACTED, "ONC-ATL Contacted");
        addHeadingCell(workbook, row, COL_FLAGGED_FOR_ONC, "Informed ONC per §170.523(s)");
        addHeadingCell(workbook, row, COL_COMPLAINT_STATUS, "Complaint Status");
        return 1;
    }

    private int addTableData(SurveillanceReportWorkbookWrapper workbook, Sheet sheet, List<QuarterlyReport> quarterlyReports) {
        int addedRows = 0;
        int rowNum = 2;

        List<Complaint> uniqueComplaints = new ArrayList<Complaint>();
        // get the complaints for each quarterly report
        // a complaint could be relevant to multiple quarterly reports so filter out duplicates
        for (QuarterlyReport report : quarterlyReports) {
            List<Complaint> complaintsRelevantToReport = complaintManager.getAllComplaintsBetweenDates(
                    report.getAcb(), report.getStartDay(), report.getEndDay());
            for (Complaint relevantComplaint : complaintsRelevantToReport) {
                if (!uniqueComplaints.contains(relevantComplaint)) {
                    uniqueComplaints.add(relevantComplaint);
                }
            }
        }
        // sort the complaints with oldest received date first
        uniqueComplaints.sort(new Comparator<Complaint>() {
            @Override
            public int compare(final Complaint o1, final Complaint o2) {
                return o1.getReceivedDate().compareTo(o2.getReceivedDate());
            }
        });

        for (Complaint complaint : uniqueComplaints) {
            boolean isFirstRowForComplaint = true;
            Row row = workbook.getRow(sheet, rowNum++);
            addComplaintData(workbook, row, complaint);
            pt.drawBorders(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, LAST_DATA_COLUMN - 1),
                    BorderStyle.HAIR, BorderExtent.HORIZONTAL);
            addedRows++;

            // sort the criteria by criteria number so all data is in a predictable order
            List<CertificationCriterion> orderedCriterion = new ArrayList<CertificationCriterion>();
            for (ComplaintCriterionMap criteriaMap : complaint.getCriteria()) {
                orderedCriterion.add(criteriaMap.getCertificationCriterion());
            }
            orderedCriterion.sort(new Comparator<CertificationCriterion>() {
                @Override
                public int compare(final CertificationCriterion o1, final CertificationCriterion o2) {
                    return o1.getNumber().compareTo(o2.getNumber());
                }
            });

            Map<Long, CertifiedProductSearchDetails> listingDetailsCache = new LinkedHashMap<Long, CertifiedProductSearchDetails>();
            List<CertifiedProductSearchDetails> orderedListings = new ArrayList<CertifiedProductSearchDetails>();
            for (ComplaintListingMap listingMap : complaint.getListings()) {
                try {
                    CertifiedProductSearchDetails cpd = cpdManager.getCertifiedProductDetailsBasicByChplProductNumber(listingMap.getChplProductNumber());
                    listingDetailsCache.put(cpd.getId(), cpd);
                    orderedListings.add(cpd);
                } catch (EntityRetrievalException ex) {
                    LOGGER.error("Could not find basic details for listing " + listingMap.getChplProductNumber(), ex);
                }
            }
            // sort the listings by chpl number so all data is in a consistent order
            orderedListings.sort(new Comparator<CertifiedProductSearchDetails>() {
                @Override
                public int compare(final CertifiedProductSearchDetails o1, final CertifiedProductSearchDetails o2) {
                    return o1.getChplProductNumber().compareTo(o2.getChplProductNumber());
                }
            });
            // sort the surveillances by chpl number + friendly surveillance id to keep data in consistent order
            List<Surveillance> orderedSurveillances = new ArrayList<Surveillance>();
            for (ComplaintSurveillanceMap survMap : complaint.getSurveillances()) {
                Surveillance surv = null;
                try {
                    surv = survManager.getById(survMap.getSurveillanceId());
                } catch (Exception ex) {
                    LOGGER.error("Could not get surveillance by ID " + survMap.getSurveillanceId());
                }
                if (surv != null) {
                    orderedSurveillances.add(surv);
                }
            }
            orderedSurveillances.sort(new Comparator<Surveillance>() {
                @Override
                public int compare(final Surveillance o1, final Surveillance o2) {
                    String o1CompareStr = o1.getCertifiedProduct().getChplProductNumber() + o1.getFriendlyId();
                    String o2CompareStr = o2.getCertifiedProduct().getChplProductNumber() + o2.getFriendlyId();
                    return o1CompareStr.compareTo(o2CompareStr);
                }
            });

            // we need the dev, product, and version for each listing associated with surveillance
            // but in case listings are duplicated get the details only once
            for (Surveillance surv : orderedSurveillances) {
                if (listingDetailsCache.get(surv.getCertifiedProduct().getId()) == null) {
                    try {
                        listingDetailsCache.put(surv.getCertifiedProduct().getId(),
                                cpdManager.getCertifiedProductDetailsBasic(surv.getCertifiedProduct().getId()));
                    } catch (EntityRetrievalException ex) {
                        LOGGER.error("Could not find basic details for listing " + surv.getCertifiedProduct().getId(), ex);
                    }
                }
            }

            // A complaint can be associated with nothing at all, with a listing,
            // with a surveillance (and implicitly the listing associated with that surveillance),
            // or with a criteria.
            // The first complaint row in this table should have all the complaint data
            // and following rows should only have the additional listing, surveillance,
            // or criteria that the complaint is associated with.
            for (CertificationCriterion criterion : orderedCriterion) {
                if (!isFirstRowForComplaint) {
                    row = workbook.getRow(sheet, rowNum++);
                    addedRows++;
                }
                addDataCell(workbook, row, COL_CRITERIA_ID, CertificationCriterionService.formatCriteriaNumber(criterion));
                // nothing to show in the rest of the cells since they are all listing/surv specific
                addDataCell(workbook, row, COL_CHPL_ID, "");
                addDataCell(workbook, row, COL_SURV_ID, "");
                addDataCell(workbook, row, COL_DEVELOPER, "");
                addDataCell(workbook, row, COL_PRODUCT, "");
                addDataCell(workbook, row, COL_VERSION, "");
                addDataCell(workbook, row, COL_SURV_NONCONFORMITY_COUNT, "");
                addDataCell(workbook, row, COL_SURV_NONCONFORMITY_TYPE, "");
                addDataCell(workbook, row, COL_SURV_OUTCOME, "");
                pt.drawBorders(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, LAST_DATA_COLUMN - 1),
                        BorderStyle.HAIR, BorderExtent.HORIZONTAL);
                isFirstRowForComplaint = false;
            }

            for (CertifiedProductSearchDetails listing : orderedListings) {
                if (!isFirstRowForComplaint) {
                    row = workbook.getRow(sheet, rowNum++);
                    addedRows++;
                }
                addDataCell(workbook, row, COL_CRITERIA_ID, "");
                addDataCell(workbook, row, COL_CHPL_ID, listing.getChplProductNumber());
                // nothing in surveillance because this complaint is only
                // associated at the listing level
                addDataCell(workbook, row, COL_SURV_ID, "");
                addDataCell(workbook, row, COL_DEVELOPER, listing.getDeveloper().getName());
                addDataCell(workbook, row, COL_PRODUCT, listing.getProduct().getName());
                addDataCell(workbook, row, COL_VERSION, listing.getVersion().getVersion());
                // nothing in surveillance outcome because this complaint is only
                // associated at the listing level
                addDataCell(workbook, row, COL_SURV_NONCONFORMITY_COUNT, "");
                addDataCell(workbook, row, COL_SURV_NONCONFORMITY_TYPE, "");
                addDataCell(workbook, row, COL_SURV_OUTCOME, "");
                pt.drawBorders(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, LAST_DATA_COLUMN - 1),
                        BorderStyle.HAIR, BorderExtent.HORIZONTAL);
                isFirstRowForComplaint = false;
            }

            for (Surveillance surv : orderedSurveillances) {
                if (!isFirstRowForComplaint) {
                    row = workbook.getRow(sheet, rowNum++);
                    addedRows++;
                }
                addDataCell(workbook, row, COL_CRITERIA_ID, "");
                addDataCell(workbook, row, COL_CHPL_ID, surv.getCertifiedProduct().getChplProductNumber());
                addDataCell(workbook, row, COL_SURV_ID, surv.getFriendlyId());
                // if we have the listing details print them out, otherwise print an error
                CertifiedProductSearchDetails cpd = listingDetailsCache.get(surv.getCertifiedProduct().getId());
                if (cpd != null) {
                    addDataCell(workbook, row, COL_DEVELOPER, cpd.getDeveloper().getName());
                    addDataCell(workbook, row, COL_PRODUCT, cpd.getProduct().getName());
                    addDataCell(workbook, row, COL_VERSION, cpd.getVersion().getVersion());
                } else {
                    addDataCell(workbook, row, COL_DEVELOPER, "?");
                    addDataCell(workbook, row, COL_PRODUCT, "?");
                    addDataCell(workbook, row, COL_VERSION, "?");
                }

                addDataCell(workbook, row, COL_SURV_NONCONFORMITY_COUNT, getNonconformityCount(surv) + "");
                addDataCell(workbook, row, COL_SURV_NONCONFORMITY_TYPE, getNonconformityTypes(surv));
                addDataCell(workbook, row, COL_SURV_OUTCOME, getSurveillanceOutcome(quarterlyReports, surv.getId()));
                pt.drawBorders(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, LAST_DATA_COLUMN - 1),
                        BorderStyle.HAIR, BorderExtent.HORIZONTAL);
                isFirstRowForComplaint = false;
            }
        }
        return addedRows;
    }

    private long getNonconformityCount(Surveillance surv) {
        if (surv != null && !CollectionUtils.isEmpty(surv.getRequirements())) {
            return surv.getRequirements().stream()
                .flatMap(req -> req.getNonconformities().stream())
                .count();

        }
        return 0;
    }

    private String getNonconformityTypes(Surveillance surv) {
        StringBuffer result = new StringBuffer();
        if (surv != null && !CollectionUtils.isEmpty(surv.getRequirements())) {
            final String survFriendlyId = surv.getFriendlyId();
            surv.getRequirements().stream()
                .flatMap(req -> req.getNonconformities().stream())
                .forEach(nc -> {
                    if (StringUtils.isEmpty(result)) {
                        result.append(survFriendlyId + ":" + nc.getType().getFormattedTitleForReport());
                    } else {
                        result.append("; " + survFriendlyId + ":" + nc.getType().getFormattedTitleForReport());
                    }
                });

        }
        return result.toString();
    }

    private String getSurveillanceOutcome(List<QuarterlyReport> reports, Long survId) {
        List<Long> reportIds = new ArrayList<Long>();
        for (QuarterlyReport report : reports) {
            reportIds.add(report.getId());
        }
        String result = "";
        List<PrivilegedSurveillance> privSurvs = privilegedSurvDao.getByReportsAndSurveillance(reportIds, survId);
        if (reportIds.size() == 1 && privSurvs.size() > 0) {
            PrivilegedSurveillance privSurv = privSurvs.get(0);
            result = (privSurv.getSurveillanceOutcome() != null
                    ? privSurv.getSurveillanceOutcome().getName()
                    : "");
        } else if (privSurvs.size() > 0) {
            Map<String, ArrayList<String>> outcomeToQuarterMap = new LinkedHashMap<String, ArrayList<String>>();
            for (PrivilegedSurveillance privSurv : privSurvs) {
                String outcomeStr = (privSurv.getSurveillanceOutcome() != null
                        ? privSurv.getSurveillanceOutcome().getName()
                        : "");
                if (outcomeToQuarterMap.get(outcomeStr) != null) {
                    outcomeToQuarterMap.get(outcomeStr).add(privSurv.getQuarterlyReport().getQuarter());
                } else {
                    ArrayList<String> quarterNames = new ArrayList<String>();
                    quarterNames.add(privSurv.getQuarterlyReport().getQuarter());
                    outcomeToQuarterMap.put(outcomeStr, quarterNames);
                }
            }
            result = MultiQuarterWorksheetBuilderUtil.buildStringFromMap(outcomeToQuarterMap);
        }
        return result;
    }

    private void addComplaintData(SurveillanceReportWorkbookWrapper workbook, Row row, Complaint complaint) {
        addDataCell(workbook, row, COL_COMPLAINT_DATE, dateFormatter.format(complaint.getReceivedDate()));
        addDataCell(workbook, row, COL_COMPLAINT_CLOSED_DATE, complaint.getClosedDate() != null ? dateFormatter.format(complaint.getClosedDate()) : "");
        addDataCell(workbook, row, COL_ACB_COMPLAINT_ID, complaint.getAcbComplaintId());
        addDataCell(workbook, row, COL_ONC_COMPLAINT_ID, complaint.getOncComplaintId());
        addDataCell(workbook, row, COL_SUMMARY, complaint.getSummary());
        addDataCell(workbook, row, COL_ACTIONS_RESPONSE, complaint.getActions());
        addDataCell(workbook, row, COL_COMPLAINT_TYPES, formatComplaintTypes(complaint.getComplaintTypes()));
        addDataCell(workbook, row, COL_COMPLAINT_TYPE_OTHER, complaint.getComplaintTypesOther());
        addDataCell(workbook, row, COL_COMPLAINANT_TYPE, complaint.getComplainantType() != null ? complaint.getComplainantType().getName() : "");
        addDataCell(workbook, row, COL_COMPLAINANT_TYPE_OTHER, complaint.getComplainantTypeOther());
        addDataCell(workbook, row, COL_COMPLAINANT_CONTACTED, complaint.isComplainantContacted() ? BOOLEAN_YES : BOOLEAN_NO);
        addDataCell(workbook, row, COL_DEVELOPER_CONTACTED, complaint.isDeveloperContacted() ? BOOLEAN_YES : BOOLEAN_NO);
        addDataCell(workbook, row, COL_ATL_CONTACTED, complaint.isOncAtlContacted() ? BOOLEAN_YES : BOOLEAN_NO);
        addDataCell(workbook, row, COL_FLAGGED_FOR_ONC, complaint.isFlagForOncReview() ? BOOLEAN_YES : BOOLEAN_NO);
        addDataCell(workbook, row, COL_COMPLAINT_STATUS, complaint.getClosedDate() == null ? Complaint.COMPLAINT_OPEN : Complaint.COMPLAINT_CLOSED);
    }

    private String formatComplaintTypes(Set<ComplaintType> complaintTypes) {
        String result = "";
        if (!CollectionUtils.isEmpty(complaintTypes)) {
            result = complaintTypes.stream()
                    .map(ct -> ct.getName())
                    .collect(Collectors.joining(", "));
        }
        return result;
    }

    private Cell addHeadingCell(SurveillanceReportWorkbookWrapper workbook, Row row, int cellNum, String cellText) {
        Cell cell = workbook.createCell(row, cellNum, workbook.getWrappedTableHeadingStyle());
        cell.setCellValue(cellText);
        return cell;
    }

    private Cell addDataCell(SurveillanceReportWorkbookWrapper workbook, Row row, int cellNum, String cellText) {
        Cell cell = workbook.createCell(row, cellNum);
        cell.setCellValue(cellText);
        return cell;
    }

    private int getNumberOfComplainantTypes() {
        return complaintManager.getComplainantTypes().size();
    }
}
