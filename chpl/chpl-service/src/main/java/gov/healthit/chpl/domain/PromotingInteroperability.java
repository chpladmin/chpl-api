package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.time.LocalDate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.util.LocalDateAdapter;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;


@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotingInteroperability implements Serializable {
    private static final long serialVersionUID = -4803363243075068608L;

    @XmlElement(name = "id", nillable = true, required = false)
    private Long id;

    @XmlElement(name = "userCount", nillable = true, required = false)
    private Long userCount;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    @XmlElement(name = "userCountDate", nillable = true, required = false)
    private LocalDate userCountDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserCount() {
        return userCount;
    }

    public void setUserCount(Long userCount) {
        this.userCount = userCount;
    }

    public LocalDate getUserCountDate() {
        return userCountDate;
    }

    public void setUserCountDate(LocalDate userCountDate) {
        this.userCountDate = userCountDate;
    }

    public boolean matches(PromotingInteroperability other) {
        boolean result = false;

        if (this.getId() != null && other.getId() != null
                && this.getId().longValue() == other.getId().longValue()) {
            result = true;
        }
        return result;
    }

}
