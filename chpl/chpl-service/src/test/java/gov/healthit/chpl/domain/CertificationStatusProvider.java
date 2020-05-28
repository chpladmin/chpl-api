package gov.healthit.chpl.domain;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({ "checkstyle:linelength" })
public class CertificationStatusProvider {
    public static final Long ACTIVE = 1L;
    public static final Long RETIRED = 2L;
    public static final Long WITHDRAWN_BY_DEVELOPER = 3L;
    public static final Long WITHDRAWN_BY_ACB = 4L;
    public static final Long PENDING = 5L;
    public static final Long SUSPENDED_BY_ACB = 6L;
    public static final Long SUSPENDED_BY_ONC = 7L;
    public static final Long TERMINATED_BY_ONC = 8L;
    public static final Long WITHDRAWN_BY_DEVELOPER_UNDER_REVIEW = 9L;

    private Map<Long, CertificationStatus> certificationStatuses = new HashMap<Long, CertificationStatus>();

    public CertificationStatusProvider() {
        certificationStatuses.put(ACTIVE, createCertificationStatus(ACTIVE, "Active"));
        certificationStatuses.put(RETIRED, createCertificationStatus(RETIRED, "Retired"));
        certificationStatuses.put(WITHDRAWN_BY_DEVELOPER, createCertificationStatus(WITHDRAWN_BY_DEVELOPER, "Withdrawn by Developer"));
        certificationStatuses.put(WITHDRAWN_BY_ACB, createCertificationStatus(WITHDRAWN_BY_ACB, "Withdrawn by ONC-ACB"));
        certificationStatuses.put(PENDING, createCertificationStatus(PENDING, "Pending"));
        certificationStatuses.put(SUSPENDED_BY_ACB, createCertificationStatus(SUSPENDED_BY_ACB, "Suspended by ONC-ACB"));
        certificationStatuses.put(SUSPENDED_BY_ONC, createCertificationStatus(SUSPENDED_BY_ONC, "Suspended by ONC"));
        certificationStatuses.put(TERMINATED_BY_ONC, createCertificationStatus(TERMINATED_BY_ONC, "Terminated by ONC"));
        certificationStatuses.put(WITHDRAWN_BY_DEVELOPER_UNDER_REVIEW, createCertificationStatus(WITHDRAWN_BY_DEVELOPER_UNDER_REVIEW, "Withdrawn by Developer Under Surveillance/Review"));
    }

    public CertificationStatus get(Long id) {
        if (certificationStatuses.containsKey(id)) {
            return certificationStatuses.get(id);
        }
        return null;
    }

    private CertificationStatus createCertificationStatus(Long id, String name) {
        return CertificationStatus.builder().id(id).name(name).build();
    }
}
