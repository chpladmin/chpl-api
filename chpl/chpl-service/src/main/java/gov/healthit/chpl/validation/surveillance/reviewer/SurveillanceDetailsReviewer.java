package gov.healthit.chpl.validation.surveillance.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.compliance.surveillance.SurveillanceDAO;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceType;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class SurveillanceDetailsReviewer implements ReadReviewer {

    private ErrorMessageUtil msgUtil;
    private SurveillanceDAO survDao;

    @Autowired
    public SurveillanceDetailsReviewer(SurveillanceDAO survDao,
           ErrorMessageUtil msgUtil) {
        this.survDao = survDao;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(Surveillance surv) {
        checkStartDayExists(surv);
        checkSurveillanceTypeValidity(surv);
        checkRandomizedSitesValidity(surv);
        checkSurveillanceEndDayRequired(surv);
        checkStartAndEndDayValidity(surv);
    }


    private void checkStartDayExists(Surveillance surv) {
        if (surv.getStartDay() == null) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.startDateRequired"));
        }
    }

    private void checkSurveillanceTypeValidity(Surveillance surv) {
        if (surv.getType() == null) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.typeRequired"));
        } else if (surv.getType().getId() == null || surv.getType().getId().longValue() <= 0) {
            SurveillanceType survType = survDao.findSurveillanceType(surv.getType().getName());
            if (survType == null) {
                surv.getErrorMessages().add(msgUtil.getMessage("surveillance.typeMismatch", surv.getType().getName()));
            } else {
                surv.setType(survType);
            }
        } else {
            SurveillanceType survType = survDao.findSurveillanceType(surv.getType().getId());
            if (survType == null) {
                surv.getErrorMessages().add(msgUtil.getMessage("surveillance.typeNotFound", surv.getType().getId()));
            } else {
                surv.setType(survType);
            }
        }
    }

    private void checkRandomizedSitesValidity(Surveillance surv) {
        // randomized surveillance requires number of sites used but
        // any other type of surveillance should not have that value
        if (surv.getType() != null && surv.getType().getName() != null
                && surv.getType().getName().equalsIgnoreCase(SurveillanceType.RANDOMIZED)) {
            if (surv.getRandomizedSitesUsed() == null || surv.getRandomizedSitesUsed().intValue() < 0) {
                surv.getErrorMessages().add(msgUtil.getMessage("surveillance.randomizedNonzeroValue"));
            }
        } else if (surv.getType() != null && surv.getType().getName() != null
                && !surv.getType().getName().equalsIgnoreCase(SurveillanceType.RANDOMIZED)) {
            if (surv.getRandomizedSitesUsed() != null && surv.getRandomizedSitesUsed().intValue() >= 0) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.randomizedSitesNotApplicable", surv.getType().getName()));
            }
        }
    }

    private void checkSurveillanceEndDayRequired(Surveillance surv) {
        boolean survRequiresCloseDate = true;
        for (SurveillanceRequirement req : surv.getRequirements()) {
            for (SurveillanceNonconformity nc : req.getNonconformities()) {
                survRequiresCloseDate = survRequiresCloseDate && doesNonconformityRequireCloseDate(nc);
            }
        }
        if (survRequiresCloseDate && surv.getEndDay() == null) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.endDateRequiredNoOpenNonConformities"));
        }
    }

    private boolean doesNonconformityRequireCloseDate(SurveillanceNonconformity nc) {
        return nc.getNonconformityCloseDay() != null;
    }

    private void checkStartAndEndDayValidity(Surveillance surv) {
        if (surv.getStartDay() != null && surv.getEndDay() != null
                && surv.getEndDay().isBefore(surv.getStartDay())) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.dateEndNotBeforeDateStart"));
        }
    }
}
