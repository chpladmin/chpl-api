package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.codesetdate.CodeSetDate;
import gov.healthit.chpl.codesetdate.CodeSetDateDAO;
import gov.healthit.chpl.dao.CertificationCriterionAttributeDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component
public class CodeSetDateAllowedByCriteriaReviewer implements Reviewer {

    private CertificationCriterionAttributeDAO certificationCriterionAttributeDAO;
    private CodeSetDateDAO codeSetDateDAO;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public CodeSetDateAllowedByCriteriaReviewer(CertificationCriterionAttributeDAO certificationCriterionAttributeDAO, CodeSetDateDAO codeSetDateDAO,
            ErrorMessageUtil errorMessageUtil) {

        this.certificationCriterionAttributeDAO = certificationCriterionAttributeDAO;
        this.codeSetDateDAO = codeSetDateDAO;
        this.errorMessageUtil = errorMessageUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        List<CertificationCriterion> criterionAllowingCodeSetDates = certificationCriterionAttributeDAO.getCriteriaForCodeSetDates();
        Map<Long, List<CodeSetDate>> mapOfCodeSetDates = codeSetDateDAO.getCodeSetDateCriteriaMaps();

        listing.getCertificationResults().forEach(cr -> {
                if (CollectionUtils.isNotEmpty(cr.getCodeSetDates())) {
                        if (!areCodeSetDatesAllowedForCriteria(cr.getCriterion(), criterionAllowingCodeSetDates)) {
                            listing.addBusinessErrorMessage(errorMessageUtil.getMessage("codeSetDate.notAllowed", cr.getCriterion().getNumber()));
                        } else {
                            cr.getCodeSetDates().forEach(csd -> {
                                if (isCodeSetDateValidForCriteria(csd.getCodeSetDate(), mapOfCodeSetDates.get(cr.getCriterion().getId()))) {
                                    listing.addBusinessErrorMessage(errorMessageUtil.getMessage("codeSetDate.notAllowedForCriteria",
                                            csd.getCodeSetDate().getRequiredDay(),
                                            cr.getCriterion().getNumber()));
                                }
                            });
                        }
                }
            });
    }

    private boolean areCodeSetDatesAllowedForCriteria(CertificationCriterion criterionToCheck, List<CertificationCriterion> criterionAllowingCodeSetDates) {
        return criterionAllowingCodeSetDates.stream()
                .filter(allowedCriterion -> allowedCriterion.getId().equals(criterionToCheck.getId()))
                .findAny()
                .isPresent();
    }

    private boolean isCodeSetDateValidForCriteria(CodeSetDate codeSetDateToCheck, List<CodeSetDate> codeSetDatesAllowedForCriteria) {
        return codeSetDatesAllowedForCriteria.stream()
                .filter(csd -> csd.getId().equals(codeSetDateToCheck))
                .findAny()
                .isPresent();
    }
}
