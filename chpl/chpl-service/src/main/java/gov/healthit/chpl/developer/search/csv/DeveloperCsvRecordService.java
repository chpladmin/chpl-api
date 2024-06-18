package gov.healthit.chpl.developer.search.csv;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.developer.search.DeveloperSearchResult;
import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.IdNamePair;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.util.DateUtil;

@Component
public class DeveloperCsvRecordService {
    private String developerUrlUnformatted;

    @Autowired
    public DeveloperCsvRecordService(@Value("${chplUrlBegin}") String chplUrlBegin,
            @Value("${developerUrlPart}") String developerUrlPart) {
        this.developerUrlUnformatted = chplUrlBegin + developerUrlPart;
    }

    public List<String> getRecord(DeveloperSearchResult dev) {
        List<String> devRecord = new ArrayList<String>();
        devRecord.add(dev.getName());
        devRecord.add(dev.getCode());
        devRecord.add(BooleanUtils.isTrue(dev.getSelfDeveloper()) ? "Yes" : "No");
        devRecord.add(String.format(developerUrlUnformatted, dev.getId() + ""));
        devRecord.add(StringUtils.isEmpty(dev.getWebsite()) ? "" : dev.getWebsite());
        devRecord.add(dev.getStatus() == null ? "" : dev.getStatus().getName());
        devRecord.add(dev.getDecertificationDate() == null ? "" : DateUtil.format(dev.getDecertificationDate()));
        devRecord.add(dev.getCurrentActiveListingCount() == null ? "0" : dev.getCurrentActiveListingCount() + "");
        devRecord.add(dev.getMostRecentPastAttestationPeriodActiveListingCount() == null ? "0" : dev.getMostRecentPastAttestationPeriodActiveListingCount() + "");
        devRecord.add(BooleanUtils.isTrue(dev.getSubmittedAttestationsForMostRecentPastPeriod()) ? "Yes" : "No");
        devRecord.add(BooleanUtils.isTrue(dev.getPublishedAttestationsForMostRecentPastPeriod()) ? "Yes" : "No");
        devRecord.add(formatAcbList(dev.getAcbsForActiveListings()));
        devRecord.add(formatAcbList(dev.getAcbsForAllListings()));
        devRecord.add(dev.getContact() == null ? "" : dev.getContact().getFullName());
        devRecord.add(dev.getContact() == null ? "" : dev.getContact().getEmail());
        devRecord.add(dev.getContact() == null ? "" : dev.getContact().getPhoneNumber());
        devRecord.add(dev.getAddress() == null ? "" : formatAddress(dev.getAddress()));
        devRecord.add(dev.getAddress() == null ? "" : dev.getAddress().getCity());
        devRecord.add(dev.getAddress() == null ? "" : dev.getAddress().getState());
        devRecord.add(dev.getAddress() == null ? "" : dev.getAddress().getZipcode());
        return devRecord;
    }

    public List<String> getRecordWithUsers(DeveloperSearchResult dev, List<User> allDeveloperUsers) {
        List<String> devRecord = getRecord(dev);
        List<User> developerUsers = allDeveloperUsers.stream()
                .filter(devUser -> devUser.getOrganizations().stream()
                                .filter(org -> org.getId().equals(dev.getId()))
                                .findAny()
                                .isPresent())
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(developerUsers)) {
            devRecord.add("");
        } else {
            devRecord.add(formatUsers(developerUsers));
        }
        return devRecord;
    }

    private String formatAddress(Address address) {
        String result = "";
        if (!StringUtils.isEmpty(address.getLine1())) {
            result += address.getLine1();
        }
        if (!StringUtils.isEmpty(address.getLine2())) {
            result += "\n" + address.getLine2();
        }
        return result;
    }

    private String formatAcbList(Set<IdNamePair> acbs) {
        return acbs.stream()
            .map(idNamePair -> idNamePair.getName())
            .collect(Collectors.joining("; "));
    }

    private String formatUsers(List<User> users) {
        return users.stream()
            .map(user -> user.getFullName() + " <" + user.getEmail() + ">")
            .collect(Collectors.joining("; "));
    }
}
