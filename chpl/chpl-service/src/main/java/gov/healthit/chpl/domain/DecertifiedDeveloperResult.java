package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.util.DateUtil;
import lombok.Data;

@Data
public class DecertifiedDeveloperResult implements Serializable {
    private static final long serialVersionUID = 7463932788024776464L;
    private Developer developer;
    private List<CertificationBody> certifyingBody;
    private Date decertificationDate;
    private Long promotingInteroperabilityUsers;
    private LocalDate earliestPromotingInteroperabilityUserCountDate;
    private LocalDate latestPromotingInteroperabilityUserCountDate;

    @Deprecated
    private Long estimatedUsers;
    @Deprecated
    private Long earliestMeaningfulUseDate;
    @Deprecated
    private Long latestMeaningfulUseDate;

    public DecertifiedDeveloperResult() {
    }

    public DecertifiedDeveloperResult(Developer developer, List<CertificationBody> certifyingBody,
            Date decertificationDate, Long promotingInteroperabilityUsers,
            Date earliestPromotingInteroperabilityUserCountDate, Date latestPromotingInteroperabilityUserCountDate) {
        this.developer = developer;
        this.certifyingBody = certifyingBody;
        this.decertificationDate = decertificationDate;
        this.promotingInteroperabilityUsers = promotingInteroperabilityUsers;
        if (earliestPromotingInteroperabilityUserCountDate != null) {
            this.earliestPromotingInteroperabilityUserCountDate
                = DateUtil.toLocalDate(earliestPromotingInteroperabilityUserCountDate.getTime());
        }
        if (latestPromotingInteroperabilityUserCountDate != null) {
            this.latestPromotingInteroperabilityUserCountDate
                = DateUtil.toLocalDate(latestPromotingInteroperabilityUserCountDate.getTime());
        }

        this.estimatedUsers = promotingInteroperabilityUsers;
        if (earliestPromotingInteroperabilityUserCountDate != null) {
            this.earliestMeaningfulUseDate = earliestPromotingInteroperabilityUserCountDate.getTime();
        }
        if (latestPromotingInteroperabilityUserCountDate != null) {
            this.latestMeaningfulUseDate = latestPromotingInteroperabilityUserCountDate.getTime();
        }
    }

    public DecertifiedDeveloperResult(DeveloperDTO developerDTO, List<CertificationBody> certifyingBody,
            Date decertificationDate, Long promotingInteroperabilityUsers,
            Date earliestPromotingInteroperabilityUserCountDate, Date latestPromotingInteroperabilityUserCountDate) {
        this.developer = new Developer(developerDTO);
        this.certifyingBody = certifyingBody;
        this.decertificationDate = decertificationDate;
        this.promotingInteroperabilityUsers = promotingInteroperabilityUsers;
        if (earliestPromotingInteroperabilityUserCountDate != null) {
            this.earliestPromotingInteroperabilityUserCountDate
                = DateUtil.toLocalDate(earliestPromotingInteroperabilityUserCountDate.getTime());
        }
        if (latestPromotingInteroperabilityUserCountDate != null) {
            this.latestPromotingInteroperabilityUserCountDate
                = DateUtil.toLocalDate(latestPromotingInteroperabilityUserCountDate.getTime());
        }

        this.estimatedUsers = promotingInteroperabilityUsers;
        if (earliestPromotingInteroperabilityUserCountDate != null) {
            this.earliestMeaningfulUseDate = earliestPromotingInteroperabilityUserCountDate.getTime();
        }
        if (latestPromotingInteroperabilityUserCountDate != null) {
            this.latestMeaningfulUseDate = latestPromotingInteroperabilityUserCountDate.getTime();
        }
    }

    public boolean refersToAcbId(Long acbId) {
        return certifyingBody.stream()
                .map(acb -> acb.getId())
                .collect(Collectors.toList()).contains(acbId);
    }

    public void incrementPromotingInteroperabilityUserCount(long increment) {
        if (this.getPromotingInteroperabilityUsers() == null) {
            this.promotingInteroperabilityUsers = increment;
            this.estimatedUsers = increment;
        } else {
            this.promotingInteroperabilityUsers += increment;
            this.estimatedUsers += increment;
        }
    }
}
