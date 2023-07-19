package gov.healthit.chpl.scheduler.job.certificationId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.domain.SimpleCertificationId;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.footer.PublicFooter;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.manager.CertificationIdManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "certificationIdEmailJobLogger")
public class CertificationIdEmailJob  implements Job {
    public static final String JOB_NAME = "certificationIdEmailJob";
    public static final String USER_KEY = "user";

    @Autowired
    private CertificationIdManager certificationIdManager;

    @Autowired
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;

    @Autowired
    private ResourcePermissions resourcePermissions;

    @Autowired
    private Environment env;

    @Autowired
    private CertificationIdCsvCreator certificationIdCsvCreator;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Complaints Report Email job *********");
        UserDTO user = (UserDTO) context.getMergedJobDataMap().get(USER_KEY);
        if (user == null) {
            LOGGER.fatal("No user provided. Report cannot continue.");
        } else {
            setSecurityContext(user);
            try {
                sendEmail(context, getReportData());
            } catch (Exception e) {
                LOGGER.catching(e);
            }
        }
        LOGGER.info("********* Completed the Complaints Report Email job *********");
    }

    private void setSecurityContext(UserDTO user) {
        JWTAuthenticatedUser mergeUser = new JWTAuthenticatedUser();
        mergeUser.setFullName(user.getFullName());
        mergeUser.setId(user.getId());
        mergeUser.setFriendlyName(user.getFriendlyName());
        mergeUser.setSubjectName(user.getUsername());
        mergeUser.getPermissions().add(user.getPermission().getGrantedPermission());

        SecurityContextHolder.getContext().setAuthentication(mergeUser);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    private List<SimpleCertificationId> getReportData() {
        List<SimpleCertificationId> certificationIds = new ArrayList<SimpleCertificationId>();
        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
            LOGGER.info("Getting all certification IDs with Products...");
            certificationIds = certificationIdManager.getAllWithProducts();
        } else if (resourcePermissions.isUserRoleCmsStaff()) {
            LOGGER.info("Getting all certification IDs...");
            certificationIds = certificationIdManager.getAll();
        }
        LOGGER.info("Got " + certificationIds.size() + " certification IDs.");
        return certificationIds;
    }

    private void sendEmail(JobExecutionContext context, List<SimpleCertificationId> rows) throws EmailNotSentException, IOException {
        UserDTO user = (UserDTO) context.getMergedJobDataMap().get(USER_KEY);
        LOGGER.info("Sending email to: " + user.getEmail());
        chplEmailFactory.emailBuilder()
                .recipient(user.getEmail())
                .subject(env.getProperty("certificationIdReport.subject"))
                .htmlMessage(createHtmlMessage(context, rows.size()))
                .fileAttachments(Arrays.asList(certificationIdCsvCreator.createCsvFile(rows)))
                .sendEmail();
        LOGGER.info("Completed Sending email to: " + user.getEmail());
    }

    private String createHtmlMessage(JobExecutionContext context, int errorCount) {
        return chplHtmlEmailBuilder.initialize()
                .heading(env.getProperty("certificationIdReport.heading"))
                .paragraph(
                        "",
                        env.getProperty("certificationIdReport.body"))
                .footer(PublicFooter.class)
                .build();
    }
}
