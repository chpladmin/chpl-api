package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class SearchOption implements Serializable {
    private static final long serialVersionUID = -6671338026335670632L;

    @Deprecated
    private boolean expandable;
    private Set<? extends Object> data;

    public SearchOption() {
        this.data = new HashSet<KeyValueModel>();
    }

    @Deprecated
    public boolean isExpandable() {
        return expandable;
    }

    @Deprecated
    public void setExpandable(final boolean expandable) {
        this.expandable = expandable;
    }

    public Set<? extends Object> getData() {
        return data;
    }

    public void setData(final Set<? extends Object> data) {
        this.data = data;
    }

}
