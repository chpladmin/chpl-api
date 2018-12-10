package gov.healthit.chpl.domain;

public class DescriptiveModel extends KeyValueModel {
    private static final long serialVersionUID = 1402909764642483654L;
    private String title;

    public DescriptiveModel() {
        super();
    }

    public DescriptiveModel(Long id, String name, String title) {
        super(id, name);
        this.title = title;
    }

    public DescriptiveModel(Long id, String name, String title, String description) {
        super(id, name, description);
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    @Override
    public boolean equals(final Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
