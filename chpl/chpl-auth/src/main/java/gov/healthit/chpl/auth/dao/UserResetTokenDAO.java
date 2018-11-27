package gov.healthit.chpl.auth.dao;

import gov.healthit.chpl.auth.dto.UserResetTokenDTO;

public interface UserResetTokenDAO {
    public UserResetTokenDTO create(String resetToken, Long userId);
    
    public UserResetTokenDTO findByAuthToken(String authToken);
    
    public void deletePreviousUserTokens(Long userId);

}
