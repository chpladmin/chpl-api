package gov.healthit.chpl.manager.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.dao.auth.UserPermissionDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;
import gov.healthit.chpl.domain.SurveillanceNonconformityDocument;
import gov.healthit.chpl.domain.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.SurveillanceRequirement;
import gov.healthit.chpl.domain.SurveillanceRequirementType;
import gov.healthit.chpl.domain.SurveillanceResultType;
import gov.healthit.chpl.domain.SurveillanceType;
import gov.healthit.chpl.domain.auth.Authority;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.auth.UserPermissionDTO;
import gov.healthit.chpl.entity.listing.CertifiedProductEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceNonconformityDocumentationEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceNonconformityEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceRequirementEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.manager.SurveillanceManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.FileUtils;
import gov.healthit.chpl.validation.surveillance.SurveillanceValidator;

@Service
public class SurveillanceManagerImpl extends SecuredManager implements SurveillanceManager {
    private static final Logger LOGGER = LogManager.getLogger(SurveillanceManagerImpl.class);

    private SurveillanceDAO survDao;
    private CertifiedProductDAO cpDao;
    private SurveillanceValidator validator;
    private UserPermissionDAO userPermissionDao;
    private FileUtils fileUtils;
    private Environment env;
    private ResourcePermissions resourcePermissions;

    @Autowired
    public SurveillanceManagerImpl(final SurveillanceDAO survDao, final CertifiedProductDAO cpDao,
            final SurveillanceValidator validator, final UserPermissionDAO userPermissionDao, final FileUtils fileUtils,
            final Environment env, final ResourcePermissions resourcePermissions) {
        this.survDao = survDao;
        this.cpDao = cpDao;
        this.validator = validator;
        this.userPermissionDao = userPermissionDao;
        this.fileUtils = fileUtils;
        this.env = env;
        this.resourcePermissions = resourcePermissions;
    }

