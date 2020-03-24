package gov.healthit.chpl.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;

/**
 * Utilities used to validate various basic elements of CHPL domain objects.
 * @author alarned
 *
 */
@Component
public class ValidationUtils {
    private static final Logger LOGGER = LogManager.getLogger(ValidationUtils.class);
    private UrlValidator urlValidator;
    private CertificationCriterionDAO criteriaDao;

    @Autowired
    public ValidationUtils(CertificationCriterionDAO criteriaDao) {
        urlValidator = new UrlValidator();
        this.criteriaDao = criteriaDao;
    }

    /**
     * Check to see if input string has either Windows or *nix flavored new line character.
     * @param input string to check
     * @return true iff string contains "\n" or "\r\n"
     */
    public boolean hasNewline(String input) {
        // check both windows and unix line separator chars
        if (input == null || StringUtils.isEmpty(input)) {
            return false;
        }
        return input.contains("\n") || input.contains("\r\n");
    }

    /**
     * Validate that input string is a well formed URL.
     * @param input URL to check
     * @return true iff input is a valid URL. Will return false for empty/null string
     */
    public boolean isWellFormedUrl(String input) {
        return urlValidator.isValid(input);
    }

    /**
     * Validation utility to check if a part of the chpl product number
     * matches a specific regex. Useful to determine if any other than the
     * allowed characters are present.
     * @param chplProductNumber the chpl product number to test
     * @param partIndex the index, 0-8
     * @param regexToMatch regex like ^[0-9]$
     * @return true of the part matches the regex and false otherwise
     */
    public boolean chplNumberPartIsValid(String chplProductNumber,
            int partIndex, String regexToMatch) {
        String[] uniqueIdParts = chplProductNumber.split("\\.");
        if (uniqueIdParts.length == ChplProductNumberUtil.CHPL_PRODUCT_ID_PARTS) {
            String chplNumberPart = uniqueIdParts[partIndex];
            if (StringUtils.isEmpty(chplNumberPart)
                    || !chplNumberPart.matches(regexToMatch)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check that input string is in the default charset.
     * @param input string to check
     * @return true iff string is in default charset
     */
    public boolean isValidUtf8(String input) {
        return isValidUtf8(Charset.defaultCharset(), input);
    }

    /**
     * Check that input string is in the passed in charset.
     * @param inputCharset charset to check
     * @param input string to check
     * @return true iff input is in inputCharset
     */
    public boolean isValidUtf8(Charset inputCharset, String input) {
        if (input == null || StringUtils.isEmpty(input)) {
            return true;
        }

        if (inputCharset.name().equals(StandardCharsets.UTF_8.name())) {
            return !hasUtf8ReplacementCharacter(input);
        } else {
            return hasNonUtf8Character(input.getBytes());
        }
    }

    /**
     * This method could be called if the encoding in which input is received IS
     * UTF-8.
     *
     * @param input string to check
     * @return true iff input string contains \uFFFD
     */
    public boolean hasUtf8ReplacementCharacter(String input) {
        if (input.contains("\uFFFD")) {
            return true;
        }
        return false;
    }

    /**
     * This method could be called if the encoding in which input is received is
     * NOT already UTF-8.
     *
     * @param input string to check
     * @return true iff input has non UTF-8 character
     */
    public boolean hasNonUtf8Character(byte[] input) {
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

    /**
     * Check to see if the input certification is in the list of certifications.
     * @param certNumber certification to look for
     * @param allCerts certifications to check in
     * @return true iff certNumber found in allCerts
     */
    public boolean hasCert(String certNumber, List<CertificationCriterion> allCerts) {
        boolean hasCert = false;
        for (int i = 0; i < allCerts.size() && !hasCert; i++) {
            if (allCerts.get(i).getNumber().equals(certNumber)) {
                hasCert = true;
            }
        }
        return hasCert;
    }

    public boolean hasCert(CertificationCriterion criterion, List<CertificationCriterion> allCerts) {
        boolean hasCert = false;
        for (int i = 0; i < allCerts.size() && !hasCert; i++) {
            if (allCerts.get(i).getId().equals(criterion.getId())) {
                hasCert = true;
            }
        }
        return hasCert;
    }

    public CertificationCriterion getCert(String certNumber, List<CertificationCriterion> allCerts) {
        CertificationCriterion cert = null;
        for (int i = 0; i < allCerts.size() && cert == null; i++) {
            if (allCerts.get(i).getNumber().equals(certNumber)) {
                cert = allCerts.get(i);
            }
        }
        return cert;
    }

    /**
     * Look for required complimentary criteria; if any one of the
     * criterionToCheck is present in allCriteriaMet then all of the
     * complimentaryCertNumbers must be present in allCriteriaMet.
     *
     * @param criterionToCheck criteria to check
     * @param allCriteriaMet criteria to check against
     * @param complimentaryCertNumbers complimentary criteria that must be present
     * @return a list of error messages
     */
    public List<String> checkComplimentaryCriteriaAllRequired(List<String> criterionToCheck,
            List<String> complimentaryCertNumbers, List<CertificationCriterion> allCriteriaMet) {
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
                        criteriaBuffer.append(getAllCriteriaWithNumber(checkedCriteria));
                    }
                    errors.add("Certification criterion " + criteriaBuffer.toString() + " was found so "
                            + getAllCriteriaWithNumber(complimentaryCert) + " is required but was not found.");
                }
            }
        }
        return errors;
    }

