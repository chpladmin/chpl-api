package gov.healthit.chpl.domain.concept;

import java.io.Serializable;

public enum UploadTemplateVersion implements Serializable {
    EDITION_2014_VERSION_1("New 2014 CHPL Upload Template v10"),
    EDITION_2014_VERSION_2("New 2014 CHPL Upload Template v11"),
    EDITION_2015_VERSION_1("2015 CHPL Upload Template v10"),
    EDITION_2015_VERSION_2("2015 CHPL Upload Template v11"),
    EDITION_2015_VERSION_3("2015 CHPL Upload Template v12"),
    EDITION_2015_VERSION_4("2015 CHPL Upload Template v18"),
    EDITION_2015_VERSION_5("2015 CHPL Upload Template v19");

    private String name;

    UploadTemplateVersion(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
