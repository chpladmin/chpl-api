package gov.healthit.chpl.subscription.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ListingSubscriptionGroup extends ChplItemSubscriptionGroup {
    private Long certifiedProductId;
    private String chplProductNumber;
    private Long developerId;
    private String developerName;
    private Long productId;
    private String productName;
    private Long versionId;
    private String version;
    private Long certificationBodyId;
    private String certificationBodyName;
}
