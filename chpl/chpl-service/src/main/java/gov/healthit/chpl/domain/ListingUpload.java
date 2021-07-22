package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.upload.listing.ListingUploadStatus;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ListingUpload implements Serializable {
    private static final long serialVersionUID = 7978604053959535573L;

    private Long id;
    private String chplProductNumber;
    private CertificationBody acb;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate certificationDate;
    private String developer;
    private String product;
    private String version;
    private Integer warningCount;
    private Integer errorCount;
    private ListingUploadStatus status;
    @XmlTransient
    @JsonIgnore
    private List<CSVRecord> records;

    public ListingUpload() {
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
