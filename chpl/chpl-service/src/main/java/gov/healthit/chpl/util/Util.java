package gov.healthit.chpl.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
        String valueA = c1.getNumber();
        String valueB = c2.getNumber();
        if (Util.isCures(c1)) {
            valueA += "(Cures Update)";
        }
        if (Util.isCures(c2)) {
            valueB += "(Cures Update)";
        }
        return Util.getCertificationResultSortIndex(valueA) - Util.getCertificationResultSortIndex(valueB);

        // Pattern pattern = Pattern.compile(
        // "^(\\d{3}\\.\\d{3})\\s{1}" // captures "170.314"
        // + "(\\([a-z]{1}\\))" // captures "(b)"
        // + "(\\([0-9]{1,2}\\))?" // captures "(5)" or "(12)"
        // + "(\\([A-Z]{1}\\))?$" // captures "(A)"
        // );
        // Matcher m1 = pattern.matcher(c1.getNumber());
        // Matcher m2 = pattern.matcher(c2.getNumber());
        // int ret = 0;
        // if (!m1.matches() || !m2.matches()) {
        // return ret;
        // }
        // ret = compareStrings(m1.group(NUMBER_TITLE), m2.group(NUMBER_TITLE));
        // if (ret != 0) {
        // return ret;
        // }
        // ret = compareStrings(m1.group(NUMBER_PARA_1), m2.group(NUMBER_PARA_1));
        // if (ret != 0) {
        // return ret;
        // }
        //
        // ret = compareInts(m1.group(NUMBER_PARA_2), m2.group(NUMBER_PARA_2));
        // if (ret != 0) {
        // return ret;
        // }
        // ret = compareStrings(m1.group(NUMBER_PARA_3), m2.group(NUMBER_PARA_3));
        // if (ret != 0) {
        // return ret;
        // }
        // return c1.getTitle().compareTo(c2.getTitle());

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

    private static Integer getCertificationResultSortIndex(String criteriaNumber) {
        Integer index = certificationResultSortOrder().indexOf(criteriaNumber);
        if (index.equals(-1)) {
            index = 10000;
        }
        LOGGER.info(criteriaNumber + " = " + index);
        return index;
    }

    private static List<String> certificationResultSortOrder() {
        return Arrays.asList("170.302 (a)",
                "170.302 (b)",
                "170.302 (c)",
                "170.302 (d)",
                "170.302 (e)",
                "170.302 (f)(1)",
                "170.302 (f)(2)",
                "170.302 (f)(3)",
                "170.302 (g)",
                "170.302 (h)",
                "170.302 (i)",
                "170.302 (j)",
                "170.302 (k)",
                "170.302 (l)",
                "170.302 (m)",
                "170.302 (n)",
                "170.302 (o)",
                "170.302 (p)",
                "170.302 (q)",
                "170.302 (r)",
                "170.302 (s)",
                "170.302 (t)",
                "170.302 (u)",
                "170.302 (v)",
                "170.302 (w)",
                "170.304 (a)",
                "170.304 (b)",
                "170.304 (c)",
                "170.304 (d)",
                "170.304 (e)",
                "170.304 (f)",
                "170.304 (g)",
                "170.304 (h)",
                "170.304 (i)",
                "170.304 (j)",
                "170.306 (a)",
                "170.306 (b)",
                "170.306 (c)",
                "170.306 (d)(1)",
                "170.306 (d)(2)",
                "170.306 (e)",
                "170.306 (f)",
                "170.306 (g)",
                "170.306 (h)",
                "170.306 (i)",
                "170.314 (a)(1)",
                "170.314 (a)(2)",
                "170.314 (a)(3)",
                "170.314 (a)(4)",
                "170.314 (a)(5)",
                "170.314 (a)(6)",
                "170.314 (a)(7)",
                "170.314 (a)(8)",
                "170.314 (a)(9)",
                "170.314 (a)(10)",
                "170.314 (a)(11)",
                "170.314 (a)(12)",
                "170.314 (a)(13)",
                "170.314 (a)(14)",
                "170.314 (a)(15)",
                "170.314 (a)(16)",
                "170.314 (a)(17)",
                "170.314 (a)(18)",
                "170.314 (a)(19)",
                "170.314 (a)(20)",
                "170.314 (b)(1)",
                "170.314 (b)(2)",
                "170.314 (b)(3)",
                "170.314 (b)(4)",
                "170.314 (b)(5)(A)",
                "170.314 (b)(5)(B)",
                "170.314 (b)(6)",
                "170.314 (b)(7)",
                "170.314 (b)(8)",
                "170.314 (b)(9)",
                "170.314 (c)(1)",
                "170.314 (c)(2)",
                "170.314 (c)(3)",
                "170.314 (d)(1)",
                "170.314 (d)(2)",
                "170.314 (d)(3)",
                "170.314 (d)(4)",
                "170.314 (d)(5)",
                "170.314 (d)(6)",
                "170.314 (d)(7)",
                "170.314 (d)(8)",
                "170.314 (d)(9)",
                "170.314 (e)(1)",
                "170.314 (e)(2)",
                "170.314 (e)(3)",
                "170.314 (f)(1)",
                "170.314 (f)(2)",
                "170.314 (f)(3)",
                "170.314 (f)(4)",
                "170.314 (f)(5)",
                "170.314 (f)(6)",
                "170.314 (f)(7)",
                "170.314 (g)(1)",
                "170.314 (g)(2)",
                "170.314 (g)(3)",
                "170.314 (g)(4)",
                "170.314 (h)(1)",
                "170.314 (h)(2)",
                "170.314 (h)(3)",
                "170.315 (a)(1)",
                "170.315 (a)(2)",
                "170.315 (a)(3)",
                "170.315 (a)(4)",
                "170.315 (a)(5)",
                "170.315 (a)(6)",
                "170.315 (a)(7)",
                "170.315 (a)(8)",
                "170.315 (a)(9)",
                "170.315 (a)(10)",
                "170.315 (a)(11)",
                "170.315 (a)(12)",
                "170.315 (a)(13)",
                "170.315 (a)(14)",
                "170.315 (a)(15)",
                "170.315 (b)(1)(Cures Update)",
                "170.315 (b)(1)",
                "170.315 (b)(2)(Cures Update)",
                "170.315 (b)(2)",
                "170.315 (b)(3)(Cures Update)",
                "170.315 (b)(3)",
                "170.315 (b)(4)",
                "170.315 (b)(5)",
                "170.315 (b)(6)",
                "170.315 (b)(7)(Cures Update)",
                "170.315 (b)(7)",
                "170.315 (b)(8)(Cures Update)",
                "170.315 (b)(8)",
                "170.315 (b)(9)(Cures Update)",
                "170.315 (b)(9)",
                "170.315 (b)(10)",
                "170.315 (c)(1)",
                "170.315 (c)(2)",
                "170.315 (c)(3)(Cures Update)",
                "170.315 (c)(3)",
                "170.315 (c)(4)",
                "170.315 (d)(1)",
                "170.315 (d)(2)(Cures Update)",
                "170.315 (d)(2)",
                "170.315 (d)(3)(Cures Update)",
                "170.315 (d)(3)",
                "170.315 (d)(4)",
                "170.315 (d)(5)",
                "170.315 (d)(6)",
                "170.315 (d)(7)",
                "170.315 (d)(8)",
                "170.315 (d)(9)",
                "170.315 (d)(10)(Cures Update)",
                "170.315 (d)(10)",
                "170.315 (d)(11)",
                "170.315 (d)(12)",
                "170.315 (d)(13)",
                "170.315 (e)(1)(Cures Update)",
                "170.315 (e)(1)",
                "170.315 (e)(2)",
                "170.315 (e)(3)",
                "170.315 (f)(1)",
                "170.315 (f)(2)",
                "170.315 (f)(3)",
                "170.315 (f)(4)",
                "170.315 (f)(5)(Cures Update)",
                "170.315 (f)(5)",
                "170.315 (f)(6)",
                "170.315 (f)(7)",
                "170.315 (g)(1)",
                "170.315 (g)(2)",
                "170.315 (g)(3)",
                "170.315 (g)(4)",
                "170.315 (g)(5)",
                "170.315 (g)(6)(Cures Update)",
                "170.315 (g)(6)",
                "170.315 (g)(7)",
                "170.315 (g)(8)",
                "170.315 (g)(9)(Cures Update)",
                "170.315 (g)(9)",
                "170.315 (g)(10)",
                "170.315 (h)(1)",
                "170.315 (h)(2)",
                "170.523 (k)(1)",
                "170.523 (k)(2)");
    }
}
