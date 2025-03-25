package gov.healthit.chpl.testdata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TestData implements Serializable {
    private static final long serialVersionUID = -3763885258251736516L;
    public static final String DEFAULT_TEST_DATA = "ONC Test Method";

    private Long id;
    private String name;

    // Do not include this property if the value is "empty". It will be empty when generating listing details
    // and will be non-empty (this included) when doing CRUD operations on test tools
    @JsonInclude(value = Include.NON_EMPTY)
    @Builder.Default
    private List<CertificationCriterion> criteria = new ArrayList<CertificationCriterion>();
}
