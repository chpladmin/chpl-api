package gov.healthit.chpl.codesetdate;

import java.io.Serializable;
import java.util.Comparator;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CertificationResultCodeSetDateComparator implements Serializable, Comparator<CertificationResultCodeSetDate> {
    private static final long serialVersionUID = 4769747378094883510L;

    @Override
    public int compare(CertificationResultCodeSetDate csd1, CertificationResultCodeSetDate csd2) {
        if (csd1.getCodeSetDate().getRequiredDay() != null && csd2.getCodeSetDate().getRequiredDay() != null) {
            return csd1.getCodeSetDate().getRequiredDay().compareTo(csd2.getCodeSetDate().getRequiredDay());
        } else if (csd1.getCodeSetDate().getId() != null && csd2.getCodeSetDate().getId() != null) {
            return csd1.getCodeSetDate().getId().compareTo(csd2.getCodeSetDate().getId());
        }
        return 0;
    }
}
