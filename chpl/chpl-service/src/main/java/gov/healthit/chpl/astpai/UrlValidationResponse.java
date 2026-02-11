package gov.healthit.chpl.astpai;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UrlValidationResponse {
    private Document document;
    private Validation validation;
    private String error;

    @NoArgsConstructor
    @Data
    @Builder
    @AllArgsConstructor
    public static final class Document {
        private String confidence;
        private String url;
    }

    @NoArgsConstructor
    @Data
    @Builder
    @AllArgsConstructor
    public static final class Validation {
        @JsonProperty("completeness_score")
        private String completenessScore;
        private List<String> recommendations;
        private String summary;
        //TODO critical failures, warnings
    }
}
