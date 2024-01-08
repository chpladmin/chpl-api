package gov.healthit.chpl.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.service.CertificationCriterionService;

@Component
public class ValidationUtils {
    private UrlValidator urlValidator;
    private CertificationCriterionService criteriaService;

    public ValidationUtils() {
        urlValidator = new UrlValidator();
    }

    @Autowired
    public ValidationUtils(CertificationCriterionService criteriaService) {
        this();
        this.criteriaService = criteriaService;
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

    public boolean chplNumberPartIsValid(String chplProductNumber, int partIndex, String regexToMatch) {
        String[] uniqueIdParts = chplProductNumber.split("\\.");
        if (uniqueIdParts.length == ChplProductNumberUtil.CHPL_PRODUCT_ID_PARTS) {
            String chplNumberPart = uniqueIdParts[partIndex];
            if (StringUtils.isEmpty(chplNumberPart) || !chplNumberPart.matches(regexToMatch)) {
                return false;
            }
        }
        return true;
    }

    public boolean chplNumberPartIsPresentAndValid(String chplProductNumber, int partIndex, String regexToMatch) {
        String[] uniqueIdParts = chplProductNumber.split("\\.");
        if (uniqueIdParts.length == ChplProductNumberUtil.CHPL_PRODUCT_ID_PARTS) {
            String chplNumberPart = uniqueIdParts[partIndex];
            if (!StringUtils.isEmpty(chplNumberPart) && chplNumberPart.matches(regexToMatch)) {
                return true;
            }
        }
        return false;
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

    public boolean hasCriterion(CertificationCriterion criterion, List<CertificationCriterion> allCerts) {
        boolean hasCert = false;
        for (int i = 0; i < allCerts.size() && !hasCert; i++) {
            if (allCerts.get(i).getId().equals(criterion.getId())) {
                hasCert = true;
            }
        }
        return hasCert;
    }

    public boolean hasAnyCriteria(List<CertificationCriterion> criteriaToFind,
            List<CertificationCriterion> allCriteria) {
        return criteriaToFind.stream()
                .filter(criterionToFind -> hasCriterion(criterionToFind, allCriteria))
                .findAny()
                .isPresent();
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

    public List<String> checkComplementaryCriteriaAllRequired(
            List<CertificationCriterion> criterionToCheck,
            List<CertificationCriterion> complementaryCriteria,
            List<CertificationCriterion> allCriteriaMet) {
        List<String> errors = new ArrayList<String>();

        criterionToCheck = criterionToCheck.stream()
                .filter(crit -> !crit.isRemoved())
                .collect(Collectors.toList());
        complementaryCriteria = complementaryCriteria.stream()
                .filter(crit -> !crit.isRemoved())
                .collect(Collectors.toList());

        if (hasAnyCriteria(criterionToCheck, allCriteriaMet)) {
            for (CertificationCriterion complementaryCriterion : complementaryCriteria) {
                boolean hasComplementaryCriterion = hasCriterion(complementaryCriterion, allCriteriaMet);
                if (!hasComplementaryCriterion) {
                    StringBuffer criteriaBuffer = new StringBuffer();
                    for (int i = 0; i < criterionToCheck.size(); i++) {
                        CertificationCriterion checkedCriterion = criterionToCheck.get(i);
                        if (i > 0) {
                            criteriaBuffer.append(" or ");
                        }
                        criteriaBuffer.append(Util.formatCriteriaNumber(checkedCriterion));
                    }
                    errors.add("Certification criterion " + criteriaBuffer.toString() + " was found so "
                            + Util.formatCriteriaNumber(complementaryCriterion) + " is required but was not found.");
                }
            }
        }
        return errors;
    }

    /**
     * This method will not handle differentiating between criteria with the same
     * criteria number, as is the case with some new Cures criteria.
     */
    @Deprecated
    public List<String> checkComplementaryCriteriaNumbersAllRequired(List<String> criterionToCheck,
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
                        errors.add(errorMessageUtil.getMessage("listing.criteria.dependentCriteriaRequired",
                                Util.formatCriteriaNumber(
                                        findCertificationCriterion(attestedToCriteria, attestedToCriterionId)),
                                Util.formatCriteriaNumber(
                                        findCertificationCriterion(requiredCriteria, requiredCriterionId))));
                    }
                }
            }
        }

