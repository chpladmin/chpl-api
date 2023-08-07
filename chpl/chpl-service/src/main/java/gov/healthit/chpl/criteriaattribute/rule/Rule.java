package gov.healthit.chpl.criteriaattribute.rule;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class Rule implements Serializable {
    private static final long serialVersionUID = 1896885792278074234L;

    /**
     * Rule internal ID
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * A string value representing an abbreviation for the specific rule name.
     */
    @XmlElement(required = true)
    private String name;
}
