package gov.healthit.chpl.dto.auth;

import gov.healthit.chpl.domain.auth.UserInvitation;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInvitationDTO {
    private UserDTO user;
    private UserInvitation invitation;
}
