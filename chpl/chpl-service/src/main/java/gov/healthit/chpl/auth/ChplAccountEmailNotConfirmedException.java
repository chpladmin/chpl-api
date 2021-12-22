package gov.healthit.chpl.auth;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ChplAccountEmailNotConfirmedException extends Exception {
    private static final long serialVersionUID = 8658136990867988527L;
    private String emailAddress;

    public ChplAccountEmailNotConfirmedException(String msg, String email) {
        super(msg);
        this.emailAddress = email;
    }

}
