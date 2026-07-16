package gov.healthit.chpl.api.domain;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiKeyConfirmation implements Serializable {
    private static final long serialVersionUID = 1201878489693322964L;

    private String requestKey;
}
