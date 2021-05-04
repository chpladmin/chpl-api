package gov.healthit.chpl.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.validator.routines.EmailValidator;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class Util {
    private static final int BASE_16 = 16;
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss";
    private static final String CURES_TITLE = "Cures Update";
    public static final String CURES_SUFFIX = " (" + CURES_TITLE + ")";

    private Util() {

    }

    public static String md5(final String input) {
        String md5 = null;
        if (null == input) {
            return null;
        }

        try {
            // Create MessageDigest object for MD5
            MessageDigest digest = MessageDigest.getInstance("MD5");

            // Update input string in message digest
            digest.update(input.getBytes(), 0, input.length());

            // Converts message digest value in base 16 (hex)
            md5 = new BigInteger(1, digest.digest()).toString(BASE_16);

        } catch (final NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return md5;
    }

    public static SimpleDateFormat getDateFormatter() {
        return new SimpleDateFormat(DATE_FORMAT);
    }

    public static SimpleDateFormat getTimestampFormatter() {
        return new SimpleDateFormat(TIMESTAMP_FORMAT);
    }

    public static Date getNewDate(final Date orig) {
        if (orig != null) {
            return new Date(orig.getTime());
        } else {
            return null;
        }
    }

    public static boolean isCures(CertificationCriterion criterion) {
        return criterion.getTitle() != null && criterion.getTitle().contains(CURES_TITLE);
    }

    public static String formatCriteriaNumber(CertificationCriterion criterion) {
        String result = criterion.getNumber();
        if (isCures(criterion)) {
            result += CURES_SUFFIX;
        }
        return result;
    }

    public static boolean isCures(CertificationCriterionDTO criterion) {
        return criterion.getTitle() != null && criterion.getTitle().contains(CURES_TITLE);
    }

    public static String formatCriteriaNumber(CertificationCriterionDTO criterion) {
        String result = criterion.getNumber();
        if (isCures(criterion)) {
            result += CURES_SUFFIX;
        }
        return result;
    }

    public static <T> List<T> getListFromIterator(Iterator<T> iterator) {
        Iterable<T> iterable = () -> iterator;

        return StreamSupport
                  .stream(iterable.spliterator(), false)
                  .collect(Collectors.toList());
    }

    public static boolean isEmailAddressValidFormat(String email) {
        return EmailValidator.getInstance().isValid(email);
    }
}
