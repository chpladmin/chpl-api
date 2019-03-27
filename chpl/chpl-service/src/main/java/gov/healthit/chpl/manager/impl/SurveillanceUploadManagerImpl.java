package gov.healthit.chpl.manager.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.SurveillanceUploadManager;
import gov.healthit.chpl.permissions.Permissions;
import gov.healthit.chpl.permissions.domains.PendingSurveillanceDomainPermissions;
import gov.healthit.chpl.upload.surveillance.SurveillanceUploadHandler;
import gov.healthit.chpl.upload.surveillance.SurveillanceUploadHandlerFactory;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.FileUtils;

@Service
public class SurveillanceUploadManagerImpl extends SecuredManager implements SurveillanceUploadManager {
    private static final Logger LOGGER = LogManager.getLogger(SurveillanceUploadManagerImpl.class);

    private FileUtils fileUtils;
    private SurveillanceUploadHandlerFactory uploadHandlerFactory;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public SurveillanceUploadManagerImpl(FileUtils fileUtils, SurveillanceUploadHandlerFactory uploadHandlerFactory,
            ErrorMessageUtil errorMessageUtil) {
        this.fileUtils = fileUtils;
        this.uploadHandlerFactory = uploadHandlerFactory;
        this.errorMessageUtil = errorMessageUtil;
    }

    @Override
    public int countSurveillanceRecords(MultipartFile file) throws ValidationException {
        String data = fileUtils.readFileAsString(file);
        return countSurveillanceRecords(data);
    }

    @Override
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
                        "The file appears to have a header line with no other information. Please make sure there "
                                + "are at least two rows in the CSV file.");
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
                                    for (String error : errors) {
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
                        for (String error : errors) {
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
    public List<String> checkUploadedSurveillanceOwnership(Surveillance pendingSurv) {
        List<String> errors = new ArrayList<String>();
        // perform additional checks if there are no errors in the uploaded
        // surveillance already
        if (pendingSurv.getErrorMessages() == null || pendingSurv.getErrorMessages().size() == 0) {
            if (!permissions.hasAccess(Permissions.PENDING_SURVEILLANCE, PendingSurveillanceDomainPermissions.UPLOAD, pendingSurv)) {
                String msg = errorMessageUtil.getMessage(
                        "pendingSurveillance.addSurveillancePermissionDenied",
                        pendingSurv.getCertifiedProduct().getChplProductNumber());
                LOGGER.error("User " + Util.getCurrentUser().getSubjectName()
                        + " does not have access to " + pendingSurv.getCertifiedProduct().getChplProductNumber()
                        + " due to ACB restrictions.");

                errors.add(msg);
            }
        }
        return errors;
    }


}