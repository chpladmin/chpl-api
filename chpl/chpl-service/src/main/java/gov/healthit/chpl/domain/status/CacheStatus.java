package gov.healthit.chpl.domain.status;

import java.io.Serializable;

/**
 * Encapsulates the status of the cache.
 * @author kekey
 *
 */
public class CacheStatus implements Serializable {
    private static final long serialVersionUID = 1309870293948223766L;
    private String status;

    public String getStatus() {
        return status;
    }
    public void setStatus(final String status) {
        this.status = status;
    }
}
