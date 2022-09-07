package gov.healthit.chpl.domain.compliance;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.util.EasternToUtcLocalDateTimeDeserializer;
import gov.healthit.chpl.util.UtcToEasternLocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DirectReviewContainer implements Serializable {
    private static final long serialVersionUID = 1938984062426698694L;

    @Builder.Default
    private List<DirectReview> directReviews = new ArrayList<DirectReview>();

    @JsonDeserialize(using = EasternToUtcLocalDateTimeDeserializer.class)
    @JsonSerialize(using = UtcToEasternLocalDateTimeSerializer.class)
    private LocalDateTime fetched;

    @JsonIgnore
    private HttpStatus httpStatus;
}
