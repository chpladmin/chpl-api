package gov.healthit.chpl.activity.history.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SvapNoticeUrlLastUpdateActivityQuery implements ListingActivityQuery {
    private Long listingId;
    private String svapNoticeUrl;
}
