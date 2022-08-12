package gov.healthit.chpl.domain.compliance;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;

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
    private HttpStatus httpStatus;
    private LocalDateTime fetched;
}
