package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.codesetdate.CodeSet;
import gov.healthit.chpl.codesetdate.CodeSetDAO;
import gov.healthit.chpl.dao.CertificationCriterionAttributeDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component
public class CodeSetAllowedByCriteriaReviewer implements Reviewer {

    private CertificationCriterionAttributeDAO certificationCriterionAttributeDAO;
    private CodeSetDAO codeSetDAO;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public CodeSetAllowedByCriteriaReviewer(CertificationCriterionAttributeDAO certificationCriterionAttributeDAO, CodeSetDAO codeSetDAO,
            ErrorMessageUtil errorMessageUtil) {

        this.certificationCriterionAttributeDAO = certificationCriterionAttributeDAO;
        this.codeSetDAO = codeSetDAO;
        this.errorMessageUtil = errorMessageUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        List<CertificationCriterion> criterionAllowingCodeSets = certificationCriterionAttributeDAO.getCriteriaForCodeSets();
        Map<Long, List<CodeSet>> mapOfCodeSets = codeSetDAO.getCodeSetCriteriaMaps();

        listing.getCertificationResults().forEach(cr -> {
                if (CollectionUtils.isNotEmpty(cr.getCodeSets())) {
                        if (!areCodeSetsAllowedForCriteria(cr.getCriterion(), criterionAllowingCodeSets)) {
                            listing.addBusinessErrorMessage(errorMessageUtil.getMessage("codeSet.notAllowed", cr.getCriterion().getNumber()));
                        } else {
                            cr.getCodeSets().forEach(csd -> {
                                if (isCodeSetValidForCriteria(csd.getCodeSet(), mapOfCodeSets.get(cr.getCriterion().getId()))) {
                                    listing.addBusinessErrorMessage(errorMessageUtil.getMessage("codeSet.notAllowedForCriteria",
                                            csd.getCodeSet().getRequiredDay(),
                                            cr.getCriterion().getNumber()));
                                }
                            });
                        }
                }
            });
    }

    private boolean areCodeSetsAllowedForCriteria(CertificationCriterion criterionToCheck, List<CertificationCriterion> criterionAllowingCodeSets) {
        return criterionAllowingCodeSets.stream()
                .filter(allowedCriterion -> allowedCriterion.getId().equals(criterionToCheck.getId()))
                .findAny()
                .isPresent();
    }

    private boolean isCodeSetValidForCriteria(CodeSet codeSetToCheck, List<CodeSet> codeSetsAllowedForCriteria) {
        return codeSetsAllowedForCriteria.stream()
                .filter(csd -> csd.getId().equals(codeSetToCheck.getId()))
                .findAny()
                .isPresent();
    }
}
