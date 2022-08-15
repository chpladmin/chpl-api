package gov.healthit.chpl.form;

import org.apache.commons.collections4.Equator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllowedResponse {
    private Long id;
    private String response;
    private Integer sortOrder;

    public static class AllowedResponseByIdEquator implements Equator<AllowedResponse> {

        @Override
        public boolean equate(AllowedResponse o1, AllowedResponse o2) {
            return o1.getId().equals(o2.getId());
        }

        @Override
        public int hash(AllowedResponse o) {
            return o.getId().intValue();
        }
    }

}
