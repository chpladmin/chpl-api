package gov.healthit.chpl.codeset;

import java.io.Serializable;
import java.util.Comparator;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CertificationResultCodeSetComparator implements Serializable, Comparator<CertificationResultCodeSet> {
    private static final long serialVersionUID = 4769747378094883510L;

    @Override
    public int compare(CertificationResultCodeSet cs1, CertificationResultCodeSet cs2) {
        if (cs1.getCodeSet().getRequiredDay() != null && cs2.getCodeSet().getRequiredDay() != null) {
            return cs1.getCodeSet().getRequiredDay().compareTo(cs2.getCodeSet().getRequiredDay());
        } else if (cs1.getCodeSet().getId() != null && cs2.getCodeSet().getId() != null) {
            return cs1.getCodeSet().getId().compareTo(cs2.getCodeSet().getId());
        }
        return 0;
    }
}
