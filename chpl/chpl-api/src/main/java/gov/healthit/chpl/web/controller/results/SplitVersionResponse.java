package gov.healthit.chpl.web.controller.results;

import gov.healthit.chpl.domain.ProductVersion;

public class SplitVersionResponse {
    private ProductVersion oldVersion;
    private ProductVersion newVersion;

    public ProductVersion getOldVersion() {
        return oldVersion;
    }
    public void setOldVersion(final ProductVersion oldVersion) {
        this.oldVersion = oldVersion;
    }
    public ProductVersion getNewVersion() {
        return newVersion;
    }
    public void setNewVersion(final ProductVersion newVersion) {
        this.newVersion = newVersion;
    }
}
