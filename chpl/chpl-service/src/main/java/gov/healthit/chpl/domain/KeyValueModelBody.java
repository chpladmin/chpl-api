package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * Object used to pass retirement date of ACB/ATL to front end for search options.
 * @author alarned
 *
 */
public class KeyValueModelBody implements Serializable {
    private static final long serialVersionUID = -6175366628840719513L;
    private Long id;
    private String name;
    private Date retirementDate;

    public KeyValueModelBody() {
    }

    public KeyValueModelBody(final Long id, final String name, final Date retirementDate) {
        this.id = id;
        this.name = name;
        this.setRetirementDate(retirementDate);
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Date getRetirementDate() {
        return retirementDate;
    }

    public void setRetirementDate(final Date retirementDate) {
        this.retirementDate = retirementDate;
    }
}
