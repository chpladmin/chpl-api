package gov.healthit.chpl.certifiedproduct.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.dao.CQMResultDAO;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CQMResultDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CqmResultSynchronizationService {
    private CertificationCriterionDAO certCriterionDao;
    private CQMCriterionDAO cqmCriterionDao;
    private CQMResultDAO cqmResultDao;

    @Autowired
    public CqmResultSynchronizationService(CertificationCriterionDAO certCriterionDao, CQMCriterionDAO cqmCriterionDao,
            CQMResultDAO cqmResultDao) {
        this.certCriterionDao = certCriterionDao;
        this.cqmCriterionDao = cqmCriterionDao;
        this.cqmResultDao = cqmResultDao;
    }

    public int synchronizeCqmss(CertifiedProductSearchDetails listing, List<CQMResultDetails> existingCqmDetails,
            List<CQMResultDetails> updatedCqmDetails)
            throws EntityCreationException, EntityRetrievalException, JsonProcessingException {

        int numChanges = 0;
        numChanges += addCqmResults(listing.getId(), existingCqmDetails, updatedCqmDetails);
        numChanges += removeCqmResults(listing.getId(), existingCqmDetails, updatedCqmDetails);
        numChanges += updateCqmResults(listing.getId(), existingCqmDetails, updatedCqmDetails);
        return numChanges;
    }

    private int addCqmResults(Long listingId, List<CQMResultDetails> existingCqms, List<CQMResultDetails> updatedCqms) {
        List<CQMResultDetails> cqmsToAdd = new ArrayList<CQMResultDetails>();
        updatedCqms.stream()
            .filter(updatedCqm -> updatedCqm.getSuccess() && !isCqmAttested(updatedCqm, existingCqms))
            .forEach(updatedCqmToAdd -> cqmsToAdd.add(updatedCqmToAdd));

        cqmsToAdd.stream()
            .forEach(cqmToAdd -> {
                try {
                    addCqmAndAllSuccessVersions(listingId, cqmToAdd);
                } catch (EntityRetrievalException | EntityCreationException ex) {
                    LOGGER.error("Error inserting CQM " + cqmToAdd.getCmsId() + " for listing ID " + listingId, ex);
                }
            });

        return cqmsToAdd.size();
    }

    private boolean isCqmAttested(CQMResultDetails cqmToFind, List<CQMResultDetails> cqms) {
        return cqms.stream()
            .filter(cqm -> (isNqfCqmMatching(cqmToFind, cqm) || isCmsCqmMatching(cqmToFind, cqm))
                    && BooleanUtils.isTrue(cqm.getSuccess()))
            .findAny().isPresent();
    }

    private void addCqmAndAllSuccessVersions(Long listingId, CQMResultDetails cqmDetails)
            throws EntityRetrievalException, EntityCreationException {
        List<CQMCriterion> cqmsToCreate = new ArrayList<CQMCriterion>();
        if (StringUtils.isEmpty(cqmDetails.getCmsId())) {
            cqmsToCreate = Stream.of(cqmCriterionDao.getNQFByNumber(cqmDetails.getNumber())).toList();
        } else if (cqmDetails.getCmsId().startsWith("CMS")) {
            cqmsToCreate.addAll(cqmDetails.getSuccessVersions().stream()
                    .map(successVersion -> cqmCriterionDao.getCMSByNumberAndVersion(cqmDetails.getCmsId(), successVersion))
                    .filter(lookedUpCqmCriterion -> lookedUpCqmCriterion != null)
                    .collect(Collectors.toList()));
        }
        if (CollectionUtils.isEmpty(cqmsToCreate)) {
            throw new EntityRetrievalException("Could not find CQM " + cqmDetails.getCmsId());
        }

        cqmsToCreate.stream()
            .forEach(cqmToCreate -> {
                //create a CQM+Version mapping for the listing and then add the linked criteria (c1,c2,c3,c4)
                CQMResultDTO newCQMResult = new CQMResultDTO();
                newCQMResult.setCertifiedProductId(listingId);
                newCQMResult.setCqmCriterionId(cqmToCreate.getCriterionId());
                newCQMResult.setSuccess(true);
                try {
                    CQMResultDTO createdCqmResult = cqmResultDao.create(newCQMResult);
                    if (cqmDetails.getCriteria() != null && cqmDetails.getCriteria().size() > 0) {
                        cqmDetails.getCriteria().stream()
                            .forEach(cqmCriterion -> cqmResultDao.createCriteriaMapping(createdCqmResult.getId(), cqmCriterion.getCriterion().getId()));
                    }
                } catch (Exception ex) {
                    LOGGER.error("Error creating CQM Result for CQM ID " + cqmToCreate.getCriterionId() + " and listing ID " + listingId, ex);
                }
            });
    }

    private int removeCqmResults(Long listingId, List<CQMResultDetails> existingCqms, List<CQMResultDetails> updatedCqms) {
        List<CQMResultDetails> cqmsToRemove = new ArrayList<CQMResultDetails>();
        existingCqms.stream()
            .filter(existingCqm -> existingCqm.getSuccess() && !isCqmAttested(existingCqm, updatedCqms))
            .forEach(existingCqmToRemove -> cqmsToRemove.add(existingCqmToRemove));

        cqmsToRemove.stream()
        .forEach(cqmToRemove -> {
            try {
                removeCqmResult(listingId, cqmToRemove);
            } catch (Exception ex) {
                LOGGER.error("Error removing CQM Result " + cqmToRemove.getId() + " for listing ID " + listingId, ex);
            }
        });

        return cqmsToRemove.size();
    }

    private void removeCqmResult(Long listingId, CQMResultDetails cqmToRemove) {
        cqmToRemove.getSuccessVersions().stream()
            .forEach(version -> {
                cqmResultDao.deleteByCmsNumberAndVersion(listingId, cqmToRemove.getCmsId(), version);
            });
        //criteria mappings will be removed via soft delete
    }

    private boolean isNqfCqmMatching(CQMResultDetails cqm1, CQMResultDetails cqm2) {
        // NQF is the same if the NQF numbers are equal
        return StringUtils.isEmpty(cqm2.getCmsId())
            && StringUtils.isEmpty(cqm1.getCmsId())
            && !StringUtils.isEmpty(cqm2.getNqfNumber())
            && !StringUtils.isEmpty(cqm1.getNqfNumber())
            && !cqm2.getNqfNumber().equals("N/A") && !cqm1.getNqfNumber().equals("N/A")
            && cqm2.getNqfNumber().equals(cqm1.getNqfNumber());
    }

    private boolean isCmsCqmMatching(CQMResultDetails cqm1, CQMResultDetails cqm2) {
        // CMS is the same if the CMS ID is equal
        return cqm2.getCmsId() != null && cqm1.getCmsId() != null
                && cqm2.getCmsId().equals(cqm1.getCmsId());
    }

    private int updateCqmResults(Long listingId, List<CQMResultDetails> existingCqms, List<CQMResultDetails> updatedCqms)
            throws EntityRetrievalException {
        List<CQMResultDetailsPair> cqmPairs = getCqmPairs(existingCqms, updatedCqms);
        cqmPairs.stream()
            .filter(cqmPair -> cqmPair.getOrig().getSuccess() && cqmPair.getUpdated().getSuccess())
            .forEach(cqmPair -> {
                //check for changes to success versions
                updateCqmSuccessVersions(listingId, cqmPair.getOrig(), cqmPair.getUpdated());
                //check for changes to associated c-criteria
                updateCqmAssociatedCriteria(cqmPair.getOrig(), cqmPair.getUpdated());
            });
        return cqmPairs.size();
    }

    private List<CQMResultDetailsPair> getCqmPairs(List<CQMResultDetails> existingCqms, List<CQMResultDetails> updatedCqms) {
        List<CQMResultDetailsPair> cqmPairs = new ArrayList<CQMResultDetailsPair>();
        if (updatedCqms != null && updatedCqms.size() > 0) {
            for (CQMResultDetails updatedCqm : updatedCqms) {
                for (CQMResultDetails existingCqm : existingCqms) {
                    if (isNqfCqmMatching(existingCqm, updatedCqm) || isCmsCqmMatching(existingCqm, updatedCqm)) {
                        cqmPairs.add(new CQMResultDetailsPair(existingCqm, updatedCqm));
                    }
                }
            }
        }
        return cqmPairs;
    }

    private int updateCqmAssociatedCriteria(CQMResultDetails existingCqm, CQMResultDetails updatedCqm) {
        List<CQMResultCertification> criteriaToAdd = new ArrayList<CQMResultCertification>();
        List<CQMResultCertification> criteriaToRemove = new ArrayList<CQMResultCertification>();

        for (CQMResultCertification existingCriterion : existingCqm.getCriteria()) {
            boolean exists = false;
            for (CQMResultCertification updatedCriterion : updatedCqm.getCriteria()) {
                if (existingCriterion.getCriterion().getId().equals(updatedCriterion.getCriterion().getId())) {
                    exists = true;
                }
            }
            if (!exists) {
                criteriaToRemove.add(existingCriterion);
            }
        }

        for (CQMResultCertification updatedCriterion : updatedCqm.getCriteria()) {
            boolean exists = false;
            for (CQMResultCertification existingCriterion : existingCqm.getCriteria()) {
                if (existingCriterion.getCriterion().getId().equals(updatedCriterion.getCriterion().getId())) {
                    exists = true;
                }
            }
            if (!exists) {
                criteriaToAdd.add(updatedCriterion);
            }
        }

        int numChanges = criteriaToAdd.size() + criteriaToRemove.size();
        for (CQMResultCertification currToAdd : criteriaToAdd) {
            Long mappedCriterionId = findCqmCriterionId(currToAdd);
            if (mappedCriterionId != null) {
                cqmResultDao.createCriteriaMapping(existingCqm.getId(), mappedCriterionId);
            }
        }
        for (CQMResultCertification currToRemove : criteriaToRemove) {
            cqmResultDao.deleteCriteriaMapping(currToRemove.getId());
        }
        return numChanges;
    }

    private int updateCqmSuccessVersions(Long listingId, CQMResultDetails existingCqm, CQMResultDetails updatedCqm) {
        List<String> versionsToAdd = new ArrayList<String>();
        List<String> versionsToRemove = new ArrayList<String>();

        for (String existingVersion : existingCqm.getSuccessVersions()) {
            boolean exists = false;
            for (String updatedVersion : updatedCqm.getSuccessVersions()) {
                if (existingVersion.equals(updatedVersion)) {
                    exists = true;
                }
            }
            if (!exists) {
                versionsToRemove.add(existingVersion);
            }
        }

        for (String updatedVersion : updatedCqm.getSuccessVersions()) {
            boolean exists = false;
            for (String existingVersion : existingCqm.getSuccessVersions()) {
                if (existingVersion.equals(updatedVersion)) {
                    exists = true;
                }
            }
            if (!exists) {
                versionsToAdd.add(updatedVersion);
            }
        }

        int numChanges = versionsToAdd.size() + versionsToRemove.size();
        for (String currToAdd : versionsToAdd) {
            try {
                cqmResultDao.create(listingId, existingCqm.getCmsId(), currToAdd, existingCqm.getCriteria());
            } catch (Exception ex) {
                LOGGER.error("Could not create mapping between listing " + listingId + " and CQM " + existingCqm.getCmsId() + " and version " + currToAdd);
            }
        }
        for (String currToRemove : versionsToRemove) {
            cqmResultDao.deleteByCmsNumberAndVersion(listingId, existingCqm.getCmsId(), currToRemove);
        }
        return numChanges;
    }

    private Long findCqmCriterionId(CQMResultCertification cqm) {
        if (cqm.getCriterion() != null && cqm.getCriterion().getId() != null) {
            return cqm.getCriterion().getId();
        } else if (cqm.getCriterion() != null && !StringUtils.isEmpty(cqm.getCriterion().getNumber())
                && !StringUtils.isEmpty(cqm.getCriterion().getTitle())) {
            CertificationCriterion cert = certCriterionDao.getByNumberAndTitle(
                    cqm.getCriterion().getNumber(), cqm.getCriterion().getTitle());
            if (cert != null) {
                return cert.getId();
            } else {
                LOGGER.error(
                        "Could not find certification criteria with number " + cqm.getCriterion().getNumber());
            }
        } else {
            LOGGER.error("A criteria id or number must be provided.");
        }
        return null;
    }

    @NoArgsConstructor
    @Data
    private static class CQMResultDetailsPair {
        private CQMResultDetails orig;
        private CQMResultDetails updated;

        CQMResultDetailsPair(CQMResultDetails orig, CQMResultDetails updated) {
            this.orig = orig;
            this.updated = updated;
        }
    }
}
