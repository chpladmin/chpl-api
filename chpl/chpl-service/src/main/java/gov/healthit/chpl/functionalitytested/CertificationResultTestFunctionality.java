package gov.healthit.chpl.functionalitytested;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Deprecated
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@ToString
@Getter
@Setter
@Builder
public class CertificationResultTestFunctionality implements Serializable {
    private static final long serialVersionUID = -1647645050538126758L;

    private Long id;
    private Long testFunctionalityId;
    private String description;
    private String name;
    private String year;

    @XmlTransient
    @JsonIgnore
    private Long certificationResultId;

    public CertificationResultTestFunctionality() {
    }

    public boolean matches(CertificationResultTestFunctionality anotherFunc) {
        boolean result = false;
        if (this.getTestFunctionalityId() != null && anotherFunc.getTestFunctionalityId() != null
                && this.getTestFunctionalityId().longValue() == anotherFunc.getTestFunctionalityId().longValue()) {
            result = true;
        } else if (!StringUtils.isEmpty(this.getName()) && !StringUtils.isEmpty(anotherFunc.getName())
                && this.getName().equalsIgnoreCase(anotherFunc.getName()) && !StringUtils.isEmpty(this.getYear())
                && !StringUtils.isEmpty(anotherFunc.getYear())
                && this.getYear().equalsIgnoreCase(anotherFunc.getYear())) {
            result = true;
        }
        return result;
    }
}
