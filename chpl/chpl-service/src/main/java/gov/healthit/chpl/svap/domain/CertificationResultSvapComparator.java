package gov.healthit.chpl.svap.domain;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

public class CertificationResultSvapComparator implements Serializable, Comparator<CertificationResultSvap> {

    private static final long serialVersionUID = -7237838265637377503L;

    @Override
    public int compare(CertificationResultSvap svap1, CertificationResultSvap svap2) {
        if (!StringUtils.isEmpty(svap1.getRegulatoryTextCitation()) && !StringUtils.isEmpty(svap2.getRegulatoryTextCitation())) {
            return svap1.getRegulatoryTextCitation().compareTo(svap2.getRegulatoryTextCitation());
        } else if (svap1.getSvapId() != null && svap2.getSvapId() != null) {
            return svap1.getSvapId().compareTo(svap2.getSvapId());
        }
        return 0;    }

}
