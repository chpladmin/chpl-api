package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.List;

public class SplitVersionsRequest implements Serializable {
    private static final long serialVersionUID = -5814308900559716235L;

    private String newVersionName;
    private String newVersionCode;
    //listings going with the new version
    private List<CertifiedProduct> newListings;
    private ProductVersion oldVersion;
    //listings staying with the existing version
    private List<CertifiedProduct> oldListings;

    public String getNewVersionName() {
        return newVersionName;
    }
    public void setNewVersionName(final String newVersionName) {
        this.newVersionName = newVersionName;
    }
    public String getNewVersionCode() {
        return newVersionCode;
    }
    public void setNewVersionCode(final String newVersionCode) {
        this.newVersionCode = newVersionCode;
    }
    public List<CertifiedProduct> getNewListings() {
        return newListings;
    }
    public void setNewListings(final List<CertifiedProduct> newListings) {
        this.newListings = newListings;
    }
    public ProductVersion getOldVersion() {
        return oldVersion;
    }
    public void setOldVersion(final ProductVersion oldVersion) {
        this.oldVersion = oldVersion;
    }
    public List<CertifiedProduct> getOldListings() {
        return oldListings;
    }
    public void setOldListings(final List<CertifiedProduct> oldListings) {
        this.oldListings = oldListings;
    }
    public static long getSerialversionuid() {
        return serialVersionUID;
    }
}
