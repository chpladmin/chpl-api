package gov.healthit.chpl.domain.error;

import java.io.Serializable;

import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;

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

    private ImmutableSortedSet<String> errorMessages;
    private ImmutableSortedSet<String> businessErrorMessages;
    private ImmutableSortedSet<String> dataErrorMessages;
    private ImmutableSortedSet<String> warningMessages;
}
