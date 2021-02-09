package gov.healthit.chpl.scheduler.job.summarystatistics.pdf;

import java.io.IOException;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;

public final class SummaryStatisticsPDFDefaults {
    private static final float[] TABLE_HEADER_BACKGROUND_COLOR = {225, 238, 217};

    public static final Integer DEFAULT_FONT_SIZE = 9;
    public static final Integer TITLE_FONT_SIZE = 18;
    public static final Integer SUBTITLE_FONT_SIZE = 14;

    private SummaryStatisticsPDFDefaults() {}

    public static PdfFont getDefaultFont() {
        try {
            return PdfFontFactory.createFont(StandardFonts.HELVETICA);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Color getTableHeaderDefaultColor() {
        return new DeviceRgb(
                TABLE_HEADER_BACKGROUND_COLOR[0],
                TABLE_HEADER_BACKGROUND_COLOR[1],
                TABLE_HEADER_BACKGROUND_COLOR[2]);
    }
}
