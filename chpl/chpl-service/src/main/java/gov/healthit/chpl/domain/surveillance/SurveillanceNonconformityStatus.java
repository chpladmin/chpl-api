package gov.healthit.chpl.domain.surveillance;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class SurveillanceNonconformityStatus implements Serializable {
    private static final long serialVersionUID = -411041849666278903L;
    public static final String OPEN = "Open";
    public static final String CLOSED = "Closed";

    private Long id;
    private String name;
}
