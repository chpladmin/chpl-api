package gov.healthit.chpl.activity.history.query;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ListingOnDateActivityQuery implements ListingActivityQuery {
    private Long listingId;
    private LocalDate day;
}
