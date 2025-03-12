package gov.healthit.chpl.testdata;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
}
