package gov.healthit.chpl.scheduler.job.surveillancereportingactivity;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
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

    public static CellStyle getDefaultStatStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints(DEFAULT_FONT_SIZE);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setDataFormat(workbook.createDataFormat().getFormat("0"));
        return style;
    }
}
