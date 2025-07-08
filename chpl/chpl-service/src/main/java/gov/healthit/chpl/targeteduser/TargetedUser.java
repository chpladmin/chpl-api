package gov.healthit.chpl.targeteduser;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TargetedUser implements Serializable {
    private static final long serialVersionUID = 6819005018143479705L;
    private Long id;
    private String name;
}
