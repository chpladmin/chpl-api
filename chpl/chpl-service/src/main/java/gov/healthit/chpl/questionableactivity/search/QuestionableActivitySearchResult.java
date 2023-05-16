package gov.healthit.chpl.questionableactivity.search;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.EasternToSystemLocalDateTimeDeserializer;
import gov.healthit.chpl.util.SystemToEasternLocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class QuestionableActivitySearchResult implements Serializable {
    private static final long serialVersionUID = -8153861360218726537L;
    private static final String LEVEL_LISTING = "Listing";
    private static final String LEVEL_CRITERIA = "Certification Criteria";

    @JsonIgnore
    @XmlTransient
    public static final List<String> CSV_HEADINGS = Stream.of("ONC-ACB", "Developer", "Product", "Version",
            "CHPL Product Number", "Current Certification Status", "Activity Timestamp", "Link", "Responsible User",
            "Activity Level", "Activity Type", "Activity", "Reason for Status Change", "Reason").toList();

    private Long id;
    private String triggerLevel;
    private String triggerName;
    private Long activityId;
    private String before;
    private String after;
    @JsonDeserialize(using = EasternToSystemLocalDateTimeDeserializer.class)
    @JsonSerialize(using = SystemToEasternLocalDateTimeSerializer.class)
    private LocalDateTime activityDate;
    private Long userId;
    private String username;
    private String certificationStatusChangeReason;
    private String reason;
    private Long developerId;
    private String developerName;
    private Long productId;
    private String productName;
    private Long versionId;
    private String versionName;
    private Long listingId;
    private String chplProductNumber;
    private Long acbId;
    private String acbName;
    private Long certificationStatusId;
    private String certificationStatusName;
    private Long certificationCriterionId;

    @JsonIgnore
    @XmlTransient
    public List<String> toListOfStringsForCsv(String listingsReportUrlPartBegin) {
        List<String> csvFields = new ArrayList<String>();
        csvFields.add(acbName);
        csvFields.add(developerName);
        csvFields.add(productName);
        csvFields.add(versionName);
        csvFields.add(chplProductNumber);
        csvFields.add(certificationStatusName);
        csvFields.add(DateUtil.formatInEasternTime(activityDate));
        if (LEVEL_LISTING.equals(triggerLevel) || LEVEL_CRITERIA.equals(triggerLevel)) {
            csvFields.add(listingsReportUrlPartBegin + "/" + listingId);
        } else {
            csvFields.add("");
        }
        csvFields.add(username);
        csvFields.add(triggerLevel);
        csvFields.add(triggerName);
        if (StringUtils.isEmpty(before)) {
            csvFields.add("Added " + after);
        } else if (StringUtils.isEmpty(after)) {
            csvFields.add("Removed " + before);
        } else {
            csvFields.add("From " + before + " to " + after);
        }
        csvFields.add(certificationStatusChangeReason);
        csvFields.add(reason);
        return csvFields;
    }
}
