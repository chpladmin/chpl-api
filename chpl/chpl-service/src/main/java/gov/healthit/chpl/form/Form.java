package gov.healthit.chpl.form;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Equator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Form implements Serializable {
    private static final long serialVersionUID = 2148616530869605769L;

    private Long id;
    private String description;
    private List<SectionHeading> sectionHeadings;

    public List<FormItem> extractFormItems() {
        return sectionHeadings.stream()
                .map(sh -> sh.getFormItems().stream())
                .flatMap(fi -> fi)
                .toList();
    }
    public List<FormItem> extractFlatFormItems() {
        return sectionHeadings.stream()
                .map(sh -> gatherAllFormItems(sh.getFormItems()).stream())
                .flatMap(fi -> fi)
                .toList();
    }

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


    private List<FormItem> gatherAllFormItems(List<FormItem> formItems) {
        List<FormItem> accumulatedFormItems = new ArrayList<FormItem>();
        formItems.forEach(fi -> {
            accumulatedFormItems.add(fi);
            accumulatedFormItems.addAll(gatherAllFormItems(fi.getChildFormItems()));
        });

        return accumulatedFormItems;
    }

}
