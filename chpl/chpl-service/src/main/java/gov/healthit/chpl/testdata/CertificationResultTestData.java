package gov.healthit.chpl.testdata;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@Builder
public class CertificationResultTestData implements Serializable {
    private static final long serialVersionUID = -7272525145274770518L;

    @Schema(description = "Certification result to test data mapping internal ID")
    private Long id;

    @Schema(description = "This variable explains the test data being used to test the associated criteria.")
    private TestData testData;

    @Schema(description = "This variable explains the version of the test data being used for a given certification criteria. "
            + "It is  a string variable that does not take any restrictions on formatting or values.")
    private String version;

    @Schema(description = "This variable indicates if test data alterations are made, a description of all modifications made. "
            + "It is a string variable that does not take any restrictions on formatting or values.")
    private String alteration;

    public CertificationResultTestData() {
        super();
    }

    public boolean matches(final CertificationResultTestData anotherTestData) {
        boolean result = false;
        if (this.getTestData() != null && anotherTestData.getTestData() != null
                && this.getTestData().getId() != null && anotherTestData.getTestData().getId() != null
                && this.getTestData().getId().longValue() == anotherTestData.getTestData().getId().longValue()
                && Objects.equals(this.getVersion(), anotherTestData.getVersion())
                && Objects.equals(this.getAlteration(), anotherTestData.getAlteration())) {
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

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getAlteration() {
        return alteration;
    }

    public void setAlteration(final String alteration) {
        this.alteration = alteration;
    }

    public TestData getTestData() {
        return testData;
    }

    public void setTestData(TestData testData) {
        this.testData = testData;
    }
}
