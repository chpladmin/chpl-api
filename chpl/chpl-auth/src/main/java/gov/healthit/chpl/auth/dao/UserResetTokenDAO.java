package gov.healthit.chpl.auth.dao;

import java.util.List;

import gov.healthit.chpl.auth.dto.UserResetTokenDTO;
import gov.healthit.chpl.auth.entity.UserResetTokenEntity;

public interface UserResetTokenDAO {
    public UserResetTokenDTO create(String resetToken, Long userId);
    
    public UserResetTokenDTO findByAuthToken(String authToken);
    
    public void deletePreviousUserTokens(Long userId);

}
