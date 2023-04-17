package gov.healthit.chpl.developer.join;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinDevelopersRequest implements Serializable {
    private static final long serialVersionUID = 736178317111978934L;

    private List<Long> developerIds;
}
