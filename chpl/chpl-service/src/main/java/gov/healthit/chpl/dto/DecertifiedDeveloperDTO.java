package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import lombok.Data;

@Data
public class DecertifiedDeveloperDTO implements Serializable {
    private static final long serialVersionUID = 4328181604320362899L;

    private DeveloperDTO developer;
    private Set<CertificationBodyDTO> acbs = new HashSet<CertificationBodyDTO>();
    private Date decertificationDate;
}
