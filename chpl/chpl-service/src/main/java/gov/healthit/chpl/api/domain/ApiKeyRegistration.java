package gov.healthit.chpl.api.domain;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiKeyRegistration implements Serializable {
    private static final long serialVersionUID = 1101884894293322964L;
    private String email;
    private String name;

}
