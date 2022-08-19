package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import gov.healthit.chpl.util.EasternToUtcLocalDateTimeDeserializer;
import gov.healthit.chpl.util.UtcToEasternLocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class Announcement implements Serializable {
    private static final long serialVersionUID = -7647761708813529969L;
    private Long id;
    private String title;
    private String text;
    @JsonDeserialize(using = EasternToUtcLocalDateTimeDeserializer.class)
    @JsonSerialize(using = UtcToEasternLocalDateTimeSerializer.class)
    private LocalDateTime startDateTime;
    @JsonDeserialize(using = EasternToUtcLocalDateTimeDeserializer.class)
    @JsonSerialize(using = UtcToEasternLocalDateTimeSerializer.class)
    private LocalDateTime endDateTime;
    @Deprecated
    @DeprecatedResponseField(removalDate = "2022-10-15",
        message = "This field is deprecated and will be removed from the response data in a future release. Please replace usage of the 'startDate' field with 'startDateTime'.")
    private Date startDate;
    @Deprecated
    @DeprecatedResponseField(removalDate = "2022-10-15",
        message = "This field is deprecated and will be removed from the response data in a future release. Please replace usage of the 'endDate' field with 'endDateTime'.")
    private Date endDate;
    private Boolean isPublic;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    public Announcement() {
        super();
    }
}
