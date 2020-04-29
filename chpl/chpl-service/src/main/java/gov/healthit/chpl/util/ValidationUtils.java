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

    public boolean hasNewline(String input) {
        // check both windows and unix line separator chars
        if (input == null || StringUtils.isEmpty(input)) {
            return false;
        }
        return input.contains("\n") || input.contains("\r\n");
    }

    public boolean isWellFormedUrl(String input) {
        return urlValidator.isValid(input);
    }

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

    public boolean isValidUtf8(String input) {
        return isValidUtf8(Charset.defaultCharset(), input);
    }

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

    public boolean hasUtf8ReplacementCharacter(String input) {
        if (input.contains("\uFFFD")) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("checkstyle:magicnumber")
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
     * This method will not handle differentiating between criteria with the same criteria number, as is the case with
     * some new Cures criteria.
     */
    @Deprecated
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

    public List<String> checkSubordinateCriteriaAllRequired(List<CertificationCriterion> subordinateCriteria,
            List<CertificationCriterion> requiredCriteria, List<CertificationCriterion> attestedToCriteria,
            ErrorMessageUtil errorMessageUtil) {
        List<String> errors = new ArrayList<String>();

        List<Long> attestedToCriteriaIds = getCertificationCriteriaIds(attestedToCriteria);
        List<Long> subordinateCriteriaIds = getCertificationCriteriaIds(subordinateCriteria);
        List<Long> requiredCriteriaIds = getCertificationCriteriaIds(requiredCriteria);

        for (Long attestedToCriterionId : attestedToCriteriaIds) {
            if (subordinateCriteriaIds.contains(attestedToCriterionId)) {
                for (Long requiredCriterionId : requiredCriteriaIds) {
                    if (!attestedToCriteriaIds.contains(requiredCriterionId)) {
                        errors.add(errorMessageUtil.getMessage(
                                "listing.criteria.dependentCriteriaRequired",
                                Util.formatCriteriaNumber(findCertificationCriterion(attestedToCriteria, attestedToCriterionId)),
                                Util.formatCriteriaNumber(findCertificationCriterion(requiredCriteria, requiredCriterionId))));
                    }
                }
            }
        }

        return errors;
    }

    private List<Long> getCertificationCriteriaIds(List<CertificationCriterion> criteria) {
        return criteria.stream()
                .map(criterion -> criterion.getId())
                .collect(Collectors.toList());
    }

    private CertificationCriterion findCertificationCriterion(List<CertificationCriterion> criteria, Long criteriaId) {
        return criteria.stream()
                .filter(c -> c.getId().equals(criteriaId))
                .findFirst()
                .orElse(null);
    }

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

    public boolean hasAnyCert(List<String> certsToCheck, List<CertificationCriterion> allCerts) {
        boolean result = false;
        for (String currCertToCheck : certsToCheck) {
            if (hasCert(currCertToCheck, allCerts)) {
                result = true;
            }
        }
        return result;
    }

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

    public boolean containsCert(CertificationResult certToCompare, String[] certs) {
        boolean hasCert = false;
        for (String cert : certs) {
            if (certToCompare.getNumber().equals(cert)) {
                hasCert = true;
            }
        }
        return hasCert;
    }

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

        // if the only attested criteria in the "class" of criteria are marked as removed
        // then the lack of a complimentary criteria is only a warning
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
