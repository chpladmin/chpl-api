package gov.healthit.chpl.complaint.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ComplainantType implements Serializable {
    private static final long serialVersionUID = 1518763798442946048L;

    private Long id;
    private String name;
}
