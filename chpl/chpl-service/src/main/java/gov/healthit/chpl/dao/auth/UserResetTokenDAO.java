package gov.healthit.chpl.dao.auth;

import gov.healthit.chpl.dto.auth.UserResetTokenDTO;

public interface UserResetTokenDAO {
    public UserResetTokenDTO create(String resetToken, Long userId);
    
    public UserResetTokenDTO findByAuthToken(String authToken);
    
    public void deletePreviousUserTokens(Long userId);

}
