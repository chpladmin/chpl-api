package gov.healthit.chpl.activity.history.query;

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
public class SvapNoticeUrlLastUpdateActivityQuery extends ListingActivityQuery {
    private String svapNoticeUrl;

    @Builder
    public SvapNoticeUrlLastUpdateActivityQuery(Long listingId, String svapNoticeUrl) {
        super(listingId);
        this.svapNoticeUrl = svapNoticeUrl;
    }
}
