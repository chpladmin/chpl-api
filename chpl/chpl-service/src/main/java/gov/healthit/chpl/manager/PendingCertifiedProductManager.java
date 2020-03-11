package gov.healthit.chpl.manager;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.dao.MacraMeasureDAO;
import gov.healthit.chpl.dao.PendingCertifiedProductDAO;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Contact;
import gov.healthit.chpl.domain.MacraMeasure;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.CQMCriterionDTO;
import gov.healthit.chpl.dto.MacraMeasureDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductMetadataDTO;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ObjectMissingValidationException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.manager.impl.SecuredManager;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.validation.listing.ListingValidatorFactory;
import gov.healthit.chpl.validation.listing.PendingValidator;

@Service
public class PendingCertifiedProductManager extends SecuredManager {
    private static final Logger LOGGER = LogManager.getLogger(PendingCertifiedProductManager.class);

    private CertificationResultRules certRules;
    private ListingValidatorFactory validatorFactory;
    private PendingCertifiedProductDAO pcpDao;
    private TestingFunctionalityManager testFunctionalityManager;
    private UserDAO userDAO;
    private CQMCriterionDAO cqmCriterionDAO;
    private MacraMeasureDAO macraDao;
    private ActivityManager activityManager;

    private List<CQMCriterion> cqmCriteria = new ArrayList<CQMCriterion>();
    private List<MacraMeasure> macraMeasures = new ArrayList<MacraMeasure>();

    @Autowired
    public PendingCertifiedProductManager(CertificationResultRules certRules,
            ListingValidatorFactory validatorFactory,
            PendingCertifiedProductDAO pcpDao,
            TestingFunctionalityManager testFunctionalityManager,
            UserDAO userDAO,
            CQMCriterionDAO cqmCriterionDAO,
            MacraMeasureDAO macraDao,
            ActivityManager activityManager
            ) {

        this.certRules = certRules;
        this.validatorFactory = validatorFactory;
        this.pcpDao = pcpDao;
        this.testFunctionalityManager = testFunctionalityManager;
        this.userDAO = userDAO;
        this.cqmCriterionDAO = cqmCriterionDAO;
        this.macraDao = macraDao;
        this.activityManager = activityManager;

        refreshData();
    }

    @Transactional
    public void refreshData() {
        cqmCriteria = new ArrayList<CQMCriterion>();
        macraMeasures = new ArrayList<MacraMeasure>();
        loadCQMCriteria();
        loadCriteriaMacraMeasures();
    }


