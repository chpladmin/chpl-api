package gov.healthit.chpl.validation.surveillance.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.surveillance.SurveillanceDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceType;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.surveillance.SurveillanceEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class SurveillanceDetailsReviewer implements Reviewer {

    private ErrorMessageUtil msgUtil;
    private CertifiedProductDAO cpDao;
    private SurveillanceDAO survDao;
    private ChplProductNumberUtil chplProductNumberUtil;

    @Autowired
    public SurveillanceDetailsReviewer(CertifiedProductDAO cpDao, SurveillanceDAO survDao,
            ChplProductNumberUtil chplProductNumberUtil, ErrorMessageUtil msgUtil) {
        this.cpDao = cpDao;
        this.survDao = survDao;
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(Surveillance surv) {
        checkChplProductNumberValidity(surv);
        checkSurveillanceIdValidity(surv);
        checkStartDayExists(surv);
        checkSurveillanceTypeValidity(surv);
        checkRandomizedSitesValidity(surv);
        checkSurveillanceEndDayRequired(surv);
    }

    private void checkChplProductNumberValidity(Surveillance surv) {
        CertifiedProductDetailsDTO cpDetails = null;

        // make sure chpl id is valid
        if (surv.getCertifiedProduct() == null) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.nullCertifiedProduct"));
        } else if (surv.getCertifiedProduct().getId() == null
                && surv.getCertifiedProduct().getChplProductNumber() == null) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.nullCertifiedProductAndChplNumber"));
        } else if (surv.getCertifiedProduct().getId() == null || surv.getCertifiedProduct().getId().longValue() <= 0) {
            // the id is null, try to lookup by unique chpl number
            String chplId = surv.getCertifiedProduct().getChplProductNumber();
            CertifiedProduct listing = chplProductNumberUtil.getListing(chplId);
            if (listing != null) {
                surv.setCertifiedProduct(listing);
            } else {
                surv.getErrorMessages().add(msgUtil.getMessage("surveillance.productUniqueIdNotFound", chplId));
            }
        } else if (surv.getCertifiedProduct().getId() != null) {
            try {
                cpDetails = cpDao.getDetailsById(surv.getCertifiedProduct().getId());
                surv.setCertifiedProduct(new CertifiedProduct(cpDetails));
            } catch (final EntityRetrievalException ex) {
                surv.getErrorMessages().add(msgUtil.getMessage("surveillance.detailsNotFoundForCertifiedProduct",
                        surv.getCertifiedProduct().getId()));
            }
        }
    }

    private void checkSurveillanceIdValidity(Surveillance surv) {
        if (!StringUtils.isEmpty(surv.getSurveillanceIdToReplace()) && surv.getCertifiedProduct() != null) {
            SurveillanceEntity existing = survDao.getSurveillanceByCertifiedProductAndFriendlyId(
                    surv.getCertifiedProduct().getId(), surv.getSurveillanceIdToReplace());
            if (existing == null) {
                surv.getErrorMessages().add(
                        msgUtil.getMessage("surveillance.surveillanceIdNotFound", surv.getSurveillanceIdToReplace()));
            }
        }
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
}
