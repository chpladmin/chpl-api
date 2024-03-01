package gov.healthit.chpl.upload.listing;

import static gov.healthit.chpl.util.LambdaExceptionUtil.rethrowConsumer;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.caching.ListingSearchCacheRefresh;
import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.dao.CQMResultDAO;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.CertificationStatusEventDAO;
import gov.healthit.chpl.dao.CertifiedProductAccessibilityStandardDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.CertifiedProductQmsStandardDAO;
import gov.healthit.chpl.dao.CertifiedProductTargetedUserDAO;
import gov.healthit.chpl.dao.CertifiedProductTestingLabDAO;
import gov.healthit.chpl.dao.CuresUpdateEventDAO;
import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductUcdProcess;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.CuresUpdateEventDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.functionalitytested.CertificationResultFunctionalityTestedDAO;
import gov.healthit.chpl.listing.measure.ListingMeasureDAO;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.manager.ProductVersionManager;
import gov.healthit.chpl.service.CuresUpdateService;
import gov.healthit.chpl.standard.CertificationResultStandardDAO;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ListingConfirmationManager {
    private DeveloperManager developerManager;
    private ProductManager productManager;
    private ProductVersionManager versionManager;
    private CertifiedProductDAO cpDao;
    private CertifiedProductTestingLabDAO cpTestingLabDao;
    private CertifiedProductQmsStandardDAO cpQmsDao;
    private CertifiedProductAccessibilityStandardDAO cpAccStdDao;
    private CertifiedProductTargetedUserDAO cpTargetedUserDao;
    private ListingGraphDAO listingGraphDao;
    private ListingMeasureDAO listingMeasureDao;
    private CertificationResultDAO certResultDao;
    private CertificationResultFunctionalityTestedDAO certResultFuncTestedDao;
    private CertificationResultStandardDAO certResultStandardDao;
    private CQMResultDAO cqmResultDao;
    private CertificationStatusEventDAO statusEventDao;
    private CuresUpdateEventDAO curesUpdateDao;
    private CuresUpdateService curesUpdateService;
    private ActivityManager activityManager;
    private CertifiedProductDetailsManager cpDetailsManager;
    private FF4j ff4j;

    private CertificationStatus activeStatus;

    @Autowired
    @SuppressWarnings("checkstyle:parameternumber")
    public ListingConfirmationManager(DeveloperManager developerManager,
            ProductManager productManager, ProductVersionManager versionManager,
            CertifiedProductDAO cpDao, CertifiedProductTestingLabDAO cpTestingLabDao,
            CertifiedProductQmsStandardDAO cpQmsDao,
            CertifiedProductAccessibilityStandardDAO cpAccStdDao,
            CertifiedProductTargetedUserDAO cpTargetedUserDao,
            ListingGraphDAO listingGraphDao, ListingMeasureDAO listingMeasureDao,
            CertificationResultDAO certResultDao, CertificationResultFunctionalityTestedDAO certResultFuncTestedDao,
            CQMResultDAO cqmResultDao,
            CertificationStatusDAO certStatusDao,  CertificationStatusEventDAO statusEventDao,
            CuresUpdateEventDAO curesUpdateDao,
            CertifiedProductDetailsManager cpDetailsManager, CuresUpdateService curesUpdateService,
            ActivityManager activityManager, CertificationResultStandardDAO certResultStandardDao,
            FF4j ff4j) {
        this.developerManager = developerManager;
        this.productManager = productManager;
        this.versionManager = versionManager;
        this.cpDao = cpDao;
        this.cpTestingLabDao = cpTestingLabDao;
        this.cpQmsDao = cpQmsDao;
        this.cpAccStdDao = cpAccStdDao;
        this.cpTargetedUserDao = cpTargetedUserDao;
        this.listingGraphDao = listingGraphDao;
        this.listingMeasureDao = listingMeasureDao;
        this.certResultDao = certResultDao;
        this.certResultFuncTestedDao = certResultFuncTestedDao;
        this.cqmResultDao = cqmResultDao;
        this.statusEventDao = statusEventDao;
        this.curesUpdateDao = curesUpdateDao;
        this.cpDetailsManager = cpDetailsManager;
        this.curesUpdateService = curesUpdateService;
        this.activityManager = activityManager;
        this.certResultStandardDao = certResultStandardDao;
        this.ff4j = ff4j;

        activeStatus = certStatusDao.getByStatusName(CertificationStatusType.Active.toString());
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.CertifiedProductDomainPermissions).CREATE, #listing)")
    @CacheEvict(value = {
            CacheNames.ALL_DEVELOPERS, CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED,
            CacheNames.COLLECTIONS_DEVELOPERS,
            CacheNames.COLLECTIONS_LISTINGS,
            CacheNames.QUESTIONABLE_ACTIVITIES
    }, allEntries = true)
    @ListingSearchCacheRefresh
    public CertifiedProductSearchDetails create(CertifiedProductSearchDetails listing)
        throws EntityCreationException, EntityRetrievalException, JsonProcessingException, ValidationException {
        if (listing.getDeveloper().getId() == null) {
            //create developer, set developer ID in listing
            Long developerId = developerManager.create(listing.getDeveloper());
            listing.getDeveloper().setId(developerId);
        }
        if (listing.getProduct().getId() == null) {
            //create product, set product ID in listing
            Long productId = productManager.create(listing.getDeveloper().getId(), listing.getProduct());
            listing.getProduct().setId(productId);
        }
        if (listing.getVersion().getId() == null) {
            //create version, set version ID in listing
            Long versionId = versionManager.create(listing.getProduct().getId(), listing.getVersion());
            listing.getVersion().setId(versionId);
        }
        Long createdListingId = cpDao.create(listing);
        listing.setId(createdListingId);

        saveListingTestingLabMappings(listing);
        saveListingQmsStandardMappings(listing);
        saveListingAccessibiltyStandardMappings(listing);
        saveListingTargetedUserMappings(listing);
        saveListingIcsMappings(listing);
        saveListingMeasures(listing);
        saveCertificationResults(listing);
        saveSed(listing);
        saveCqms(listing);
        saveInitialCertificationEvent(listing);
        if (!ff4j.check(FeatureList.EDITIONLESS)) {
            saveInitialCuresUpdateEvent(listing);
        }
        CertifiedProductSearchDetails confirmedListing = cpDetailsManager.getCertifiedProductDetails(listing.getId());
        try {
            logCertifiedProductCreateActivity(confirmedListing);
        } catch (Exception ex) {
            LOGGER.error("Unable to log create activity for listing " + listing.getId(), ex);
        }
        return confirmedListing;
    }

    private void saveListingTestingLabMappings(CertifiedProductSearchDetails listing) throws EntityCreationException {
        if (!CollectionUtils.isEmpty(listing.getTestingLabs())) {
            listing.getTestingLabs().stream()
                .forEach(rethrowConsumer(atl -> cpTestingLabDao.createListingTestingLabMapping(listing.getId(), atl.getTestingLab().getId())));
        }
    }

    private void saveListingQmsStandardMappings(CertifiedProductSearchDetails listing)
        throws EntityCreationException {
        if (!CollectionUtils.isEmpty(listing.getQmsStandards())) {
            listing.getQmsStandards().stream()
                .forEach(rethrowConsumer(qmsStandard -> cpQmsDao.createListingQmsStandardMapping(listing.getId(), qmsStandard)));
        }
    }

    private void saveListingAccessibiltyStandardMappings(CertifiedProductSearchDetails listing)
            throws EntityCreationException {
        if (!CollectionUtils.isEmpty(listing.getAccessibilityStandards())) {
            listing.getAccessibilityStandards().stream()
                .forEach(rethrowConsumer(accStandard -> cpAccStdDao.createListingAccessibilityStandardMapping(listing.getId(), accStandard)));
        }
    }

    private void saveListingTargetedUserMappings(CertifiedProductSearchDetails listing)
            throws EntityCreationException {
        if (!CollectionUtils.isEmpty(listing.getTargetedUsers())) {
            listing.getTargetedUsers().stream()
                .forEach(rethrowConsumer(targetedUser -> cpTargetedUserDao.createListingTargetedUserMapping(listing.getId(), targetedUser)));
        }
    }

    private void saveListingIcsMappings(CertifiedProductSearchDetails listing) throws EntityCreationException {
        if (listing.getIcs() != null && !CollectionUtils.isEmpty(listing.getIcs().getParents())) {
            listing.getIcs().getParents().stream()
                .forEach(rethrowConsumer(parent -> listingGraphDao.createListingMap(listing.getId(), parent.getId())));
        }
        if (listing.getIcs() != null && !CollectionUtils.isEmpty(listing.getIcs().getChildren())) {
            listing.getIcs().getChildren().stream()
                .forEach(rethrowConsumer(child -> listingGraphDao.createListingMap(child.getId(), listing.getId())));
        }
    }

    private void saveListingMeasures(CertifiedProductSearchDetails listing) throws EntityCreationException {
        if (!CollectionUtils.isEmpty(listing.getMeasures())) {
            listing.getMeasures().stream()
                .forEach(rethrowConsumer(measure -> listingMeasureDao.createCertifiedProductMeasureMapping(listing.getId(), measure)));
        }
    }

    private void saveCertificationResults(CertifiedProductSearchDetails listing) throws EntityCreationException {
        if (CollectionUtils.isEmpty(listing.getCertificationResults())) {
            return;
        }
        listing.getCertificationResults().stream()
            .forEach(rethrowConsumer(certResult -> saveCertificationResult(listing.getId(), certResult)));
    }

    private void saveCertificationResult(Long listingId, CertificationResult certResult) throws EntityCreationException {
        Long certResultId = certResultDao.create(listingId, certResult);
        certResult.setId(certResultId);
        if (BooleanUtils.isTrue(certResult.getSuccess())) {
            saveAdditionalSoftware(certResult);
            saveOptionalStandards(certResult);
            saveTestData(certResult);
            saveTestProcedures(certResult);
            saveFunctionalitiesTested(certResult);
            saveStandards(certResult);
            saveTestTools(certResult);
            saveConformanceMethods(certResult);
            saveSvaps(certResult);
        }
    }

    private void saveAdditionalSoftware(CertificationResult certResult) throws EntityCreationException {
        if (!CollectionUtils.isEmpty(certResult.getAdditionalSoftware())) {
            certResult.getAdditionalSoftware().stream()
                .forEach(rethrowConsumer(additionalSoftware -> certResultDao.createAdditionalSoftwareMapping(certResult.getId(), additionalSoftware)));
        }
    }

    private void saveOptionalStandards(CertificationResult certResult) throws EntityCreationException {
        if (!CollectionUtils.isEmpty(certResult.getOptionalStandards())) {
            certResult.getOptionalStandards().stream()
                .forEach(rethrowConsumer(optionalStandard -> certResultDao.createOptionalStandardMapping(certResult.getId(), optionalStandard)));
        }
    }

    private void saveTestData(CertificationResult certResult) throws EntityCreationException {
        if (!CollectionUtils.isEmpty(certResult.getTestDataUsed())) {
            certResult.getTestDataUsed().stream()
                .forEach(rethrowConsumer(testData -> certResultDao.createTestDataMapping(certResult.getId(), testData)));
        }
    }

    private void saveTestProcedures(CertificationResult certResult) throws EntityCreationException {
        if (!CollectionUtils.isEmpty(certResult.getTestProcedures())) {
            certResult.getTestProcedures().stream()
                .forEach(rethrowConsumer(testProcedure -> certResultDao.createTestProcedureMapping(certResult.getId(), testProcedure)));
        }
    }

    private void saveFunctionalitiesTested(CertificationResult certResult) throws EntityCreationException {
        if (!CollectionUtils.isEmpty(certResult.getFunctionalitiesTested())) {
            certResult.getFunctionalitiesTested().stream()
                .forEach(rethrowConsumer(functionalityTested -> certResultFuncTestedDao.createFunctionalityTestedMapping(certResult.getId(), functionalityTested)));
        }
    }

    private void saveStandards(CertificationResult certResult) throws EntityCreationException {
        if (!CollectionUtils.isEmpty(certResult.getStandards())) {
            certResult.getStandards().stream()
                .forEach(rethrowConsumer(standard -> certResultStandardDao.createStandardMapping(certResult.getId(), standard)));
        }
    }

    private void saveTestTools(CertificationResult certResult) throws EntityCreationException {
        if (!CollectionUtils.isEmpty(certResult.getTestToolsUsed())) {
            certResult.getTestToolsUsed().stream()
                .forEach(rethrowConsumer(testTool -> certResultDao.createTestToolMapping(certResult.getId(), testTool)));
        }
    }

    private void saveConformanceMethods(CertificationResult certResult) throws EntityCreationException {
        if (!CollectionUtils.isEmpty(certResult.getConformanceMethods())) {
            certResult.getConformanceMethods().stream()
                .forEach(rethrowConsumer(conformanceMethod -> certResultDao.createConformanceMethodMapping(certResult.getId(), conformanceMethod)));
        }
    }

    private void saveSvaps(CertificationResult certResult) throws EntityCreationException {
        if (!CollectionUtils.isEmpty(certResult.getSvaps())) {
            certResult.getSvaps().stream()
                .forEach(rethrowConsumer(svap -> certResultDao.createSvapMapping(certResult.getId(), svap)));
        }
    }

    private void saveSed(CertifiedProductSearchDetails listing) throws EntityCreationException {
        if (listing.getSed() != null && !CollectionUtils.isEmpty(listing.getSed().getUcdProcesses())) {
            listing.getSed().getUcdProcesses().stream()
                .forEach(rethrowConsumer(ucdProcess -> saveUcdProcess(listing, ucdProcess)));
        }
        if (listing.getSed() != null && !CollectionUtils.isEmpty(listing.getSed().getTestTasks())) {
            listing.getSed().getTestTasks().stream()
                .forEach(rethrowConsumer(testTask -> saveTestTask(listing, testTask, listing.getSed().getTestTasks())));
        }
    }

    private void saveUcdProcess(CertifiedProductSearchDetails listing, CertifiedProductUcdProcess ucdProcess) throws EntityCreationException {
        List<Long> certResultIds = ucdProcess.getCriteria().stream()
            .map(criterion -> getCertificationResultId(listing, criterion))
            .filter(crId -> crId != null)
            .collect(Collectors.toList());
        certResultIds.stream()
            .forEach(rethrowConsumer(certResultId -> certResultDao.createUcdProcessMapping(certResultId, ucdProcess)));
    }

    private void saveTestTask(CertifiedProductSearchDetails listing, TestTask testTask, List<TestTask> allTestTasks) throws EntityCreationException {
        List<Long> certResultIds = testTask.getCriteria().stream()
                .map(criterion -> getCertificationResultId(listing, criterion))
                .filter(crId -> crId != null)
                .collect(Collectors.toList());
            certResultIds.stream()
                .forEach(rethrowConsumer(certResultId -> certResultDao.createTestTaskMapping(certResultId, testTask, allTestTasks)));
    }

    private Long getCertificationResultId(CertifiedProductSearchDetails listing, CertificationCriterion criterion) {
        Optional<CertificationResult> certResult = listing.getCertificationResults().stream()
                .filter(cr -> cr.getCriterion().getId().equals(criterion.getId()))
                .findAny();
        if (certResult.isPresent()) {
            return certResult.get().getId();
        }
        return null;
    }

    private void saveCqms(CertifiedProductSearchDetails listing) throws EntityCreationException {
        //only save attested CQMs
        listing.getCqmResults().stream()
            .filter(cqmResult -> BooleanUtils.isTrue(cqmResult.getSuccess()))
            .forEach(rethrowConsumer(cqmResult -> cqmResultDao.create(listing.getId(), cqmResult)));
    }

    private void saveInitialCertificationEvent(CertifiedProductSearchDetails listing)
            throws EntityCreationException {
        Date certificationDate = new Date(listing.getCertificationDate());
        CertificationStatusEvent certEvent = CertificationStatusEvent.builder()
                .eventDate(certificationDate.getTime())
                .status(activeStatus)
                .build();
        statusEventDao.create(listing.getId(), certEvent);
    }

    private void saveInitialCuresUpdateEvent(CertifiedProductSearchDetails listing) throws EntityCreationException {
        CuresUpdateEventDTO curesEvent = CuresUpdateEventDTO.builder()
                .eventDate(new Date(listing.getCertificationDate()))
                .curesUpdate(curesUpdateService.isCuresUpdate(listing))
                .certifiedProductId(listing.getId())
                .build();
        curesUpdateDao.create(curesEvent);
    }

    private void logCertifiedProductCreateActivity(CertifiedProductSearchDetails listing) throws JsonProcessingException, EntityCreationException, EntityRetrievalException {
        activityManager.addActivity(ActivityConcept.CERTIFIED_PRODUCT, listing.getId(),
            "Created a certified product", null, listing);
    }
}