        return errors;
    }

    private List<Long> getCertificationCriteriaIds(List<CertificationCriterion> criteria) {
        return criteria.stream().map(criterion -> criterion.getId()).collect(Collectors.toList());
    }

    private CertificationCriterion findCertificationCriterion(List<CertificationCriterion> criteria, Long criteriaId) {
        return criteria.stream().filter(c -> c.getId().equals(criteriaId)).findFirst().orElse(null);
    }

    public List<String> checkComplementaryCriteriaAnyRequired(List<CertificationCriterion> criteriaToCheck,
            List<CertificationCriterion> complementaryCriteria, List<CertificationCriterion> allCriteriaMet) {
        List<String> errors = new ArrayList<String>();
        boolean hasAnyCriteria = hasAnyCriteria(
                criteriaToCheck.stream()
                        .filter(crit -> !crit.isRemoved())
                        .collect(Collectors.toList()),
                allCriteriaMet);
        if (hasAnyCriteria) {
            boolean hasAnyComplementaryCriteria = hasAnyCriteria(complementaryCriteria, allCriteriaMet);
            if (!hasAnyComplementaryCriteria) {
                StringBuffer criteriaBuffer = new StringBuffer();
                for (int i = 0; i < criteriaToCheck.size(); i++) {
                    CertificationCriterion checkedCriterion = criteriaToCheck.get(i);
                    if (i > 0) {
                        criteriaBuffer.append(" or ");
                    }
                    criteriaBuffer.append(Util.formatCriteriaNumber(checkedCriterion));
                }

                StringBuffer complementaryBuffer = new StringBuffer();
                for (int i = 0; i < complementaryCriteria.size(); i++) {
                    CertificationCriterion complementaryCriterion = complementaryCriteria.get(i);
                    if (i > 0) {
                        complementaryBuffer.append(" or ");
                    }
                    complementaryBuffer.append(Util.formatCriteriaNumber(complementaryCriterion));
                }
                errors.add("Certification criterion " + criteriaBuffer.toString() + " was found so "
                        + complementaryBuffer.toString() + " is required but was not found.");
            }
        }
        return errors;
    }

    public List<String> checkComplementaryCriteriaNumbersAnyRequired(List<String> criterionToCheck,
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

    public boolean containsCert(CertificationResult certToCompare, String[] certs) {
        boolean hasCert = false;
        for (String cert : certs) {
            if (certToCompare.getCriterion().getNumber().equals(cert)) {
                hasCert = true;
            }
        }
        return hasCert;
    }

    public List<String> checkClassOfCriteriaForMissingComplementaryCriteriaErrors(String criterionNumberStart,
            List<CertificationCriterion> allCriteriaMet, List<CertificationCriterion> complementaryCriteria) {
        return checkClassSubsetOfCriteriaForMissingComplementaryCriteriaErrors(criterionNumberStart, allCriteriaMet,
                complementaryCriteria, new ArrayList<CertificationCriterion>());
    }

    public List<String> checkClassOfCriteriaForMissingComplementaryCriteriaNumberErrors(String criterionNumberStart,
            List<CertificationCriterion> allCriteriaMet, List<String> complementaryCertNumbers) {
        return checkClassSubsetOfCriteriaForMissingComplementaryCriteriaNumberErrors(criterionNumberStart, allCriteriaMet,
                complementaryCertNumbers, new ArrayList<String>());
    }

    public List<String> checkClassSubsetOfCriteriaForMissingComplementaryCriteriaErrors(String criterionNumberStart,
            List<CertificationCriterion> allCriteriaMet, List<CertificationCriterion> complementaryCriteria,
            List<CertificationCriterion> excludedCriteria) {
        return checkClassSubsetOfCriteriaForMissingComplementaryCriteriaNumberErrors(criterionNumberStart,
                allCriteriaMet,
                complementaryCriteria.stream().map(criterion -> criterion.getNumber()).distinct().collect(Collectors.toList()),
                excludedCriteria.stream().map(criterion -> criterion.getNumber()).distinct().collect(Collectors.toList()));
    }

    public List<String> checkClassSubsetOfCriteriaForMissingComplementaryCriteriaNumberErrors(String criterionNumberStart,
            List<CertificationCriterion> allCriteriaMet, List<String> complimentaryCriteriaNumbers,
            List<String> excludedCertNumbers) {
        List<String> errors = new ArrayList<String>();
        List<CertificationCriterion> presentAttestedCriteriaInClass = allCriteriaMet.stream()
                .filter(certResult -> certResult.getNumber().startsWith(criterionNumberStart)
                        && (certResult.isRemoved() == null || certResult.isRemoved().equals(Boolean.FALSE))
                        && !excludedCertNumbers.contains(certResult.getNumber()))
                .collect(Collectors.<CertificationCriterion>toList());

        if (presentAttestedCriteriaInClass != null && presentAttestedCriteriaInClass.size() > 0) {
            for (String requiredCriterionNumber : complimentaryCriteriaNumbers) {
                boolean hasComplimentaryCriterion = false;
                for (CertificationCriterion attestedCriterion : allCriteriaMet) {
                    if (attestedCriterion.getNumber().equals(requiredCriterionNumber)) {
                        hasComplimentaryCriterion = true;
                    }
                }

                if (!hasComplimentaryCriterion) {
                    errors.add("Certification criterion " + criterionNumberStart + "(*) was found " + "so "
                            + getAllCriteriaWithNumber(requiredCriterionNumber) + " is required but was not found.");
                }
            }
        }
        return errors;
    }

    public List<String> checkClassOfCriteriaForMissingComplementaryCriteriaWarnings(String criterionNumberStart,
            List<CertificationCriterion> allCriteriaMet, List<CertificationCriterion> complementaryCriteria) {
        return checkClassSubsetOfCriteriaForMissingComplementaryCriteriaWarnings(criterionNumberStart, allCriteriaMet,
                complementaryCriteria, new ArrayList<CertificationCriterion>());
    }

    public List<String> checkClassOfCriteriaForMissingComplementaryCriteriaNumberWarnings(String criterionNumberStart,
            List<CertificationCriterion> allCriteriaMet, List<String> complementaryCriteriaNumbers) {
        return checkClassSubsetOfCriteriaForMissingComplementaryCriteriaNumberWarnings(criterionNumberStart, allCriteriaMet, complementaryCriteriaNumbers,
                new ArrayList<String>());
    }

    public List<String> checkClassSubsetOfCriteriaForMissingComplementaryCriteriaWarnings(String criterionNumberStart,
            List<CertificationCriterion> allCriteriaMet, List<CertificationCriterion> complementaryCriteria,
            List<CertificationCriterion> excludedCriteria) {
        return checkClassSubsetOfCriteriaForMissingComplementaryCriteriaNumberWarnings(criterionNumberStart,
                allCriteriaMet,
                complementaryCriteria.stream().map(criterion -> criterion.getNumber()).distinct().collect(Collectors.toList()),
                excludedCriteria.stream().map(criterion -> criterion.getNumber()).distinct().collect(Collectors.toList()));
    }

    public List<String> checkClassSubsetOfCriteriaForMissingComplementaryCriteriaNumberWarnings(String criterionNumberStart,
            List<CertificationCriterion> allCriteriaMet, List<String> complementaryCriteriaNumbers,
            List<String> excludedCertNumbers) {
        List<String> warnings = new ArrayList<String>();
        List<CertificationCriterion> removedAttestedCriteriaInClass = allCriteriaMet.stream()
                .filter(certResult -> certResult.getNumber().startsWith(criterionNumberStart)
                        && (certResult.isRemoved() == null || certResult.isRemoved().equals(Boolean.TRUE))
                        && !excludedCertNumbers.contains(certResult.getNumber()))
                .collect(Collectors.<CertificationCriterion>toList());
        List<CertificationCriterion> presentAttestedCriteriaInClass = allCriteriaMet.stream()
                .filter(certResult -> certResult.getNumber().startsWith(criterionNumberStart)
                        && (certResult.isRemoved() == null || certResult.isRemoved().equals(Boolean.FALSE))
                        && !excludedCertNumbers.contains(certResult.getNumber()))
                .collect(Collectors.<CertificationCriterion>toList());

        // if the only attested criteria in the "class" of criteria are marked as
        // removed
        // then the lack of a complimentary criteria is only a warning
        if (removedAttestedCriteriaInClass != null && removedAttestedCriteriaInClass.size() > 0
                && (presentAttestedCriteriaInClass == null || presentAttestedCriteriaInClass.size() == 0)) {
            for (String requiredCriterionNumber : complementaryCriteriaNumbers) {
                boolean hasComplimentaryCriterion = false;
                for (CertificationCriterion attestedCriterion : allCriteriaMet) {
                    if (attestedCriterion.getNumber().equals(requiredCriterionNumber)) {
                        hasComplimentaryCriterion = true;
                    }
                }

                if (!hasComplimentaryCriterion) {
                    warnings.add("Certification criterion " + criterionNumberStart + "(*) was found " + "so "
                            + getAllCriteriaWithNumber(requiredCriterionNumber) + " is required but was not found.");
                }
            }
        }
        return warnings;
    }

    public List<String> checkSpecificCriterionForMissingComplementaryCriteriaErrors(CertificationCriterion criterion,
            List<CertificationCriterion> allCriteriaMet, List<CertificationCriterion> complementaryCriteria) {
        return checkSpecificCriterionForMissingComplementaryCriteriaNumberErrors(criterion,
                allCriteriaMet,
                complementaryCriteria.stream().map(crit -> crit.getNumber()).distinct().collect(Collectors.toList()));
    }

    public List<String> checkSpecificCriterionForMissingComplementaryCriteriaNumberErrors(CertificationCriterion criterion,
            List<CertificationCriterion> allCriteriaMet, List<String> complementaryCertNumbers) {
        List<String> errors = new ArrayList<String>();
        boolean hasCriterion = false;
        for (CertificationCriterion currCriteria : allCriteriaMet) {
            if (currCriteria.getId().equals(criterion.getId())) {
                hasCriterion = true;
            }
        }
        if (hasCriterion) {
            for (String currRequiredCriteria : complementaryCertNumbers) {
                boolean hasComplementaryCert = false;
                for (CertificationCriterion certCriteria : allCriteriaMet) {
                    if (certCriteria.getNumber().equals(currRequiredCriteria)) {
                        hasComplementaryCert = true;
                    }
                }

                if (!hasComplementaryCert) {
                    errors.add("Certification criterion " + Util.formatCriteriaNumber(criterion) + " was found so "
                            + getAllCriteriaWithNumber(currRequiredCriteria) + " is required but was not found.");
                }
            }
        }
        return errors;
    }

    public String getAllCriteriaWithNumber(String criterionNumber) {
        List<CertificationCriterion> allCriteriaWithNumber = criteriaService.getByNumber(criterionNumber);
        List<String> allCriteriaNumbers = allCriteriaWithNumber.stream()
                .filter(criterion -> BooleanUtils.isFalse(criterion.isRemoved()))
                .map(criterion -> Util.formatCriteriaNumber(criterion)).collect(Collectors.toList());
        return allCriteriaNumbers.stream().collect(Collectors.joining(" or "));
    }

    public List<CertificationCriterion> getAttestedCriteria(CertifiedProductSearchDetails listing) {
        return listing.getCertificationResults().stream()
                .filter(certResult -> BooleanUtils.isTrue(certResult.getSuccess()))
                .map(attestedCertResult -> attestedCertResult.getCriterion())
                .collect(Collectors.<CertificationCriterion>toList());
    }

    public boolean isEligibleForErrors(CertificationResult certResult) {
        return certResult.getCriterion() != null
                && BooleanUtils.isNotTrue(certResult.getCriterion().isRemoved())
                && BooleanUtils.isTrue(certResult.getSuccess());
    }
}
