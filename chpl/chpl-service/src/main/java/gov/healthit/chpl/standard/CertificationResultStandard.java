package gov.healthit.chpl.standard;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Schema(description = "The standards associated to the certification result")
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class CertificationResultStandard implements Serializable {
    private static final long serialVersionUID = 418146022721549464L;

    @Schema(description = "Standard to certification result mapping internal ID")
    private Long id;

    @Schema(description = "Standard")
    private Standard standard;

    @XmlTransient
    @JsonIgnore
    private Long certificationResultId;

    public boolean matches(CertificationResultStandard anotherStandard) {
        boolean result = false;
        if (this.getStandard().getId() != null && anotherStandard.getStandard().getId() != null
                && this.getStandard().getId().longValue() == anotherStandard.getStandard().getId().longValue()) {
            result = true;
        } else if (!StringUtils.isEmpty(this.getStandard().getRegulatoryTextCitation())
                    && !StringUtils.isEmpty(anotherStandard.getStandard().getRegulatoryTextCitation())
                && this.getStandard().getRegulatoryTextCitation().equalsIgnoreCase(anotherStandard.getStandard().getRegulatoryTextCitation())) {
            result = true;
        }
        return result;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getCertificationResultId() {
        return this.certificationResultId;
    }

    public void setCertificationResultId(Long certificationResultId) {
        this.certificationResultId = certificationResultId;
    }

    public Standard getStandard() {
        return standard;
    }

    public void setStandard(Standard standard) {
        this.standard = standard;
    }

}
