package gov.healthit.chpl.upload.listing;

import static gov.healthit.chpl.util.LambdaExceptionUtil.rethrowConsumer;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.caching.CacheNames;
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
import gov.healthit.chpl.dao.FuzzyChoicesDAO;
import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductUcdProcess;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.CuresUpdateEventDTO;
import gov.healthit.chpl.dto.FuzzyChoicesDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.entity.FuzzyType;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.listing.measure.ListingMeasureDAO;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.manager.ProductVersionManager;
import gov.healthit.chpl.service.CuresUpdateService;
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
    private FuzzyChoicesDAO fuzzyChoicesDao;
    private CertificationResultDAO certResultDao;
    private CQMResultDAO cqmResultDao;
    private CertificationStatusEventDAO statusEventDao;
    private CuresUpdateEventDAO curesUpdateDao;
    private CuresUpdateService curesUpdateService;
    private ActivityManager activityManager;
    private CertifiedProductDetailsManager cpDetailsManager;

    private CertificationStatus activeStatus;

    @Autowired
    @SuppressWarnings("checkstyle:parameternumber")
    public ListingConfirmationManager(DeveloperManager developerManager,
            ProductManager productManager, ProductVersionManager versionManager,
            CertifiedProductDAO cpDao, CertifiedProductTestingLabDAO cpTestingLabDao,
            FuzzyChoicesDAO fuzzyChoicesDao, CertifiedProductQmsStandardDAO cpQmsDao,
            CertifiedProductAccessibilityStandardDAO cpAccStdDao,
            CertifiedProductTargetedUserDAO cpTargetedUserDao,
            ListingGraphDAO listingGraphDao, ListingMeasureDAO listingMeasureDao,
            CertificationResultDAO certResultDao, CQMResultDAO cqmResultDao,
            CertificationStatusDAO certStatusDao,  CertificationStatusEventDAO statusEventDao,
            CuresUpdateEventDAO curesUpdateDao,
            CertifiedProductDetailsManager cpDetailsManager, CuresUpdateService curesUpdateService,
            ActivityManager activityManager) {
        this.developerManager = developerManager;
        this.productManager = productManager;
        this.versionManager = versionManager;
        this.cpDao = cpDao;
        this.cpTestingLabDao = cpTestingLabDao;
        this.fuzzyChoicesDao = fuzzyChoicesDao;
        this.cpQmsDao = cpQmsDao;
        this.cpAccStdDao = cpAccStdDao;
        this.cpTargetedUserDao = cpTargetedUserDao;
        this.listingGraphDao = listingGraphDao;
        this.listingMeasureDao = listingMeasureDao;
        this.certResultDao = certResultDao;
        this.cqmResultDao = cqmResultDao;
        this.statusEventDao = statusEventDao;
        this.curesUpdateDao = curesUpdateDao;
        this.cpDetailsManager = cpDetailsManager;
        this.curesUpdateService = curesUpdateService;
        this.activityManager = activityManager;

        activeStatus = certStatusDao.getByStatusName(CertificationStatusType.Active.toString());
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.CertifiedProductDomainPermissions).CREATE, #listing)")
    @CacheEvict(value = {
            CacheNames.ALL_DEVELOPERS, CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED,
            CacheNames.COLLECTIONS_DEVELOPERS,
            CacheNames.COLLECTIONS_LISTINGS, CacheNames.COLLECTIONS_SEARCH, CacheNames.PRODUCT_NAMES, CacheNames.DEVELOPER_NAMES
    }, allEntries = true)
    public CertifiedProductSearchDetails create(CertifiedProductSearchDetails listing)
        throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
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
        saveInitialCuresUpdateEvent(listing);
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
                .forEach(rethrowConsumer(atl -> cpTestingLabDao.createListingTestingLabMapping(listing.getId(), atl.getTestingLabId())));
        }
    }

    private void saveListingQmsStandardMappings(CertifiedProductSearchDetails listing)
        throws EntityCreationException {
        if (!CollectionUtils.isEmpty(listing.getQmsStandards())) {
            try {
                List<String> fuzzyChoices = fuzzyChoicesDao.getByType(FuzzyType.QMS_STANDARD).getChoices();
                listing.getQmsStandards().stream()
                    .filter(qmsStandard -> !fuzzyChoices.contains(qmsStandard.getQmsStandardName()))
                    .forEach(qmsStandard -> addQmsStandardToFuzzyChoices(qmsStandard, fuzzyChoices));
            } catch (IOException | EntityRetrievalException ex) {
                LOGGER.error("Cannot get QMS Standard fuzzy choices", ex);
            }
            listing.getQmsStandards().stream()
                .forEach(rethrowConsumer(qmsStandard -> cpQmsDao.createListingQmsStandardMapping(listing.getId(), qmsStandard)));
        }
    }

    //TODO: Remove as part of OCD-4041
    private void addQmsStandardToFuzzyChoices(CertifiedProductQmsStandard qmsStandard, List<String> fuzzyChoices) {
        fuzzyChoices.add(qmsStandard.getQmsStandardName());
        FuzzyChoicesDTO dto = new FuzzyChoicesDTO();
        dto.setFuzzyType(FuzzyType.QMS_STANDARD);
        dto.setChoices(fuzzyChoices);
        try {
            fuzzyChoicesDao.update(dto);
        } catch (IOException | EntityCreationException | EntityRetrievalException ex) {
            LOGGER.error("Cannot update fuzzy choices with " + qmsStandard.getQmsStandardName(), ex);
        }
    }

    private void saveListingAccessibiltyStandardMappings(CertifiedProductSearchDetails listing)
            throws EntityCreationException {
        if (!CollectionUtils.isEmpty(listing.getAccessibilityStandards())) {
            try {
                List<String> fuzzyChoices = fuzzyChoicesDao.getByType(FuzzyType.ACCESSIBILITY_STANDARD).getChoices();
                listing.getAccessibilityStandards().stream()
                    .filter(accStandard -> !fuzzyChoices.contains(accStandard.getAccessibilityStandardName()))
                    .forEach(accStandard -> addAccessibilityStandardToFuzzyChoices(accStandard, fuzzyChoices));
            } catch (IOException | EntityRetrievalException ex) {
                LOGGER.error("Cannot get Accessibility Standard fuzzy choices", ex);
            }
            listing.getAccessibilityStandards().stream()
                .forEach(rethrowConsumer(accStandard -> cpAccStdDao.createListingAccessibilityStandardMapping(listing.getId(), accStandard)));
        }
    }

    //TODO: Remove as part of OCD-4040
    private void addAccessibilityStandardToFuzzyChoices(CertifiedProductAccessibilityStandard accessibilityStandard,
            List<String> fuzzyChoices) {
        fuzzyChoices.add(accessibilityStandard.getAccessibilityStandardName());
        FuzzyChoicesDTO dto = new FuzzyChoicesDTO();
        dto.setFuzzyType(FuzzyType.ACCESSIBILITY_STANDARD);
        dto.setChoices(fuzzyChoices);
        try {
            fuzzyChoicesDao.update(dto);
        } catch (IOException | EntityCreationException | EntityRetrievalException ex) {
            LOGGER.error("Cannot update fuzzy choices with " + accessibilityStandard.getAccessibilityStandardName(), ex);
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
        if (BooleanUtils.isTrue(certResult.isSuccess())) {
            saveAdditionalSoftware(certResult);
            saveOptionalStandards(certResult);
            saveTestData(certResult);
            saveTestProcedures(certResult);
            saveTestFunctionality(certResult);
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

    private void saveTestFunctionality(CertificationResult certResult) throws EntityCreationException {
        if (!CollectionUtils.isEmpty(certResult.getTestFunctionality())) {
            certResult.getTestFunctionality().stream()
                .forEach(rethrowConsumer(testFunctionality -> certResultDao.createTestFunctionalityMapping(certResult.getId(), testFunctionality)));
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
            .collect(Collectors.toList());
        certResultIds.stream()
            .forEach(rethrowConsumer(certResultId -> certResultDao.createUcdProcessMapping(certResultId, ucdProcess)));
    }

    private void saveTestTask(CertifiedProductSearchDetails listing, TestTask testTask, List<TestTask> allTestTasks) throws EntityCreationException {
        List<Long> certResultIds = testTask.getCriteria().stream()
                .map(criterion -> getCertificationResultId(listing, criterion))
                .collect(Collectors.toList());
            certResultIds.stream()
                .forEach(rethrowConsumer(certResultId -> certResultDao.createTestTaskMapping(certResultId, testTask, allTestTasks)));
    }

    private Long getCertificationResultId(CertifiedProductSearchDetails listing, CertificationCriterion criterion) {
        return listing.getCertificationResults().stream()
                .filter(certResult -> certResult.getCriterion().getId().equals(criterion.getId()))
                .findAny().get().getId();
    }

    private void saveCqms(CertifiedProductSearchDetails listing) throws EntityCreationException {
        //only save attested CQMs
        listing.getCqmResults().stream()
            .filter(cqmResult -> BooleanUtils.isTrue(cqmResult.isSuccess()))
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
