package gov.healthit.chpl.scheduler.job.onetime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.mail.Authenticator;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.UserDeveloperMapDAO;
import gov.healthit.chpl.dao.auth.InvitationDAO;
import gov.healthit.chpl.dao.auth.UserPermissionDAO;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.auth.Authority;
import gov.healthit.chpl.domain.auth.UserInvitation;
import gov.healthit.chpl.domain.auth.UserPermission;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.UserDeveloperMapDTO;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.EmailOverrider;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.entity.developer.DeveloperEntity;
import gov.healthit.chpl.exception.UserCreationException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.util.Util;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "inviteDevelopersJobLogger")
public class InviteDevelopersJob implements Job {

    @Autowired
    private BulkDeveloperEmailer bulkEmailer;

    @Autowired
    private DeveloperListingMapDao developerListingMapDao;

    @Autowired
    private UserDeveloperMapDAO userDeveloperMapDao;

    @SuppressWarnings("checkstyle:linelength")
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Invite Developers job *********");
        setSecurityContext();
        try {
            List<CertificationStatusType> certificationStatuses = Stream.of(CertificationStatusType.Active,
                    CertificationStatusType.SuspendedByAcb,
                    CertificationStatusType.SuspendedByOnc).collect(Collectors.toList());
            List<Developer> allDevelopers = developerListingMapDao.getDevelopersWithListingsInEditionAndStatus(
                    CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId(), certificationStatuses);
            LOGGER.info("There are " + allDevelopers.size() + " in the system with Active or Suspended 2015 listings.");
            allDevelopers.sort(new Comparator<Developer>() {
                @Override
                public int compare(Developer dev1, Developer dev2) {
                    return dev1.getName().compareToIgnoreCase(dev2.getName());
                }
            });

            bulkEmailer.open();
            List<Developer> developersNeedingInvitations = allDevelopers.stream()
                .filter(developer -> !doesDeveloperHaveUserAccounts(developer))
                .collect(Collectors.toList());
            bulkEmailer.inviteAllDeveloperPocs(developersNeedingInvitations);
        } catch (Exception e) {
            LOGGER.catching(e);
        } finally {
           bulkEmailer.close();
        }
        LOGGER.info("********* Completed the Invite Developers job *********");

    }

    private boolean doesDeveloperHaveUserAccounts(Developer developer) {
        List<UserDeveloperMapDTO> userDeveloperMaps = userDeveloperMapDao.getByDeveloperId(developer.getDeveloperId());
        LOGGER.info(String.format("Developer '" + developer.getName() + "' (id: "
                + developer.getDeveloperId() + ") has " + userDeveloperMaps.size() + " user%s",
                userDeveloperMaps.size() == 1 ? "" : "s"));
        return !CollectionUtils.isEmpty(userDeveloperMaps);
    }

    private void setSecurityContext() {
        JWTAuthenticatedUser adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(User.ADMIN_USER_ID);
        adminUser.setFriendlyName("Admin");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));

        SecurityContextHolder.getContext().setAuthentication(adminUser);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    @Component("bulkDeveloperEmailer")
    @NoArgsConstructor
    private static class BulkDeveloperEmailer {
        private ChplHtmlEmailBuilder htmlEmailBuilder;
        private InvitationDAO invitationDao;
        private Environment env;

        private String chplUrlBegin;
        private String chplEmailValediction;
        private String accountInvitationTitle;
        private String accountInvitationHeading;
        private String accountInvitationParagraph1;
        private String accountInvitationParagraph2;
        private String accountInvitationLink;

        private UserPermission permission;
        private Session emailSession;
        private Transport transport;

        @Autowired
        @SuppressWarnings({"checkstyle:parameternumber"})
        BulkDeveloperEmailer(ChplHtmlEmailBuilder htmlEmailBuilder,
                InvitationDAO invitationDao,
                UserPermissionDAO permissionDao,
                Environment env,
                @Value("${account.invitation.title}") String accountInvitationTitle,
                @Value("${account.invitation.heading}") String accountInvitationHeading,
                @Value("${account.invitation.paragraph1}") String accountInvitationParagraph1,
                @Value("${account.invitation.paragraph2}") String accountInvitationParagraph2,
                @Value("${account.invitation.invitationLink}") String accountInvitationLink,
                @Value("${invitationLengthInDays}") Long invitationLengthDays,
                @Value("${chplUrlBegin}") String chplUrlBegin,
                @Value("${footer.publicUrl}") String publicFeedbackUrl,
                @Value("${chpl.email.valediction}") String chplEmailValediction) {
            this.htmlEmailBuilder = htmlEmailBuilder;
            this.invitationDao = invitationDao;
            this.env = env;

            try {
                permission = permissionDao.getPermissionFromAuthority(Authority.ROLE_DEVELOPER);
            } catch (UserPermissionRetrievalException ex) {
                LOGGER.error("Could not look up permission " + Authority.ROLE_DEVELOPER, ex);
            }

            this.accountInvitationTitle = accountInvitationTitle;
            this.accountInvitationHeading = accountInvitationHeading;
            this.accountInvitationParagraph1 = accountInvitationParagraph1;
            this.accountInvitationParagraph2 = String.format(accountInvitationParagraph2, invitationLengthDays);
            this.accountInvitationLink = accountInvitationLink;
            this.chplUrlBegin = chplUrlBegin;
            this.chplEmailValediction = String.format(chplEmailValediction, publicFeedbackUrl);
        }

        public void open() {
            LOGGER.info("Opening connection to email server.");
            emailSession = Session.getInstance(getProperties(), getAuthenticator(getProperties()));

            try {
                transport = emailSession.getTransport();
                transport.connect();
            } catch (Exception ex) {
                LOGGER.error("Could not set up BulkDeveloperEmailer.", ex);
            }
        }

        public void close() {
            LOGGER.info("Closing connection to email server.");

            if (transport == null || !transport.isConnected()) {
                LOGGER.info("Email transport is already connected");
                return;
            }

            try {
                transport.close();
            } catch (Exception ex) {
                LOGGER.error("Could not close the email transport.", ex);
            }
        }

        @Transactional
        public void inviteAllDeveloperPocs(List<Developer> developers) throws UserCreationException, UserRetrievalException,
            AddressException, MessagingException {
            for (Developer developer : developers) {
                if (developerPocIsValid(developer)) {
                    UserInvitation invitation = createInvitation(developer);
                    String htmlMessage = createHtmlInvitation(invitation);
                    MimeMessage email = createEmail(invitation.getEmailAddress(), htmlMessage);
                    email.saveChanges();
                    transport.sendMessage(email, email.getAllRecipients());
                    LOGGER.info("Emailed user " + developer.getContact().getEmail() + " for developer '"
                            + developer.getName() + "' (id: " + developer.getDeveloperId() + ").");
                }
            }
        }

        private String createHtmlInvitation(UserInvitation invitation) {
            String htmlMessage = htmlEmailBuilder.initialize()
                    .heading(accountInvitationTitle)
                    .paragraph(accountInvitationHeading, accountInvitationParagraph1)
                    .paragraph(null, String.format(accountInvitationLink, chplUrlBegin, invitation.getInvitationToken()))
                    .paragraph(null, accountInvitationParagraph2)
                    .paragraph(null, chplEmailValediction)
                    .footer(true)
                    .build();
            return htmlMessage;
        }

        private boolean developerPocIsValid(Developer developer) {
            if (developer.getContact() == null || StringUtils.isEmpty(developer.getContact().getEmail())) {
                LOGGER.warn("Developer '" + developer.getName() + "' (id: " + developer.getDeveloperId() + ") has no POC. No invitation can be sent.");
                return false;
            } else if (!EmailValidator.getInstance().isValid(developer.getContact().getEmail())) {
                LOGGER.warn("Developer '" + developer.getName() + "' (id: " + developer.getDeveloperId() + ") "
                        + "has a POC with an invalid email address: '" + developer.getContact().getEmail() + "'."
                        + "No invitation can be sent.");
                return false;
            }
            return true;
        }

        private UserInvitation createInvitation(Developer developer) throws UserCreationException, UserRetrievalException {
            String emailAddress = developer.getContact().getEmail();
            UserInvitation invitation = UserInvitation.builder()
                    .emailAddress(emailAddress)
                    .permissionObjectId(developer.getDeveloperId())
                    .hash(Util.md5(emailAddress + System.currentTimeMillis()))
                    .permission(permission)
                    .build();
            Long createdInvitationId = invitationDao.create(invitation);
            LOGGER.info("Created invitation for " + emailAddress + " with ID " + createdInvitationId);
            UserInvitation createdInvitation = invitationDao.getById(createdInvitationId);
            return createdInvitation;
        }

        private MimeMessage createEmail(String recipient, String htmlMessage) throws AddressException, MessagingException {
            List<String> recipients = Stream.of(recipient).collect(Collectors.toList());

            EmailOverrider overrider = new EmailOverrider(env);
            MimeMessage message = new MimeMessage(emailSession);

            message.addRecipients(RecipientType.TO, overrider.getRecipients(recipients));
            message.setFrom(new InternetAddress(getProperties().getProperty("smtpFrom")));
            message.setSubject(accountInvitationTitle);
            message.setSentDate(new Date());
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(overrider.getBody(htmlMessage, recipients));
            message.setContent(multipart, "text/html; charset=UTF-8");
            return message;
        }

        private Authenticator getAuthenticator(Properties properties) {
            return new Authenticator() {
                @Override
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(properties.getProperty("smtpUsername"),
                            properties.getProperty("smtpPassword"));
                }
            };
        }

        private Properties getProperties() {
            // sets SMTP server properties
            Properties properties = new Properties();
            properties.put("mail.smtp.host", env.getProperty("smtpHost"));
            properties.put("mail.smtp.port", env.getProperty("smtpPort"));
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("smtpUsername", env.getProperty("smtpUsername"));
            properties.put("smtpPassword", env.getProperty("smtpPassword"));
            properties.put("smtpFrom", env.getProperty("smtpFrom"));

            return properties;
        }
    }

    @Component("developerListingMapDao")
    @NoArgsConstructor
    private static class DeveloperListingMapDao extends BaseDAOImpl {

        @Transactional
        public List<Developer> getDevelopersWithListingsInEditionAndStatus(Long certificationEditionId,
                List<CertificationStatusType> certificationStatuses) {
                String hql = "SELECT DISTINCT dev "
                        + "FROM DeveloperEntity dev, CertifiedProductDetailsEntity cpd "
                        + "LEFT OUTER JOIN FETCH dev.address "
                        + "LEFT OUTER JOIN FETCH dev.contact "
                        + "LEFT OUTER JOIN FETCH dev.statusEvents statusEvents "
                        + "LEFT OUTER JOIN FETCH statusEvents.developerStatus "
                        + "LEFT OUTER JOIN FETCH dev.developerCertificationStatuses "
                        + "WHERE cpd.developerId = dev.id "
                        + "AND cpd.certificationEditionId = :certificationEditionId "
                        + "AND cpd.certificationStatusName IN (:certificationStatusNames) "
                        + "AND cpd.deleted = false ";
                Query query = entityManager.createQuery(hql, DeveloperEntity.class);
                List<String> certificationStatusNames = certificationStatuses.stream()
                        .map(CertificationStatusType::getName)
                        .collect(Collectors.toList());
                query.setParameter("certificationEditionId", certificationEditionId);
                query.setParameter("certificationStatusNames", certificationStatusNames);

                List<DeveloperEntity> queryResults = query.getResultList();
                if (queryResults == null || queryResults.size() == 0) {
                    return new ArrayList<Developer>();
                }
                return queryResults.stream()
                        .map(entity -> entity.toDomain())
                        .collect(Collectors.toList());
        }
    }
}
