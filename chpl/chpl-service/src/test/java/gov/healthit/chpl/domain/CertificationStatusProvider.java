package gov.healthit.chpl.domain;

import java.util.HashMap;
import java.util.Map;

public class CertificationStatusProvider {
    public static Long ACTIVE = 1L;
    public static Long RETIRED = 2L;
    public static Long WITHDRAWN_BY_DEVELOPER = 3L;
    public static Long WITHDRAWN_BY_ACB = 4L;
    public static Long PENDING = 5L;
    public static Long SUSPENDED_BY_ACB = 6L;
    public static Long SUSPENDED_BY_ONC = 7L;
    public static Long TERMINATED_BY_ONC = 8L;
    public static Long WITHDRAWN_BY_DEVELOPER_UNDER_REVIEW = 9L;

    private Map<Long, CertificationStatus> certificationStatuses = new HashMap<Long, CertificationStatus>();

    public CertificationStatusProvider() {
        certificationStatuses.put(1L, createCertificationStatus(1L, "Active"));
        certificationStatuses.put(2L, createCertificationStatus(2L, "Retired"));
        certificationStatuses.put(3L, createCertificationStatus(3L, "Withdrawn by Developer"));
        certificationStatuses.put(4L, createCertificationStatus(4L, "Withdrawn by ONC-ACB"));
        certificationStatuses.put(5L, createCertificationStatus(5L, "Pending"));
        certificationStatuses.put(6L, createCertificationStatus(6L, "Suspended by ONC-ACB"));
        certificationStatuses.put(7L, createCertificationStatus(7L, "Suspended by ONC"));
        certificationStatuses.put(8L, createCertificationStatus(8L, "Terminated by ONC"));
        certificationStatuses.put(9L, createCertificationStatus(9L, "Withdrawn by Developer Under Surveillance/Review"));
    }

    public CertificationStatus get(Long id) {
        if (certificationStatuses.containsKey(id)) {
            return certificationStatuses.get(id);
        }
        return null;
    }

    private CertificationStatus createCertificationStatus(Long id, String name) {
        return CertificationStatus.builder()
                .id(id)
                .name(name)
                .build();
    }
}
