package gov.healthit.chpl.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductEntity;
import gov.healthit.chpl.exception.DeprecatedUploadTemplateException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.impl.SecuredManager;
import gov.healthit.chpl.upload.certifiedProduct.CertifiedProductUploadHandler;
import gov.healthit.chpl.upload.certifiedProduct.CertifiedProductUploadHandlerFactory;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Service("certifiedProductUploadManager")
public class CertifiedProductUploadManager extends SecuredManager {
    private static final Logger LOGGER = LogManager.getLogger(CertifiedProductUploadManager.class);

    private ErrorMessageUtil msgUtil;
    private CertifiedProductUploadHandlerFactory uploadHandlerFactory;

    public CertifiedProductUploadManager() {
    }

    @Autowired
    public CertifiedProductUploadManager(ErrorMessageUtil msgUtil,
            CertifiedProductUploadHandlerFactory uploadHandlerFactory) {

        this.msgUtil = msgUtil;
        this.uploadHandlerFactory = uploadHandlerFactory;
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFIED_PRODUCT, "
            + "T(gov.healthit.chpl.permissions.domains.CertifiedProductDomainPermissions).UPLOAD)")
    @Transactional(readOnly = false)
    public List<PendingCertifiedProductEntity> parseListingsFromFile(MultipartFile file)
            throws DeprecatedUploadTemplateException, InvalidArgumentsException, ValidationException,
            JsonProcessingException {
        if (file.isEmpty()) {
            throw new ValidationException(msgUtil.getMessage("upload.emptyFile"));
        }

        if (!file.getContentType().equalsIgnoreCase("text/csv")
                && !file.getContentType().equalsIgnoreCase("application/vnd.ms-excel")) {
            throw new ValidationException(msgUtil.getMessage("upload.notCSV"));
        }

        List<CSVRecord> allRecords = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
                CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL)) {
            allRecords = parser.getRecords();
            if (allRecords.size() <= 1) {
                throw new ValidationException(msgUtil.getMessage("listing.upload.emptyRows"));
            }
        } catch (IOException ioEx) {
            LOGGER.error("Could not get input stream for uploaded file " + file.getName());
            throw new ValidationException(msgUtil.getMessage("listing.upload.couldNotParse", file.getName()));
        }

        List<PendingCertifiedProductEntity> listingsToAdd = new ArrayList<PendingCertifiedProductEntity>();
        if (allRecords != null && allRecords.size() > 0) {
            CertifiedProductUploadHandler uploadHandler = uploadHandlerFactory.getHandler(allRecords.get(0));
            LinkedHashMap<Integer, List<CSVRecord>> parsedListingData
                = groupListingRecords(allRecords.subList(1, allRecords.size()));
            Set<String> duplicateErrors = getDuplicateErrors(parsedListingData);
            if (duplicateErrors != null && duplicateErrors.size() > 0) {
                throw new ValidationException(duplicateErrors);
            }

            Set<String> handlerErrors = new HashSet<String>();
            for (List<CSVRecord> currListingRecords : parsedListingData.values()) {
                uploadHandler.setRecord(currListingRecords);
                if (uploadHandler.getUploadTemplateVersion() != null
                        && uploadHandler.getUploadTemplateVersion().getDeprecated()) {
                    throw new DeprecatedUploadTemplateException();
                }

                try {
                    PendingCertifiedProductEntity pendingCp = uploadHandler.handle();
                    listingsToAdd.add(pendingCp);
                } catch (InvalidArgumentsException ex) {
                    LOGGER.error("Failed uploading file " + file.getName(), ex);
                    handlerErrors.add(ex.getMessage());
                } catch (Exception ex) {
                    LOGGER.error("Failed uploading file " + file.getName(), ex);
                    handlerErrors.add(ex.getMessage());
                }
            }

            if (handlerErrors.size() > 0) {
                throw new ValidationException(handlerErrors);
            }
            Set<String> allErrors = new HashSet<String>();
            for (PendingCertifiedProductEntity listingToAdd : listingsToAdd) {
                if (listingToAdd.getErrorMessages() != null && listingToAdd.getErrorMessages().size() > 0) {
                    allErrors.addAll(listingToAdd.getErrorMessages());
                }
            }
            if (allErrors.size() > 0) {
                throw new ValidationException(allErrors, null);
            }
        }
        return listingsToAdd;
    }

    private LinkedHashMap<Integer, List<CSVRecord>> groupListingRecords(List<CSVRecord> allRecords) {
        LinkedHashMap<Integer, List<CSVRecord>> listingRecordGroups = new LinkedHashMap<Integer, List<CSVRecord>>();
        List<CSVRecord> listingRows = new ArrayList<CSVRecord>();
        int listingCount = 0;

        for (CSVRecord record : allRecords) {
            String currUniqueId = record.get(0);
            if (!StringUtils.isEmpty(currUniqueId)) {
                String currStatus = record.get(1);
                if (currStatus.equalsIgnoreCase("NEW") && listingRows.size() > 0) {
                    listingRecordGroups.put(++listingCount, new ArrayList<CSVRecord>(listingRows));
                    listingRows.clear();
                }
            }
            listingRows.add(record);
        }
        listingRecordGroups.put(++listingCount, listingRows);
        return listingRecordGroups;
    }

    private Set<String> getDuplicateErrors(LinkedHashMap<Integer, List<CSVRecord>> groupedListingRecords) {
        Set<String> duplicateErrors = new LinkedHashSet<String>();
        Set<String> uniqueIds = new LinkedHashSet<String>();
        for (List<CSVRecord> listingRecords : groupedListingRecords.values()) {
            if (listingRecords.size() > 0 && listingRecords.get(0) != null) {
                CSVRecord firstRecord = listingRecords.get(0);
                if (!StringUtils.isEmpty(firstRecord.get(0))) {
                    String currUniqueId = firstRecord.get(0);
                    if (!currUniqueId.contains("XXXX") && uniqueIds.contains(currUniqueId)) {
                        duplicateErrors.add(msgUtil.getMessage("upload.duplicateUniqueIds", currUniqueId));
                    }
                    uniqueIds.add(currUniqueId);
                }
            }
        }
        return duplicateErrors;
    }
}
