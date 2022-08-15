package gov.healthit.chpl.form.validation;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormValidationResult implements Serializable {
    private static final long serialVersionUID = 2311258208768688617L;

    private Boolean valid;
    private List<String> errorMessages;
}
