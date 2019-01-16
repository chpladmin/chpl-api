package gov.healthit.chpl.job;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.job.JobDTO;
import gov.healthit.chpl.entity.job.JobStatusType;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.SurveillanceManager;
import gov.healthit.chpl.manager.SurveillanceUploadManager;
import gov.healthit.chpl.upload.surveillance.SurveillanceUploadHandler;
import gov.healthit.chpl.upload.surveillance.SurveillanceUploadHandlerFactory;
import gov.healthit.chpl.validation.surveillance.SurveillanceValidator;

@Component
@Scope("prototype") // tells spring to make a new instance of this class every
// time it is needed
public class SurveillanceUploadJob extends RunnableJob {
    private static final Logger LOGGER = LogManager.getLogger(SurveillanceUploadJob.class);

    private MessageSource messageSource;
    private CertifiedProductManager cpManager;
    private SurveillanceManager survManager;
    private SurveillanceUploadManager survUploadManager;
    private SurveillanceValidator survValidator;
    private SurveillanceUploadHandlerFactory uploadHandlerFactory;
    private SurveillanceDAO surveillanceDAO;

    @Autowired
    public SurveillanceUploadJob(final MessageSource messageSource, final CertifiedProductManager cpManager,
            final SurveillanceManager survManager, final SurveillanceUploadManager survUploadManager,
            final SurveillanceValidator survValidator, final SurveillanceUploadHandlerFactory uploadHandlerFactory,
            final SurveillanceDAO surveillanceDAO) {
        this.messageSource = messageSource;
        this.cpManager = cpManager;
        this.survManager = survManager;
        this.survUploadManager = survUploadManager;
        this.survValidator = survValidator;
        this.uploadHandlerFactory = uploadHandlerFactory;
        this.surveillanceDAO = surveillanceDAO;
    }

    public SurveillanceUploadJob() {
        LOGGER.debug("Created new Surveillance Upload Job");
    }

    public SurveillanceUploadJob(final JobDTO job) {
        LOGGER.debug("Created new Surveillance Upload Job");
        this.job = job;
    }

