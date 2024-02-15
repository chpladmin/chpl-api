package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dao.CertificationCriterionAttributeDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component
public class CodeSetDateAllowedByCriteriaReviewer implements Reviewer {

    private CertificationCriterionAttributeDAO certificationCriterionAttributeDAO;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public CodeSetDateAllowedByCriteriaReviewer(CertificationCriterionAttributeDAO certificationCriterionAttributeDAO, ErrorMessageUtil errorMessageUtil) {
        this.certificationCriterionAttributeDAO = certificationCriterionAttributeDAO;
        this.errorMessageUtil = errorMessageUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        List<CertificationCriterion> criterionAllowingCodeSetDates = certificationCriterionAttributeDAO.getCriteriaForCodeSetDates();

        listing.getCertificationResults().stream()
            .forEach(cr -> {
                if (CollectionUtils.isNotEmpty(cr.getCodeSetDates()) && !areCodeSetDatesAllowedForCriteria(cr.getCriterion(), criterionAllowingCodeSetDates)) {
                    listing.addBusinessErrorMessage(errorMessageUtil.getMessage("codeSetDate.notAllowed", cr.getCriterion().getNumber()));
                }
            });
    }

    private boolean areCodeSetDatesAllowedForCriteria(CertificationCriterion criterionToCheck, List<CertificationCriterion> criterionAllowingCodeSetDates) {
        return criterionAllowingCodeSetDates.stream()
                .filter(allowedCriterion -> allowedCriterion.getId().equals(criterionToCheck.getId()))
                .findAny()
                .isPresent();
    }
}
