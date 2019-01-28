package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.dao.UserDAO;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.dao.MacraMeasureDAO;
import gov.healthit.chpl.dao.PendingCertifiedProductDAO;
import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.Contact;
import gov.healthit.chpl.domain.MacraMeasure;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.domain.concept.ActivityConcept;
import gov.healthit.chpl.dto.CQMCriterionDTO;
import gov.healthit.chpl.dto.MacraMeasureDTO;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ObjectMissingValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.PendingCertifiedProductManager;
import gov.healthit.chpl.manager.TestingFunctionalityManager;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.validation.listing.ListingValidatorFactory;
import gov.healthit.chpl.validation.listing.PendingValidator;

@Service
public class PendingCertifiedProductManagerImpl implements PendingCertifiedProductManager {
    private static final Logger LOGGER = LogManager.getLogger(PendingCertifiedProductManagerImpl.class);

    @Autowired
    private CertificationResultRules certRules;
    @Autowired
    private ListingValidatorFactory validatorFactory;

    @Autowired
    private PendingCertifiedProductDAO pcpDao;
    @Autowired
    private TestingFunctionalityManager testFunctionalityManager;
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private CQMCriterionDAO cqmCriterionDAO;
    @Autowired
    private MacraMeasureDAO macraDao;
    private List<CQMCriterion> cqmCriteria = new ArrayList<CQMCriterion>();
    private List<MacraMeasure> macraMeasures = new ArrayList<MacraMeasure>();

    @Autowired
    private ActivityManager activityManager;

