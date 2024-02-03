package gov.healthit.chpl.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Deprecated
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegacyCertificationStatus {
    private long id;
    private String name;
}
