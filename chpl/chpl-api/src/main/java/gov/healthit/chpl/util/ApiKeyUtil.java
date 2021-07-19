package gov.healthit.chpl.util;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.exception.InvalidArgumentsException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ApiKeyUtil {
    private static final String API_KEY_HEADER = "API-Key";
    private static final String API_KEY_PARAMETER = "api_key";

    public static String getApiKeyFromRequest(HttpServletRequest request) throws InvalidArgumentsException {
        String key = null;
        String keyFromHeader = request.getHeader(API_KEY_HEADER);
        String keyFromParam = request.getParameter(API_KEY_PARAMETER);

        if (!StringUtils.isAnyEmpty(keyFromHeader, keyFromParam) && StringUtils.equals(keyFromHeader, keyFromParam)) {
            key = keyFromHeader;
        } else if (StringUtils.isEmpty(keyFromHeader) && !StringUtils.isEmpty(keyFromParam)) {
            key = keyFromParam;
        } else if (!StringUtils.isEmpty(keyFromHeader) && StringUtils.isEmpty(keyFromParam)) {
            key = keyFromHeader;
        } else {
            LOGGER.error("API Key presented in the header (" + keyFromHeader + ") does not match API Key presented in URL parameter (" + keyFromParam + ").");
            throw new InvalidArgumentsException();
        }
        return key;
    }
}
