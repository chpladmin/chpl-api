package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestingLab implements Serializable {
    private static final long serialVersionUID = 7787353272569398682L;
    public static final String MULTIPLE_TESTING_LABS_CODE = "99";

    private Long id;
    private String name;
    private String atlCode;
    private String website;
    private Address address;
    private boolean retired;


    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate retirementDay;
}
