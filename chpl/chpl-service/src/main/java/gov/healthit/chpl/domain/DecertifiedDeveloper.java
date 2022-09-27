package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import lombok.Data;

@Data
@Deprecated
public class DecertifiedDeveloper implements Serializable {
    private static final long serialVersionUID = -6684356941321177859L;
    private Long developerId;
    private String developerName;
    private Date decertificationDate;
    private Set<String> acbNames = new HashSet<String>();
}
