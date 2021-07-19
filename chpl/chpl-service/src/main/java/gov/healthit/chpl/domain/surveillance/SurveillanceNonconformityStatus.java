package gov.healthit.chpl.domain.surveillance;

import java.io.Serializable;

import lombok.Data;

@Data
public class SurveillanceNonconformityStatus implements Serializable {
    private static final long serialVersionUID = -411041849666278903L;
    public static final String OPEN = "Open";
    public static final String CLOSED = "Closed";

    private Long id;

    private String name;

    public SurveillanceNonconformityStatus() {
    }
}
