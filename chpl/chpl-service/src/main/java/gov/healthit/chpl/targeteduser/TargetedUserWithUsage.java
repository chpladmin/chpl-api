package gov.healthit.chpl.targeteduser;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;

import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TargetedUserWithUsage implements Serializable {
    private static final long serialVersionUID = 5819005018143800705L;

    @JsonIgnore
    public static final List<String> CSV_HEADINGS = Stream.of(
            "Name", "Created On",
            CertificationStatusType.Active.getName(),
            CertificationStatusType.SuspendedByOnc.getName(),
            CertificationStatusType.SuspendedByAcb.getName(),
            CertificationStatusType.TerminatedByOnc.getName(),
            CertificationStatusType.WithdrawnByDeveloperUnderReview.getName(),
            CertificationStatusType.WithdrawnByAcb.getName(),
            CertificationStatusType.WithdrawnByDeveloper.getName(),
            CertificationStatusType.Retired.getName()
            ).toList();

    private Long id;
    private String name;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate creationDate;
    private List<UsageByCertificationStatus> usage;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static final class UsageByCertificationStatus {
        private CertificationStatusType certificationStatus;
        private Long listingCount;
    }

    @JsonIgnore
    public List<String> toListOfStringsForCsv() {
        List<String> csvFields = new ArrayList<String>();
        csvFields.add(name);
        csvFields.add(DateUtil.format(creationDate));
        csvFields.add(getCountForStatus(CertificationStatusType.Active));
        csvFields.add(getCountForStatus(CertificationStatusType.SuspendedByOnc));
        csvFields.add(getCountForStatus(CertificationStatusType.SuspendedByAcb));
        csvFields.add(getCountForStatus(CertificationStatusType.TerminatedByOnc));
        csvFields.add(getCountForStatus(CertificationStatusType.WithdrawnByDeveloperUnderReview));
        csvFields.add(getCountForStatus(CertificationStatusType.WithdrawnByAcb));
        csvFields.add(getCountForStatus(CertificationStatusType.WithdrawnByDeveloper));
        csvFields.add(getCountForStatus(CertificationStatusType.Retired));
        return csvFields;
    }

    private String getCountForStatus(CertificationStatusType certStatus) {
        Optional<UsageByCertificationStatus> usageForStatus = getUsage().stream()
                .filter(u -> u.getCertificationStatus().equals(certStatus))
                .findAny();
        if (usageForStatus.isPresent()) {
            return usageForStatus.get().getListingCount() + "";
        }
        return "0";
    }
}