    @Override
    @Transactional(readOnly = true)
    public Surveillance getById(final Long survId) throws EntityRetrievalException {
        SurveillanceEntity surv = survDao.getSurveillanceById(survId);
        Surveillance result = convertToDomain(surv);
        validator.validate(result, true);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Surveillance> getByCertifiedProduct(final Long cpId) {
        List<SurveillanceEntity> survResults = survDao.getSurveillanceByCertifiedProductId(cpId);
        List<Surveillance> results = new ArrayList<Surveillance>();
        if (survResults != null) {
            for (SurveillanceEntity survResult : survResults) {
                Surveillance surv = convertToDomain(survResult);
                validator.validate(surv, true);
                results.add(surv);
            }
        }
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public SurveillanceNonconformityDocument getDocumentById(final Long docId, final boolean getFileContents)
            throws EntityRetrievalException {
        SurveillanceNonconformityDocumentationEntity docEntity = survDao.getDocumentById(docId);

        SurveillanceNonconformityDocument doc = null;
        if (docEntity != null) {
            doc = convertToDomain(docEntity, getFileContents);
        }
        return doc;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceDomainPermissions).CREATE, #acbId)")
    public Long createSurveillance(final Long acbId, final Surveillance surv)
            throws UserPermissionRetrievalException, SurveillanceAuthorityAccessDeniedException {
        Long insertedId = null;
        checkSurveillanceAuthority(surv);
        updateNullAuthority(surv);

        try {
            insertedId = survDao.insertSurveillance(surv);
        } catch (final UserPermissionRetrievalException ex) {
            LOGGER.error("Error inserting surveillance.", ex);
            throw ex;
        }

        return insertedId;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceDomainPermissions).ADD_DOCUMENT, #acbId)")
    public Long addDocumentToNonconformity(final Long acbId, final Long nonconformityId,
            final SurveillanceNonconformityDocument doc) throws EntityRetrievalException {
        Long insertedId = null;
        insertedId = survDao.insertNonconformityDocument(nonconformityId, doc);
        return insertedId;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceDomainPermissions).UPDATE, #acbId)")
    public void updateSurveillance(final Long acbId, final Surveillance surv) throws EntityRetrievalException,
            UserPermissionRetrievalException, SurveillanceAuthorityAccessDeniedException {
        SurveillanceEntity dbSurvEntity = new SurveillanceEntity();
        try {
            dbSurvEntity = survDao.getSurveillanceById(surv.getId());
        } catch (final NullPointerException e) {
            LOGGER.debug("Surveillance id is null");
        }
        Surveillance dbSurv = new Surveillance();
        dbSurv.setId(dbSurvEntity.getId());
        UserPermissionDTO upDto = userPermissionDao.findById(dbSurvEntity.getUserPermissionId());
        dbSurv.setAuthority(upDto.getAuthority());
        checkSurveillanceAuthority(dbSurv);
        try {
            survDao.updateSurveillance(surv);
        } catch (final UserPermissionRetrievalException ex) {
            LOGGER.error("Error updating surveillance.", ex);
            throw ex;
        }
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceDomainPermissions).DELETE, #acbId)")
    public void deleteSurveillance(final Long acbId, final Surveillance surv)
            throws EntityRetrievalException, SurveillanceAuthorityAccessDeniedException {
        checkSurveillanceAuthority(surv);
        survDao.deleteSurveillance(surv);
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceDomainPermissions).DELETE_DOCUMENT, #acbId)")
    public void deleteNonconformityDocument(final Long acbId, final Long documentId) throws EntityRetrievalException {
        survDao.deleteNonconformityDocument(documentId);
    }

    @Transactional(readOnly = true)
    @Override
    public void validate(final Surveillance surveillance) {
        validator.validate(surveillance, true);
    }

    @Override
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SURVEILLANCE, "
            + "T(gov.healthit.chpl.permissions.domains.SurveillanceDomainPermissions).BASIC_REPORT)")
    public File getBasicReportDownloadFile() throws IOException {
        return fileUtils.getNewestFileMatchingName("^" + env.getProperty("surveillanceBasicReportName") + "-.+\\.csv$");
    }

    @Override
    public File getAllSurveillanceDownloadFile() throws IOException {
        return fileUtils.getNewestFileMatchingName("^" + env.getProperty("surveillanceAllReportName") + "-.+\\.csv$");
    }

    @Override
    public File getSurveillanceWithNonconformitiesDownloadFile() throws IOException {
        return fileUtils.getNewestFileMatchingName(
                "^" + env.getProperty("surveillanceNonconformitiesReportName") + "-.+\\.csv$");
    }

    private SurveillanceNonconformityDocument convertToDomain(final SurveillanceNonconformityDocumentationEntity entity,
            final boolean getContents) {
        SurveillanceNonconformityDocument doc = new SurveillanceNonconformityDocument();
        doc.setId(entity.getId());
        doc.setFileType(entity.getFileType());
        doc.setFileName(entity.getFileName());
        if (getContents) {
            doc.setFileContents(entity.getFileData());
        }
        return doc;
    }

    private Surveillance convertToDomain(final SurveillanceEntity entity) {
        Surveillance surv = new Surveillance();
        surv.setId(entity.getId());
        surv.setFriendlyId(entity.getFriendlyId());
        surv.setStartDate(entity.getStartDate());
        surv.setEndDate(entity.getEndDate());
        surv.setRandomizedSitesUsed(entity.getNumRandomizedSites());
        surv.setAuthority(userPermissionDao.findById(entity.getUserPermissionId()).getAuthority());
        surv.setLastModifiedDate(entity.getLastModifiedDate());

        if (entity.getCertifiedProduct() != null) {
            CertifiedProductEntity cpEntity = entity.getCertifiedProduct();
            try {
                CertifiedProductDetailsDTO cpDto = cpDao.getDetailsById(cpEntity.getId());
                surv.setCertifiedProduct(new CertifiedProduct(cpDto));
            } catch (final EntityRetrievalException ex) {
                LOGGER.error("Could not find details for certified product " + cpEntity.getId());
            }
        } else {
            CertifiedProduct cp = new CertifiedProduct();
            cp.setId(entity.getCertifiedProductId());
            surv.setCertifiedProduct(cp);
        }

        if (entity.getSurveillanceType() != null) {
            SurveillanceType survType = new SurveillanceType();
            survType.setId(entity.getSurveillanceType().getId());
            survType.setName(entity.getSurveillanceType().getName());
            surv.setType(survType);
        } else {
            SurveillanceType survType = new SurveillanceType();
            survType.setId(entity.getSurveillanceTypeId());
            surv.setType(survType);
        }

        if (entity.getSurveilledRequirements() != null) {
            for (SurveillanceRequirementEntity reqEntity : entity.getSurveilledRequirements()) {
                SurveillanceRequirement req = new SurveillanceRequirement();
                req.setId(reqEntity.getId());
                if (reqEntity.getCertificationCriterionEntity() != null) {
                    req.setRequirement(reqEntity.getCertificationCriterionEntity().getNumber());
                } else {
                    req.setRequirement(reqEntity.getSurveilledRequirement());
                }

                if (reqEntity.getSurveillanceResultTypeEntity() != null) {
                    SurveillanceResultType result = new SurveillanceResultType();
                    result.setId(reqEntity.getSurveillanceResultTypeEntity().getId());
                    result.setName(reqEntity.getSurveillanceResultTypeEntity().getName());
                    req.setResult(result);
                } else {
                    SurveillanceResultType result = new SurveillanceResultType();
                    result.setId(reqEntity.getSurveillanceResultTypeId());
                    req.setResult(result);
                }

                if (reqEntity.getSurveillanceRequirementType() != null) {
                    SurveillanceRequirementType result = new SurveillanceRequirementType();
                    result.setId(reqEntity.getSurveillanceRequirementType().getId());
                    result.setName(reqEntity.getSurveillanceRequirementType().getName());
                    req.setType(result);
                } else {
                    SurveillanceRequirementType result = new SurveillanceRequirementType();
                    result.setId(reqEntity.getSurveillanceRequirementTypeId());
                    req.setType(result);
                }

                if (reqEntity.getNonconformities() != null) {
                    for (SurveillanceNonconformityEntity ncEntity : reqEntity.getNonconformities()) {
                        SurveillanceNonconformity nc = new SurveillanceNonconformity();
                        nc.setCapApprovalDate(ncEntity.getCapApproval());
                        nc.setCapEndDate(ncEntity.getCapEndDate());
                        nc.setCapMustCompleteDate(ncEntity.getCapMustCompleteDate());
                        nc.setCapStartDate(ncEntity.getCapStart());
                        nc.setDateOfDetermination(ncEntity.getDateOfDetermination());
                        nc.setDeveloperExplanation(ncEntity.getDeveloperExplanation());
                        nc.setFindings(ncEntity.getFindings());
                        nc.setId(ncEntity.getId());
                        nc.setNonconformityType(ncEntity.getType());
                        nc.setResolution(ncEntity.getResolution());
                        nc.setSitesPassed(ncEntity.getSitesPassed());
                        nc.setSummary(ncEntity.getSummary());
                        nc.setTotalSites(ncEntity.getTotalSites());
                        nc.setLastModifiedDate(ncEntity.getLastModifiedDate());
                        if (ncEntity.getNonconformityStatus() != null) {
                            SurveillanceNonconformityStatus status = new SurveillanceNonconformityStatus();
                            status.setId(ncEntity.getNonconformityStatus().getId());
                            status.setName(ncEntity.getNonconformityStatus().getName());
                            nc.setStatus(status);
                        } else {
                            SurveillanceNonconformityStatus status = new SurveillanceNonconformityStatus();
                            status.setId(ncEntity.getNonconformityStatusId());
                            nc.setStatus(status);
                        }
                        req.getNonconformities().add(nc);

                        if (ncEntity.getDocuments() != null && ncEntity.getDocuments().size() > 0) {
                            for (SurveillanceNonconformityDocumentationEntity docEntity : ncEntity.getDocuments()) {
                                SurveillanceNonconformityDocument doc = convertToDomain(docEntity, false);
                                nc.getDocuments().add(doc);
                            }
                        }
                    }
                }
                surv.getRequirements().add(req);
            }
        }
        return surv;
    }

    private void checkSurveillanceAuthority(final Surveillance surv) throws SurveillanceAuthorityAccessDeniedException {
        Boolean hasOncAdmin = resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc();
        Boolean hasAcbAdmin = resourcePermissions.isUserRoleAcbAdmin();
        if (StringUtils.isEmpty(surv.getAuthority())) {
            // If user has ROLE_ADMIN and ROLE_ACB return 403
            if (hasOncAdmin && hasAcbAdmin) {
                String errorMsg = "Surveillance cannot be created by user having " + Authority.ROLE_ADMIN + " and "
                        + Authority.ROLE_ACB;
                LOGGER.error(errorMsg);
                throw new SurveillanceAuthorityAccessDeniedException(errorMsg);
            }
        } else {
            // Cannot have surveillance authority as ROLE_ONC for user lacking ROLE_ADMIN or ROLE_ONC
            if (surv.getAuthority().equalsIgnoreCase(Authority.ROLE_ONC) && !hasOncAdmin) {
                String errorMsg = "User must have authority " + Authority.ROLE_ADMIN + " or " + Authority.ROLE_ONC;
                LOGGER.error(errorMsg);
                throw new SurveillanceAuthorityAccessDeniedException(errorMsg);
            } else if (surv.getAuthority().equalsIgnoreCase(Authority.ROLE_ACB)) {
                // Cannot have surveillance authority as ACB for user lacking ONC and ACB roles
                if (!hasOncAdmin && !hasAcbAdmin) {
                    String errorMsg = "User must have ONC or ACB roles for a surveillance authority created by ACB";
                    LOGGER.error(errorMsg);
                    throw new SurveillanceAuthorityAccessDeniedException(errorMsg);
                }
            }
        }
    }

    private void updateNullAuthority(final Surveillance surv) {
        Boolean hasOncAdmin = resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc();
        Boolean hasAcbAdmin = resourcePermissions.isUserRoleAcbAdmin();
        if (StringUtils.isEmpty(surv.getAuthority())) {
            if (hasOncAdmin) {
                surv.setAuthority(Authority.ROLE_ADMIN);
            } else if (hasAcbAdmin) {
                surv.setAuthority(Authority.ROLE_ACB);
            }
        }
    }
}
