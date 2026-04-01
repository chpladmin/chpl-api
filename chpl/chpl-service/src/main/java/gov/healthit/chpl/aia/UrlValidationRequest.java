package gov.healthit.chpl.astpai;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class UrlValidationRequest {
    private String url;

    @JsonProperty("chpl_product_number")
    private String chplProductNumber;

    @JsonProperty("target_year")
    private Integer targetYear;

    @JsonProperty("max_depth")
    private Integer maxDepth;

}
