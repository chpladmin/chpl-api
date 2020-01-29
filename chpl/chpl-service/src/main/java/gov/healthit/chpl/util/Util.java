package gov.healthit.chpl.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.dto.CertificationCriterionDTO;

public final class Util {
    private static final Logger LOGGER = LogManager.getLogger(Util.class);
    private static final int BASE_16 = 16;
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss";

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
        Integer c1Number = Integer.parseInt(c1.getCertificationEdition());
        Integer c2Number = Integer.parseInt(c2.getCertificationEdition());
        if (c1Number.compareTo(c2Number) != 0) {
            return c1Number.compareTo(c2Number);
        }
        if (c1.getCertificationEdition().equalsIgnoreCase("2011")) {
            c1Number = Integer.parseInt(c1.getNumber().split(" ")[0].split(".")[1]);
            c2Number = Integer.parseInt(c2.getNumber().split(" ")[0].split(".")[1]);
            if (c1Number.compareTo(c2Number) != 0) {
                return c1Number.compareTo(c2Number);
            }
        }
        String c1String = c1.getNumber().split(" ")[1].substring(1, 2);
        String c2String = c2.getNumber().split(" ")[1].substring(1, 2);
        if (c1String.compareTo(c2String) != 0) {
            return c1String.compareTo(c2String);
        }
        c1String = c1.getNumber().split(" ")[1].split("\\)\\(")[1];
        c1Number = Integer.parseInt(c1String.substring(0, c1String.indexOf(")")));
        c2String = c2.getNumber().split(" ")[1].split("\\)\\(")[1];
        c2Number = Integer.parseInt(c2String.substring(0, c2String.indexOf(")")));
        if (c1Number.compareTo(c2Number) != 0) {
            return c1Number.compareTo(c2Number);
        }
        c1String = c1.getNumber().split(" ")[1].split("\\)\\(")[2];
        c1Number = Integer.parseInt(c1String.substring(0, c1String.indexOf(")")));
        c2String = c2.getNumber().split(" ")[1].split("\\)\\(")[2];
        c2Number = Integer.parseInt(c2String.substring(0, c2String.indexOf(")")));
        if (c1Number.compareTo(c2Number) != 0) {
            return c1Number.compareTo(c2Number);
        }
        return c1.getTitle().compareTo(c2.getTitle());
    }
}
