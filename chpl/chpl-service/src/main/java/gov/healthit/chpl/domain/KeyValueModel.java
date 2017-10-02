package gov.healthit.chpl.domain;

import java.io.Serializable;

import org.springframework.util.StringUtils;

public class KeyValueModel implements Serializable {
    private static final long serialVersionUID = -6175366628840719513L;
    private Long id;
    private String name;
    private String description;

    public KeyValueModel() {
    }

    public KeyValueModel(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public KeyValueModel(Long id, String name, String description) {
        this(id, name);
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DescriptiveModel))
            return false;
        if (obj == this)
            return true;

        DescriptiveModel rhs = (DescriptiveModel) obj;

        if (StringUtils.isEmpty(rhs.getName()) != StringUtils.isEmpty(this.getName())) {
            return false;
        }

        return rhs.getName().equals(this.getName());
    }

    @Override
    public int hashCode() {
        if (StringUtils.isEmpty(this.getName())) {
            return 0;
        }
        return this.getName().hashCode();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
