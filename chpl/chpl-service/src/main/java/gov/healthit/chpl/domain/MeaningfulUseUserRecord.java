package gov.healthit.chpl.domain;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MeaningfulUseUserRecord implements Serializable {
    private static final long serialVersionUID = -4837801214615959268L;
    private String productNumber;
    private Long certifiedProductId;
    private Long numberOfUsers;
    private Integer csvLineNumber;
    private String error;

    public MeaningfulUseUserRecord(String productNumber, Long numberOfUsers) {
        this.productNumber = productNumber;
        this.numberOfUsers = numberOfUsers;
    }

    public MeaningfulUseUserRecord(String productNumber, Long certifiedProductId, Long numberOfUsers, Integer csvLineNumber) {
        this.productNumber = productNumber;
        this.certifiedProductId = certifiedProductId;
        this.numberOfUsers = numberOfUsers;
        this.csvLineNumber = csvLineNumber;
    }
}
