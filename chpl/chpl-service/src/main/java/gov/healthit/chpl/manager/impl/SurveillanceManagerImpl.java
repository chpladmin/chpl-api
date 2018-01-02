package gov.healthit.chpl.manager.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.dao.UserDAO;
import gov.healthit.chpl.auth.dao.UserPermissionDAO;
import gov.healthit.chpl.auth.domain.Authority;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.dto.UserPermissionDTO;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.Contact;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;
import gov.healthit.chpl.domain.SurveillanceNonconformityDocument;
import gov.healthit.chpl.domain.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.SurveillanceRequirement;
import gov.healthit.chpl.domain.SurveillanceRequirementType;
import gov.healthit.chpl.domain.SurveillanceResultType;
import gov.healthit.chpl.domain.SurveillanceType;
import gov.healthit.chpl.domain.concept.ActivityConcept;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.ValidationMessageType;
import gov.healthit.chpl.entity.listing.CertifiedProductEntity;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceEntity;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceNonconformityEntity;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceRequirementEntity;
import gov.healthit.chpl.entity.surveillance.PendingSurveillanceValidationEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceNonconformityDocumentationEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceNonconformityEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceRequirementEntity;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.SurveillanceManager;
import gov.healthit.chpl.upload.surveillance.SurveillanceUploadHandler;
import gov.healthit.chpl.upload.surveillance.SurveillanceUploadHandlerFactory;
import gov.healthit.chpl.util.FileUtils;
import gov.healthit.chpl.validation.surveillance.SurveillanceValidator;
import gov.healthit.chpl.web.controller.InvalidArgumentsException;
import gov.healthit.chpl.web.controller.exception.ObjectMissingValidationException;
import gov.healthit.chpl.web.controller.exception.ValidationException;

@Service
public class SurveillanceManagerImpl implements SurveillanceManager {
    private static final Logger LOGGER = LogManager.getLogger(SurveillanceManagerImpl.class);
    @Autowired
    private Environment env;

    @Autowired
    MessageSource messageSource;
    @Autowired
    private CertifiedProductManager cpManager;
    @Autowired
    private CertificationBodyManager acbManager;
    
    @Autowired
    SurveillanceDAO survDao;
    @Autowired
    CertifiedProductDAO cpDao;
    @Autowired
    UserDAO userDAO;
    @Autowired
    private SurveillanceUploadHandlerFactory uploadHandlerFactory;
    @Autowired
    SurveillanceValidator validator;
    @Autowired
    UserPermissionDAO userPermissionDao;

    @Autowired
    private ActivityManager activityManager;

