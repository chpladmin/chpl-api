package gov.healthit.chpl.domain;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;

@Data
public class ListingUpload implements Serializable {
    private static final long serialVersionUID = 7978604053959535573L;

    private Long id;
    private String chplProductNumber;
    private CertificationBody acb;
    private Integer errorCount;
    private Integer warningCount;

    @Override
    public boolean equals(Object another) {
        if (another == null || !(another instanceof ListingUpload)) {
            return false;
        }
        ListingUpload anotherListingUpload = (ListingUpload) another;
        if (StringUtils.isNotEmpty(this.getChplProductNumber())
                && StringUtils.isNotEmpty(anotherListingUpload.getChplProductNumber())) {
            return StringUtils.equals(this.getChplProductNumber(), anotherListingUpload.getChplProductNumber());
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (StringUtils.isEmpty(this.getChplProductNumber())) {
            return -1;
        }
        return this.getChplProductNumber().hashCode();
    }

}
