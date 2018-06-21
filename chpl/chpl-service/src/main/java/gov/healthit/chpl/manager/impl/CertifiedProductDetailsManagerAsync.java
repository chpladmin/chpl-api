package gov.healthit.chpl.manager.impl;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.dao.CQMResultDetailsDAO;
import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.dto.CQMResultDetailsDTO;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Service("certifiedProductDetailsManagerAsync")
public class CertifiedProductDetailsManagerAsync {
    private static final Logger LOGGER = LogManager.getLogger(CertifiedProductDetailsManagerAsync.class);

    @Async
    public Future<List<CertifiedProductDetailsDTO>> getCertifiedProductChildren(
            final ListingGraphDAO listingGraphDao, final Long id) {
        return getFutureCertifiedProductChildren(listingGraphDao, id);
    }

    public Future<List<CertifiedProductDetailsDTO>> getFutureCertifiedProductChildren(
            final ListingGraphDAO listingGraphDao, final Long id) {

        LOGGER.debug("Starting the retrieval of the Children");
        Date start = new Date();
        List<CertifiedProductDetailsDTO> children = listingGraphDao.getChildren(id);
        Date end = new Date();
        LOGGER.debug("Time to retrieve Children: " + (end.getTime() - start.getTime()));
        return new AsyncResult<List<CertifiedProductDetailsDTO>>(children);
    }

    @Async
    public Future<List<CertifiedProductDetailsDTO>> getCertifiedProductParent(
            final ListingGraphDAO listingGraphDao, final Long id) {
        return getFutureCertifiedProductParent(listingGraphDao, id);
    }

    public Future<List<CertifiedProductDetailsDTO>> getFutureCertifiedProductParent(
            final ListingGraphDAO listingGraphDao, final Long id) {
        LOGGER.debug("Starting the retrieval of the Parents");
        Date start = new Date();
        List<CertifiedProductDetailsDTO> children = listingGraphDao.getParents(id);
        Date end = new Date();
        LOGGER.debug("Time to retrieve Parents: " + (end.getTime() - start.getTime()));
        return new AsyncResult<List<CertifiedProductDetailsDTO>>(children);
    }

    @Async
    public Future<List<CertificationResultDetailsDTO>> getCertificationResultDetailsDTOs(
            final CertificationResultDetailsDAO certificationResultDetailsDAO, final Long id) {
        return getFutureCertificationResultDetailsDTOs(certificationResultDetailsDAO, id);
    }

    public Future<List<CertificationResultDetailsDTO>> getFutureCertificationResultDetailsDTOs(
            final CertificationResultDetailsDAO certificationResultDetailsDAO, final Long id) {

        LOGGER.debug("Starting the retrieval of the Certification Result Details");
        Date start = new Date();
        List<CertificationResultDetailsDTO> certificationResultDetailsDTOs = null;
        try {
            certificationResultDetailsDTOs =
                    certificationResultDetailsDAO.getCertificationResultDetailsByCertifiedProductId(id);
        } catch (EntityRetrievalException e) {
            e.printStackTrace();
        }
        Date end = new Date();
        LOGGER.debug("Time to populate Certification Result Details: " + (end.getTime() - start.getTime()));

        return new AsyncResult<List<CertificationResultDetailsDTO>>(certificationResultDetailsDTOs);
    }

    @Async
    public Future<List<CQMResultDetailsDTO>> getCqmResultDetailsDTOs(
            final CQMResultDetailsDAO cqmResultDetailsDAO, final Long id) {
        return getFutureCqmResultDetailsDTOs(cqmResultDetailsDAO, id);
    }

    public Future<List<CQMResultDetailsDTO>> getFutureCqmResultDetailsDTOs(
            final CQMResultDetailsDAO cqmResultDetailsDAO, final Long id) {

        LOGGER.debug("Starting the retrieval of the CQM Result Details");
        Date start = new Date();
        List<CQMResultDetailsDTO> cqmResultDetailsDTOs = null;
        try {
            cqmResultDetailsDTOs = cqmResultDetailsDAO.getCQMResultDetailsByCertifiedProductId(id);
        } catch (EntityRetrievalException e) {
            e.printStackTrace();
        }
        Date end = new Date();
        LOGGER.debug("Time to populate CQM Result Details: " + (end.getTime() - start.getTime()));

        return new AsyncResult<List<CQMResultDetailsDTO>>(cqmResultDetailsDTOs);
    }
}