    @Override
    @Transactional(readOnly = true)
    public Surveillance getById(Long survId) throws EntityRetrievalException {
        SurveillanceEntity surv = survDao.getSurveillanceById(survId);
        Surveillance result = convertToDomain(surv);
        validator.validate(result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Surveillance getByFriendlyIdAndProduct(Long certifiedProductId, String survFriendlyId) {
        SurveillanceEntity surv = survDao.getSurveillanceByCertifiedProductAndFriendlyId(certifiedProductId,
                survFriendlyId);
        if (surv == null) {
            throw new EntityNotFoundException("Could not find surveillance for certified product " + certifiedProductId
                    + " with friendly id " + survFriendlyId);
        }
        Surveillance result = convertToDomain(surv);
        validator.validate(result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Surveillance> getByCertifiedProduct(Long cpId) {
        List<SurveillanceEntity> survResults = survDao.getSurveillanceByCertifiedProductId(cpId);
        List<Surveillance> results = new ArrayList<Surveillance>();
        if (survResults != null) {
            for (SurveillanceEntity survResult : survResults) {
                Surveillance surv = convertToDomain(survResult);
                validator.validate(surv);
                results.add(surv);
            }
        }
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public SurveillanceNonconformityDocument getDocumentById(Long docId, boolean getFileContents)
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
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_ACB') "
            + "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
    public Long createSurveillance(Long acbId, Surveillance surv)
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
    @PreAuthorize("hasRole('ROLE_ADMIN') or " + "(hasRole('ROLE_ACB') "
            + "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
    public Long addDocumentToNonconformity(Long acbId, Long nonconformityId, SurveillanceNonconformityDocument doc)
            throws EntityRetrievalException {
        Long insertedId = null;
        insertedId = survDao.insertNonconformityDocument(nonconformityId, doc);
        return insertedId;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_ACB') "
            + "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
    public void updateSurveillance(Long acbId, Surveillance surv) throws EntityRetrievalException,
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
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_ACB') "
            + "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
    public void deleteSurveillance(Long acbId, Surveillance surv)
            throws EntityRetrievalException, SurveillanceAuthorityAccessDeniedException {
        checkSurveillanceAuthority(surv);
        survDao.deleteSurveillance(surv);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_ACB') "
            + "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
    public void deleteNonconformityDocument(Long acbId, Long documentId) throws EntityRetrievalException {
        survDao.deleteNonconformityDocument(documentId);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_ACB') "
            + "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)")
    public List<Surveillance> getPendingByAcb(Long acbId) {
        List<PendingSurveillanceEntity> pendingResults = survDao.getPendingSurveillanceByAcb(acbId);
        List<Surveillance> results = new ArrayList<Surveillance>();
        if (pendingResults != null) {
            for (PendingSurveillanceEntity pr : pendingResults) {
                Surveillance surv = convertToDomain(pr);
                results.add(surv);
            }
        }
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_ACB') "
            + "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)")
    public Surveillance getPendingById(Long acbId, Long survId, boolean includeDeleted)
            throws EntityRetrievalException {
        PendingSurveillanceEntity pending = survDao.getPendingSurveillanceById(survId, includeDeleted);
        Surveillance surv = convertToDomain(pending);
        return surv;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ACB') "
            + "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)")
    public Long createPendingSurveillance(Long acbId, Surveillance surv) {
        Long insertedId = null;

        try {
            insertedId = survDao.insertPendingSurveillance(surv);
        } catch (Exception ex) {
            LOGGER.error("Error inserting pending surveillance.", ex);
        }

        return insertedId;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ACB') "
            + "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)")
    public void deletePendingSurveillance(Long acbId, Long survId, boolean isConfirmed)
            throws ObjectMissingValidationException, JsonProcessingException, EntityRetrievalException,
            EntityCreationException {
        PendingSurveillanceEntity surv = survDao.getPendingSurveillanceById(survId, true);
        CertifiedProductEntity ownerCp = surv.getCertifiedProduct();
        if (ownerCp == null) {
            throw new EntityNotFoundException("Could not find certified product associated with pending surveillance.");
        }

        Surveillance toDelete = getPendingById(acbId, survId, true);

        if (isPendingSurveillanceAvailableForUpdate(ownerCp.getCertificationBodyId(), surv)) {
            try {
                survDao.deletePendingSurveillance(toDelete);
            } catch (Exception ex) {
                LOGGER.error("Error marking pending surveillance with id " + toDelete.getId() + " as deleted.", ex);
            }
            StringBuilder activityMsg = new StringBuilder()
                    .append("Pending surveillance " + toDelete.getId() + " has been ");
            if (isConfirmed) {
                activityMsg.append("confirmed.");
            } else {
                activityMsg.append("rejected.");
            }
            activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_PENDING_SURVEILLANCE, toDelete.getId(),
                    activityMsg.toString(), toDelete, null);
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ACB')")
    public void deletePendingSurveillance(List<CertificationBodyDTO> userAcbs, Long survId, boolean isConfirmed)
            throws EntityNotFoundException, AccessDeniedException, ObjectMissingValidationException,
            JsonProcessingException, EntityRetrievalException, EntityCreationException {
        PendingSurveillanceEntity surv = survDao.getPendingSurveillanceById(survId, true);
        if (surv == null) {
            throw new EntityNotFoundException("Could not find pending surveillance with id " + survId);
        }
        CertifiedProductEntity ownerCp = surv.getCertifiedProduct();
        if (ownerCp == null) {
            throw new EntityNotFoundException("Could not find certified product associated with pending surveillance.");
        }
        boolean userHasAcbPermissions = false;
        for (CertificationBodyDTO acb : userAcbs) {
            if (acb.getId() != null && ownerCp.getCertificationBodyId() != null
                    && acb.getId().longValue() == ownerCp.getCertificationBodyId().longValue()) {
                userHasAcbPermissions = true;
            }
        }

        if (!userHasAcbPermissions) {
            throw new AccessDeniedException("Permission denied on ACB " + ownerCp.getCertificationBodyId()
                    + " for user " + Util.getCurrentUser().getSubjectName());
        }

        if (isPendingSurveillanceAvailableForUpdate(ownerCp.getCertificationBodyId(), surv)) {
            Surveillance toDelete = convertToDomain(surv);
            try {
                survDao.deletePendingSurveillance(toDelete);
            } catch (Exception ex) {
                LOGGER.error("Error marking pending surveillance with id " + toDelete.getId() + " as deleted.", ex);
            }

            StringBuilder activityMsg = new StringBuilder()
                    .append("Pending surveillance " + toDelete.getId() + " has been ");
            if (isConfirmed) {
                activityMsg.append("confirmed.");
            } else {
                activityMsg.append("rejected.");
            }
            activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_PENDING_SURVEILLANCE, toDelete.getId(),
                    activityMsg.toString(), toDelete, null);
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ACB') "
            + "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)")
    public boolean isPendingSurveillanceAvailableForUpdate(Long acbId, Long pendingSurvId)
            throws EntityRetrievalException, ObjectMissingValidationException {
        PendingSurveillanceEntity pendingSurv = survDao.getPendingSurveillanceById(pendingSurvId, true);
        return isPendingSurveillanceAvailableForUpdate(acbId, pendingSurv);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ACB') "
            + "and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin)")
    public boolean isPendingSurveillanceAvailableForUpdate(Long acbId, PendingSurveillanceEntity pendingSurv)
            throws EntityRetrievalException, ObjectMissingValidationException {
        if (pendingSurv.getDeleted().booleanValue() == true) {
            ObjectMissingValidationException alreadyDeletedEx = new ObjectMissingValidationException();
            alreadyDeletedEx.getErrorMessages()
                    .add("This pending surveillance has already been confirmed or rejected by another user.");
            alreadyDeletedEx.setObjectId(pendingSurv.getId().toString());
            alreadyDeletedEx.setStartDate(pendingSurv.getStartDate());
            alreadyDeletedEx.setEndDate(pendingSurv.getEndDate());

            try {
                UserDTO lastModifiedUser = userDAO.getById(pendingSurv.getLastModifiedUser());
                if (lastModifiedUser != null) {
                    Contact contact = new Contact();
                    contact.setFirstName(lastModifiedUser.getFirstName());
                    contact.setLastName(lastModifiedUser.getLastName());
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
        }
        return pendingSurv != null;
    }

    @Override
    @Transactional(readOnly = true)
    public void validate(Surveillance surveillance) {
        validator.validate(surveillance);
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ONC_STAFF')")
    public File getProtectedDownloadFile(String filenameToDownload) throws IOException {
        return getFileFromDownloadFolder(filenameToDownload);
    }

    @Override
    public File getDownloadFile(String filenameToDownload) throws IOException {
        return getFileFromDownloadFolder(filenameToDownload);
    }

    private File getFileFromDownloadFolder(String filenameToDownload) throws IOException {
        String downloadFileLocation = env.getProperty("downloadFolderPath");

        File downloadFile = new File(downloadFileLocation + File.separator + filenameToDownload);
        if (!downloadFile.exists() || !downloadFile.canRead()) {
            throw new IOException("Cannot read download file at " + downloadFileLocation
                    + ". File does not exist or cannot be read.");
        }
        return downloadFile;
    }
    
    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ACB')")
    public int countSurveillanceRecords(MultipartFile file) throws ValidationException {
        String data = FileUtils.readFileAsString(file);
        return countSurveillanceRecords(data);
    }
    
    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ACB')")
    public int countSurveillanceRecords(String fileContents) throws ValidationException {
        int survCount = 0;
        
        BufferedReader reader = null;
        CSVParser parser = null;
        try {
            reader = new BufferedReader(new StringReader(fileContents));
            parser = new CSVParser(reader, CSVFormat.EXCEL);

            List<CSVRecord> records = parser.getRecords();
            if (records.size() <= 1) {
                throw new ValidationException(
                        "The file appears to have a header line with no other information. Please make sure there are at least two rows in the CSV file.");
            }
            CSVRecord heading = null;
            for (int i = 0; i < records.size(); i++) {
                CSVRecord currRecord = records.get(i);
    
                if (heading == null && !StringUtils.isEmpty(currRecord.get(1))
                        && currRecord.get(0).equals(HEADING_CELL_INDICATOR)) {
                    // have to find the heading first
                    heading = currRecord;
                } else if (heading != null) {
                    if (!StringUtils.isEmpty(currRecord.get(0).trim())) {
                        String currRecordStatus = currRecord.get(0).trim();
    
                        if (currRecordStatus.equalsIgnoreCase(NEW_SURVEILLANCE_BEGIN_INDICATOR)
                                || currRecordStatus.equalsIgnoreCase(UPDATE_SURVEILLANCE_BEGIN_INDICATOR)) {
                            // we hit a new surveillance item
                            survCount++;
                        }
                    }
                }
            }
        } catch (final IOException ioEx) {
            String msg = "Could not read the uploaded file as a CSV.";
            LOGGER.error(msg);
            throw new ValidationException(msg);
        } finally {
            try {
                parser.close();
            } catch (Exception ignore) {
            }
            try {
                reader.close();
            } catch (Exception ignore) {
            }
        }
        return survCount;
    }
    
    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ACB')")
    public List<Surveillance> parseUploadFile(MultipartFile file) throws ValidationException {
        List<Surveillance> pendingSurvs = new ArrayList<Surveillance>();

        BufferedReader reader = null;
        CSVParser parser = null;
        try {
            reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            parser = new CSVParser(reader, CSVFormat.EXCEL);

            List<CSVRecord> records = parser.getRecords();
            if (records.size() <= 1) {
                throw new ValidationException(
                        "The file appears to have a header line with no other information. Please make sure there are at least two rows in the CSV file.");
            }

            Set<String> handlerErrors = new HashSet<String>();
            
            //parse the entire file into groups of records, 
            //one group per surveillance item
            CSVRecord heading = null;
            List<CSVRecord> rows = new ArrayList<CSVRecord>();
            for (int i = 0; i < records.size(); i++) {
                CSVRecord currRecord = records.get(i);

                if (heading == null && !StringUtils.isEmpty(currRecord.get(1))
                        && currRecord.get(0).equals(HEADING_CELL_INDICATOR)) {
                    // have to find the heading first
                    heading = currRecord;
                } else if (heading != null) {
                    if (!StringUtils.isEmpty(currRecord.get(0).trim())) {
                        String currRecordStatus = currRecord.get(0).trim();

                        if (currRecordStatus.equalsIgnoreCase(NEW_SURVEILLANCE_BEGIN_INDICATOR)
                                || currRecordStatus.equalsIgnoreCase(UPDATE_SURVEILLANCE_BEGIN_INDICATOR)) {
                            // parse the previous recordset because we hit a new surveillance item
                            // if this is the last recordset, we'll handle that later
                            if (rows.size() > 0) {
                                try {
                                    SurveillanceUploadHandler handler = uploadHandlerFactory.getHandler(heading, rows);
                                    Surveillance pendingSurv = handler.handle();
                                    List<String> errors = checkUploadedSurveillanceOwnership(pendingSurv);
                                    for(String error : errors) {
                                        pendingSurv.getErrorMessages().add(error);
                                    }
                                    pendingSurvs.add(pendingSurv);
                                } catch (final InvalidArgumentsException ex) {
                                    handlerErrors.add(ex.getMessage());
                                }
                            }
                            rows.clear();
                            rows.add(currRecord);
                        } else if (currRecordStatus.equalsIgnoreCase(SUBELEMENT_INDICATOR)) {
                            rows.add(currRecord);
                        } // ignore blank rows
                    }
                }

                // add the last object
                if (i == records.size() - 1 && !rows.isEmpty()) {
                    try {
                        SurveillanceUploadHandler handler = uploadHandlerFactory.getHandler(heading, rows);
                        Surveillance pendingSurv = handler.handle();
                        List<String> errors = checkUploadedSurveillanceOwnership(pendingSurv);
                        for(String error : errors) {
                            pendingSurv.getErrorMessages().add(error);
                        }
                        pendingSurvs.add(pendingSurv);
                    } catch (final InvalidArgumentsException ex) {
                        handlerErrors.add(ex.getMessage());
                    }
                }
            }
            if (heading == null) {
                handlerErrors.add("Could not find heading row in the uploaded file.");
            }

            // if we couldn't parse the files (bad format or something), stop
            // here with the errors
            if (handlerErrors.size() > 0) {
                throw new ValidationException(handlerErrors, null);
            }

            // we parsed the files but maybe some of the data in them has errors
            // that are too severe to continue putting them in the database
            Set<String> allErrors = new HashSet<String>();
            for (Surveillance surv : pendingSurvs) {
                if (surv.getErrorMessages() != null && surv.getErrorMessages().size() > 0) {
                    allErrors.addAll(surv.getErrorMessages());
                }
            }

            if (allErrors.size() > 0) {
                throw new ValidationException(allErrors, null);
            }
        } catch (final IOException ioEx) {
            LOGGER.error("Could not get input stream for uploaded file " + file.getName());
            throw new ValidationException("Could not get input stream for uploaded file " + file.getName());
        } finally {
            try {
                parser.close();
            } catch (Exception ignore) {
            }
            try {
                reader.close();
            } catch (Exception ignore) {
            }
        }
        return pendingSurvs;
    }
    
    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ACB')")
    public List<String> checkUploadedSurveillanceOwnership(Surveillance pendingSurv) {
        List<String> errors = new ArrayList<String>();
        // perform additional checks if there are no errors in the uploaded
        // surveillance already
        if (pendingSurv.getErrorMessages() == null || pendingSurv.getErrorMessages().size() == 0) {
            // check this pendingSurv to confirm the user has ACB permissions on
            // the appropriate ACB for the CHPL ID specified
            CertifiedProductDTO surveilledProduct = null;
            try {
                surveilledProduct = cpManager.getById(pendingSurv.getCertifiedProduct().getId());
            } catch (final EntityRetrievalException ex) {
                String msg = String.format(
                        messageSource.getMessage(
                                new DefaultMessageSourceResolvable(
                                        "pendingSurveillance.certifiedProductIdNotFound"),
                                LocaleContextHolder.getLocale()),
                        pendingSurv.getCertifiedProduct().getId());
                LOGGER.error(msg);
                errors.add(msg);
            }

            if (surveilledProduct != null) {
                try {
                    acbManager.getById(surveilledProduct.getCertificationBodyId());
                } catch (final EntityRetrievalException ex) {
                    String msg = String.format(messageSource.getMessage(
                            new DefaultMessageSourceResolvable(
                                    "pendingSurveillance.certificationBodyIdNotFound"),
                            LocaleContextHolder.getLocale()),
                    surveilledProduct.getCertificationBodyId());
                    LOGGER.error(msg);
                    errors.add(msg);
                } catch (final AccessDeniedException denied) {
                    String msg = String.format(
                            messageSource.getMessage(
                                    new DefaultMessageSourceResolvable(
                                            "pendingSurveillance.addSurveillancePermissionDenied"),
                                    LocaleContextHolder.getLocale()),
                            pendingSurv.getCertifiedProduct().getChplProductNumber());
                    LOGGER.error("User " + Util.getCurrentUser().getSubjectName()
                            + " does not have access to the ACB with id " + surveilledProduct.getCertificationBodyId());
                    errors.add(msg);
                }
            }
        }
        return errors;
    }
    
    private Surveillance convertToDomain(PendingSurveillanceEntity pr) {
        Surveillance surv = new Surveillance();
        surv.setId(pr.getId());
        surv.setSurveillanceIdToReplace(pr.getSurvFriendlyIdToReplace());
        surv.setStartDate(pr.getStartDate());
        surv.setEndDate(pr.getEndDate());
        surv.setRandomizedSitesUsed(pr.getNumRandomizedSites());
        if (pr.getCertifiedProduct() != null) {
            CertifiedProductEntity cpEntity = pr.getCertifiedProduct();
            try {
                CertifiedProductDetailsDTO cpDto = cpDao.getDetailsById(cpEntity.getId());
                surv.setCertifiedProduct(new CertifiedProduct(cpDto));
            } catch (final EntityRetrievalException ex) {
                LOGGER.error("Could not find details for certified product " + cpEntity.getId());
            }
        } else {
            CertifiedProduct cp = new CertifiedProduct();
            cp.setId(pr.getCertifiedProductId());
            cp.setChplProductNumber(pr.getCertifiedProductUniqueId());
            surv.setCertifiedProduct(cp);
        }

        SurveillanceType survType = new SurveillanceType();
        survType.setName(pr.getSurveillanceType());
        surv.setType(survType);

        if (pr.getSurveilledRequirements() != null) {
            for (PendingSurveillanceRequirementEntity preq : pr.getSurveilledRequirements()) {
                SurveillanceRequirement req = new SurveillanceRequirement();
                req.setId(preq.getId());
                req.setRequirement(preq.getSurveilledRequirement());
                SurveillanceResultType result = new SurveillanceResultType();
                result.setName(preq.getResult());
                req.setResult(result);
                SurveillanceRequirementType reqType = new SurveillanceRequirementType();
                reqType.setName(preq.getRequirementType());
                req.setType(reqType);

                if (preq.getNonconformities() != null) {
                    for (PendingSurveillanceNonconformityEntity pnc : preq.getNonconformities()) {
                        SurveillanceNonconformity nc = new SurveillanceNonconformity();
                        nc.setCapApprovalDate(pnc.getCapApproval());
                        nc.setCapEndDate(pnc.getCapEndDate());
                        nc.setCapMustCompleteDate(pnc.getCapMustCompleteDate());
                        nc.setCapStartDate(pnc.getCapStart());
                        nc.setDateOfDetermination(pnc.getDateOfDetermination());
                        nc.setDeveloperExplanation(pnc.getDeveloperExplanation());
                        nc.setFindings(pnc.getFindings());
                        nc.setId(pnc.getId());
                        nc.setNonconformityType(pnc.getType());
                        nc.setResolution(pnc.getResolution());
                        nc.setSitesPassed(pnc.getSitesPassed());
                        nc.setSummary(pnc.getSummary());
                        nc.setTotalSites(pnc.getTotalSites());
                        SurveillanceNonconformityStatus status = new SurveillanceNonconformityStatus();
                        status.setName(pnc.getStatus());
                        nc.setStatus(status);
                        req.getNonconformities().add(nc);
                    }
                }
                surv.getRequirements().add(req);
            }
        }

        if (pr.getValidation() != null && pr.getValidation().size() > 0) {
            for (PendingSurveillanceValidationEntity validation : pr.getValidation()) {
                if (validation.getMessageType() == ValidationMessageType.Error) {
                    surv.getErrorMessages().add(validation.getMessage());
                }
            }
        }
        return surv;
    }

    private SurveillanceNonconformityDocument convertToDomain(SurveillanceNonconformityDocumentationEntity entity,
            boolean getContents) {
        SurveillanceNonconformityDocument doc = new SurveillanceNonconformityDocument();
        doc.setId(entity.getId());
        doc.setFileType(entity.getFileType());
        doc.setFileName(entity.getFileName());
        if (getContents) {
            doc.setFileContents(entity.getFileData());
        }
        return doc;
    }

    private Surveillance convertToDomain(SurveillanceEntity entity) {
        Surveillance surv = new Surveillance();
        surv.setId(entity.getId());
        surv.setFriendlyId(entity.getFriendlyId());
        surv.setStartDate(entity.getStartDate());
        surv.setEndDate(entity.getEndDate());
        surv.setRandomizedSitesUsed(entity.getNumRandomizedSites());
        surv.setAuthority(userPermissionDao.findById(entity.getUserPermissionId()).getAuthority());

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

    private void checkSurveillanceAuthority(Surveillance surv) throws SurveillanceAuthorityAccessDeniedException {
        Boolean hasOncAdmin = Util.isUserRoleAdmin();
        Boolean hasAcbAdmin = Util.isUserRoleAcbAdmin();
        if (StringUtils.isEmpty(surv.getAuthority())) {
            // If user has ROLE_ADMIN and ROLE_ACB
            // return 403
            if (hasOncAdmin && hasAcbAdmin) {
                String errorMsg = "Surveillance cannot be created by user having " + Authority.ROLE_ADMIN + " and "
                        + Authority.ROLE_ACB;
                LOGGER.error(errorMsg);
                throw new SurveillanceAuthorityAccessDeniedException(errorMsg);
            }
        } else {
            // Cannot have surveillance authority as ROLE_ADMIN for user lacking
            // ROLE_ADMIN
            if (surv.getAuthority().equalsIgnoreCase(Authority.ROLE_ADMIN) && !hasOncAdmin) {
                String errorMsg = "User must have authority " + Authority.ROLE_ADMIN;
                LOGGER.error(errorMsg);
                throw new SurveillanceAuthorityAccessDeniedException(errorMsg);
            }
            // Cannot have surveillance authority as ACB for user lacking ONC
            // and ACB roles
            else if (surv.getAuthority().equalsIgnoreCase(Authority.ROLE_ACB)) {
                if (!hasOncAdmin && !hasAcbAdmin ) {
                    String errorMsg = "User must have ONC or ACB roles for a surveillance authority created by ACB";
                    LOGGER.error(errorMsg);
                    throw new SurveillanceAuthorityAccessDeniedException(errorMsg);
                }
            }
        }
    }

    private void updateNullAuthority(Surveillance surv) {
        Boolean hasOncAdmin = Util.isUserRoleAdmin();
        Boolean hasAcbAdmin = Util.isUserRoleAcbAdmin();
        if (StringUtils.isEmpty(surv.getAuthority())) {
            if (hasOncAdmin) {
                surv.setAuthority(Authority.ROLE_ADMIN);
            } else if (hasAcbAdmin) {
                surv.setAuthority(Authority.ROLE_ACB);
            }
        }
    }
}
