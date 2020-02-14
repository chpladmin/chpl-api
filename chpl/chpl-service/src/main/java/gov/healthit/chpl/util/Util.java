package gov.healthit.chpl.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.dto.CertificationCriterionDTO;

public final class Util {
    private static final Logger LOGGER = LogManager.getLogger(Util.class);
    private static final int BASE_16 = 16;
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss";

    private static final int NUMBER_TITLE = 1;
    private static final int NUMBER_PARA_1 = 2;
    private static final int NUMBER_PARA_2 = 3;
    private static final int NUMBER_PARA_3 = 4;

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

    public static String coerceToCriterionNumberFormat(final String input) {
        String formatRegex = "^\\d{3}\\.\\d{3}\\s{1}\\([a-z]{1}\\)(\\([0-9]{1,2}\\))?$";
        if (input.matches(formatRegex)) {
            LOGGER.debug("\tMatches required format. Not changing input.");
            return input;
        }

        String adjustedInput = input.toLowerCase();
        adjustedInput = adjustedInput.trim();
        if (adjustedInput.matches(formatRegex)) {
            LOGGER.debug("\tTrimmed space and made lower case: " + adjustedInput);
            return adjustedInput;
        }

        // check for the middle space
        int openParenIndex = adjustedInput.indexOf('(');
        if (openParenIndex > 0) {
            int currIndex = openParenIndex;
            boolean foundNonspaceChar = false;
            while (currIndex > 0 && !foundNonspaceChar) {
                currIndex--;
                String currChar = adjustedInput.charAt(currIndex) + "";
                if (currChar.matches("\\S")) {
                    foundNonspaceChar = true;
                }
            }

            if (currIndex >= 0) {
                adjustedInput = adjustedInput.substring(0, currIndex + 1) + " "
                        + adjustedInput.substring(openParenIndex);
            }
        }

        if (adjustedInput.matches(formatRegex)) {
            LOGGER.debug("\tAdjusted spaces in the middle of the criterion: " + adjustedInput);
            return adjustedInput;
        }

        return input;
    }

    public static Date getNewDate(final Date orig) {
        if (orig != null) {
            return new Date(orig.getTime());
        } else {
            return null;
        }
    }

    public static int sortCriteria(CertificationCriterionDTO c1, CertificationCriterionDTO c2) {
        Pattern pattern = Pattern.compile(
                "^(\\d{3}\\.\\d{3})\\s{1}"      // captures "170.314"
                        + "(\\([a-z]{1}\\))"    // captures "(b)"
                        + "(\\([0-9]{1,2}\\))?" // captures "(5)" or "(12)"
                        + "(\\([A-Z]{1}\\))?$"  // captures "(A)"
                );
        Matcher m1 = pattern.matcher(c1.getNumber());
        Matcher m2 = pattern.matcher(c2.getNumber());
        int ret = 0;
        if (!m1.matches() || !m2.matches()) {
            return ret;
        }
        ret = compareStrings(m1.group(NUMBER_TITLE), m2.group(NUMBER_TITLE));
        if (ret != 0) {
            return ret;
        }
        ret = compareStrings(m1.group(NUMBER_PARA_1), m2.group(NUMBER_PARA_1));
        if (ret != 0) {
            return ret;
        }
        ret = compareInts(m1.group(NUMBER_PARA_2), m2.group(NUMBER_PARA_2));
        if (ret != 0) {
            return ret;
        }
        ret = compareStrings(m1.group(NUMBER_PARA_3), m2.group(NUMBER_PARA_3));
        if (ret != 0) {
            return ret;
        }
        return c1.getTitle().compareTo(c2.getTitle());
    }

    private static int compareStrings(String s1, String s2) {
        if (s1 == null && s2 == null) {
            return 0;
        }
        if (s1 == null) {
            return -1;
        }
        if (s2 == null) {
            return 1;
        }
        return s1.compareTo(s2);
    }

    private static int compareInts(String s1, String s2) {
        if (s1 == null && s2 == null) {
            return 0;
        }
        if (s1 == null) {
            return -1;
        }
        if (s2 == null) {
            return 1;
        }
        Integer i1 = Integer.parseInt(s1.substring(1, s1.length() - 1));
        Integer i2 = Integer.parseInt(s2.substring(1, s2.length() - 1));
        return i1.compareTo(i2);
    }
}
