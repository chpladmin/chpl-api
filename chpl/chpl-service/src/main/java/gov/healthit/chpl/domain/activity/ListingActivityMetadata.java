package gov.healthit.chpl.domain.activity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper=true)
public class ListingActivityMetadata extends ActivityMetadata {
    private static final long serialVersionUID = 5473773376581297578L;

    private String chplProductNumber;
    private String acbName;
    private String developerName;
    private String productName;
    private String edition;
    private Boolean curesUpdate;
    private Long certificationDate;
}
