package gov.healthit.chpl.validation.surveillance.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class ChplProductNumberReviewer extends Reviewer {

    private CertifiedProductDAO cpDao;

    @Autowired
    public ChplProductNumberReviewer(CertifiedProductDAO cpDao, ErrorMessageUtil msgUtil) {
        super(msgUtil);
        this.cpDao = cpDao;
    }

    public void review(Surveillance surv) {
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
            if (chplId.startsWith("CHP-")) {
                try {
                    CertifiedProductDTO chplProduct = cpDao.getByChplNumber(chplId);
                    if (chplProduct != null) {
                        cpDetails = cpDao.getDetailsById(chplProduct.getId());
                        if (cpDetails != null) {
                            surv.setCertifiedProduct(new CertifiedProduct(cpDetails));
                        } else {
                            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.certifiedProductIdNotFound",
                                    chplId, chplProduct.getId()));
                        }
                    } else {
                        surv.getErrorMessages().add(msgUtil.getMessage("surveillance.productIdNotFound", chplId));
                    }
                } catch (final EntityRetrievalException ex) {
                    surv.getErrorMessages()
                            .add(msgUtil.getMessage("surveillance.productDetailsRetrievalException", chplId));
                }
            } else {
                try {
                    cpDetails = cpDao.getByChplUniqueId(chplId);
                    if (cpDetails != null) {
                        surv.setCertifiedProduct(new CertifiedProduct(cpDetails));
                    } else {
                        surv.getErrorMessages().add(msgUtil.getMessage("surveillance.productUniqueIdNotFound", chplId));
                    }
                } catch (final EntityRetrievalException ex) {
                    surv.getErrorMessages()
                            .add(msgUtil.getMessage("surveillance.productDetailsRetrievalException", chplId));
                }
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
}
