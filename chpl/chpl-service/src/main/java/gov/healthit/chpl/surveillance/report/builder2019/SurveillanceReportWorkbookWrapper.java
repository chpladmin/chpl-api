package gov.healthit.chpl.surveillance.report.builder2019;

import java.awt.Color;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.text.AttributedString;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;
import org.springframework.util.StringUtils;

public class SurveillanceReportWorkbookWrapper {
    public static final int DEFAULT_MAX_COLUMN = 16384;
    private static final int WORKSHEET_FONT_POINTS  = 10;
    private static final int WORKSHEET_LARGE_FONT_POINTS = 12;

    private Workbook workbook;
    private Font boldFont, smallFont, boldSmallFont, italicSmallFont, boldItalicSmallFont,
    italicUnderlinedSmallFont;
    private CellStyle boldStyle, smallStyle, italicSmallStyle, boldItalicSmallStyle,
    italicUnderlinedSmallStyle, topAlignedWrappedStyle, sectionNumberingStyle, sectionHeadingStyle,
    rightAlignedTableHeadingStyle, leftAlignedTableHeadingStyle, wrappedTableHeadingStyle, tableSubheadingStyle;

    public SurveillanceReportWorkbookWrapper() throws IOException {
        this.workbook = XSSFWorkbookFactory.create(true);
        initializeFonts();
        initializeStyles();
    }

    public Sheet getSheet(String sheetName, int lastDataColumn) {
        return getSheet(sheetName, null, lastDataColumn);
    }

