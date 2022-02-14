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
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.PendingSurveillanceManager;
import gov.healthit.chpl.manager.SurveillanceUploadManager;
import gov.healthit.chpl.upload.surveillance.SurveillanceUploadHandler;
import gov.healthit.chpl.upload.surveillance.SurveillanceUploadHandlerFactory;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
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
    private PendingSurveillanceManager pendingSurvManager;

    @Autowired
    private SurveillanceUploadManager survUploadManager;

    @Autowired
    private SurveillanceUploadHandlerFactory uploadHandlerFactory;

    @Autowired
    private CertificationBodyDAO acbDAO;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

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
            String fileContents = jobDataMap.getString(FILE_CONTENTS_KEY);
            Set<String> processingErrors = new LinkedHashSet<String>();
            Set<Surveillance> pendingSurvs = parseSurveillance(fileContents, processingErrors);
            processPendingSurveillance(pendingSurvs, processingErrors);
            emailResultsToUser(pendingSurvs, processingErrors, user);
        }
        LOGGER.info("********* Completed the Surveillance Upload job. *********");
    }

    private Set<Surveillance> parseSurveillance(String fileContents, Set<String> processingErrors) {
        Set<Surveillance> pendingSurvs = new LinkedHashSet<Surveillance>();
        try (BufferedReader reader = new BufferedReader(new StringReader(fileContents));
                CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL)) {
            List<CSVRecord> records = parser.getRecords();
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
                                    List<String> errors = survUploadManager.checkUploadedSurveillanceOwnership(pendingSurv);
                                    errors.addAll(survUploadManager.checkNonConformityStatusAndCloseDate(pendingSurv));
                                    // Add any errors that were found when getting the Surveillance
                                    errors.addAll(pendingSurv.getErrorMessages());

                                    for (String error : errors) {
                                        processingErrors.add(error);
                                    }
                                    survUploadManager.setNonConformityCloseDate(pendingSurv);
                                    pendingSurvs.add(pendingSurv);
                                } catch (InvalidArgumentsException ex) {
                                    LOGGER.error(ex.getMessage());
                                    processingErrors.add("Line " + i + " Error: " + ex.getMessage());
                                }
                            }
                            rows.clear();
                            rows.add(currRecord);
                        } else if (currRecordStatus
                                .equalsIgnoreCase(SurveillanceUploadManager.SUBELEMENT_INDICATOR)) {
                            rows.add(currRecord);
                        } // ignore blank rows
                    }

                    // add the last object
                    if (i == records.size() && !rows.isEmpty()) {
                        try {
                            SurveillanceUploadHandler handler = uploadHandlerFactory.getHandler(heading, rows);
                            Surveillance pendingSurv = handler.handle();
                            List<String> errors = survUploadManager.checkUploadedSurveillanceOwnership(pendingSurv);
                            for (String error : errors) {
                                processingErrors.add(error);
                            }
                            pendingSurvs.add(pendingSurv);
                        } catch (InvalidArgumentsException ex) {
                            LOGGER.error(ex.getMessage());
                            processingErrors.add("Line " + i + " Error: " + ex.getMessage());
                        }
                    }
                }
            }
        } catch (IOException ioEx) {
            String msg = errorMessageUtil.getMessage("surveillance.inputStream");
            LOGGER.error(msg);
            processingErrors.add(msg);
        }
        return pendingSurvs;
    }

    private void processPendingSurveillance(Set<Surveillance> pendingSurvs, Set<String> processingErrors) {
        for (Surveillance surv : pendingSurvs) {
            CertifiedProductDTO owningCp = null;
            if (surv != null && surv.getCertifiedProduct() != null && surv.getCertifiedProduct().getId() != null) {
                try {
                    owningCp = cpManager.getById(surv.getCertifiedProduct().getId());
                    pendingSurvManager.createPendingSurveillance(surv);
                } catch (AccessDeniedException denied) {
                    String permissionErrorMessage = "";
                    if (owningCp != null && owningCp.getCertificationBodyId() != null) {
                        try {
                            CertificationBodyDTO acbDTO = acbDAO.getById(owningCp.getCertificationBodyId());
                            permissionErrorMessage = errorMessageUtil.getMessage("surveillance.permissionErrorWithAcb",
                                    AuthUtil.getCurrentUser().getSubjectName(), owningCp.getChplProductNumber(),
                                    acbDTO.getName());
                        } catch (Exception e) {
                            permissionErrorMessage = errorMessageUtil.getMessage("surveillance.permissionError",
                                    AuthUtil.getCurrentUser().getSubjectName());
                        }
                    } else {
                        permissionErrorMessage = errorMessageUtil.getMessage("surveillance.permissionError",
                                AuthUtil.getCurrentUser().getSubjectName());
                    }

                    LOGGER.error(permissionErrorMessage, denied);
                    processingErrors.add(permissionErrorMessage);
                } catch (Exception ex) {
                    String msg = errorMessageUtil.getMessage("surveillance.errorAdding");
                    LOGGER.error(msg, ex);
                    processingErrors.add(msg);
                }
            }
        }
    }

    private void emailResultsToUser(Set<Surveillance> pendingSurveillance, Set<String> processingMessages, UserDTO user) {
        String subject = env.getProperty("surveillance.upload.subject");
        String htmlBody = createHtmlEmailBody(pendingSurveillance, processingMessages);
        try {
            sendEmail(user.getEmail(), subject, htmlBody);
        } catch (EmailNotSentException ex) {
            LOGGER.error("Unable to send email to " + user.getEmail());
            LOGGER.catching(ex);
        }
    }

    private void sendEmail(String recipientEmail, String subject, String htmlMessage)
            throws EmailNotSentException {
        LOGGER.info("Sending email to: " + recipientEmail);
        LOGGER.info("Message to be sent: " + htmlMessage);

        chplEmailFactory.emailBuilder()
                .recipient(recipientEmail)
                .subject(subject)
                .htmlMessage(htmlMessage)
                .sendEmail();
    }

    private String createHtmlEmailBody(Set<Surveillance> pendingSurveillance, Set<String> processingMessages) {
        String htmlMessage = String.format(env.getProperty("surveillance.upload.body.begin"),
                pendingSurveillance.size(),
                env.getProperty("chplUrlBegin") + "/#/surveillance/confirm");
        if (processingMessages.size() > 0) {
            String htmlMessageList = "";
            for (String msg : processingMessages) {
                htmlMessageList += "<li>" + msg + "</li>";
            }
            htmlMessage += String.format(env.getProperty("surveillance.upload.body.errors"), htmlMessageList);
        }
        return htmlMessage;
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
