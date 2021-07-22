package gov.healthit.chpl.scheduler.job.promotingInteroperability;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PromotingInteroperabilityUserRecord implements Serializable {
    private static final long serialVersionUID = -4837801214615959268L;
    private String chplProductNumber;
    private Long listingId;
    private Long userCount;
    private Integer csvLineNumber;
    private String error;

    public PromotingInteroperabilityUserRecord(String chplProductNumber, Long userCount) {
        this.chplProductNumber = chplProductNumber;
        this.userCount = userCount;
    }

    public PromotingInteroperabilityUserRecord(String chplProductNumber, Long listingId,
            Long userCount, Integer csvLineNumber) {
        this.chplProductNumber = chplProductNumber;
        this.listingId = listingId;
        this.userCount = userCount;
        this.csvLineNumber = csvLineNumber;
    }
}
