package gov.healthit.chpl.upload.listing.normalizer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RwtNormalizer {
    private static final String[] ACCEPTED_DATE_FORMATS = {
            "MM/dd/yyyy", "MM/dd/yy", "MM-dd-yy", "MM-dd-yyyy", "yyyy-MM-dd", "MMM d, yyyy", "MMM dd, yyyy",
            "MMMM d, yyyy", "MMMM dd, yyyy"
    };
    private List<DateTimeFormatter> formatters;

    public RwtNormalizer() {
        formatters = new ArrayList<DateTimeFormatter>();
        for (String format : ACCEPTED_DATE_FORMATS) {
            formatters.add(DateTimeFormatter.ofPattern(format));
        }
    }

    public void normalize(CertifiedProductSearchDetails listing) {
        if (!StringUtils.isEmpty(listing.getUserEnteredRwtPlansCheckDate())
                && listing.getRwtPlansCheckDate() == null) {
            listing.setRwtPlansCheckDate(getAsLocalDate(listing.getUserEnteredRwtPlansCheckDate()));
        }
        if (!StringUtils.isEmpty(listing.getUserEnteredRwtResultsCheckDate())
                && listing.getRwtResultsCheckDate() == null) {
            listing.setRwtResultsCheckDate(getAsLocalDate(listing.getUserEnteredRwtResultsCheckDate()));
        }
    }

    private LocalDate getAsLocalDate(String value) {
        LocalDate parsedDate = null;
        for (DateTimeFormatter formatter : formatters) {
            try {
                LocalDate ld = LocalDate.parse(value, formatter);
                if (ld != null && parsedDate == null) {
                    parsedDate = ld;
                }
            } catch (Exception ignore) { }
        }
        return parsedDate;
    }
}
