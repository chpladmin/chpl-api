package gov.healthit.chpl.codeset;

import java.util.Comparator;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CodeSetComparator implements Comparator<CodeSet> {

    @Override
    public int compare(CodeSet cs1, CodeSet cs2) {
        if (cs1.getRequiredDay() != null  && cs2.getRequiredDay() != null) {
            return cs1.getRequiredDay().compareTo(cs2.getRequiredDay());
        } else if (cs1.getId() != null && cs2.getId() != null) {
            return cs1.getId().compareTo(cs2.getId());
        }
        return 0;
    }
}
