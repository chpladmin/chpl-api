package gov.healthit.chpl.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;

public class ValidationUtils {
    private static final Logger LOGGER = LogManager.getLogger(ValidationUtils.class);

    public static boolean hasNewline(String input) {
        //check both windows and unix line separator chars
        if(input == null || StringUtils.isEmpty(input)) {
            return false;
        }
        return input.contains("\n") || input.contains("\r\n");
    }
    
    public static boolean isValidUtf8(String input) {
        return isValidUtf8(Charset.defaultCharset(), input);
    }
    
    public static boolean isValidUtf8(Charset inputCharset, String input) {
        if(input == null || StringUtils.isEmpty(input)) {
            return true;
        }
        
        if (inputCharset.name().equals(StandardCharsets.UTF_8.name())) {
            LOGGER.debug("Looking for UTF-8 Replacement Character in " + input);
            return !hasUtf8ReplacementCharacter(input);
        } else {
            LOGGER.debug("Looking for non UTF-8 character in " + input);
            return hasNonUtf8Character(input.getBytes());
        }
    }

    /**
     * This method could be called if the encoding in which input is received IS
     * UTF-8.
     * 
     * @param input
     * @return
     */
    public static boolean hasUtf8ReplacementCharacter(String input) {
        if (input.contains("\uFFFD")) {
            return true;
        }
        return false;
    }

    /**
     * This method could be called if the encoding in which input is received is
     * NOT already UTF-8
     * 
     * @param input
     * @return
     */
    public static boolean hasNonUtf8Character(byte[] input) {
        int i = 0;
        // Check for BOM
        if (input.length >= 3 && (input[0] & 0xFF) == 0xEF && (input[1] & 0xFF) == 0xBB & (input[2] & 0xFF) == 0xBF) {
            i = 3;
        }

        int end;
        for (int j = input.length; i < j; ++i) {
            int octet = input[i];
            if ((octet & 0x80) == 0) {
                continue; // ASCII
            }

            // Check for UTF-8 leading byte
            if ((octet & 0xE0) == 0xC0) {
                end = i + 1;
            } else if ((octet & 0xF0) == 0xE0) {
                end = i + 2;
            } else if ((octet & 0xF8) == 0xF0) {
                end = i + 3;
            } else {
                // Java only supports BMP so 3 is max
                return false;
            }

            while (i < end) {
                i++;
                octet = input[i];
                if ((octet & 0xC0) != 0x80) {
                    // Not a valid trailing byte
                    return false;
                }
            }
        }
        return true;
    }
    
    public static boolean hasCert(String certNumber, final List<String> allCerts){
        boolean hasCert = false;
        for (int i = 0; i < allCerts.size() && !hasCert; i++) {
            if (allCerts.get(i).equals(certNumber)) {
                hasCert = true;
            }
        }
        return hasCert;
    }
    
    /**
     * Look for required complimentary criteria; if any one of the
     * criterionToCheck is present in allCriteriaMet then all of the
     * complimentaryCertNumbers must be present in allCriteriaMet.
     *
     * @param criterionToCheck
     * @param allCriteriaMet
     * @param complimentaryCertNumbers
     * @return a list of error messages
     */
    public static List<String> checkComplimentaryCriteriaAllRequired(final List<String> criterionToCheck,
            final List<String> complimentaryCertNumbers, final List<String> allCriteriaMet) {
        List<String> errors = new ArrayList<String>();
        boolean hasAnyCert = hasAnyCert(criterionToCheck, allCriteriaMet);
        if (hasAnyCert) {
            for (String complimentaryCert : complimentaryCertNumbers) {
                boolean hasComplimentaryCert = hasCert(complimentaryCert, allCriteriaMet);

                if (!hasComplimentaryCert) {
                    StringBuffer criteriaBuffer = new StringBuffer();
                    for (int i = 0; i < criterionToCheck.size(); i++) {
                        String checkedCriteria = criterionToCheck.get(i);
                        if (i > 0) {
                            criteriaBuffer.append(" or ");
                        }
                        criteriaBuffer.append(checkedCriteria);
                    }
                    errors.add("Certification criterion " + criteriaBuffer.toString() + " was found so "
                            + complimentaryCert + " is required but was not found.");
                }
            }
        }
        return errors;
    }

