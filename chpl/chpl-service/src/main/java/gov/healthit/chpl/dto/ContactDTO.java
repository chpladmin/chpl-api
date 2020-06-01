package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.entity.ContactEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class ContactDTO implements Serializable {
    private static final long serialVersionUID = 5417465972193498436L;
    private Long id;
    private String fullName;
    private String friendlyName;
    private String email;
    private String phoneNumber;
    private String title;
    private Date signatureDate;

    public ContactDTO(final ContactEntity entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.fullName = entity.getFullName();
            this.friendlyName = entity.getFriendlyName();
            this.email = entity.getEmail();
            this.phoneNumber = entity.getPhoneNumber();
            this.title = entity.getTitle();
            this.signatureDate = entity.getSignatureDate();
        }
    }
}
