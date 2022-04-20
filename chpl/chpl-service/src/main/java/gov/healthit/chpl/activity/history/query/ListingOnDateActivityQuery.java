package gov.healthit.chpl.activity.history.query;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class ListingOnDateActivityQuery extends ListingActivityQuery {
    private LocalDate day;

    @Builder
    public ListingOnDateActivityQuery(Long listingId, LocalDate day) {
        super(listingId);
        this.day = day;
    }
}
