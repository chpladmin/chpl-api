package gov.healthit.chpl.domain.surveillance;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import gov.healthit.chpl.dto.SurveillanceTypeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@Builder
@AllArgsConstructor
public class SurveillanceType implements Serializable {
    private static final long serialVersionUID = 5788880200952752783L;

    public static final String REACTIVE = "Reactive";
    public static final String RANDOMIZED = "Randomized";

    /**
     * Surveillance type internal ID
     */
    @Schema(description = "Surveillance type internal ID")
    @XmlElement(required = true)
    private Long id;

    /**
     * Surveillance type name (randomized, reactive)
     */
    @Schema(description = "Surveillance type name (randomized, reactive)")
    @XmlElement(required = true)
    private String name;

    public SurveillanceType() {

    }

    public SurveillanceType(SurveillanceTypeDTO dto) {
        BeanUtils.copyProperties(dto, this);
    }

    /**
     * Checks the id and name fields to determine if the two
     * surveillance type fields are the same.
     * Expect one or both fields to be filled in always.
     * @param anotherType
     * @return whether the two objects are the same
     */
    public boolean matches(final SurveillanceType anotherType) {
        if (this.id != null && anotherType.id != null && this.id.longValue() == anotherType.id.longValue()) {
            return true;
        } else if (!StringUtils.isEmpty(this.name) && !StringUtils.isEmpty(anotherType.name)
                && this.name.equalsIgnoreCase(anotherType.name)) {
            return true;
        }
        return false;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
