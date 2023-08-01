package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.dto.CertificationResultTestProcedureDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * The test procedure used for the certification criteria
 *
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
public class CertificationResultTestProcedure implements Serializable {
    private static final long serialVersionUID = -8648559250833503194L;

    /**
     * Test Procedure to certification result mapping internal ID
     */
    @Schema(description = "Test Procedure to certification result mapping internal ID")
    @XmlElement(required = true)
    private Long id;

    /**
     * This variable explains the test procedure being used to test
     * the associated criteria. It is applicable for 2015 Edition.
     */
    @Schema(description = "This variable explains the test procedure being used to test "
            + "the associated criteria. It is applicable for 2015 Edition.")
    @XmlElement(required = true)
    private TestProcedure testProcedure;

    /**
     * The test procedure version used for a given certification criteria. This
     * variable is a string variable that does not take any restrictions on
     * formatting or values and is applicable for 2014 and 2015 Edition.
     */
    @Schema(description = "The test procedure version used for a given certification criteria. This "
            + "variable is a string variable that does not take any restrictions on "
            + "formatting or values and is applicable for 2014 and 2015 Edition.")
    @XmlElement(required = true)
    private String testProcedureVersion;

    public CertificationResultTestProcedure() {
        super();
    }

    public CertificationResultTestProcedure(CertificationResultTestProcedureDTO dto) {
        this.id = dto.getId();
        TestProcedure tp = new TestProcedure();
        if (dto.getTestProcedure() == null) {
            tp.setId(dto.getTestProcedureId());
        } else {
            tp.setId(dto.getTestProcedure().getId());
            tp.setName(dto.getTestProcedure().getName());
        }
        this.testProcedure = tp;
        this.testProcedureVersion = dto.getVersion();
    }

    public boolean matches(final CertificationResultTestProcedure anotherProc) {
        boolean result = false;
        if (this.getTestProcedure() != null && anotherProc.getTestProcedure() != null
                && this.getTestProcedure().getId() != null && anotherProc.getTestProcedure().getId() != null
                && this.getTestProcedure().getId().longValue() == anotherProc.getTestProcedure().getId().longValue()
                && !StringUtils.isEmpty(this.getTestProcedureVersion())
                && !StringUtils.isEmpty(anotherProc.getTestProcedureVersion())
                && this.getTestProcedureVersion().equalsIgnoreCase(anotherProc.getTestProcedureVersion())) {
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

    public String getTestProcedureVersion() {
        return testProcedureVersion;
    }

    public void setTestProcedureVersion(final String testProcedureVersion) {
        this.testProcedureVersion = testProcedureVersion;
    }

    public TestProcedure getTestProcedure() {
        return testProcedure;
    }

    public void setTestProcedure(TestProcedure testProcedure) {
        this.testProcedure = testProcedure;
    }
}
