package gov.healthit.chpl.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.exception.EntityRetrievalException;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
@NoArgsConstructor
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

        return getListing(chplProductNumber) != null;
    }

    public CertifiedProduct getListing(String chplProductNumber) {
        CertifiedProduct listing = null;
        try {
            listing = cpDao.getByChplProductNumber(chplProductNumber);
        } catch (Exception ex) {
            LOGGER.error("Could not get listing with CHPL Product Number " + chplProductNumber, ex);
            return null;
        }
        return listing;
    }
}
