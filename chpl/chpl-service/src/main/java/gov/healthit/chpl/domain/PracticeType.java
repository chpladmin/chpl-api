package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//TODO OCD-4288 - NEED TEXT TO DESCRIBE THIS OBJECT
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlAccessorOrder(value = XmlAccessOrder.ALPHABETICAL)
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class PracticeType implements Serializable {
    private static final long serialVersionUID = 8826782928545744059L;

    private Long id;
    private String description;
    private String name;
}