    /**
     * Look for required complimentary criteria; if any one of the
     * criterionToCheck is present in allCriteriaMet, then any one
     * of the complimentaryCertNumbers must also be present in allCriteriaMet.
     *
     * @param criterionToCheck criteria to check
     * @param allCriteriaMet criteria to check against
     * @param complimentaryCertNumbers complimentary criteria of which at least one must be present
     * @return a list of error messages
     */
    public List<String> checkComplimentaryCriteriaAnyRequired(List<String> criterionToCheck,
            List<String> complimentaryCertNumbers, List<CertificationCriterion> allCriteriaMet) {
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
                    criteriaBuffer.append(getAllCriteriaWithNumber(checkedCriteria));
                }

                StringBuffer complementaryBuffer = new StringBuffer();
                for (int i = 0; i < complimentaryCertNumbers.size(); i++) {
                    String complimentaryCriteria = complimentaryCertNumbers.get(i);
                    if (i > 0) {
                        complementaryBuffer.append(" or ");
                    }
                    complementaryBuffer.append(getAllCriteriaWithNumber(complimentaryCriteria));
                }
                errors.add("Certification criterion " + criteriaBuffer.toString() + " was found so "
                        + complementaryBuffer.toString() + " is required but was not found.");
            }
        }
        return errors;
    }

    /**
     * Returns true if any of the passed in certs are present.
     * @param certsToCheck criteria to check
     * @param allCerts criteria to check against
     * @return true iff at least one of certsToCheck is in allCerts
     */
    public boolean hasAnyCert(List<String> certsToCheck, List<CertificationCriterion> allCerts) {
        boolean result = false;
        for (String currCertToCheck : certsToCheck) {
            if (hasCert(currCertToCheck, allCerts)) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Returns true if any of the passed in certs are present.
     * @param certToCompare criteria to check
     * @param certs criteria to check against
     * @return true iff at least one of certToCompare is in certs
     */
    public boolean containsCert(PendingCertificationResultDTO certToCompare, String[] certs) {
        boolean hasCert = false;
        for (String cert : certs) {
            if (certToCompare.getCriterion() != null
                    && certToCompare.getCriterion().getNumber().equals(cert)) {
                hasCert = true;
            }
        }
        return hasCert;
    }

    /**
     * Returns true if any of the passed in certs are present.
     * @param certToCompare criteria to check
     * @param certs criteria to check against
     * @return true iff at least one of certToCompare is in certs
     */
    public boolean containsCert(CertificationResult certToCompare, String[] certs) {
        boolean hasCert = false;
        for (String cert : certs) {
            if (certToCompare.getNumber().equals(cert)) {
                hasCert = true;
            }
        }
        return hasCert;
    }

    /**
     * look for required complimentary certs when one of the criteria met is a
     * certain class of cert... such as 170.315 (a)(*)
     *
     * @param criterionNumberStart class of criteria to check
     * @param allCriteriaMet all criteria met
     * @param complimentaryCertNumbers complimentary criteria that must be met
     * @return list of errors
     */
    public List<String> checkClassOfCriteriaForErrors(String criterionNumberStart,
            List<CertificationCriterion> allCriteriaMet, List<String> complimentaryCertNumbers) {
        List<String> errors = new ArrayList<String>();
        List<CertificationCriterion> presentAttestedCriteriaInClass = allCriteriaMet.stream()
                .filter(certResult -> certResult.getNumber().startsWith(criterionNumberStart)
                        && (certResult.getRemoved() == null
                        || certResult.getRemoved().equals(Boolean.FALSE)))
                .collect(Collectors.<CertificationCriterion>toList());

        if (presentAttestedCriteriaInClass != null && presentAttestedCriteriaInClass.size() > 0) {
            for (String currRequiredCriteria : complimentaryCertNumbers) {
                boolean hasComplimentaryCert = false;
                for (CertificationCriterion certResult : allCriteriaMet) {
                    if (certResult.getNumber().equals(currRequiredCriteria)) {
                        hasComplimentaryCert = true;
                    }
                }

                if (!hasComplimentaryCert) {
                    errors.add("Certification criterion " + criterionNumberStart + "(*) was found " + "so "
                            + getAllCriteriaWithNumber(currRequiredCriteria) + " is required but was not found.");
                }
            }
        }
        return errors;
    }

    public List<String> checkClassOfCriteriaForWarnings(String criterionNumberStart,
            List<CertificationCriterion> allCriteriaMet, List<String> complimentaryCertNumbers) {
        List<String> warnings = new ArrayList<String>();
        List<CertificationCriterion> removedAttestedCriteriaInClass = allCriteriaMet.stream()
                .filter(certResult -> certResult.getNumber().startsWith(criterionNumberStart)
                        && (certResult.getRemoved() == null
                        || certResult.getRemoved().equals(Boolean.TRUE)))
                .collect(Collectors.<CertificationCriterion>toList());
        List<CertificationCriterion> presentAttestedCriteriaInClass = allCriteriaMet.stream()
                .filter(certResult -> certResult.getNumber().startsWith(criterionNumberStart)
                        && (certResult.getRemoved() == null
                        || certResult.getRemoved().equals(Boolean.FALSE)))
                .collect(Collectors.<CertificationCriterion>toList());

        //if the only attested criteria in the "class" of criteria are marked as removed
        //then the lack of a complimentary criteria is only a warning
        if (removedAttestedCriteriaInClass != null && removedAttestedCriteriaInClass.size() > 0
                && (presentAttestedCriteriaInClass == null || presentAttestedCriteriaInClass.size() == 0)) {
            for (String currRequiredCriteria : complimentaryCertNumbers) {
                boolean hasComplimentaryCert = false;
                for (CertificationCriterion certResult : allCriteriaMet) {
                    if (certResult.getNumber().equals(currRequiredCriteria)) {
                        hasComplimentaryCert = true;
                    }
                }

                if (!hasComplimentaryCert) {
                    warnings.add("Certification criterion " + criterionNumberStart + "(*) was found " + "so "
                            + getAllCriteriaWithNumber(currRequiredCriteria) + " is required but was not found.");
                }
            }
        }
        return warnings;
    }

    /**
     * Look for a required complimentary criteria when a specific criteria has
     * been met.
     *
     * @param criterionNumber criteria to check
     * @param allCriteriaMet all criteria met
     * @param complimentaryCertNumbers complimentary criteria that must be met
     * @return list of errors
     */
    public List<String> checkSpecificCriteriaForErrors(String criterionNumber,
            List<CertificationCriterion> allCriteriaMet, List<String> complimentaryCertNumbers) {
        List<String> errors = new ArrayList<String>();
        boolean hasCriterion = false;
        for (CertificationCriterion currCriteria : allCriteriaMet) {
            if (currCriteria.getNumber().equals(criterionNumber)) {
                hasCriterion = true;
            }
        }
        if (hasCriterion) {
            for (String currRequiredCriteria : complimentaryCertNumbers) {
                boolean hasComplimentaryCert = false;
                for (CertificationCriterion certCriteria : allCriteriaMet) {
                    if (certCriteria.getNumber().equals(currRequiredCriteria)) {
                        hasComplimentaryCert = true;
                    }
                }

                if (!hasComplimentaryCert) {
                    errors.add("Certification criterion " + criterionNumber + " was found so "
                            + getAllCriteriaWithNumber(currRequiredCriteria) + " is required but was not found.");
                }
            }
        }
        return errors;
    }

    public String getAllCriteriaWithNumber(String criterionNumber) {
        List<CertificationCriterionDTO> allCriteriaWithNumber = criteriaDao.getAllByNumber(criterionNumber);
        List<String> allCriteriaNumbers = allCriteriaWithNumber.stream()
                .map(criterion -> Util.formatCriteriaNumber(criterion))
                .collect(Collectors.toList());
        return allCriteriaNumbers.stream().collect(Collectors.joining(" or "));
    }

    public List<CertificationCriterion> getAttestedCriteria(CertifiedProductSearchDetails listing) {
        List<CertificationResult> attestedCertificationResults = listing.getCertificationResults().stream()
                .filter(certResult -> certResult.isSuccess() != null && certResult.isSuccess().equals(Boolean.TRUE))
                .collect(Collectors.<CertificationResult>toList());

        List<CertificationCriterion> criteria = new ArrayList<CertificationCriterion>();
        for (CertificationResult cr : attestedCertificationResults) {
            criteria.add(cr.getCriterion());
        }
        return criteria;
    }

    public List<CertificationCriterion> getAttestedCriteria(PendingCertifiedProductDTO listing) {
        List<PendingCertificationResultDTO> attestedCertificationResults = listing.getCertificationCriterion().stream()
                .filter(certResult -> certResult.getMeetsCriteria() != null && certResult.getMeetsCriteria().equals(Boolean.TRUE))
                .collect(Collectors.<PendingCertificationResultDTO>toList());

        List<CertificationCriterion> criteria = new ArrayList<CertificationCriterion>();
        for (PendingCertificationResultDTO cr : attestedCertificationResults) {
            CertificationCriterionDTO criterionDto = cr.getCriterion();
            criteria.add(new CertificationCriterion(criterionDto));
        }
        return criteria;
    }
}
