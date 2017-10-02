package gov.healthit.chpl.domain;

import java.io.Serializable;

public class KeyValueModelStatuses extends KeyValueModel implements Serializable {
    private static final long serialVersionUID = -7750374871468678005L;
    Statuses statuses;

    public KeyValueModelStatuses() {
    }

    public KeyValueModelStatuses(Statuses statuses) {
        this.statuses = statuses;
    }

    public KeyValueModelStatuses(Long id, String name, Statuses statuses) {
        super.setId(id);
        super.setName(name);
        this.statuses = statuses;
    }

    public Statuses getStatuses() {
        return this.statuses;
    }

    public void setStatuses(final Statuses statuses) {
        this.statuses = statuses;
    }

}
