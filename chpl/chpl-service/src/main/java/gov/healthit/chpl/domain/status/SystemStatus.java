package gov.healthit.chpl.domain.status;

import java.io.Serializable;

/**
 * Encapsulates the status of the system as a whole.
 * Indicates whether the server is running and
 * the cache has been fully initialized.
 * @author kekey
 *
 */
public class SystemStatus implements Serializable {
    private static final long serialVersionUID = 1306741244948223766L;
    private String running;
    private String cache;

    public String getRunning() {
        return running;
    }
    public void setRunning(final String running) {
        this.running = running;
    }
    public String getCache() {
        return cache;
    }
    public void setCache(final String cache) {
        this.cache = cache;
    }
}
