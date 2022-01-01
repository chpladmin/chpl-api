package gov.healthit.chpl.upload.listing;

import static gov.healthit.chpl.util.LambdaExceptionUtil.rethrowConsumer;

import java.io.IOException;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.CertifiedProductAccessibilityStandardDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.CertifiedProductQmsStandardDAO;
import gov.healthit.chpl.dao.CertifiedProductTargetedUserDAO;
import gov.healthit.chpl.dao.CertifiedProductTestingLabDAO;
import gov.healthit.chpl.dao.FuzzyChoicesDAO;
import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.domain.CertifiedProductAccessibilityStandard;
import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.FuzzyChoicesDTO;
import gov.healthit.chpl.entity.FuzzyType;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.listing.measure.ListingMeasureDAO;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.ProductManager;
import gov.healthit.chpl.manager.ProductVersionManager;
import gov.healthit.chpl.service.realworldtesting.RealWorldTestingEligiblityCachingService;
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
    private ActivityManager activityManager;
    private CertifiedProductDetailsManager cpDetailsManager;
    private RealWorldTestingEligiblityCachingService rwtCachingService;

    @Autowired
    @SuppressWarnings("checkstyle:parameternumber")
    public ListingConfirmationManager(DeveloperManager developerManager,
            ProductManager productManager, ProductVersionManager versionManager,
            CertifiedProductDAO cpDao, CertifiedProductTestingLabDAO cpTestingLabDao,
            FuzzyChoicesDAO fuzzyChoicesDao, CertifiedProductQmsStandardDAO cpQmsDao,
            CertifiedProductAccessibilityStandardDAO cpAccStdDao,
            CertifiedProductTargetedUserDAO cpTargetedUserDao,
            ListingGraphDAO listingGraphDao, ListingMeasureDAO listingMeasureDao,
            CertificationResultDAO certResultDao,
            CertifiedProductDetailsManager cpDetailsManager,
            ActivityManager activityManager, RealWorldTestingEligiblityCachingService rwtCachingService) {
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
        this.cpDetailsManager = cpDetailsManager;
        this.activityManager = activityManager;
    }


    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.CertifiedProductDomainPermissions).CREATE, #listing)")
    @CacheEvict(value = {
            CacheNames.ALL_DEVELOPERS, CacheNames.ALL_DEVELOPERS_INCLUDING_DELETED, CacheNames.COLLECTIONS_DEVELOPERS,
            CacheNames.COLLECTIONS_LISTINGS, CacheNames.COLLECTIONS_SEARCH, CacheNames.PRODUCT_NAMES, CacheNames.DEVELOPER_NAMES
    }, allEntries = true)
    public Long create(CertifiedProductSearchDetails listing)
        throws EntityCreationException, EntityRetrievalException, JsonProcessingException {
        if (listing.getDeveloper().getDeveloperId() == null) {
            //create developer, set developer ID in listing
            Long developerId = developerManager.create(listing.getDeveloper());
            listing.getDeveloper().setDeveloperId(developerId);
        }
        if (listing.getProduct().getProductId() == null) {
            //create product, set product ID in listing
            Long productId = productManager.create(listing.getDeveloper().getDeveloperId(), listing.getProduct());
            listing.getProduct().setProductId(productId);
        }
        if (listing.getVersion().getVersionId() == null) {
            //create version, set version ID in listing
            Long versionId = versionManager.create(listing.getProduct().getProductId(), listing.getVersion());
            listing.getVersion().setVersionId(versionId);
        }
        Long createdListingId = cpDao.create(listing);
        saveListingTestingLabMappings(listing);
        saveListingQmsStandardMappings(listing);
        saveListingAccessibiltyStandardMappings(listing);
        saveListingTargetedUserMappings(listing);
        saveListingIcsMappings(listing);
        saveListingMeasures(listing);
        //SED
        //Certification Results
        //CQMs
        try {
            logCertifiedProductCreateActivity(listing.getId());
        } catch (Exception ex) {
            LOGGER.error("Unable to log create activity for listing " + listing.getId(), ex);
        }
        rwtCachingService.calculateRwtEligibility(listing.getId());
        return createdListingId;
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

    }

    private void logCertifiedProductCreateActivity(Long listingId) throws JsonProcessingException, EntityCreationException, EntityRetrievalException {
        CertifiedProductSearchDetails confirmedListing
            = cpDetailsManager.getCertifiedProductDetails(listingId);
        activityManager.addActivity(ActivityConcept.CERTIFIED_PRODUCT, listingId,
            "Created a certified product", null, confirmedListing);
    }
}
