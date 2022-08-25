package gov.healthit.chpl.attestation.manager;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.attestation.domain.AttestationPeriodDeveloperException;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class AttestationExceptionEmail {
        private ChplEmailFactory chplEmailFactory;
        private ChplHtmlEmailBuilder chplHtmlEmailBuilder;
        private ResourcePermissions resourcePermissions;
        private String emailSubject;
        private String emailBody;


        @Autowired
        public AttestationExceptionEmail(ChplEmailFactory chplEmailFactory, ChplHtmlEmailBuilder chplHtmlEmailBuilder,
                ResourcePermissions resourcePermissions,
                @Value("${changeRequest.attestation.exception.subject}") String emailSubject,
                @Value("${changeRequest.attestation.exception.body}") String emailBody) {
            this.chplEmailFactory = chplEmailFactory;
            this.chplHtmlEmailBuilder = chplHtmlEmailBuilder;
            this.resourcePermissions = resourcePermissions;
            this.emailSubject = emailSubject;
            this.emailBody = emailBody;
        }

        public void send(AttestationPeriodDeveloperException attestationException) throws EmailNotSentException {
            List<UserDTO> recipients = resourcePermissions.getAllUsersOnDeveloper(
                    Developer.builder().id(attestationException.getDeveloper().getId()).build());

            chplEmailFactory.emailBuilder()
                .recipients(recipients.stream()
                        .map(user -> user.getEmail())
                        .collect(Collectors.toList()))
                .subject(emailSubject)
                .htmlMessage(createExceptionHtmlMessage(attestationException))
                .sendEmail();

        }

        private String createExceptionHtmlMessage(AttestationPeriodDeveloperException attestationException) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, YYYY");
            return chplHtmlEmailBuilder.initialize()
                    .heading("Developer Attestations Submission Reopened")
                    .paragraph("", String.format(emailBody, getActingBody(), formatter.format(attestationException.getExceptionEnd())))
                    .footer(true)
                    .build();
        }

        private String getActingBody() {
            if (resourcePermissions.isUserRoleAcbAdmin()) {
                List<CertificationBodyDTO> acbsForUser = resourcePermissions.getAllAcbsForCurrentUser();
                if (CollectionUtils.isEmpty(acbsForUser)) {
                    LOGGER.warn("No ACBs were found for the current user " + AuthUtil.getCurrentUser().getSubjectName());
                    return "an ONC-ACB";
                }
                return acbsForUser.get(0).getName();
            } else if (resourcePermissions.isUserRoleAdmin()) {
                return "CHPL Admin";
            } else if (resourcePermissions.isUserRoleOnc()) {
                return "ONC";
            } else {
                LOGGER.warn("The current user did not have one of the expected roles... " + AuthUtil.getCurrentUser().getSubjectName());
                return "an administrator";
            }
        }
}
