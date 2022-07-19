package gov.healthit.chpl.form;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Form implements Serializable {
    private static final long serialVersionUID = 2148616530869605769L;

    private Long id;
    private String description;

    @Singular
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

    private List<FormItem> gatherAllFormItems(List<FormItem> formItems) {
        List<FormItem> accumulatedFormItems = new ArrayList<FormItem>();
        formItems.forEach(fi -> {
            accumulatedFormItems.add(fi);
            accumulatedFormItems.addAll(gatherAllFormItems(fi.getChildFormItems()));
        });

        return accumulatedFormItems;
    }

}
