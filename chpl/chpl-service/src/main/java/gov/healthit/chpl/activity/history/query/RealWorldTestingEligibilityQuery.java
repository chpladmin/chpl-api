package gov.healthit.chpl.activity.history.query;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class RealWorldTestingEligibilityQuery extends ListingActivityQuery {
    private LocalDate asOfDate;

    @Builder
    public RealWorldTestingEligibilityQuery(Long listingId, LocalDate asOfDate) {
        super(listingId);
        this.asOfDate = asOfDate;
    }
}
