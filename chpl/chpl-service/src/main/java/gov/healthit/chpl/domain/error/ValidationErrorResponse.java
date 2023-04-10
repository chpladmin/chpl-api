package gov.healthit.chpl.domain.error;

import java.io.Serializable;
import java.util.ArrayList;
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

    @Builder.Default
    private Collection<String> errorMessages = new ArrayList<String>();

    @Builder.Default
    private Collection<String> businessErrorMessages = new ArrayList<String>();

    @Builder.Default
    private Collection<String> dataErrorMessages = new ArrayList<String>();

    @Builder.Default
    private Collection<String> warningMessages = new ArrayList<String>();
}
