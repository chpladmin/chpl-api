package gov.healthit.chpl.domain.auth;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import gov.healthit.chpl.domain.Organization;
import gov.healthit.chpl.domain.contact.Person;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class User extends Person implements Serializable {
    private static final long serialVersionUID = 8408154701107113148L;

    private Long userId;
    private UUID cognitoId;
    private String role;
    private String subjectName;
    private String friendlyName;
    private Boolean accountLocked;
    private Boolean accountEnabled;
    private Boolean credentialsExpired;
    private Boolean passwordResetRequired;
    private Date lastLoggedInDate;
    private List<Organization> organizations = new ArrayList<Organization>();
    private String hash;
    private String status;

}