    @Override
    @Transactional
    public void run() {
        super.run();

        double jobPercentComplete = 0;
        Set<Surveillance> pendingSurvs = new LinkedHashSet<Surveillance>();

        try (BufferedReader reader = new BufferedReader(new StringReader(job.getData()));
                CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL)) {

            List<CSVRecord> records = parser.getRecords();
            if (records.size() <= 1) {
                String msg = "The file appears to have a header line with no other information. "
                        + "Please make sure there are at least two rows in the CSV file.";
                LOGGER.error(msg);
                addJobMessage(msg);
                updateStatus(100, JobStatusType.Error);
                try {
                    parser.close();
                } catch (Exception ignore) {
                }
                try {
                    reader.close();
                } catch (Exception ignore) {
                }
            } else {
                // figure out how many surveillances are in the file,
                //this is like 2% of the work
                int survCount = 0;
                try {
                    survCount = survUploadManager.countSurveillanceRecords(job.getData());
                } catch (Exception ex) {
                    addJobMessage(ex.getMessage());
                    updateStatus(100, JobStatusType.Error);
                }
                if (survCount > 0) {
                    jobPercentComplete = 2.0;
                    updateStatus(jobPercentComplete, JobStatusType.In_Progress);

                    //now do the actual parsing
                    List<String> parserErrors = new ArrayList<String>();
                    CSVRecord heading = null;
                    List<CSVRecord> rows = new ArrayList<CSVRecord>();
                    for (int i = 1; i <= records.size(); i++) {
                        CSVRecord currRecord = records.get(i - 1);
                        if (heading == null && !StringUtils.isEmpty(currRecord.get(1))
                                && currRecord.get(0).equals(SurveillanceUploadManager.HEADING_CELL_INDICATOR)) {
                            // have to find the heading first
                            heading = currRecord;
                        } else if (heading != null) {
                            if (!StringUtils.isEmpty(currRecord.get(0).trim())) {
                                String currRecordStatus = currRecord.get(0).trim();

                                if (currRecordStatus.equalsIgnoreCase(SurveillanceUploadManager.NEW_SURVEILLANCE_BEGIN_INDICATOR)
                                        || currRecordStatus.equalsIgnoreCase(
                                                SurveillanceUploadManager.UPDATE_SURVEILLANCE_BEGIN_INDICATOR)) {
                                    // parse the previous recordset because we hit a new surveillance item
                                    // if this is the last recordset, we'll handle that later
                                    if (rows.size() > 0) {
                                        try {
                                            SurveillanceUploadHandler handler = uploadHandlerFactory.getHandler(heading, rows);
                                            Surveillance pendingSurv = handler.handle();
                                            List<String> errors =
                                                    survUploadManager.checkUploadedSurveillanceOwnership(pendingSurv);
                                            for (String error : errors) {
                                                parserErrors.add(error);
                                            }
                                            pendingSurvs.add(pendingSurv);

                                            //Add some percent complete between 2 and 50
                                            jobPercentComplete += 48.0 / survCount;
                                            updateStatus(jobPercentComplete, JobStatusType.In_Progress);
                                        } catch (final InvalidArgumentsException ex) {
                                            LOGGER.error(ex.getMessage());
                                            parserErrors.add("Line " + i + " Error: " + ex.getMessage());
                                        }
                                    }
                                    rows.clear();
                                    rows.add(currRecord);
                                } else if (currRecordStatus.equalsIgnoreCase(SurveillanceUploadManager.SUBELEMENT_INDICATOR)) {
                                    rows.add(currRecord);
                                } // ignore blank rows
                            }
                        }

                        // add the last object
                        if (i == records.size() - 1 && !rows.isEmpty()) {
                            try {
                                SurveillanceUploadHandler handler = uploadHandlerFactory.getHandler(heading, rows);
                                Surveillance pendingSurv = handler.handle();
                                List<String> errors = survUploadManager.checkUploadedSurveillanceOwnership(pendingSurv);
                                for (String error : errors) {
                                    parserErrors.add(error);
                                }
                                pendingSurvs.add(pendingSurv);
                            } catch (final InvalidArgumentsException ex) {
                                LOGGER.error(ex.getMessage());
                                parserErrors.add("Line " + i + " Error: " + ex.getMessage());
                            }
                        }
                    }

                    if (parserErrors != null && parserErrors.size() > 0) {
                        for (String error: parserErrors) {
                            addJobMessage(error);
                        }
                        updateStatus(100, JobStatusType.Error);
                    }
                    jobPercentComplete = 50.0;
                    updateStatus(jobPercentComplete, JobStatusType.In_Progress);
                }
            }
        } catch (final IOException ioEx) {
            String msg = "Could not get input stream for job data string for job with ID " + job.getId();
            LOGGER.error(msg);
            addJobMessage(msg);
            updateStatus(100, JobStatusType.Error);
        }

        // now load everything that was parsed
        for (Surveillance surv : pendingSurvs) {
            CertifiedProductDTO owningCp = null;
            try {
                owningCp = cpManager.getById(surv.getCertifiedProduct().getId());
                survValidator.validate(surv, false);

                //TODO - This needs to be fixed
                //survManager.createPendingSurveillance(owningCp.getCertificationBodyId(), surv);
                surveillanceDAO.insertPendingSurveillance(surv);


                jobPercentComplete += 50.0 / pendingSurvs.size();
                updateStatus(jobPercentComplete, JobStatusType.In_Progress);
            } catch (final AccessDeniedException denied) {
                String msg = "User " + Util.getCurrentUser().getSubjectName()
                        + " does not have access to add surveillance"
                        + (owningCp != null
                        ? " to ACB with ID '" + owningCp.getCertificationBodyId() + "'."
                                : ".");
                LOGGER.error(msg);
                addJobMessage(msg);
            } catch (Exception ex) {
                String msg =
                        "Error adding a new pending surveillance. Please make sure all required fields are present.";
                LOGGER.error(msg);
                addJobMessage(msg);
            }
        }
        this.complete();
    }

    public SurveillanceManager getSurvManager() {
        return survManager;
    }

    public void setSurvManager(final SurveillanceManager survManager) {
        this.survManager = survManager;
    }

    public SurveillanceValidator getSurvValidator() {
        return survValidator;
    }

    public void setSurvValidator(final SurveillanceValidator survValidator) {
        this.survValidator = survValidator;
    }

    public SurveillanceUploadHandlerFactory getUploadHandlerFactory() {
        return uploadHandlerFactory;
    }

    public void setUploadHandlerFactory(final SurveillanceUploadHandlerFactory uploadHandlerFactory) {
        this.uploadHandlerFactory = uploadHandlerFactory;
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(final MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public CertifiedProductManager getCpManager() {
        return cpManager;
    }

    public void setCpManager(final CertifiedProductManager cpManager) {
        this.cpManager = cpManager;
    }
}
