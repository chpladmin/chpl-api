package gov.healthit.chpl.caching;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.authentication.AdminUserAuthenticator;
import gov.healthit.chpl.auth.domain.Authority;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.PendingCertifiedProductDAO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertificationIdManager;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;
import gov.healthit.chpl.manager.PendingCertifiedProductManager;
import gov.healthit.chpl.manager.SearchMenuManager;

@Component
public class AsynchronousCacheInitialization {
    private static final Logger LOGGER = LogManager.getLogger(AsynchronousCacheInitialization.class);

    @Autowired
    private CertificationIdManager certificationIdManager;
    @Autowired
    private SearchMenuManager searchMenuManager;
    @Autowired
    private CertifiedProductSearchManager certifiedProductSearchManager;
    @Autowired
    private PendingCertifiedProductManager pcpManager;
    @Autowired
    private CertificationBodyDAO certificationBodyDAO;

    @Async
    @Transactional
    public Future<Boolean> initializeSearchOptions() throws EntityRetrievalException {
        LOGGER.info("Starting cache initialization for SearchViewController.getPopulateSearchData()");
        searchMenuManager.getCertBodyNames();
        searchMenuManager.getEditionNames(false);
        searchMenuManager.getEditionNames(true);
        searchMenuManager.getCertificationStatuses();
        searchMenuManager.getPracticeTypeNames();
        searchMenuManager.getClassificationNames();
        searchMenuManager.getProductNames();
        searchMenuManager.getDeveloperNames();
        searchMenuManager.getCQMCriterionNumbers(false);
        searchMenuManager.getCQMCriterionNumbers(true);
        searchMenuManager.getCertificationCriterionNumbers(false);
        searchMenuManager.getCertificationCriterionNumbers(true);
        LOGGER.info("Finished cache initialization for SearchViewController.getPopulateSearchData()");
        return new AsyncResult<>(true);
    }

    @Async
    @Transactional
    public Future<Boolean> initializeBasicSearch() throws IOException, EntityRetrievalException, InterruptedException {
        LOGGER.info("Starting cache initialization for CertifiedProductSearchManager.search()");
        certifiedProductSearchManager.search();
        LOGGER.info("Finished cache initialization for CertifiedProductSearchManager.search()");
        return new AsyncResult<>(true);
    }

    @Async
    @Transactional
    public Future<Boolean> initializeCertificationIdsGetAll()
            throws IOException, EntityRetrievalException, InterruptedException {
        LOGGER.info("Starting cache initialization for CertificationIdManager.getAll()");
        certificationIdManager.getAllCached();
        LOGGER.info("Finished cache initialization for CertificationIdManager.getAll()");
        return new AsyncResult<>(true);
    }

    @Async
    @Transactional
    public Future<Boolean> initializeCertificationIdsGetAllWithProducts()
            throws IOException, EntityRetrievalException, InterruptedException {
        LOGGER.info("Starting cache initialization for CertificationIdManager.getAllWithProducts()");
        certificationIdManager.getAllWithProductsCached();
        LOGGER.info("Finished cache initialization for CertificationIdManager.getAllWithProducts()");
        return new AsyncResult<>(true);
    }

    @Async
    @Transactional
    public Future<Boolean> initializeFindPendingListingsByAcbId() throws IOException, EntityRetrievalException, InterruptedException {
        LOGGER.info("Starting cache initialization for PendingCertifiedProductDAO.findByAcbId()");
        List<CertificationBodyDTO> acbs = certificationBodyDAO.findAllActive();
        //assume the admin role to query for pending certified products
        Authentication actor = new AdminUserAuthenticator();
        SecurityContextHolder.getContext().setAuthentication(actor);
        for (CertificationBodyDTO dto : acbs) {
            pcpManager.getPendingCertifiedProductsCached(dto.getId());
        }
        SecurityContextHolder.getContext().setAuthentication(null);
        LOGGER.info("Finished cache initialization for PendingCertifiedProductDAO.findByAcbId()");
        return new AsyncResult<>(true);
    }
}
