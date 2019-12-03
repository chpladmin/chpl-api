package gov.healthit.chpl.validation.surveillance.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.surveillance.SurveillanceDAO;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceType;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class RequiredFieldsReviewer extends Reviewer {

    private SurveillanceDAO survDao;

    @Autowired
    public RequiredFieldsReviewer(SurveillanceDAO survDao, ErrorMessageUtil msgUtil) {
        super(msgUtil);
        this.survDao = survDao;
    }

    public void review(Surveillance surv) {
        if (surv.getStartDate() == null) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.startDateRequired"));
        }

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

        if (surv.getRequirements() == null || surv.getRequirements().size() == 0) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.requirementIsRequiredForProduct",
                    surv.getCertifiedProduct().getChplProductNumber()));
        }
    }
}
