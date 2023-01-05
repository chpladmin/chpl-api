package gov.healthit.chpl.complaint.search;

import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dto.CertificationBodyDTO;

@Component
public class ComplaintSearchRequestNormalizer {

    private CertificationBodyDAO certificationBodyDao;

    @Autowired
    public ComplaintSearchRequestNormalizer(CertificationBodyDAO certificationBodyDao) {
        this.certificationBodyDao = certificationBodyDao;
    }

    public void normalize(ComplaintSearchRequest request) {
        normalizeSearchTerm(request);
        normalizeInformedOncs(request);
        normalizeAtlsContacted(request);
        normalizeComplainantsContacted(request);
        normalizeDevelopersContacted(request);
        normalizeCertificationBodyNames(request);
        normalizeComplainantTypeNames(request);
        normalizeCurrentStatusNames(request);
        normalizeClosedDates(request);
        normalizeReceivedDates(request);
        normalizeOpenDuringRangeDates(request);
        normalizeOrderBy(request);
        normalizePageNumber(request);
        normalizePageSize(request);

        unionCertificationBodies(request);
    }

    private void normalizeSearchTerm(ComplaintSearchRequest request) {
        if (!StringUtils.isBlank(request.getSearchTerm())) {
            request.setSearchTerm(StringUtils.normalizeSpace(request.getSearchTerm()));
        } else {
            request.setSearchTerm(null);
        }
    }

    private void normalizeInformedOncs(ComplaintSearchRequest request) {
        if (!CollectionUtils.isEmpty(request.getInformedOncStrings())) {
            request.getInformedOncStrings().stream()
                .forEach(informedOncString -> normalizeInformedOncString(request, informedOncString));
        }
    }

    private void normalizeInformedOncString(ComplaintSearchRequest request, String informedOncString) {
        String informedOnc = StringUtils.normalizeSpace(informedOncString);
        if (!StringUtils.isBlank(informedOnc) && isParseableBoolean(informedOnc)) {
            request.getInformedOnc().add(BooleanUtils.toBooleanObject(informedOnc));
        }
    }

    private void normalizeAtlsContacted(ComplaintSearchRequest request) {
        if (!CollectionUtils.isEmpty(request.getOncAtlContactedStrings())) {
            request.getOncAtlContactedStrings().stream()
                .forEach(atlContactedString -> normalizeAtlContactedString(request, atlContactedString));
        }
    }

    private void normalizeAtlContactedString(ComplaintSearchRequest request, String atlContactedString) {
        String atlContacted = StringUtils.normalizeSpace(atlContactedString);
        if (!StringUtils.isBlank(atlContacted) && isParseableBoolean(atlContacted)) {
            request.getOncAtlContacted().add(BooleanUtils.toBooleanObject(atlContacted));
        }
    }

    private void normalizeComplainantsContacted(ComplaintSearchRequest request) {
        if (!CollectionUtils.isEmpty(request.getComplainantContactedStrings())) {
            request.getComplainantContactedStrings().stream()
                .forEach(complainantContactedString -> normalizeComplainantContactedString(request, complainantContactedString));
        }
    }

    private void normalizeComplainantContactedString(ComplaintSearchRequest request, String complainantContactedString) {
        String complainantContacted = StringUtils.normalizeSpace(complainantContactedString);
        if (!StringUtils.isBlank(complainantContacted) && isParseableBoolean(complainantContacted)) {
                request.getComplainantContacted().add(BooleanUtils.toBooleanObject(complainantContacted));
        }
    }

    private void normalizeDevelopersContacted(ComplaintSearchRequest request) {
        if (!CollectionUtils.isEmpty(request.getDeveloperContactedStrings())) {
            request.getDeveloperContactedStrings().stream()
                .forEach(developerContactedString -> normalizeDeveloperContactedString(request, developerContactedString));
        }
    }

    private void normalizeDeveloperContactedString(ComplaintSearchRequest request, String developerContactedString) {
        String developerContacted = StringUtils.normalizeSpace(developerContactedString);
        if (!StringUtils.isBlank(developerContacted) && isParseableBoolean(developerContacted)) {
            request.getDeveloperContacted().add(BooleanUtils.toBooleanObject(developerContacted));
        }
    }

    private void normalizeCertificationBodyNames(ComplaintSearchRequest request) {
        if (!CollectionUtils.isEmpty(request.getCertificationBodyNames())) {
            request.setCertificationBodyNames(request.getCertificationBodyNames().stream()
                    .filter(acbName -> !StringUtils.isBlank(acbName))
                    .map(acbName -> StringUtils.normalizeSpace(acbName))
                    .collect(Collectors.toSet()));
        }
    }