    public Sheet getSheet(String sheetName, Color tabColor, int lastDataColumn) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(sheetName);
            if (sheet instanceof XSSFSheet) {
                XSSFSheet xssfSheet = (XSSFSheet) sheet;
                if (tabColor != null) {
                    DefaultIndexedColorMap colorMap = new DefaultIndexedColorMap();
                    XSSFColor xssfTabColor = new XSSFColor(tabColor, colorMap);
                    xssfSheet.setTabColor(xssfTabColor);
                }

                //hide all the columns after the data
                if (lastDataColumn > 0) {
                    CTCol col = xssfSheet.getCTWorksheet().getColsArray(0).addNewCol();
                    col.setMin(lastDataColumn);
                    col.setMax(DEFAULT_MAX_COLUMN); // the last column (1-indexed)
                    col.setHidden(true);
                }
            }
        }
        return sheet;
    }

    /**
     * If you right click on a column in Excel, it gives you some number for the column width.
     * That is in character units (i.e. if the width is 8.43 then 8.43 characters fit in the cell).
     * The POI library uses different units (1/256 of a character width). This function converts the excel
     * width units into the POI width units. It's pretty close but not 100%.
     */
    public int getColumnWidth(double excelWidth) {
        return (int)(excelWidth * 256);
    }

    /**
     * Given a string and the width of the column figure out how many lines the text
     * will take up using this workbooks default font.
     */
    public int calculateLineCount(String text, Sheet sheet, int firstColIndex, int lastColIndex) {
        return calculateLineCount(text, smallFont, sheet, firstColIndex, lastColIndex);
    }

    /**
     * Given a string and the width of column (in... units?? pixels?) figure out
     * how many lines the string of text it will take up using the given font if the text wraps.
     */
    public int calculateLineCount(String text, Font font, Sheet sheet, int firstColIndex, int lastColIndex) {
        int totalLineCount = 0;
        //count newline characters that are present in the text first
        int newlineCharCount = StringUtils.countOccurrencesOf(text, "\n");
        //do we need to account for other newlines? (specifically thinking of \r\n)
        //not sure because i think all our users would be putting in data from windows
        //which only has \n but this code will run on linux so... ??

        if (newlineCharCount == 0) {
            totalLineCount = calculateLineCountWithoutNewlines(text, font, sheet, firstColIndex, lastColIndex);
        } else {
            //find each section of this text between newlines; check if that section
            //wraps over multiple lines and add to the count
            for (int i = 0; i < text.length(); i++) {
                int indexOfNextNewline = text.indexOf("\n", i);
                if (indexOfNextNewline == i) {
                    //a newline with no other text characters
                    totalLineCount++;
                } else if (indexOfNextNewline > i) {
                    //a paragraph inbetween newlnes
                    String sectionText = text.substring(i, indexOfNextNewline);
                    int sectionLineCount = calculateLineCountWithoutNewlines(sectionText, font, sheet, firstColIndex, lastColIndex);
                    totalLineCount += sectionLineCount;
                    i = indexOfNextNewline;
                } else if (indexOfNextNewline == -1 && i < text.length()) {
                    //last section, no newlines after it
                    String sectionText = text.substring(i);
                    int sectionLineCount = calculateLineCountWithoutNewlines(sectionText, font, sheet, firstColIndex, lastColIndex);
                    totalLineCount += sectionLineCount;
                    i = text.length();
                }
            }
        }

        return totalLineCount;
    }

    /**
     * Calculate the amount of lines the given text will take up
     * given the column width available and the fact that the supplied
     * text does not have any explicit newlines in it.
     * Using the "smallFont" as our default since most user-entered text cells
     * are styled with that one. It sometimes returns one line too many and I'm not sure why.
     */
    public int calculateLineCountWithoutNewlines(String textWithoutNewlines, Font font,
            Sheet sheet, int firstColIndex, int lastColIndex) {
        int lineCount = 0;

        if (StringUtils.isEmpty(textWithoutNewlines)) {
            return lineCount;
        }

        //calculate the total column width available for the text
        int totalColWidthInPoiUnits = 0;
        for (int i = firstColIndex; i <= lastColIndex; i++) {
            totalColWidthInPoiUnits += sheet.getColumnWidth(i);
        }
        //convert from 1/256 character units to character units
        int totalColWidthInChars = totalColWidthInPoiUnits / 256;
        //convert from character units to java.awt.Font units
        //not sure about the multiplier of 5.. there is almost certainly
        //a better, less mysterious formula but I can't figure it out and '5' works.
        int totalColWidth = totalColWidthInChars * 5;

        //measure the text string against the column width to see how many lines it takes up
        java.awt.Font currFont = new java.awt.Font(font.getFontName(), 0, font.getFontHeightInPoints());
        AttributedString attrStr = new AttributedString(textWithoutNewlines);
        attrStr.addAttribute(TextAttribute.FONT, currFont);
        FontRenderContext frc = new FontRenderContext(null, true, true);
        LineBreakMeasurer measurer = new LineBreakMeasurer(attrStr.getIterator(), frc);
        int nextPos = 0;
        while (measurer.getPosition() < textWithoutNewlines.length()) {
            nextPos = measurer.nextOffset(totalColWidth);
            lineCount++;
            measurer.setPosition(nextPos);
        }
        return lineCount;
    }

    /**
     * Create a new cell and apply the default workbook style to it.
     */
    public Cell createCell(Row row, int cellIndex) {
        return createCell(row, cellIndex, smallStyle);
    }

    /**
     * Create a new cell and apply the a style to it.
     */
    public Cell createCell(Row row, int cellIndex, CellStyle style) {
        Cell cell = null;
        try {
            cell = row.createCell(cellIndex);
            cell.setCellStyle(style);
        } catch (Exception ex) {
            System.err.println("Error creating cell in row " + row.getRowNum() + " at column " + cellIndex);
            ex.printStackTrace();
        }
        return cell;
    }

    /**
     * Creates a row if it's not already created. Otherwise returns the existing row.
     */
    public Row getRow(Sheet sheet, int rowIndex) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }
        return row;
    }

    /**
     * Set up all the fonts we need across the different sheets.
     */
    private void initializeFonts() {
        boldFont = workbook.createFont();
        boldFont.setBold(true);
        boldFont.setFontHeightInPoints((short) WORKSHEET_LARGE_FONT_POINTS);

        smallFont = workbook.createFont();
        smallFont.setFontHeightInPoints((short) WORKSHEET_FONT_POINTS);

        boldSmallFont = workbook.createFont();
        boldSmallFont.setBold(true);
        boldSmallFont.setFontHeightInPoints((short) WORKSHEET_FONT_POINTS);

        italicSmallFont = workbook.createFont();
        italicSmallFont.setItalic(true);
        italicSmallFont.setFontHeightInPoints((short) WORKSHEET_FONT_POINTS);

        boldItalicSmallFont = workbook.createFont();
        boldItalicSmallFont.setBold(true);
        boldItalicSmallFont.setItalic(true);
        boldItalicSmallFont.setFontHeightInPoints((short) WORKSHEET_FONT_POINTS);

        italicUnderlinedSmallFont = workbook.createFont();
        italicUnderlinedSmallFont.setItalic(true);
        italicUnderlinedSmallFont.setUnderline(Font.U_SINGLE);
        italicUnderlinedSmallFont.setFontHeightInPoints((short) WORKSHEET_FONT_POINTS);
    }

    /**
     * Set up all the cell styles we need across the different sheets
     */
    private void initializeStyles() {
        boldStyle = workbook.createCellStyle();
        boldStyle.setFont(boldFont);
        boldStyle.setFillForegroundColor(IndexedColors.WHITE.index);
        boldStyle.setFillBackgroundColor(IndexedColors.WHITE.index);

        smallStyle = workbook.createCellStyle();
        smallStyle.setFont(smallFont);
        smallStyle.setFillForegroundColor(IndexedColors.WHITE.index);
        smallStyle.setFillBackgroundColor(IndexedColors.WHITE.index);

        italicSmallStyle = workbook.createCellStyle();
        italicSmallStyle.setFont(italicSmallFont);
        italicSmallStyle.setFillForegroundColor(IndexedColors.WHITE.index);
        italicSmallStyle.setFillBackgroundColor(IndexedColors.WHITE.index);

        topAlignedWrappedStyle = workbook.createCellStyle();
        topAlignedWrappedStyle.setFont(smallFont);
        topAlignedWrappedStyle.setVerticalAlignment(VerticalAlignment.TOP);
        topAlignedWrappedStyle.setWrapText(true);
        topAlignedWrappedStyle.setFillForegroundColor(IndexedColors.WHITE.index);
        topAlignedWrappedStyle.setFillBackgroundColor(IndexedColors.WHITE.index);

        sectionNumberingStyle = workbook.createCellStyle();
        sectionNumberingStyle.setAlignment(HorizontalAlignment.RIGHT);
        sectionNumberingStyle.setFont(boldSmallFont);
        sectionNumberingStyle.setFillForegroundColor(IndexedColors.WHITE.index);
        sectionNumberingStyle.setFillBackgroundColor(IndexedColors.WHITE.index);

        sectionHeadingStyle = workbook.createCellStyle();
        sectionHeadingStyle.setAlignment(HorizontalAlignment.LEFT);
        sectionHeadingStyle.setFont(boldSmallFont);
        sectionHeadingStyle.setFillForegroundColor(IndexedColors.WHITE.index);
        sectionHeadingStyle.setFillBackgroundColor(IndexedColors.WHITE.index);

        boldItalicSmallStyle = workbook.createCellStyle();
        boldItalicSmallStyle.setFont(boldItalicSmallFont);
        boldItalicSmallStyle.setFillForegroundColor(IndexedColors.WHITE.index);
        boldItalicSmallStyle.setFillBackgroundColor(IndexedColors.WHITE.index);

        italicUnderlinedSmallStyle = workbook.createCellStyle();
        italicUnderlinedSmallStyle.setFont(italicUnderlinedSmallFont);
        italicUnderlinedSmallStyle.setFillForegroundColor(IndexedColors.WHITE.index);
        italicUnderlinedSmallStyle.setFillBackgroundColor(IndexedColors.WHITE.index);

        rightAlignedTableHeadingStyle = workbook.createCellStyle();
        rightAlignedTableHeadingStyle.setFont(boldSmallFont);
        rightAlignedTableHeadingStyle.setAlignment(HorizontalAlignment.RIGHT);
        rightAlignedTableHeadingStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.index);
        rightAlignedTableHeadingStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        rightAlignedTableHeadingStyle.setFillBackgroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.index);

        leftAlignedTableHeadingStyle = workbook.createCellStyle();
        leftAlignedTableHeadingStyle.setFont(boldSmallFont);
        leftAlignedTableHeadingStyle.setAlignment(HorizontalAlignment.LEFT);
        leftAlignedTableHeadingStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.index);
        leftAlignedTableHeadingStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        leftAlignedTableHeadingStyle.setFillBackgroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.index);

        wrappedTableHeadingStyle = workbook.createCellStyle();
        wrappedTableHeadingStyle.setFont(boldSmallFont);
        wrappedTableHeadingStyle.setVerticalAlignment(VerticalAlignment.BOTTOM);
        wrappedTableHeadingStyle.setAlignment(HorizontalAlignment.LEFT);
        wrappedTableHeadingStyle.setWrapText(true);
        wrappedTableHeadingStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.index);
        wrappedTableHeadingStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        wrappedTableHeadingStyle.setFillBackgroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.index);

        tableSubheadingStyle = workbook.createCellStyle();
        tableSubheadingStyle.setFont(boldSmallFont);
        tableSubheadingStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
        tableSubheadingStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        tableSubheadingStyle.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.index);
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public Font getBoldFont() {
        return boldFont;
    }

    public void setBoldFont(Font boldFont) {
        this.boldFont = boldFont;
    }

    public Font getSmallFont() {
        return smallFont;
    }

    public void setSmallFont(Font smallFont) {
        this.smallFont = smallFont;
    }

    public Font getBoldSmallFont() {
        return boldSmallFont;
    }

    public void setBoldSmallFont(Font boldSmallFont) {
        this.boldSmallFont = boldSmallFont;
    }

    public Font getItalicSmallFont() {
        return italicSmallFont;
    }

    public void setItalicSmallFont(Font italicSmallFont) {
        this.italicSmallFont = italicSmallFont;
    }

    public Font getBoldItalicSmallFont() {
        return boldItalicSmallFont;
    }

    public void setBoldItalicSmallFont(Font boldItalicSmallFont) {
        this.boldItalicSmallFont = boldItalicSmallFont;
    }

    public Font getItalicUnderlinedSmallFont() {
        return italicUnderlinedSmallFont;
    }

    public void setItalicUnderlinedSmallFont(Font italicUnderlinedSmallFont) {
        this.italicUnderlinedSmallFont = italicUnderlinedSmallFont;
    }

    public CellStyle getBoldStyle() {
        return boldStyle;
    }

    public void setBoldStyle(CellStyle boldStyle) {
        this.boldStyle = boldStyle;
    }

    public CellStyle getSmallStyle() {
        return smallStyle;
    }

    public void setSmallStyle(CellStyle smallStyle) {
        this.smallStyle = smallStyle;
    }

    public CellStyle getItalicSmallStyle() {
        return italicSmallStyle;
    }

    public void setItalicSmallStyle(CellStyle italicSmallStyle) {
        this.italicSmallStyle = italicSmallStyle;
    }

    public CellStyle getBoldItalicSmallStyle() {
        return boldItalicSmallStyle;
    }

    public void setBoldItalicSmallStyle(CellStyle boldItalicSmallStyle) {
        this.boldItalicSmallStyle = boldItalicSmallStyle;
    }

    public CellStyle getItalicUnderlinedSmallStyle() {
        return italicUnderlinedSmallStyle;
    }

    public void setItalicUnderlinedSmallStyle(CellStyle italicUnderlinedSmallStyle) {
        this.italicUnderlinedSmallStyle = italicUnderlinedSmallStyle;
    }

    public CellStyle getTopAlignedWrappedStyle() {
        return topAlignedWrappedStyle;
    }

    public void setTopAlignedWrappedStyle(CellStyle topAlignedWrappedStyle) {
        this.topAlignedWrappedStyle = topAlignedWrappedStyle;
    }

    public CellStyle getSectionNumberingStyle() {
        return sectionNumberingStyle;
    }

    public void setSectionNumberingStyle(CellStyle sectionNumberingStyle) {
        this.sectionNumberingStyle = sectionNumberingStyle;
    }

    public CellStyle getSectionHeadingStyle() {
        return sectionHeadingStyle;
    }

    public void setSectionHeadingStyle(CellStyle sectionHeadingStyle) {
        this.sectionHeadingStyle = sectionHeadingStyle;
    }

    public CellStyle getRightAlignedTableHeadingStyle() {
        return rightAlignedTableHeadingStyle;
    }

    public void setRightAlignedTableHeadingStyle(CellStyle rightAlignedTableHeadingStyle) {
        this.rightAlignedTableHeadingStyle = rightAlignedTableHeadingStyle;
    }

    public CellStyle getLeftAlignedTableHeadingStyle() {
        return leftAlignedTableHeadingStyle;
    }

    public void setLeftAlignedTableHeadingStyle(CellStyle leftAlignedTableHeadingStyle) {
        this.leftAlignedTableHeadingStyle = leftAlignedTableHeadingStyle;
    }

    public CellStyle getWrappedTableHeadingStyle() {
        return wrappedTableHeadingStyle;
    }

    public void setWrappedTableHeadingStyle(CellStyle wrappedTableHeadingStyle) {
        this.wrappedTableHeadingStyle = wrappedTableHeadingStyle;
    }

    public CellStyle getTableSubheadingStyle() {
        return tableSubheadingStyle;
    }

    public void setTableSubheadingStyle(CellStyle tableSubheadingStyle) {
        this.tableSubheadingStyle = tableSubheadingStyle;
    }

}
