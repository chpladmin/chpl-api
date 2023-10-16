package gov.healthit.chpl.util;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import gov.healthit.chpl.certificationCriteria.CriterionStatus;

public class CriterionStatusAdapter extends XmlAdapter<String, CriterionStatus> {
    @Override
    public CriterionStatus unmarshal(String v) throws Exception {
        return CriterionStatus.valueOf(v);
    }

    @Override
    public String marshal(CriterionStatus v) throws Exception {
        return v.name();
    }
}