    /**
     * Look for required complimentary criteria; if any one of the
     * criterionToCheck is present in allCriteriaMet, then any one
     * of the complimentaryCertNumbers must also be present in allCriteriaMet
     *
     * @param criterionToCheck
     * @param allCriteriaMet
     * @param complimentaryCertNumbers
     * @return a list of error messages
     */
    public static List<String> checkComplimentaryCriteriaAnyRequired(final List<String> criterionToCheck,
            final List<String> complimentaryCertNumbers, final List<String> allCriteriaMet) {
        List<String> errors = new ArrayList<String>();
        boolean hasAnyCert = hasAnyCert(criterionToCheck, allCriteriaMet);
        if (hasAnyCert) {
            boolean hasAnyComplimentaryCert = hasAnyCert(complimentaryCertNumbers, allCriteriaMet);
            if (!hasAnyComplimentaryCert) {
                StringBuffer criteriaBuffer = new StringBuffer();
                for (int i = 0; i < criterionToCheck.size(); i++) {
                    String checkedCriteria = criterionToCheck.get(i);
                    if (i > 0) {
                        criteriaBuffer.append(" or ");
                    }
                    criteriaBuffer.append(checkedCriteria);
                }

                StringBuffer complementaryBuffer = new StringBuffer();
                for (int i = 0; i < complimentaryCertNumbers.size(); i++) {
                    String complimentaryCriteria = complimentaryCertNumbers.get(i);
                    if (i > 0) {
                        complementaryBuffer.append(" or ");
                    }
                    complementaryBuffer.append(complimentaryCriteria);
                }
                errors.add("Certification criterion " + criteriaBuffer.toString() + " was found so "
                        + complementaryBuffer.toString() + " is required but was not found.");
            }
        }
        return errors;
    }
    
    /**
     * Returns true if any of the passed in certs are present
     * @param toCheck
     * @param allCerts
     * @return
     */
    public static boolean hasAnyCert(final List<String> certsToCheck, final List<String> allCerts) {
        boolean result = false;
        for (String currCertToCheck : certsToCheck) {
            if (hasCert(currCertToCheck, allCerts)) {
                result = true;
            }
        }
        return result;
    }
    
    public static boolean containsCert(final PendingCertificationResultDTO certToCompare, final String[] certs){
        boolean hasCert = false;
        for (String cert : certs) {
            if (certToCompare.getNumber().equals(cert)){
                hasCert = true;
            }
        }
        return hasCert;
    }
    
    public static boolean containsCert(final CertificationResult certToCompare, final String[] certs){
        boolean hasCert = false;
        for (String cert : certs) {
            if (certToCompare.getNumber().equals(cert)){
                hasCert = true;
            }
        }
        return hasCert;
    }
    
    /**
     * look for required complimentary certs when one of the criteria met is a
     * certain class of cert... such as 170.315 (a)(*)
     * 
     * @param criterionNumberStart
     * @param allCriteriaMet
     * @param complimentaryCertNumbers
     * @return
     */
    public static List<String> checkClassOfCriteriaForErrors(String criterionNumberStart, List<String> allCriteriaMet,
            List<String> complimentaryCertNumbers) {
        List<String> errors = new ArrayList<String>();
        boolean hasCriterion = false;
        for (String currCriteria : allCriteriaMet) {
            if (currCriteria.startsWith(criterionNumberStart)) {
                hasCriterion = true;
            }
        }
        if (hasCriterion) {
            for (String currRequiredCriteria : complimentaryCertNumbers) {
                boolean hasComplimentaryCert = false;
                for (String certCriteria : complimentaryCertNumbers) {
                    if (certCriteria.equals(currRequiredCriteria)) {
                        hasComplimentaryCert = true;
                    }
                }

                if (!hasComplimentaryCert) {
                    errors.add("Certification criterion " + criterionNumberStart + "(*) was found " + "so "
                            + currRequiredCriteria + " is required but was not found.");
                }
            }
        }
        return errors;
    }
    
    /**
     * Look for a required complimentary criteria when a specific criteria has
     * been met
     * 
     * @param criterionNumber
     * @param allCriteriaMet
     * @param complimentaryCertNumbers
     * @return
     */
    public static List<String> checkSpecificCriteriaForErrors(String criterionNumber, List<String> allCriteriaMet,
            List<String> complimentaryCertNumbers) {
        List<String> errors = new ArrayList<String>();
        boolean hasCriterion = false;
        for (String currCriteria : allCriteriaMet) {
            if (currCriteria.equals(criterionNumber)) {
                hasCriterion = true;
            }
        }
        if (hasCriterion) {
            for (String currRequiredCriteria : complimentaryCertNumbers) {
                boolean hasComplimentaryCert = false;
                for (String certCriteria : complimentaryCertNumbers) {
                    if (certCriteria.equals(currRequiredCriteria)) {
                        hasComplimentaryCert = true;
                    }
                }

                if (!hasComplimentaryCert) {
                    errors.add("Certification criterion " + criterionNumber + "(*) was found " + "so "
                            + currRequiredCriteria + " is required but was not found.");
                }
            }
        }
        return errors;
    }
}
