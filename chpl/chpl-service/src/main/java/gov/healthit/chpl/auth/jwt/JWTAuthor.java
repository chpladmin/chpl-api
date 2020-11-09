package gov.healthit.chpl.auth.jwt;

import java.util.List;
import java.util.Map;

import gov.healthit.chpl.dto.auth.UserDTO;

public interface JWTAuthor {

    String createJWT(UserDTO user, Map<String, String> stringClaims, Map<String, List<String>> listClaims);

}
