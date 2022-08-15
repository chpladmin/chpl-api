package gov.healthit.chpl.form;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Equator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FormItem {
    private Long id;

    @JsonIgnore
    private Form form;

    private Question question;
    private AllowedResponse parentResponse;

    @Singular
    private List<FormItem> childFormItems;

    private Integer sortOrder;
    private Boolean required;

    @Builder.Default
    private List<AllowedResponse> submittedResponses = new ArrayList<AllowedResponse>();

    public static class FormItemByIdEquator implements Equator<FormItem> {
        @Override
        public boolean equate(FormItem o1, FormItem o2) {
           return o1.getId().equals(o2.getId())
                   && CollectionUtils.isEqualCollection(o1.getSubmittedResponses(), o2.getSubmittedResponses(), new AllowedResponse.AllowedResponseByIdEquator());
        }

        @Override
        public int hash(FormItem o) {
            AllowedResponse.AllowedResponseByIdEquator equator = new AllowedResponse.AllowedResponseByIdEquator();
            return o.getId().intValue()
                    + o.getSubmittedResponses().stream()
                        .collect(Collectors.summingInt(resp -> equator.hash(resp)));
        }
    }

}
