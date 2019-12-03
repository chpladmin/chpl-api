package gov.healthit.chpl.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * A utility class for reading or interpreting properties from the environment.properties file.
 * 
 * @author kekey
 *
 */
@Component
public class PropertyUtil {
    private Environment env;

    @Autowired
    public PropertyUtil(final Environment env) {
        this.env = env;
    }

    /**
     * Determines if listing details may be retrieved asynchronously. Defaults to false if the property is not
     * specified.
     * 
     * @return
     */
    public Boolean isAsyncListingDetailsEnabled() {
        if (StringUtils.isEmpty(env.getProperty("asyncListingDetailsEnabled"))) {
            return false;
        }
        return env.getProperty("asyncListingDetailsEnabled").equalsIgnoreCase("true");
    }

    /**
     * Determines if the listing collection cache may be refreshed asynchronously. Defaults to false if the property is
     * not specified.
     * 
     * @return
     */
    public Boolean isAsyncCacheRefreshEnabled() {
        if (StringUtils.isEmpty(env.getProperty("asyncCacheRefreshEnabled"))) {
            return false;
        }
        return env.getProperty("asyncCacheRefreshEnabled").equalsIgnoreCase("true");
    }
}