    @Transactional(readOnly = true)
    @PostAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.PendingCertifiedProductDomainPermissions).GET_DETAILS_BY_ID,"
            + "returnObject)")
    public PendingCertifiedProductDetails getById(final Long id)
            throws EntityRetrievalException, AccessDeniedException {
        return getById(id, false);
    }

    /**
     * ROLE_ONC is allowed to see pending listings only for activity and no other times.
     */

    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.PendingCertifiedProductDomainPermissions).GET_DETAILS_BY_ID_FOR_ACTIVITY)")
    public PendingCertifiedProductDetails getByIdForActivity(final Long id)
            throws EntityRetrievalException, AccessDeniedException {
        return getById(id, true);
    }


    @Transactional(readOnly = true)
    @PostAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.PendingCertifiedProductDomainPermissions).GET_DETAILS_BY_ID,"
            + "returnObject)")
    public PendingCertifiedProductDetails getById(final Long id, final boolean includeRetired)
            throws EntityRetrievalException, AccessDeniedException {

        PendingCertifiedProductDTO pendingCp = pcpDao.findById(id, includeRetired);

        // the user has permission so continue getting the pending cp
        updateCertResults(pendingCp);
        validate(pendingCp);

        PendingCertifiedProductDetails pcpDetails = new PendingCertifiedProductDetails(pendingCp);
        addAllVersionsToCmsCriterion(pcpDetails);
        addAllMeasuresToCertificationCriteria(pcpDetails);
        addAvailableTestFunctionalities(pcpDetails);
        return pcpDetails;
    }


    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.PendingCertifiedProductDomainPermissions).GET_ALL_METADATA)")
    @PostFilter("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.PendingCertifiedProductDomainPermissions).GET_ALL_METADATA, filterObject)")
    public List<PendingCertifiedProductMetadataDTO> getAllPendingCertifiedProductMetadata() {
        List<PendingCertifiedProductMetadataDTO> products = pcpDao.getAllMetadata();
        return products;
    }


    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.PendingCertifiedProductDomainPermissions).GET_ALL)")
    public List<PendingCertifiedProductDTO> getAllPendingCertifiedProducts() {
        List<PendingCertifiedProductDTO> products = pcpDao.findAll();
        updateCertResults(products);
        validate(products);

        return products;
    }

    /**
     * This method is included so that the pending listings may be pre-loaded in a background cache without having to
     * duplicate manager logic. Prefer users of this class to call getPendingCertifiedProductsCached.
     *
     * @param acbId
     * @return
     */

    @Transactional(readOnly = true)
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.PendingCertifiedProductDomainPermissions).GET_BY_ACB, #acbId)")
    public List<PendingCertifiedProductDTO> getPendingCertifiedProducts(final Long acbId) {
        List<PendingCertifiedProductDTO> products = pcpDao.findByAcbId(acbId);
        updateCertResults(products);
        validate(products);

        return products;
    }


    @Transactional(rollbackFor = {
            EntityRetrievalException.class, EntityCreationException.class, JsonProcessingException.class
    })
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.PendingCertifiedProductDomainPermissions).CREATE_OR_REPLACE, #toCreate)")
    public PendingCertifiedProductDTO createOrReplace(PendingCertifiedProductEntity toCreate)
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
            // something unexpected happened on upload make sure the user gets an appropriate error message
            EntityCreationException toThrow = new EntityCreationException(
                    "An unexpected error occurred. Please review the information in your upload file. The CHPL team has been notified.");
            toThrow.setStackTrace(ex.getStackTrace());
            throw toThrow;
        }

        String activityMsg = "Certified product " + pendingCpDto.getProductName() + " is pending.";
        activityManager.addActivity(ActivityConcept.PENDING_CERTIFIED_PRODUCT, pendingCpDto.getId(),
                activityMsg, null, pendingCpDto);

        return pendingCpDto;
    }


    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.PendingCertifiedProductDomainPermissions).DELETE, #pendingProductId)")
    public void deletePendingCertifiedProduct(final Long pendingProductId)
            throws EntityRetrievalException, EntityNotFoundException, EntityCreationException, AccessDeniedException,
            JsonProcessingException, ObjectMissingValidationException {

        PendingCertifiedProductDTO pendingCp = pcpDao.findById(pendingProductId, true);
        //dao throws entity not found exception if bad id
        if (pendingCp != null) {
            if (isPendingListingAvailableForUpdate(pendingCp)) {
                pcpDao.delete(pendingProductId);
                String activityMsg = "Pending certified product " + pendingCp.getProductName() + " has been rejected.";
                activityManager.addActivity(ActivityConcept.PENDING_CERTIFIED_PRODUCT, pendingCp.getId(),
                        activityMsg, pendingCp, null);
            }
        }
    }


    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.PendingCertifiedProductDomainPermissions).CONFIRM, #acbId)")
    public void confirm(final Long acbId, final Long pendingProductId)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
        PendingCertifiedProductDTO pendingCp = pcpDao.findById(pendingProductId, true);
        pcpDao.delete(pendingProductId);

        String activityMsg = "Pending certified product " + pendingCp.getProductName() + " has been confirmed.";
        activityManager.addActivity(ActivityConcept.PENDING_CERTIFIED_PRODUCT, pendingCp.getId(),
                activityMsg, pendingCp, pendingCp);

    }


    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).PENDING_CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.PendingCertifiedProductDomainPermissions).UPDATEABLE, #acbId)")
    public boolean isPendingListingAvailableForUpdate(final Long acbId, final Long pendingProductId)
            throws EntityRetrievalException, ObjectMissingValidationException {
        PendingCertifiedProductDTO pendingCp = pcpDao.findById(pendingProductId, true);
        return isPendingListingAvailableForUpdate(pendingCp);
    }

    private boolean isPendingListingAvailableForUpdate(final PendingCertifiedProductDTO pendingCp)
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
                if (!certRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.GAP)) {
                    certResult.setGap(null);
                } else if (certResult.getGap() == null) {
                    certResult.setGap(Boolean.FALSE);
                }

                if (!certRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.G1_SUCCESS)) {
                    certResult.setG1Success(null);
                } else if (certResult.getG1Success() == null) {
                    certResult.setG1Success(Boolean.FALSE);
                }

                if (!certRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.G2_SUCCESS)) {
                    certResult.setG2Success(null);
                } else if (certResult.getG2Success() == null) {
                    certResult.setG2Success(Boolean.FALSE);
                }

                if (!certRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.G1_MACRA)) {
                    certResult.setG1MacraMeasures(null);
                }
                if (!certRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.G2_MACRA)) {
                    certResult.setG2MacraMeasures(null);
                }
                if (!certRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.API_DOCUMENTATION)) {
                    certResult.setApiDocumentation(null);
                } else if (certResult.getApiDocumentation() == null) {
                    certResult.setApiDocumentation("");
                }

                if (!certRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.EXPORT_DOCUMENTATION)) {
                    certResult.setExportDocumentation(null);
                } else if (certResult.getExportDocumentation() == null) {
                    certResult.setExportDocumentation("");
                }

                if (!certRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.DOCUMENTATION_URL)) {
                    certResult.setDocumentationUrl(null);
                } else if (certResult.getDocumentationUrl() == null) {
                    certResult.setDocumentationUrl("");
                }

                if (!certRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.USE_CASES)) {
                    certResult.setUseCases(null);
                } else if (certResult.getUseCases() == null) {
                    certResult.setUseCases("");
                }

                if (!certRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.PRIVACY_SECURITY)) {
                    certResult.setPrivacySecurityFramework(null);
                } else if (certResult.getPrivacySecurityFramework() == null) {
                    certResult.setPrivacySecurityFramework("");
                }

                if (!certRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.ATTESTATION_ANSWER)) {
                    certResult.setAttestationAnswer(null);
                } else if (certResult.getAttestationAnswer() == null) {
                    certResult.setAttestationAnswer(Boolean.FALSE);
                }

                if (!certRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.SED)) {
                    certResult.setSed(null);
                } else if (certResult.getSed() == null) {
                    certResult.setSed(Boolean.FALSE);
                }

                if (!certRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.UCD_FIELDS)) {
                    certResult.setUcdProcesses(null);
                }
                if (!certRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.ADDITIONAL_SOFTWARE)) {
                    certResult.setAdditionalSoftware(null);
                }
                if (!certRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.FUNCTIONALITY_TESTED)) {
                    certResult.setTestFunctionality(null);
                }
                if (!certRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.STANDARDS_TESTED)) {
                    certResult.setTestStandards(null);
                }
                if (!certRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.TEST_DATA)) {
                    certResult.setTestData(null);
                }
                if (!certRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.TEST_PROCEDURE)) {
                    certResult.setTestProcedures(null);
                }
                if (!certRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.TEST_TOOLS_USED)) {
                    certResult.setTestTools(null);
                }
                if (!certRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.TEST_TASK)) {
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


    public void addAllVersionsToCmsCriterion(final PendingCertifiedProductDetails pcpDetails) {
        // now add allVersions for CMSs
        String certificationEdition =
                pcpDetails.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_NAME_KEY).toString();
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


    public void addAllMeasuresToCertificationCriteria(final PendingCertifiedProductDetails pcpDetails) {
        // now add allMeasures for criteria
        for (CertificationResult cert : pcpDetails.getCertificationResults()) {
            for (MacraMeasure measure : macraMeasures) {
                if (measure.getCriteria().getId().equals(cert.getCriterion().getId())) {
                    cert.getAllowedMacraMeasures().add(measure);
                }
            }
        }
    }


    public void addAvailableTestFunctionalities(final PendingCertifiedProductDetails pcpDetails) {
        // now add allMeasures for criteria
        for (CertificationResult cert : pcpDetails.getCertificationResults()) {
            String edition =
                    pcpDetails.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_NAME_KEY).toString();
            Long practiceTypeId = null;
            if (pcpDetails.getPracticeType().containsKey("id")) {
                if (pcpDetails.getPracticeType().get("id") != null) {
                    practiceTypeId = Long.valueOf(pcpDetails.getPracticeType().get("id").toString());
                }
            }
            cert.setAllowedTestFunctionalities(
                    testFunctionalityManager.getTestFunctionalities(cert.getCriterion().getId(), edition, practiceTypeId));
        }
    }
}
