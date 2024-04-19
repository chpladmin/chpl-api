package gov.healthit.chpl.domain.activity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ProductActivityMetadata extends ActivityMetadata {
    private static final long serialVersionUID = 9069117187924463180L;

    private String developerName;
    private String productName;

}
