package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import lombok.Data;

@Data
public class ListingUpload implements Serializable {
    private static final long serialVersionUID = 7978604053959535573L;

    private Long id;
    private String chplProductNumber;
    private CertificationBody acb;
    private LocalDate certificationDate;
    private String developer;
    private String product;
    private String version;
    private Integer warningCount;
    private Integer errorCount;
    private Set<String> uploadErrors;
    private List<CSVRecord> records;

    public ListingUpload() {
        uploadErrors = new LinkedHashSet<String>();
        records = new ArrayList<CSVRecord>();
    }

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