    @PostConstruct
    public void setup() {
        loadCQMCriteria();
        loadCriteriaMacraMeasures();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.PendingCertifiedProductDomainPermissions).GET_DETAILS_BY_ID)")
    public PendingCertifiedProductDetails getById(final Long id)
            throws EntityRetrievalException, AccessDeniedException {
        return getById(id, false);
    }

    /**
     * ROLE_ONC is allowed to see pending listings only for activity and no
     * other times.
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.PendingCertifiedProductDomainPermissions).GET_DETAILS_BY_ID_FOR_ACTIVITY)")
    public PendingCertifiedProductDetails getByIdForActivity(final Long id)
            throws EntityRetrievalException, AccessDeniedException {
        return getById(id, true);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.PendingCertifiedProductDomainPermissions).GET_DETAILS_BY_ID)")
    public PendingCertifiedProductDetails getById(final Long id, final boolean includeRetired)
            throws EntityRetrievalException, AccessDeniedException {

        PendingCertifiedProductDTO pendingCp = pcpDao.findById(id, includeRetired);

        // the user has permission so continue getting the pending cp
        updateCertResults(pendingCp);
        validate(pendingCp);

        PendingCertifiedProductDetails pcpDetails = new PendingCertifiedProductDetails(pendingCp);
        addAllVersionsToCmsCriterion(pcpDetails);
        addAllMeasuresToCertificationCriteria(pcpDetails);
        return pcpDetails;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.PendingCertifiedProductDomainPermissions).GET_ALL)")
    public List<PendingCertifiedProductDTO> getAllPendingCertifiedProducts() {
        List<PendingCertifiedProductDTO> products = pcpDao.findAll();
        updateCertResults(products);
        validate(products);

        return products;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.PendingCertifiedProductDomainPermissions).GET_BY_ACB, #acbId)")
    public List<PendingCertifiedProductDTO> getPendingCertifiedProducts(final Long acbId) {
        List<PendingCertifiedProductDTO> products = pcpDao.findByAcbId(acbId);
        updateCertResults(products);
        validate(products);

        return products;
    }

    @Override
    @Transactional(rollbackFor = {
            EntityRetrievalException.class, EntityCreationException.class, JsonProcessingException.class
    })
    @CacheEvict(value = {
            CacheNames.FIND_BY_ACB_ID
    }, allEntries = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.PendingCertifiedProductDomainPermissions).CREATE_OR_REPLACE, #acbId)")
    public PendingCertifiedProductDTO createOrReplace(final Long acbId, final PendingCertifiedProductEntity toCreate)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException {
        Long existingId = pcpDao.findIdByOncId(toCreate.getUniqueId());
        if (existingId != null) {
            pcpDao.delete(existingId);
        }

        PendingCertifiedProductDTO pendingCpDto = null;
        try {
            // insert the record
            pendingCpDto = pcpDao.create(toCreate);
            updateCertResults(pendingCpDto);
            validate(pendingCpDto);
        } catch (Exception ex) {
            // something unexpected happened on upload
            // make sure the user gets an appropriate error message
            EntityCreationException toThrow = new EntityCreationException(
                    "An unexpected error occurred. Please review the information in your upload file. The CHPL team has been notified.");
            toThrow.setStackTrace(ex.getStackTrace());
            throw toThrow;
        }

        String activityMsg = "Certified product " + pendingCpDto.getProductName() + " is pending.";
        activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_PENDING_CERTIFIED_PRODUCT, pendingCpDto.getId(),
                activityMsg, null, pendingCpDto);

        return pendingCpDto;
    }

    @Override
    @Transactional
    @CacheEvict(value = {
            CacheNames.FIND_BY_ACB_ID
    }, allEntries = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.PendingCertifiedProductDomainPermissions).DELETE, #acbId)")
    public void deletePendingCertifiedProduct(final Long acbId, final Long pendingProductId)
            throws EntityRetrievalException, EntityNotFoundException, EntityCreationException, AccessDeniedException,
            JsonProcessingException, ObjectMissingValidationException {

        PendingCertifiedProductDTO pendingCp = pcpDao.findById(pendingProductId, true);
        if (pendingCp == null) {
            throw new EntityNotFoundException("Could not find pending certified product with id " + pendingProductId);
        }

        if (isPendingListingAvailableForUpdate(pendingCp.getCertificationBodyId(), pendingCp)) {
            pcpDao.delete(pendingProductId);
            String activityMsg = "Pending certified product " + pendingCp.getProductName() + " has been rejected.";
            activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_PENDING_CERTIFIED_PRODUCT, pendingCp.getId(),
                    activityMsg, pendingCp, null);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {
            CacheNames.FIND_BY_ACB_ID
    }, allEntries = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.PendingCertifiedProductDomainPermissions).CONFIRM, #acbId)")
    public void confirm(final Long acbId, final Long pendingProductId)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
        PendingCertifiedProductDTO pendingCp = pcpDao.findById(pendingProductId, true);
        pcpDao.delete(pendingProductId);

        String activityMsg = "Pending certified product " + pendingCp.getProductName() + " has been confirmed.";
        activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_PENDING_CERTIFIED_PRODUCT, pendingCp.getId(),
                activityMsg, pendingCp, pendingCp);

    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.PendingCertifiedProductDomainPermissions).UPDATEABLE, #acbId)")
    public boolean isPendingListingAvailableForUpdate(final Long acbId, final Long pendingProductId)
            throws EntityRetrievalException, ObjectMissingValidationException {
        PendingCertifiedProductDTO pendingCp = pcpDao.findById(pendingProductId, true);
        return isPendingListingAvailableForUpdate(acbId, pendingCp);
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.PendingCertifiedProductDomainPermissions).UPDATEABLE, #acbId)")
    public boolean isPendingListingAvailableForUpdate(final Long acbId, final PendingCertifiedProductDTO pendingCp)
            throws EntityRetrievalException, ObjectMissingValidationException {
        if (pendingCp.getDeleted().booleanValue()) {
            ObjectMissingValidationException alreadyDeletedEx = new ObjectMissingValidationException();
            alreadyDeletedEx.getErrorMessages()
                    .add("This pending certified product has already been confirmed or rejected by another user.");
            alreadyDeletedEx.setObjectId(pendingCp.getUniqueId());

            try {
                UserDTO lastModifiedUser = userDAO.getById(pendingCp.getLastModifiedUser());
                if (lastModifiedUser != null) {
                    Contact contact = new Contact();
                    contact.setFullName(lastModifiedUser.getFullName());
                    contact.setFriendlyName(lastModifiedUser.getFriendlyName());
                    contact.setEmail(lastModifiedUser.getEmail());
                    contact.setPhoneNumber(lastModifiedUser.getPhoneNumber());
                    contact.setTitle(lastModifiedUser.getTitle());
                    alreadyDeletedEx.setContact(contact);
                } else {
                    alreadyDeletedEx.setContact(null);
                }
            } catch (final UserRetrievalException ex) {
                alreadyDeletedEx.setContact(null);
            }

            throw alreadyDeletedEx;
        } else {
            // If pendingCP were null, we would have gotten an NPE by this point
            return true;
        }
    }

    private void updateCertResults(final PendingCertifiedProductDTO dto) {
        List<PendingCertifiedProductDTO> products = new ArrayList<PendingCertifiedProductDTO>();
        products.add(dto);
        updateCertResults(products);
    }

    private void updateCertResults(final List<PendingCertifiedProductDTO> products) {
        for (PendingCertifiedProductDTO product : products) {
            for (PendingCertificationResultDTO certResult : product.getCertificationCriterion()) {
                if (!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.GAP)) {
                    certResult.setGap(null);
                }
                if (!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.G1_SUCCESS)) {
                    certResult.setG1Success(null);
                }
                if (!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.G2_SUCCESS)) {
                    certResult.setG2Success(null);
                }
                if (!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.G1_MACRA)) {
                    certResult.setG1MacraMeasures(null);
                }
                if (!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.G2_MACRA)) {
                    certResult.setG2MacraMeasures(null);
                }
                if (!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.API_DOCUMENTATION)) {
                    certResult.setApiDocumentation(null);
                }
                if (!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.PRIVACY_SECURITY)) {
                    certResult.setPrivacySecurityFramework(null);
                }
                if (!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.SED)) {
                    certResult.setSed(null);
                }
                if (!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.UCD_FIELDS)) {
                    certResult.setUcdProcesses(null);
                }
                if (!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.ADDITIONAL_SOFTWARE)) {
                    certResult.setAdditionalSoftware(null);
                }
                if (!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.FUNCTIONALITY_TESTED)) {
                    certResult.setTestFunctionality(null);
                }
                if (!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.STANDARDS_TESTED)) {
                    certResult.setTestStandards(null);
                }
                if (!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.TEST_DATA)) {
                    certResult.setTestData(null);
                }
                if (!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.TEST_PROCEDURE)) {
                    certResult.setTestProcedures(null);
                }
                if (!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.TEST_TOOLS_USED)) {
                    certResult.setTestTools(null);
                }
                if (!certRules.hasCertOption(certResult.getNumber(), CertificationResultRules.TEST_TASK)) {
                    certResult.setTestTasks(null);
                }
            }
        }
    }

    private void loadCriteriaMacraMeasures() {
        List<MacraMeasureDTO> dtos = macraDao.findAll();
        for (MacraMeasureDTO dto : dtos) {
            MacraMeasure measure = new MacraMeasure(dto);
            macraMeasures.add(measure);
        }
    }

    private void loadCQMCriteria() {
        List<CQMCriterionDTO> dtos = cqmCriterionDAO.findAll();
        for (CQMCriterionDTO dto : dtos) {
            CQMCriterion criterion = new CQMCriterion();
            criterion.setCmsId(dto.getCmsId());
            criterion.setCqmCriterionTypeId(dto.getCqmCriterionTypeId());
            criterion.setCqmDomain(dto.getCqmDomain());
            criterion.setCqmVersionId(dto.getCqmVersionId());
            criterion.setCqmVersion(dto.getCqmVersion());
            criterion.setCriterionId(dto.getId());
            criterion.setDescription(dto.getDescription());
            criterion.setNqfNumber(dto.getNqfNumber());
            criterion.setNumber(dto.getNumber());
            criterion.setTitle(dto.getTitle());
            cqmCriteria.add(criterion);
        }
    }

    private List<CQMCriterion> getAvailableCQMVersions() {
        List<CQMCriterion> criteria = new ArrayList<CQMCriterion>();

        for (CQMCriterion criterion : cqmCriteria) {
            if (!StringUtils.isEmpty(criterion.getCmsId()) && criterion.getCmsId().startsWith("CMS")) {
                criteria.add(criterion);
            }
        }
        return criteria;
    }

    private void validate(final List<PendingCertifiedProductDTO> products) {
        for (PendingCertifiedProductDTO dto : products) {
            PendingValidator validator = validatorFactory.getValidator(dto);
            if (validator != null) {
                validator.validate(dto);
            }
        }
    }

    private void validate(final PendingCertifiedProductDTO... products) {
        for (PendingCertifiedProductDTO dto : products) {
            PendingValidator validator = validatorFactory.getValidator(dto);
            if (validator != null) {
                validator.validate(dto);
            }
        }
    }

    @Override
    public void addAllVersionsToCmsCriterion(final PendingCertifiedProductDetails pcpDetails) {
        // now add allVersions for CMSs
        String certificationEdition = pcpDetails.getCertificationEdition().get("name").toString();
        if (!certificationEdition.startsWith("2011")) {
            List<CQMCriterion> cqms = getAvailableCQMVersions();
            for (CQMCriterion cqm : cqms) {
                boolean cqmExists = false;
                for (CQMResultDetails details : pcpDetails.getCqmResults()) {
                    if (cqm.getCmsId().equals(details.getCmsId())) {
                        cqmExists = true;
                        details.getAllVersions().add(cqm.getCqmVersion());
                    }
                }
                if (!cqmExists) {
                    CQMResultDetails result = new CQMResultDetails();
                    result.setCmsId(cqm.getCmsId());
                    result.setNqfNumber(cqm.getNqfNumber());
                    result.setNumber(cqm.getNumber());
                    result.setTitle(cqm.getTitle());
                    result.setDescription(cqm.getDescription());
                    result.setSuccess(Boolean.FALSE);
                    result.getAllVersions().add(cqm.getCqmVersion());
                    result.setTypeId(cqm.getCqmCriterionTypeId());
                    pcpDetails.getCqmResults().add(result);
                }
            }
        }
    }

    @Override
    public void addAllMeasuresToCertificationCriteria(final PendingCertifiedProductDetails pcpDetails) {
        // now add allMeasures for criteria
        for (CertificationResult cert : pcpDetails.getCertificationResults()) {
            for (MacraMeasure measure : macraMeasures) {
                if (measure.getCriteria().getNumber().equals(cert.getNumber())) {
                    cert.getAllowedMacraMeasures().add(measure);
                }
            }
        }
    }

    @Override
    public void addAvailableTestFunctionalities(final PendingCertifiedProductDetails pcpDetails) {
        // now add allMeasures for criteria
        for (CertificationResult cert : pcpDetails.getCertificationResults()) {
            String edition = pcpDetails.getCertificationEdition().get("name").toString();
            Long practiceTypeId = null;
            if (pcpDetails.getPracticeType().containsKey("id")) {
                if (pcpDetails.getPracticeType().get("id") != null) {
                    practiceTypeId = Long.valueOf(pcpDetails.getPracticeType().get("id").toString());
                }
            }
            String criteriaNumber = cert.getNumber();
            cert.setAllowedTestFunctionalities(
                    testFunctionalityManager.getTestFunctionalities(criteriaNumber, edition, practiceTypeId));
        }
    }
}
