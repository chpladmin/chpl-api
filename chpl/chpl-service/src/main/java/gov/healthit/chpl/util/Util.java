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
import gov.healthit.chpl.domain.NonconformityType;
import gov.healthit.chpl.domain.surveillance.RequirementType;
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

        } catch (NoSuchAlgorithmException e) {
            LOGGER.error(e.getMessage(), e);
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

    private static boolean isCures(String title) {
        return title != null && title.contains(CURES_TITLE);
    }

    public static boolean isCures(CertificationCriterion criterion) {
        return isCures(criterion.getTitle());
    }

    public static String formatCriteriaNumber(NonconformityType nonconformityType) {
        String result = nonconformityType.getNumber();
        if (isCures(nonconformityType.getTitle())) {
            result += CURES_SUFFIX;
        }
        return result;
    }

    public static String formatCriteriaNumber(RequirementType requirementType) {
        String result = requirementType.getNumber();
        if (isCures(requirementType.getTitle())) {
            result += CURES_SUFFIX;
        }
        return result;
    }

    public static String formatCriteriaNumber(CertificationCriterion criterion) {
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

    public static String joinListGrammatically(List<String> list) {
        if (list == null || list.size() == 0) {
            return "";
        } else {
            return list.size() > 1
                    ? String.join(", ", list.subList(0, list.size() - 1))
                        .concat(String.format("%s and ", list.size() > 2 ? "," : ""))
                        .concat(list.get(list.size() - 1))
                    : list.get(0);
        }
    }

    public static String joinListGrammatically(List<String> list, String finalJoinStr) {
        if (list == null || list.size() == 0) {
            return "";
        } else {
            return list.size() > 1
                    ? String.join(", ", list.subList(0, list.size() - 1))
                        .concat(String.format("%s " + finalJoinStr + " ", list.size() > 2 ? "," : ""))
                        .concat(list.get(list.size() - 1))
                    : list.get(0);
        }
    }
}
