package gov.healthit.chpl.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@NoArgsConstructor
@Log4j2
public class CertifiedProductUtil {
    private CertifiedProductDAO cpDao;

    @Autowired
    public CertifiedProductUtil(CertifiedProductDAO cpDao) {
        this.cpDao = cpDao;
    }

    public boolean chplIdExists(String chplProductNumber) throws EntityRetrievalException {
        if (StringUtils.isEmpty(chplProductNumber)) {
            return false;
        }

        boolean exists = false;
        if (chplProductNumber.startsWith(ChplProductNumberUtil.LEGACY_ID_BEGIN)) {
            CertifiedProductDTO existing = cpDao.getByChplNumber(chplProductNumber);
            if (existing != null) {
                exists = true;
            }
        } else {
            try {
                CertifiedProductDetailsDTO existing = cpDao.getByChplUniqueId(chplProductNumber);
                if (existing != null) {
                    exists = true;
                }
            } catch (final EntityRetrievalException ex) {
                LOGGER.error("Could not look up " + chplProductNumber, ex);
            }
        }
        return exists;
    }

    public CertifiedProduct getListing(String chplProductNumber) {
        CertifiedProduct listing = null;
        if (chplProductNumber.startsWith(ChplProductNumberUtil.LEGACY_ID_BEGIN)) {
            try {
                CertifiedProductDTO chplProduct = cpDao.getByChplNumber(chplProductNumber);
                if (chplProduct != null) {
                    CertifiedProductDetailsDTO cpDetails = cpDao.getDetailsById(chplProduct.getId());
                    if (cpDetails != null) {
                        listing = new CertifiedProduct(cpDetails);
                    }
                }
            } catch (final EntityRetrievalException ex) {
                LOGGER.error("Could not look up " + chplProductNumber, ex);
            }
        } else {
            try {
                CertifiedProductDetailsDTO cpDetails = cpDao.getByChplUniqueId(chplProductNumber);
                if (cpDetails != null) {
                    listing = new CertifiedProduct(cpDetails);
                }
            } catch (final EntityRetrievalException ex) {
                LOGGER.error("Could not look up " + chplProductNumber, ex);
            }
        }
        return listing;
    }
}
