package gov.healthit.chpl.activity.history;

import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.ActivityDTO;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ListingActivityUtil {
    private ObjectMapper jsonMapper;

    public ListingActivityUtil() {
        jsonMapper = new ObjectMapper();
    }

    public void sortNewestActivityFirst(List<ActivityDTO> activities) {
        activities.sort(new Comparator<ActivityDTO>() {
            @Override
            public int compare(ActivityDTO o1, ActivityDTO o2) {
                if (ObjectUtils.allNotNull(o1, o2, o1.getActivityDate(), o2.getActivityDate())) {
                    return o2.getActivityDate().compareTo(o1.getActivityDate());
                }
                return 0;
            }
        });
    }

    public void sortOldestActivityFirst(List<ActivityDTO> activities) {
        activities.sort(new Comparator<ActivityDTO>() {
            @Override
            public int compare(ActivityDTO o1, ActivityDTO o2) {
                if (ObjectUtils.allNotNull(o1, o2, o1.getActivityDate(), o2.getActivityDate())) {
                    return o1.getActivityDate().compareTo(o2.getActivityDate());
                }
                return 0;
            }
        });
    }

    public CertifiedProductSearchDetails getListing(String listingJson) {
        CertifiedProductSearchDetails listing = null;
        if (!StringUtils.isEmpty(listingJson)) {
            try {
                listing =
                    jsonMapper.readValue(listingJson, CertifiedProductSearchDetails.class);
            } catch (Exception ex) {
                LOGGER.error("Could not parse activity JSON " + listingJson, ex);
            }
        }
        return listing;
    }
}
