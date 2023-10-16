package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;

@Component("sedRelatedCriteriaReviewer")
public class SedRelatedCriteriaReviewer {
    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;
    private CertificationCriterion g3;
    private List<CertificationCriterion> sedRelatedCriteria;

    @Autowired
    public SedRelatedCriteriaReviewer(ValidationUtils validationUtils, CertificationCriterionService criteriaService,
            ErrorMessageUtil msgUtil) {
        this.validationUtils = validationUtils;
        this.msgUtil = msgUtil;
        g3 = criteriaService.get(Criteria2015.G_3);
        sedRelatedCriteria = Stream.of(
                criteriaService.get(Criteria2015.A_1),
                criteriaService.get(Criteria2015.A_2),
                criteriaService.get(Criteria2015.A_3),
                criteriaService.get(Criteria2015.A_4),
                criteriaService.get(Criteria2015.A_5),
                criteriaService.get(Criteria2015.A_6),
                criteriaService.get(Criteria2015.A_7),
                criteriaService.get(Criteria2015.A_8),
                criteriaService.get(Criteria2015.A_9),
                criteriaService.get(Criteria2015.A_14),
                criteriaService.get(Criteria2015.B_2_OLD),
                criteriaService.get(Criteria2015.B_2_CURES),
                criteriaService.get(Criteria2015.B_3_OLD),
                criteriaService.get(Criteria2015.B_3_CURES))
                .collect(Collectors.toList());
    }

    public void review(CertifiedProductSearchDetails listing) {
        reviewG3IsPresentIfRequired(listing);
        reviewG3IsNotPresentIfNotAllowed(listing);
    }

    private void reviewG3IsPresentIfRequired(CertifiedProductSearchDetails listing) {
        List<CertificationCriterion> attestedCriteria = validationUtils.getAttestedCriteria(listing);
        List<CertificationCriterion> presentAttestedSedCriteria = attestedCriteria.stream()
                .filter(criterion -> criterion.isRemoved() == null || criterion.isRemoved().equals(Boolean.FALSE))
                .filter(criterion -> certIdIsInCertList(criterion, sedRelatedCriteria.stream().map(sedCriterion -> sedCriterion.getId()).collect(Collectors.toList())))
                .collect(Collectors.<CertificationCriterion> toList());
        boolean hasG3 = validationUtils.hasCriterion(g3, attestedCriteria);

        if (presentAttestedSedCriteria != null && presentAttestedSedCriteria.size() > 0 && !hasG3) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.criteriaRequired", Util.formatCriteriaNumber(g3)));
        }
    }

    private void reviewG3IsNotPresentIfNotAllowed(CertifiedProductSearchDetails listing) {
        List<CertificationCriterion> attestedCriteria = validationUtils.getAttestedCriteria(listing);
        List<CertificationCriterion> presentAttestedSedCriteria = attestedCriteria.stream()
                .filter(criterion -> criterion.isRemoved() == null || criterion.isRemoved().equals(Boolean.FALSE))
                .filter(criterion -> certIdIsInCertList(criterion, sedRelatedCriteria.stream().map(sedCriterion -> sedCriterion.getId()).collect(Collectors.toList())))
                .collect(Collectors.<CertificationCriterion> toList());
        boolean hasG3 = validationUtils.hasCriterion(g3, attestedCriteria);

        if ((presentAttestedSedCriteria == null || presentAttestedSedCriteria.size() == 0)
                && hasG3) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.g3NotAllowed"));
        }
    }

    private boolean certIdIsInCertList(CertificationCriterion criterion, List<Long> certIdList) {
        return certIdList.stream()
                .filter(certId -> criterion.getId().equals(certId))
                .findAny().isPresent();
    }
}
