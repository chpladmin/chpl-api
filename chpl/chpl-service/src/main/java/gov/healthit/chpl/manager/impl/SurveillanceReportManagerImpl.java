package gov.healthit.chpl.manager.impl;

import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderExtent;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellRange;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeUtil;
import org.apache.poi.ss.util.PropertyTemplate;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.IndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.surveillance.report.AnnualReportDAO;
import gov.healthit.chpl.dao.surveillance.report.QuarterDAO;
import gov.healthit.chpl.dao.surveillance.report.QuarterlyReportDAO;
import gov.healthit.chpl.dto.surveillance.report.AnnualReportDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.manager.SurveillanceReportManager;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Service
public class SurveillanceReportManagerImpl extends SecuredManager implements SurveillanceReportManager {
    private QuarterlyReportDAO quarterlyDao;
    private AnnualReportDAO annualDao;
    private QuarterDAO quarterDao;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public SurveillanceReportManagerImpl(final QuarterlyReportDAO quarterlyDao,
            final AnnualReportDAO annualDao, final QuarterDAO quarterDao,
            final ErrorMessageUtil msgUtil) {
        this.quarterlyDao = quarterlyDao;
        this.annualDao = annualDao;
        this.quarterDao = quarterDao;
        this.msgUtil = msgUtil;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).CREATE_QUARTERLY, #toCreate)")
    public QuarterlyReportDTO createQuarterlyReport(final QuarterlyReportDTO toCreate)
    throws EntityCreationException, InvalidArgumentsException {
        //Quarterly report has to be associated with a year (annual report)
        //and a quarter (Q1, Q2, etc).
        //Make sure those pieces of data exist.

        AnnualReportDTO associatedAnnualReport = toCreate.getAnnualReport();
        if (associatedAnnualReport == null || associatedAnnualReport.getYear() == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("report.quarterlySurveillance.missingYear"));
        } else if (associatedAnnualReport.getAcb() == null || associatedAnnualReport.getAcb().getId() == null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("report.quarterlySurveillance.missingAcb"));
        } else if (associatedAnnualReport.getId() == null) {
            AnnualReportDTO existingAnnualReport =
                    annualDao.getByAcbAndYear(associatedAnnualReport.getAcb().getId(), associatedAnnualReport.getYear());
            //if there's no report yet for this year and acb, create one
            if (existingAnnualReport == null) {
                AnnualReportDTO createdAnnualReport = annualDao.create(associatedAnnualReport);
                toCreate.setAnnualReport(createdAnnualReport);
            } else {
                toCreate.setAnnualReport(existingAnnualReport);
            }
        }

        if (toCreate.getQuarter() == null
                || (toCreate.getQuarter().getId() == null && StringUtils.isEmpty(toCreate.getQuarter().getName()))) {
            throw new InvalidArgumentsException("report.quarterlySurveillance.missingQuarter");
        } else if (toCreate.getQuarter().getId() == null && toCreate.getQuarter().getName() != null) {
            QuarterDTO quarter = quarterDao.getByName(toCreate.getQuarter().getName());
            if (quarter == null) {
                throw new InvalidArgumentsException(
                        msgUtil.getMessage("report.quarterlySurveillance.badQuarter", toCreate.getQuarter().getName()));
            }
            toCreate.setQuarter(quarter);
        }

        //make sure there's not already a quarterly report for this acb and year and quarter
        QuarterlyReportDTO existingQuarterlyReport =
                quarterlyDao.getByQuarterAndAcbAndYear(toCreate.getQuarter().getId(),
                        toCreate.getAnnualReport().getAcb().getId(),
                        toCreate.getAnnualReport().getYear());
        if (existingQuarterlyReport != null) {
            throw new EntityCreationException("report.quarterlySurveillance.exists");
        }

        QuarterlyReportDTO created = quarterlyDao.create(toCreate);
        return created;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).UPDATE_QUARTERLY, #toUpdate)")
    public QuarterlyReportDTO updateQuarterlyReport(final QuarterlyReportDTO toUpdate)
    throws EntityRetrievalException {
        QuarterlyReportDTO updated = quarterlyDao.update(toUpdate);
        return updated;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).DELETE_QUARTERLY, #id)")
    public void deleteQuarterlyReport(final Long id) throws EntityRetrievalException {
        quarterlyDao.delete(id);
    }

    /**
     * Returns all the quarterly reports the current user has access to.
     */
    @Override
    @Transactional
    @PostFilter("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY, filterObject)")
    public List<QuarterlyReportDTO> getQuarterlyReports() {
        return quarterlyDao.getAll();
    }

    /**
     * Gets the quarterly report by ID if the user has access.
     */
    @Override
    @Transactional
    @PostAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE_REPORT, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceReportDomainPermissions).GET_QUARTERLY,"
            + "returnObject)")
    public QuarterlyReportDTO getQuarterlyReport(final Long id) throws EntityRetrievalException {
        return quarterlyDao.getById(id);
    }

    public Workbook exportQuarterlyReport(final Long id) throws EntityRetrievalException,
        IOException {
        QuarterlyReportDTO report = getQuarterlyReport(id);
        Workbook workbook = XSSFWorkbookFactory.create(true);
        createReportInformationWorksheet(workbook, report);
        createActivitiesAndOutcomesWorksheet(workbook);
        createComplaintsWorksheet(workbook);
        createSurveillanceSummaryWorksheet(workbook);
        createSurveillanceExperienceWorksheet(workbook);
        return workbook;
    }

    private void createReportInformationWorksheet(final Workbook workbook,
            final QuarterlyReportDTO report) {
        CellStyle boldStyle = workbook.createCellStyle();
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        boldFont.setFontHeightInPoints((short)12);
        boldStyle.setFont(boldFont);

        CellStyle smallStyle = workbook.createCellStyle();
        Font smallFont = workbook.createFont();
        smallFont.setFontHeightInPoints((short)10);
        smallStyle.setFont(smallFont);

        CellStyle italicSmallStyle = workbook.createCellStyle();
        Font italicSmallFont = workbook.createFont();
        italicSmallFont.setItalic(true);
        italicSmallFont.setFontHeightInPoints((short)10);
        italicSmallStyle.setFont(italicSmallFont);

        CellStyle boldSmallStyle = workbook.createCellStyle();
        Font boldSmallFont = workbook.createFont();
        boldSmallFont.setItalic(true);
        boldSmallFont.setFontHeightInPoints((short)10);
        boldSmallStyle.setFont(boldSmallFont);

        CellStyle boldItalicSmallStyle = workbook.createCellStyle();
        Font boldItalicSmallFont = workbook.createFont();
        boldItalicSmallFont.setBold(true);
        boldItalicSmallFont.setItalic(true);
        boldItalicSmallFont.setFontHeightInPoints((short)10);
        boldItalicSmallStyle.setFont(boldItalicSmallFont);

        PropertyTemplate pt = new PropertyTemplate();
        pt.drawBorders(new CellRangeAddress(8, 8, 1, 1),
                BorderStyle.MEDIUM, BorderExtent.ALL);

        Sheet sheet = workbook.createSheet("Report Information");
        if (sheet instanceof XSSFSheet) {
            XSSFSheet xssfSheet = (XSSFSheet) sheet;
            XSSFColor xssfColor = new XSSFColor(
                    new java.awt.Color(141, 180, 226), new DefaultIndexedColorMap());
            xssfSheet.setTabColor(xssfColor);
        }
        sheet.setDisplayGridlines(false);
        Row row = sheet.createRow(1);
        Cell cell = row.createCell(1);
        cell.setCellStyle(boldStyle);
        cell.setCellValue("ONC-Authorized Certification Body (ONC-ACB) 2019 Report Template for Surveillance Results");
        row = sheet.createRow(2);
        cell = row.createCell(1);
        cell.setCellStyle(boldItalicSmallStyle);
        cell.setCellValue("Template Version: SR19-1.0");
        row = sheet.createRow(4);
        cell = row.createCell(1);
        cell.setCellStyle(italicSmallStyle);
        cell.setCellValue("Instructions");
        row = sheet.createRow(5);
        cell = row.createCell(1);
        cell.setCellStyle(smallStyle);
        cell.setCellValue("This workbook provides a template for preparing your organization's quarterly and annual Surveillance Reports. It is provided for the convenience of ONC-ACBs and is designed to be used alongside Program Policy Resources #18-01, #18-02, and #18-03 (October 5, 2018), and each ONC-ACB's surveillance plan."
                + "\n\n"
                + "Please fill out the boxes below each question and data requested in the included worksheets.");
        row = sheet.createRow(7);
        cell = row.createCell(0);
        cell.setCellStyle(boldSmallStyle);
        cell.setCellValue("I.");
        cell = row.createCell(1);
        cell.setCellStyle(boldSmallStyle);
        cell.setCellValue("Reporting ONC-ACB");
        row = sheet.createRow(8);
        cell = row.createCell(1);
        cell.setCellValue("This report is submitted by the below named ONC-ACB in accordance with 45 CFR § 170.523(i)(2) and 45 CFR § 170.556(e).");
        row = sheet.createRow(9);
        cell = row.createCell(1);
        cell.setCellValue(report.getAnnualReport().getAcb().getName());

        pt.applyBorders(sheet);
    }

    private void createActivitiesAndOutcomesWorksheet(final Workbook workbook) {
        workbook.createSheet("Activities and Outcomes");
    }

    private void createComplaintsWorksheet(final Workbook workbook) {
        workbook.createSheet("Complaints");
    }

    private void createSurveillanceSummaryWorksheet(final Workbook workbook) {
        workbook.createSheet("Surveillance Summary");
    }

    private void createSurveillanceExperienceWorksheet(final Workbook workbook) {
        workbook.createSheet("Surveillance Experience");
    }
}
