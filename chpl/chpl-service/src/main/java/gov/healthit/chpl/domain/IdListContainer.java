package gov.healthit.chpl.domain;

import java.util.ArrayList;
import java.util.List;

public class IdListContainer {
    private List<Long> ids;

    public IdListContainer() {
        ids = new ArrayList<Long>();
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }
}
