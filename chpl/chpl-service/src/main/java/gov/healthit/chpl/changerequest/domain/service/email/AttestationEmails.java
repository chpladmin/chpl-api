package gov.healthit.chpl.changerequest.domain.service.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
public class AttestationEmails {
    private AttestationSubmittedEmail submittedEmail;
    private AttestationAcceptedEmail acceptedEmail;
    private AttestationPendingDeveloperActionEmail pendingDeveloperActionEmail;
    private AttestationRejectedEmail rejectedEmail;
    private AttestationWithdrawnEmail withdrawnEmail;
    private AttestationUpdatedEmail updatedEmail;

    @Autowired
    public AttestationEmails(AttestationSubmittedEmail submittedEmail,
            AttestationAcceptedEmail acceptedEmail,
            AttestationPendingDeveloperActionEmail pendingDeveloperActionEmail,
            AttestationRejectedEmail rejectedEmail,
            AttestationWithdrawnEmail withdrawnEmail,
            AttestationUpdatedEmail updatedEmail) {
        this.submittedEmail = submittedEmail;
        this.acceptedEmail = acceptedEmail;
        this.pendingDeveloperActionEmail = pendingDeveloperActionEmail;
        this.rejectedEmail = rejectedEmail;
        this.withdrawnEmail = withdrawnEmail;
        this.updatedEmail = updatedEmail;
    }
}
