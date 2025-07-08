package gov.healthit.chpl.sed;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EducationType implements Serializable {
    private static final long serialVersionUID = -89921866320827189L;
    private Long id;
    private String name;
}
