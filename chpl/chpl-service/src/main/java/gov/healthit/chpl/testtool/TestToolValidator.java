package gov.healthit.chpl.testtool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.criteriaattribute.rule.RuleDAO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class TestToolValidator {
    private static final int MAX_LISTINGS_IN_DELETE_ERROR_MESSAGE = 25;

    private ErrorMessageUtil errorMessageUtil;
    private RuleDAO ruleDAO;
    private TestToolDAO testToolDAO;

    @Autowired
    public TestToolValidator(ErrorMessageUtil errorMessageUtil, RuleDAO ruleDAO, TestToolDAO testToolDAO) {
        this.errorMessageUtil = errorMessageUtil;
        this.ruleDAO = ruleDAO;
        this.testToolDAO = testToolDAO;
    }

    public void validateForEdit(TestTool testTool) throws ValidationException, EntityRetrievalException {
        Set<String> messages = new HashSet<String>();

        if (StringUtils.isEmpty(testTool.getValue())) {
            messages.add(errorMessageUtil.getMessage("testTool.edit.emptyValue"));
        }

        if (CollectionUtils.isEmpty(testTool.getCriteria())) {
            messages.add(errorMessageUtil.getMessage("testTool.edit.noCriteria"));
        } else {
            if (isTestToolDuplicateOnEdit(testTool)) {
                messages.add(errorMessageUtil.getMessage("testTool.edit.duplicate"));
            }
            messages.addAll(validateCriteriaRemovedFromTestTool(testTool));
        }

        if (testTool.getRule() != null
                && ruleDAO.getRuleEntityById(testTool.getRule().getId()) == null) {
            messages.add(errorMessageUtil.getMessage("testTool.edit.notFoundRule"));
        }

        if (messages.size() > 0) {
            ValidationException e = new ValidationException(messages);
            throw e;
        }
    }

    public void validateForAdd(TestTool testTool) throws ValidationException, EntityRetrievalException {
        Set<String> messages = new HashSet<String>();

        if (StringUtils.isEmpty(testTool.getValue())) {
            messages.add(errorMessageUtil.getMessage("testTool.edit.emptyValue"));
        }

        if (CollectionUtils.isEmpty(testTool.getCriteria())) {
            messages.add(errorMessageUtil.getMessage("testTool.edit.noCriteria"));
        }

        if (isTestToolDuplicateOnAdd(testTool)) {
            messages.add(errorMessageUtil.getMessage("testTool.edit.duplicate"));
        }

        if (testTool.getRule() != null
                && ruleDAO.getRuleEntityById(testTool.getRule().getId()) == null) {
            messages.add(errorMessageUtil.getMessage("testTool.edit.notFoundRule"));
        }

        if (messages.size() > 0) {
            ValidationException e = new ValidationException(messages);
            throw e;
        }

    }

    public void validateForDelete(TestTool testTool) throws ValidationException {
        List<CertifiedProductDetailsDTO> listings = new ArrayList<CertifiedProductDetailsDTO>();
        try {
            listings = testToolDAO.getCertifiedProductsByTestTool(testTool);
        } catch (EntityRetrievalException ex) {
            throw new ValidationException(ex.getMessage());
        }

        if (!CollectionUtils.isEmpty(listings)) {
            String message = errorMessageUtil.getMessage("testTool.delete.listingsExist",
                    listings.size(),
                    listings.size() > 1 ? "s" : "");
            if (listings.size() < MAX_LISTINGS_IN_DELETE_ERROR_MESSAGE) {
                message = message + ": "
                        + listings.stream()
                            .map(listing -> listing.getChplProductNumber())
                            .collect(Collectors.joining(", "));
            }

            ValidationException e = new ValidationException(message);
            throw e;
        }
    }

    private Set<String> validateCriteriaRemovedFromTestTool(TestTool testTool) {
        Set<String> messages = new HashSet<String>();
        TestTool origTestTool = testToolDAO.getById(testTool.getId());

        getCriteriaRemovedFromTestTool(testTool, origTestTool).stream()
                .forEach(crit -> {
                    List<CertifiedProductDetailsDTO> listings = new ArrayList<CertifiedProductDetailsDTO>();
                    try {
                        listings = testToolDAO.getCertifiedProductsByTestToolAndCriteria(origTestTool, crit);
                    } catch (EntityRetrievalException ex) {
                        messages.add(ex.getMessage());
                    }

                    if (!CollectionUtils.isEmpty(listings)) {
                        String message = errorMessageUtil.getMessage("testTool.edit.deletedCriteria.listingsExist",
                                CertificationCriterionService.formatCriteriaNumber(crit),
                                listings.size(),
                                listings.size() > 1 ? "s" : "");
                        if (listings.size() < MAX_LISTINGS_IN_DELETE_ERROR_MESSAGE) {
                                message = message + ": "
                                        + listings.stream()
                                        .map(listing -> listing.getChplProductNumber())
                                        .collect(Collectors.joining(", "));
                        }
                        messages.add(message);
                    }
                });
        return messages;
    }

    private boolean isTestToolDuplicateOnEdit(TestTool testTool) throws EntityRetrievalException {
        String updatedCitationText = testTool.getRegulatoryTextCitation() != null ? testTool.getRegulatoryTextCitation() : "";

        return testToolDAO.getAllTestToolCriteriaMaps().stream()
                .filter(map -> {
                        String origCitationText = map.getTestTool().getRegulatoryTextCitation() != null ? map.getTestTool().getRegulatoryTextCitation() : "";

                        return map.getTestTool().getValue().equalsIgnoreCase(testTool.getValue())
                                && origCitationText.equalsIgnoreCase(updatedCitationText)
                                && !map.getTestTool().getId().equals(testTool.getId());
                })
                .findAny()
                .isPresent();
    }

    private boolean isTestToolDuplicateOnAdd(TestTool testTool) throws EntityRetrievalException {
        String updatedCitationText = testTool.getRegulatoryTextCitation() != null ? testTool.getRegulatoryTextCitation() : "";

        return testToolDAO.getAllTestToolCriteriaMaps().stream()
                .filter(map -> {
                        String origCitationText = map.getTestTool().getRegulatoryTextCitation() != null
                                ? map.getTestTool().getRegulatoryTextCitation()
                                : "";

                        return map.getTestTool().getValue().equalsIgnoreCase(testTool.getValue())
                                && origCitationText.equalsIgnoreCase(updatedCitationText);
                })
                .findAny()
                .isPresent();
    }

    private List<CertificationCriterion> getCriteriaRemovedFromTestTool(TestTool updatedTestTool, TestTool originalTestTool) {
        return  subtractLists(originalTestTool.getCriteria(), updatedTestTool.getCriteria());
    }

    private List<CertificationCriterion> subtractLists(List<CertificationCriterion> listA, List<CertificationCriterion> listB) {
        Predicate<CertificationCriterion> notInListB = certFromA -> !listB.stream()
                .anyMatch(cert -> certFromA.equals(cert));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }
}
