package gov.healthit.chpl.scheduler.job.summarystatistics.pdf;

import java.io.IOException;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;

public class SummaryStatisticsPDFDefaults {
    public static final Integer DEFAULT_FONT_SIZE = 9;
    public static final Integer TITLE_FONT_SIZE = 18;
    public static final Integer SUBTITLE_FONT_SIZE = 14;


    public static PdfFont getDefaultFont() {
        try {
            return PdfFontFactory.createFont(StandardFonts.HELVETICA);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
