package gov.healthit.chpl.auth.dao;

import java.util.List;

import gov.healthit.chpl.auth.dto.UserResetTokenDTO;

public interface UserResetTokenDAO {
    public UserResetTokenDTO create(String resetToken, Long userId);
    
    public UserResetTokenDTO findByAuthToken(String authToken);
    
    public List<UserResetTokenDTO> findAllById(Long id);
    
    public void deletePreviousUserTokens(Long userId);

}
