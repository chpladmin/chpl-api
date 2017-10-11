package gov.healthit.chpl.domain.concept;

import java.io.Serializable;

public enum UploadTemplateVersion implements Serializable {
    EDITION_2014_VERSION_1("2014 v1"), 
    EDITION_2015_VERSION_1("2015 v1"), 
    EDITION_2015_VERSION_2("2015 v2");

    private String name;

    private UploadTemplateVersion(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