    private void unionCertificationBodies(ComplaintSearchRequest request) {
        // ACB IDs and ACB Names are both allowed as search request inputs.
        // Only the IDs will be used when we do the search.
        if (!CollectionUtils.isEmpty(request.getCertificationBodyNames())) {
            request.getCertificationBodyNames().stream()
                .forEach(acbName -> addAcbToIdList(request, acbName));
        }
    }

    private void addAcbToIdList(ComplaintSearchRequest request, String acbName) {
        CertificationBodyDTO acb = certificationBodyDao.getByName(acbName);
        if (acb != null) {
            request.getAcbIds().add(acb.getId());
        }
    }

    private void normalizeComplainantTypeNames(ComplaintSearchRequest request) {
        if (!CollectionUtils.isEmpty(request.getComplainantTypeNames())) {
            request.setComplainantTypeNames(request.getComplainantTypeNames().stream()
                    .filter(ctName -> !StringUtils.isBlank(ctName))
                    .map(ctName -> StringUtils.normalizeSpace(ctName))
                    .collect(Collectors.toSet()));
        }
    }

    private void normalizeCurrentStatusNames(ComplaintSearchRequest request) {
        if (!CollectionUtils.isEmpty(request.getCurrentStatusNames())) {
            request.setCurrentStatusNames(request.getCurrentStatusNames().stream()
                    .filter(statusName -> !StringUtils.isBlank(statusName))
                    .map(statusName -> StringUtils.normalizeSpace(statusName))
                    .collect(Collectors.toSet()));
        }
    }

    private void normalizeClosedDates(ComplaintSearchRequest request) {
        if (!StringUtils.isEmpty(request.getClosedDateStart())) {
            request.setClosedDateStart(StringUtils.normalizeSpace(request.getClosedDateStart()));
        }
        if (!StringUtils.isEmpty(request.getClosedDateEnd())) {
            request.setClosedDateEnd(StringUtils.normalizeSpace(request.getClosedDateEnd()));
        }
    }

    private void normalizeReceivedDates(ComplaintSearchRequest request) {
        if (!StringUtils.isEmpty(request.getReceivedDateStart())) {
            request.setReceivedDateStart(StringUtils.normalizeSpace(request.getReceivedDateStart()));
        }
        if (!StringUtils.isEmpty(request.getReceivedDateEnd())) {
            request.setReceivedDateEnd(StringUtils.normalizeSpace(request.getReceivedDateEnd()));
        }
    }

    private void normalizeOpenDuringRangeDates(ComplaintSearchRequest request) {
        if (!StringUtils.isEmpty(request.getOpenDuringRangeStart())) {
            request.setOpenDuringRangeStart(StringUtils.normalizeSpace(request.getOpenDuringRangeStart()));
        }
        if (!StringUtils.isEmpty(request.getOpenDuringRangeEnd())) {
            request.setOpenDuringRangeEnd(StringUtils.normalizeSpace(request.getOpenDuringRangeEnd()));
        }
    }

    private void normalizeOrderBy(ComplaintSearchRequest request) {
        if (!StringUtils.isBlank(request.getOrderByString())
                && request.getOrderBy() == null) {
            try {
                request.setOrderBy(
                        OrderByOption.valueOf(StringUtils.normalizeSpace(request.getOrderByString()).toUpperCase()));
            } catch (Exception ignore) {
            }
        }
    }

    private void normalizePageNumber(ComplaintSearchRequest request) {
        if (!StringUtils.isEmpty(request.getPageNumberString())
                && isParseableInt(request.getPageNumberString())) {
            request.setPageNumber(Integer.parseInt(request.getPageNumberString()));
        }
    }

    private void normalizePageSize(ComplaintSearchRequest request) {
        if (!StringUtils.isEmpty(request.getPageSizeString())
                && isParseableInt(request.getPageSizeString())) {
            request.setPageSize(Integer.parseInt(request.getPageSizeString()));
        }
    }

    private boolean isParseableBoolean(String value) {
        Boolean booleanObj = null;
        try {
            booleanObj = BooleanUtils.toBooleanObject(value);
        } catch (Exception ex) {
            return false;
        }
        return booleanObj != null;
    }

    private boolean isParseableInt(String value) {
        try {
            Integer.parseInt(value);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }
}
