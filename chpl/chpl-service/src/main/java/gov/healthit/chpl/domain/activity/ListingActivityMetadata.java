package gov.healthit.chpl.domain.activity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@RequiredArgsConstructor
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
