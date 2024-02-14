package gov.healthit.chpl.codesetdate;

import java.util.Comparator;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CodeSetDateComparator implements Comparator<CodeSetDate> {

    @Override
    public int compare(CodeSetDate csd1, CodeSetDate csd2) {
        if (csd1.getRequiredDay() != null  && csd2.getRequiredDay() != null) {
            return csd1.getRequiredDay().compareTo(csd2.getRequiredDay());
        } else if (csd1.getId() != null && csd2.getId() != null) {
            return csd1.getId().compareTo(csd2.getId());
        }
        return 0;
    }
}
