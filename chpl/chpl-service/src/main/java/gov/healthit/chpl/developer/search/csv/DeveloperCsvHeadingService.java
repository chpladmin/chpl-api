package gov.healthit.chpl.developer.search.csv;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.manager.AttestationPeriodService;
import gov.healthit.chpl.util.DateUtil;

@Component
public class DeveloperCsvHeadingService {
    public static final String[] ANONYMOUS_DEVELOPER_HEADINGS = new String[] {"Name", "Code", "Self Developer",
            "Link in CHPL", "Website", "Status", "Decertification Date",
            "Number of Active Listings",
            "Number of Active Listings (%s)",
            "Submitted Attestations (%s)",
            "Published Attestations (%s)",
            "ONC-ACBs for Active Listings",
            "ONC-ACBs for All Listings",
            "Contact Name", "Contact Email", "Contact Phone Number",
            "Address", "City", "State", "Zipcode" };
    public static final String[] AUTHENTICATED_DEVELOPER_HEADINGS = new String[] {"Users"};
    private AttestationPeriodService attestationPeriodService;

    @Autowired
    public DeveloperCsvHeadingService(AttestationPeriodService attestationPeriodService) {
        this.attestationPeriodService = attestationPeriodService;
    }

    public List<String> getCsvHeadings() {
        List<String> unformattedAnonymousHeadings = Stream.of(ANONYMOUS_DEVELOPER_HEADINGS).collect(Collectors.toList());
        AttestationPeriod mostRecentPastPeriod = attestationPeriodService.getMostRecentPastAttestationPeriod();
        List<String> formattedAnonymousHeadings = unformattedAnonymousHeadings.stream()
            .map(heading -> formatAttestationPeriodInHeading(heading, mostRecentPastPeriod))
            .collect(Collectors.toList());
        return formattedAnonymousHeadings;
    }

    public List<String> getCsvHeadingsWithUsers() {
        return Stream.concat(getCsvHeadings().stream(), Stream.of(AUTHENTICATED_DEVELOPER_HEADINGS))
                .collect(Collectors.toList());
    }

    private String formatAttestationPeriodInHeading(String heading, AttestationPeriod mostRecentPastPeriod) {
        String headingCopy = new String(heading);
        if (headingCopy.contains("%s")) {
            return String.format(headingCopy,
                    DateUtil.format(mostRecentPastPeriod.getPeriodStart()) + " - " + DateUtil.format(mostRecentPastPeriod.getPeriodEnd()));
        }
        return headingCopy;
    }
}
