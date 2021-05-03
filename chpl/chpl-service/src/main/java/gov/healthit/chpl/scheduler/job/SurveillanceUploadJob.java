package gov.healthit.chpl.scheduler.job;

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
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.surveillance.SurveillanceDAO;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.SurveillanceManager;
import gov.healthit.chpl.manager.SurveillanceUploadManager;
import gov.healthit.chpl.upload.surveillance.SurveillanceUploadHandler;
import gov.healthit.chpl.upload.surveillance.SurveillanceUploadHandlerFactory;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.surveillance.SurveillanceCreationValidator;
import lombok.extern.log4j.Log4j2;

@DisallowConcurrentExecution
@Log4j2(topic = "surveillanceUploadJobLogger")
public class SurveillanceUploadJob implements Job {
    public static final String JOB_NAME = "surveillanceUploadJob";
    public static final String FILE_CONTENTS_KEY = "fileContents";
    public static final String USER_KEY = "user";

    @Autowired
    private Environment env;
    @Autowired
    private ErrorMessageUtil errorMessageUtil;
    @Autowired
    private CertifiedProductManager cpManager;
    @Autowired
    private SurveillanceManager survManager;
    @Autowired
    private SurveillanceUploadManager survUploadManager;
    @Autowired
    private SurveillanceCreationValidator survValidator;
    @Autowired
    private SurveillanceUploadHandlerFactory uploadHandlerFactory;
    @Autowired
    private SurveillanceDAO surveillanceDAO;
    @Autowired
    private CertificationBodyDAO acbDAO;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Surveillance Upload job. *********");

        JobDataMap jobDataMap = jobContext.getMergedJobDataMap();
        UserDTO user = (UserDTO) jobDataMap.get(USER_KEY);
        if (user == null) {
            LOGGER.fatal("No user could be found in the job data.");
        } else {
            setSecurityContext(user);
            String fileContents = (String) jobDataMap.getString(FILE_CONTENTS_KEY);
            Set<Surveillance> pendingSurvs = parseSurveillance(fileContents);
            processPendingSurveillance(pendingSurvs);
        }
        LOGGER.info("********* Completed the Surveillance Upload job. *********");
    }

    private Set<Surveillance> parseSurveillance(String fileContents) {
        Set<Surveillance> pendingSurvs = new LinkedHashSet<Surveillance>();

        try (BufferedReader reader = new BufferedReader(new StringReader(fileContents));
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
                // this is like 2% of the work
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

                    // now do the actual parsing
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

                                if (currRecordStatus
                                        .equalsIgnoreCase(SurveillanceUploadManager.NEW_SURVEILLANCE_BEGIN_INDICATOR)
                                        || currRecordStatus.equalsIgnoreCase(
                                                SurveillanceUploadManager.UPDATE_SURVEILLANCE_BEGIN_INDICATOR)) {
                                    // parse the previous recordset because we hit a new surveillance item
                                    // if this is the last recordset, we'll handle that later
                                    if (rows.size() > 0) {
                                        try {
                                            SurveillanceUploadHandler handler = uploadHandlerFactory.getHandler(heading,
                                                    rows);
                                            Surveillance pendingSurv = handler.handle();
                                            List<String> errors = survUploadManager
                                                    .checkUploadedSurveillanceOwnership(pendingSurv);
                                            // Add any errors that were found when getting the Surveillance
                                            errors.addAll(pendingSurv.getErrorMessages());

                                            for (String error : errors) {
                                                parserErrors.add(error);
                                            }
                                            pendingSurvs.add(pendingSurv);

                                            // Add some percent complete between 2 and 50
                                            jobPercentComplete += 48.0 / survCount;
                                            updateStatus(jobPercentComplete, JobStatusType.In_Progress);
                                        } catch (final InvalidArgumentsException ex) {
                                            LOGGER.error(ex.getMessage());
                                            parserErrors.add("Line " + i + " Error: " + ex.getMessage());
                                        }
                                    }
                                    rows.clear();
                                    rows.add(currRecord);
                                } else if (currRecordStatus
                                        .equalsIgnoreCase(SurveillanceUploadManager.SUBELEMENT_INDICATOR)) {
                                    rows.add(currRecord);
                                } // ignore blank rows
                            }
                        }

                        // add the last object
                        if (i == records.size() && !rows.isEmpty()) {
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
                        for (String error : parserErrors) {
                            addJobMessage(error);
                        }
                        updateStatus(100, JobStatusType.Error);
                    }
                    jobPercentComplete = 50.0;
                    updateStatus(jobPercentComplete, JobStatusType.In_Progress);
                }
            }
        } catch (final IOException ioEx) {
            String msg = errorMessageUtil.getMessage("surveillance.inputStream");
            LOGGER.error(msg);
            addJobMessage(msg);
            updateStatus(100, JobStatusType.Error);
        }
        return pendingSurvs;
    }

    private void processPendingSurveillance(Set<Surveillance> pendingSurvs) {
        for (Surveillance surv : pendingSurvs) {
            CertifiedProductDTO owningCp = null;
            if (surv != null && surv.getCertifiedProduct() != null && surv.getCertifiedProduct().getId() != null) {
                try {
                    owningCp = cpManager.getById(surv.getCertifiedProduct().getId());

                    survValidator.validate(surv);
                    surveillanceDAO.insertPendingSurveillance(surv);

                    jobPercentComplete += 50.0 / pendingSurvs.size();
                    updateStatus(jobPercentComplete, JobStatusType.In_Progress);
                } catch (final AccessDeniedException denied) {
                    String msg = "";
                    if (owningCp != null && owningCp.getCertificationBodyId() != null) {
                        try {
                            CertificationBodyDTO acbDTO = acbDAO.getById(owningCp.getCertificationBodyId());
                            msg = errorMessageUtil.getMessage("surveillance.permissionErrorWithAcb",
                                    AuthUtil.getCurrentUser().getSubjectName(), owningCp.getChplProductNumber(),
                                    acbDTO.getName());
                        } catch (Exception e) {
                            msg = errorMessageUtil.getMessage("surveillance.permissionError",
                                    AuthUtil.getCurrentUser().getSubjectName());
                        }
                    } else {
                        msg = errorMessageUtil.getMessage("surveillance.permissionError",
                                AuthUtil.getCurrentUser().getSubjectName());
                    }

                    LOGGER.error(msg);
                    addJobMessage(msg);
                } catch (Exception ex) {
                    String msg = errorMessageUtil.getMessage("surveillance.errorAdding");
                    LOGGER.error(msg);
                    addJobMessage(msg);
                }
            }
        }
        this.complete();
    }

    private void setSecurityContext(UserDTO user) {
        JWTAuthenticatedUser jobUser = new JWTAuthenticatedUser();
        jobUser.setFullName(user.getFullName());
        jobUser.setId(user.getId());
        jobUser.setFriendlyName(user.getFriendlyName());
        jobUser.setSubjectName(user.getUsername());
        jobUser.getPermissions().add(user.getPermission().getGrantedPermission());

        SecurityContextHolder.getContext().setAuthentication(jobUser);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }
}
