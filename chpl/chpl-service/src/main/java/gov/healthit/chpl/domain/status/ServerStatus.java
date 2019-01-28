package gov.healthit.chpl.domain.status;

import java.io.Serializable;

/**
 * Encapsulates the status of the server whether it's running or not.
 * @author kekey
 *
 */
public class ServerStatus implements Serializable {
    private static final long serialVersionUID = 1309870244948223766L;
    private String status;

    public String getStatus() {
        return status;
    }
    public void setStatus(final String status) {
        this.status = status;
    }
}
