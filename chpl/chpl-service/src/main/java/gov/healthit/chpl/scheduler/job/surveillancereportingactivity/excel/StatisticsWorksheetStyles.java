package gov.healthit.chpl.scheduler.job.surveillancereportingactivity.excel;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;

public class StatisticsWorksheetStyles {
    private static final Short DEFAULT_FONT_SIZE = 11;

    public static CellStyle getMainHeaderStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontName("Calibri");
        font.setFontHeightInPoints(DEFAULT_FONT_SIZE);

        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        style.setBorderBottom(BorderStyle.THIN);
        style.setFont(font);
        return style;
    }

    public static CellStyle getAcbNameStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontName("Calibri");
        font.setFontHeightInPoints(DEFAULT_FONT_SIZE);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    public static CellStyle getSubheaderStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setItalic(true);
        font.setFontName("Calibri");
        font.setFontHeightInPoints(DEFAULT_FONT_SIZE);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    public static CellStyle getDefaultStatStyle(Workbook workbook, Boolean useAlternateColor) {
        Font font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints(DEFAULT_FONT_SIZE);

        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setFont(font);
        style.setDataFormat(workbook.createDataFormat().getFormat("0"));
        if (useAlternateColor) {
            style.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.index);
        } else {
            style.setFillForegroundColor(IndexedColors.PALE_BLUE.index);
        }
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    public static CellStyle getDefaultStatStyleForString(Workbook workbook, Boolean useAlternateColor) {
        Font font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints(DEFAULT_FONT_SIZE);

        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setFont(font);
        if (useAlternateColor) {
            style.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE1.index);
        } else {
            style.setFillForegroundColor(IndexedColors.PALE_BLUE.index);
        }
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

}
