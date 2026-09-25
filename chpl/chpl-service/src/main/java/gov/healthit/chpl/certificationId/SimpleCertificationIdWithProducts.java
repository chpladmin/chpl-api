package gov.healthit.chpl.certificationId;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@SuperBuilder
public class SimpleCertificationIdWithProducts extends SimpleCertificationId implements Serializable {
    private static final long serialVersionUID = -2818214498196264669L;

    private List<String> products;
}
