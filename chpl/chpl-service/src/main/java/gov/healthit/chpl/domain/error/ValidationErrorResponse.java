package gov.healthit.chpl.domain.error;

import java.io.Serializable;
import java.util.Collection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ValidationErrorResponse implements Serializable {
    private static final long serialVersionUID = -2186304674032903240L;
    private Collection<String> errorMessages;
    private Collection<String> businessErrorMessages;
    private Collection<String> dataErrorMessages;
    private Collection<String> warningMessages;
}
