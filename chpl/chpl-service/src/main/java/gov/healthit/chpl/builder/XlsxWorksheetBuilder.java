package gov.healthit.chpl.builder;

import java.awt.Color;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;

public abstract class XlsxWorksheetBuilder {
    public static final int DEFAULT_MAX_COLUMN = 16384;
    private static final int WORKSHEET_FONT_POINTS  = 10;
    private static final int WORKSHEET_LARGE_FONT_POINTS = 12;

    protected Workbook workbook;
    protected Font boldFont, smallFont, boldSmallFont, italicSmallFont, boldItalicSmallFont,
    italicUnderlinedSmallFont;
    protected CellStyle boldStyle, smallStyle, italicSmallStyle, boldItalicSmallStyle,
    italicUnderlinedSmallStyle, wrappedStyle, sectionNumberingStyle, sectionHeadingStyle,
    tableHeadingStyle;

    public XlsxWorksheetBuilder(final Workbook workbook) {
        this.workbook = workbook;
        initializeFonts();
        initializeStyles();
    }

    public abstract int getLastDataColumn();
    public abstract int getLastDataRow();

    /**
     * Get or create a new worksheet within the workbook.
     * Worksheets are identified by name.
     * @param sheetName
     * @return
     */
    public Sheet getSheet(final String sheetName, final Color tabColor) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(sheetName);
            if (sheet instanceof XSSFSheet) {
                XSSFSheet xssfSheet = (XSSFSheet) sheet;
                DefaultIndexedColorMap colorMap = new DefaultIndexedColorMap();
                XSSFColor xssfTabColor = new XSSFColor(tabColor, colorMap);
                xssfSheet.setTabColor(xssfTabColor);

                //hide all the columns after E
                CTCol col = xssfSheet.getCTWorksheet().getColsArray(0).addNewCol();
                col.setMin(getLastDataColumn());
                col.setMax(DEFAULT_MAX_COLUMN); // the last column (1-indexed)
                col.setHidden(true);

                //TODO: figure out how to hide rows after lastDataRow
            }
        }
        return sheet;
    }

    /**
     * If you right click on a column in Excel, it gives you some number for the column width.
     * That is in different units than the POI library uses. This function converts the excel
     * width units into the POI width units. It's pretty close but not 100%.
     * @param excelWidth
     * @return
     */
    public int getColumnWidth(final int excelWidth) {
        return (excelWidth * 256) + 200;
    }

    /**
     * Create a new cell and apply the default worksheet style to it.
     * @param row
     * @param cellIndex
     * @return
     */
    public Cell createCell(final Row row, final int cellIndex) {
        Cell cell = row.createCell(cellIndex);
        cell.setCellStyle(smallStyle);
        return cell;
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

        wrappedStyle = workbook.createCellStyle();
        wrappedStyle.setFont(smallFont);
        wrappedStyle.setWrapText(true);
        wrappedStyle.setFillForegroundColor(IndexedColors.WHITE.index);
        wrappedStyle.setFillBackgroundColor(IndexedColors.WHITE.index);

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

        tableHeadingStyle = workbook.createCellStyle();
        tableHeadingStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.index);
        tableHeadingStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        tableHeadingStyle.setFillBackgroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.index);
    }


    //TODO: pulled from the internet and don't want to lose this
    //may be able to use it to auto size the rows containing free text
//    private void autoSizeRowHeight() {
//        java.awt.Font currFont = new java.awt.Font("Calibri", 0, 11);
//        AttributedString attrStr = new AttributedString(record.getDescription());
//        attrStr.addAttribute(TextAttribute.FONT, currFont);
//
//        // Use LineBreakMeasurer to count number of lines needed for the text
//        FontRenderContext frc = new FontRenderContext(null, true, true);
//        LineBreakMeasurer measurer = new LineBreakMeasurer(attrStr.getIterator(), frc);
//        int nextPos = 0;
//        int lineCnt = 0;
//
//        while (measurer.getPosition() < record.getDescription().length()) {
//            System.out.println(measurer.getPosition());
//            nextPos = measurer.nextOffset(mergedCellWidth); // mergedCellWidth is the max width of each line
//            lineCnt++;
//            measurer.setPosition(nextPos);
//            System.out.println(measurer.getPosition());
//        }
//
//        row.setHeight((short)(row.getHeight() * lineCnt));
//    }
}
