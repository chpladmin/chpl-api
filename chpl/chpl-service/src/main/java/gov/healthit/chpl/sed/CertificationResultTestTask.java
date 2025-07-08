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
public class CertificationResultTestTask implements Serializable {
    private static final long serialVersionUID = -2963883181763817735L;
    private Long id;
    private Long certificationResultId;
    private Long testTaskId;
    private TestTask testTask;
}
