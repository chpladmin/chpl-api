package gov.healthit.chpl.realworldtesting.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;

import gov.healthit.chpl.util.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RealWorldTestingReport {
    private String acbName;
    private String chplProductNumber;
    private String currentStatus;
    private LocalDate certificationDate;
    private String productName;
    private Long productId;
    private String developerName;
    private Long developerId;
    private List<String> developerUsers;
    private Integer rwtEligibilityYear;
    private Boolean ics;
    private String rwtPlansUrl;
    private LocalDate rwtPlansCheckDate;
    private String rwtResultsUrl;
    private LocalDate rwtResultsCheckDate;
    private String rwtPlansMessage;
    private String rwtResultsMessage;

    public List<String> toListOfStrings() {
        List<String> results = new ArrayList<String>();
        results.add(acbName);
        results.add(chplProductNumber);
        results.add(currentStatus);
        results.add(DateUtil.format(certificationDate));
        results.add(productName);
        results.add(productId == null ? null : productId.toString());
        results.add(developerName);
        results.add(developerId == null ? null : developerId.toString());
        results.add(!CollectionUtils.isEmpty(developerUsers) ? developerUsers.stream().collect(Collectors.joining("; ")) : "");
        results.add(rwtEligibilityYear == null ? null : rwtEligibilityYear.toString());
        results.add(BooleanUtils.isTrue(ics) ? "Yes" : "");
        results.add(rwtPlansUrl);
        results.add(rwtPlansCheckDate == null ? null : rwtPlansCheckDate.toString());
        results.add(rwtResultsUrl);
        results.add(rwtResultsCheckDate == null ? null : rwtResultsCheckDate.toString());
        results.add(rwtPlansMessage);
        results.add(rwtResultsMessage);
        return results;
    }

    public static List<String> getHeaders() {
        List<String> results = new ArrayList<String>();
        results.add("ONC-ACB Name");
        results.add("CHPL ID");
        results.add("Current Status");
        results.add("Certification Date");
        results.add("Product Name");
        results.add("Product DBID");
        results.add("Developer Name");
        results.add("Developer DBID");
        results.add("Developer Users");
        results.add("Initial RWT Year");
        results.add("ICS");
        results.add("RWT Plans URL");
        results.add("RWT Plans Submission Confirmed");
        results.add("RWT Results URL");
        results.add("RWT Results Submission Confirmed");
        results.add("RWT Plans Message");
        results.add("RWT Results Message");
        return results;
    }
}
