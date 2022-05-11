package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.util.LocalDateTimeAdapter;
import gov.healthit.chpl.util.LocalDateTimeDeserializer;
import gov.healthit.chpl.util.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
public class CertifiedProductChplProductNumberHistory implements Serializable {
    private static final long serialVersionUID = -2085183878716253974L;

    /**
     * Internal ID of the CHPL Product Number history record.
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * A CHPL Product Number that could have been used to reference this listing in the past.
     */
    @XmlElement(required = true)
    private String chplProductNumber;

    /**
     * A timestamp indicating when this historical CHPL Product Number stopped being referenced
     * for this listing.
     */
    @XmlElement(required = true)
    @XmlJavaTypeAdapter(value = LocalDateTimeAdapter.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime endDateTime;

    public CertifiedProductChplProductNumberHistory() {
        super();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof CertifiedProductChplProductNumberHistory)) {
            return false;
        }

        CertifiedProductChplProductNumberHistory other = (CertifiedProductChplProductNumberHistory) obj;
        return Objects.equals(this.getChplProductNumber(), other.getChplProductNumber())
                && Objects.equals(this.endDateTime, other.getEndDateTime());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChplProductNumber() {
        return chplProductNumber;
    }

    public void setChplProductNumber(String chplProductNumber) {
        this.chplProductNumber = chplProductNumber;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

}
