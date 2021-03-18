package gov.healthit.chpl.scheduler.job.summarystatistics.pdf;

import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.property.TextAlignment;

public class SummaryStatisticsPdfFooterEvent implements IEventHandler {
    private static final String FOOTER_TEXT =
            "* Developers and products may be certified by more than one ONC-ACB. Therefore, "
            + "counts for each ONC-ACB may not add up to the total developer or product count on CHPL.";

    @SuppressWarnings("resource")
    @Override
    public void handleEvent(Event event) {
        PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
        PdfDocument pdf = docEvent.getDocument();
        PdfPage page = docEvent.getPage();
        Rectangle pageSize = page.getPageSize();
        PdfCanvas pdfCanvas = new PdfCanvas(page.getLastContentStream(), page.getResources(), pdf);
        Canvas canvas = new Canvas(pdfCanvas, pageSize);
        canvas.setFont(SummaryStatisticsPdfDefaults.getDefaultFont());
        canvas.setFontSize(SummaryStatisticsPdfDefaults.FOOTER_FONT_SIZE);
        //Canvas c = new Canvas
        float x = (pageSize.getLeft() + pageSize.getRight()) / 2;
        float y = pageSize.getBottom() + 15;
        canvas.showTextAligned(FOOTER_TEXT, x, y, TextAlignment.CENTER);
    }


}
