package gov.healthit.chpl.scheduler.job.surveillancereportingactivity.excel;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;

public final class CellHelper {

    private CellHelper() { }

    public static Cell setCellValueAsString(Cell cell, String value, String defaultValue) {
        if (value != null) {
            cell.setCellValue(value);
        } else {
            cell.setCellValue(defaultValue);
        }
        return cell;
    }

    public static Cell setCellValueAsLocalDate(Cell cell, LocalDate value, String defaultValue) {
        return setCellValueAsLocalDate(cell, value, defaultValue, null);
    }

    public static Cell setCellValueAsLocalDate(Cell cell, LocalDate value, String defaultValue, CellStyle style) {
        if (value != null) {
            cell.setCellValue(convertLocalDateToDate(value));
        } else {
            cell.setCellValue(defaultValue);
        }
        if (style != null) {
            cell.setCellStyle(style);
        }
        return cell;
    }

    public static Cell setCellValueAsInteger(Cell cell, Integer value, String defaultValue) {
        return setCellValueAsInteger(cell, value, defaultValue, null);
    }

    public static Cell setCellValueAsInteger(Cell cell, Integer value, String defaultValue, CellStyle style) {
        if (value != null) {
            cell.setCellValue(value);
        } else {
            cell.setCellValue(defaultValue);
        }
        if (style != null) {
            cell.setCellStyle(style);
        }
        return cell;
    }

    private static Date convertLocalDateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
