package gov.healthit.chpl.validation.surveillance.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.surveillance.SurveillanceDAO;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.entity.surveillance.SurveillanceEntity;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class SurveillanceIdReviewer extends Reviewer {

    private SurveillanceDAO survDao;

    @Autowired
    public SurveillanceIdReviewer(SurveillanceDAO survDao,
            ErrorMessageUtil msgUtil) {
        super(msgUtil);
        this.survDao = survDao;
    }

    public void review(Surveillance surv) {
        if (!StringUtils.isEmpty(surv.getSurveillanceIdToReplace()) && surv.getCertifiedProduct() != null) {
            SurveillanceEntity existing = survDao.getSurveillanceByCertifiedProductAndFriendlyId(
                    surv.getCertifiedProduct().getId(), surv.getSurveillanceIdToReplace());
            if (existing == null) {
                surv.getErrorMessages().add(
                        msgUtil.getMessage("surveillance.surveillanceIdNotFound", surv.getSurveillanceIdToReplace()));
            }
        }
    }
}
