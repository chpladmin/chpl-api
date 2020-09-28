package gov.healthit.chpl.validation.listing.reviewer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.svap.dao.SvapDAO;
import gov.healthit.chpl.svap.domain.CertificationResultSvap;
import gov.healthit.chpl.svap.domain.Svap;
import gov.healthit.chpl.svap.domain.SvapCriteriaMap;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("svapReviewer")
public class SvapReviewer implements Reviewer{
    private Map<Long, List<SvapCriteriaMap>> svapCriteriaMap = new HashMap<Long, List<SvapCriteriaMap>>();
    private SvapDAO svapDao;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public SvapReviewer(SvapDAO svapDao, ErrorMessageUtil errorMessageUtil) {
        this.svapDao = svapDao;
        this.errorMessageUtil = errorMessageUtil;
    }

    @PostConstruct
    public void init() throws EntityRetrievalException {
        svapCriteriaMap = svapDao.getAllSvapCriteriaMap().stream()
                .collect(Collectors.groupingBy(scm -> scm.getCriterion().getId()));
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {

        //Make sure there are no SVAPs for non-2015 listings
        if (!isListing2015Edition(listing)) {
            listing.getCertificationResults().stream()
                    .filter(cr -> cr.getSvaps() != null && cr.getSvaps().size() > 0)
                    .forEach(cr -> listing.getErrorMessages().add(
                            errorMessageUtil.getMessage("listing.criteria.svap.invalidEdition",
                                    cr.getNumber(), getListingEdition(listing))));
        } else {
            List<CertificationResultSvap> svaps = listing.getCertificationResults().stream()
                .filter(cr -> cr.isSuccess() && cr.getSvaps() != null && cr.getSvaps().size() > 0)
                .map(cr -> cr.getSvaps())
                .flatMap(s -> s.stream()
                        .collect(Collectors.toList()));


            }
        }




    }

    private boolean isListing2015Edition(CertifiedProductSearchDetails listing) {
        return getListingEdition(listing).equals(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
    }

    private String getListingEdition(CertifiedProductSearchDetails listing) {
        return listing.getCertificationEdition().containsKey(CertifiedProductSearchDetails.EDITION_NAME_KEY) ?
                listing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_NAME_KEY).toString() : "";
    }

    private boolean isSvapValidForCriteria(Long svapId, Long criteriaId) {
        if (svapCriteriaMap.containsKey(criteriaId)) {
            return svapCriteriaMap.get(criteriaId).stream()
                    .filter(scm -> scm.getSvap().getSvapId().equals(svapId))
                    .findAny()
                    .isPresent();
        } else {
            return false;
        }
    }
}
