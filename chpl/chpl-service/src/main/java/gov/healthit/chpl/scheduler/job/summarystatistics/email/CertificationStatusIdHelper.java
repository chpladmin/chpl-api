package gov.healthit.chpl.scheduler.job.summarystatistics.email;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.util.CertificationStatusUtil;
import lombok.Data;

@Data
public class CertificationStatusIdHelper {
    private List<CertificationStatus> certificationStatuses;
    private List<Long> nonRetiredStatusIds, activeAndSuspendedStatusIds,
        suspendedStatusIds, withdrawnByDeveloperStatusIds;

    public CertificationStatusIdHelper(CertificationStatusDAO certificationStatusDao) {
        certificationStatuses = certificationStatusDao.findAll();
        nonRetiredStatusIds = CertificationStatusUtil.getNonretiredStatuses().stream()
                .map(status -> getStatusId(status))
                .collect(Collectors.toList());
        activeAndSuspendedStatusIds = CertificationStatusUtil.getActiveStatuses().stream()
                .map(status -> getStatusId(status))
                .collect(Collectors.toList());
        suspendedStatusIds = CertificationStatusUtil.getSuspendedStatuses().stream()
                .map(status -> getStatusId(status))
                .collect(Collectors.toList());
        withdrawnByDeveloperStatusIds = Stream.of(CertificationStatusType.WithdrawnByDeveloper)
                .map(status -> getStatusId(status))
                .collect(Collectors.toList());
    }

    private Long getStatusId(CertificationStatusType statusType) {
        return certificationStatuses.stream()
            .filter(status -> status.getName().equalsIgnoreCase(statusType.getName()))
            .findAny().get().getId();
    }
}
