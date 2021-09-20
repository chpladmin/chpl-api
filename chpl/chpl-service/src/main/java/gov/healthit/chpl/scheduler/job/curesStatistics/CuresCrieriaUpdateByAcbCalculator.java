package gov.healthit.chpl.scheduler.job.curesStatistics;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.SpecialProperties;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.service.CertificationCriterionService;
import lombok.extern.log4j.Log4j2;
import one.util.streamex.StreamEx;

@Log4j2(topic = "curesStatisticsCreatorJobLogger")
@Component
public class CuresCrieriaUpdateByAcbCalculator {
    //private CertificationCriterionService certificationCriterionService;
    private CertifiedProductDAO certifiedProductDAO;
    private CertificationResultActivityHistoryHelper activityStatisticsHelper;
    private CertificationBodyDAO certificationBodyDAO;

    private List<CuresCriteriaUpdate> curesCriteriaUpdates = new ArrayList<CuresCrieriaUpdateByAcbCalculator.CuresCriteriaUpdate>();
    private List<CertificationStatusType> activeStatuses;
    private Date curesEffectiveDate;


    @Autowired
    public CuresCrieriaUpdateByAcbCalculator(CertificationCriterionService certificationCriterionService,
            CertifiedProductDAO certifiedProductDAO,
            CertificationBodyDAO certificationBodyDAO,
            CertificationResultActivityHistoryHelper activityStatisticsHelper,
            SpecialProperties specialProperties) {

        //this.certificationCriterionService = certificationCriterionService;
        this.certifiedProductDAO = certifiedProductDAO;
        this.activityStatisticsHelper = activityStatisticsHelper;
        this.certificationBodyDAO = certificationBodyDAO;

        curesEffectiveDate = specialProperties.getEffectiveRuleDate();

        activeStatuses = Stream.of(CertificationStatusType.Active,
                CertificationStatusType.SuspendedByAcb,
                CertificationStatusType.SuspendedByOnc)
                .collect(Collectors.toList());

        curesCriteriaUpdates = certificationCriterionService.getOriginalToCuresCriteriaMap().entrySet().stream()
                .map(entry -> new CuresCriteriaUpdate(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }


    public List<CuresCriteriaUpdateByAcb> calculate() throws EntityRetrievalException {
        List<CuresCriteriaUpdateByAcb> curesCriteriaUpdateByAcbs = new ArrayList<CuresCriteriaUpdateByAcb>();
        for (CuresCriteriaUpdate curesCriteriaUpdate : curesCriteriaUpdates) {
            List<CertifiedProductDetailsDTO> listingsAttestingToCriterion = certifiedProductDAO.getListingsAttestingToCriterion(curesCriteriaUpdate.getCuresCriterion().getId(), activeStatuses);

            List<Long> acbIds = getListOfAcbs(listingsAttestingToCriterion);

            for (Long acbId : acbIds) {
                CuresCriteriaUpdateByAcb x = new CuresCriteriaUpdateByAcb();
                x.setCuresCriteriaUpdate(curesCriteriaUpdate);
                x.setCertificationBody(new CertificationBody(certificationBodyDAO.getById(acbId)));
                x.setCountOriginalUpgradedToCures(calculateUpgradeCriterionByAcb(acbId, listingsAttestingToCriterion, curesCriteriaUpdate.getOriginalCriterion()));
                x.setCountCuresWhenCreated(calculateNewCriterionByAcb(acbId, listingsAttestingToCriterion, curesCriteriaUpdate.getOriginalCriterion()));

                curesCriteriaUpdateByAcbs.add(x);
            }
        }

        curesCriteriaUpdateByAcbs.stream()
                .forEach(item -> LOGGER.info(String.format("%s -- %s -- %s -- %s", item.getCertificationBody().getName(), item.getCuresCriteriaUpdate().getOriginalCriterion().getNumber(), item.getCountOriginalUpgradedToCures(), item.getCountCuresWhenCreated())));
        return curesCriteriaUpdateByAcbs;
    }

    private Long calculateUpgradeCriterionByAcb(Long acbId, List<CertifiedProductDetailsDTO> certifiedProductDetails, CertificationCriterion criterion) {
        return certifiedProductDetails.stream()
                .filter(listing -> listing.getCertificationBodyId().equals(acbId))
                .filter(listing -> activityStatisticsHelper.didListingRemoveAttestationToCriterionDuringTimeInterval(listing.getId(), criterion, curesEffectiveDate, new Date()))
                .collect(Collectors.counting());
    }

    private Long calculateNewCriterionByAcb(Long acbId, List<CertifiedProductDetailsDTO> certifiedProductDetails, CertificationCriterion criterion) {
        return certifiedProductDetails.stream()
                .filter(listing -> listing.getCertificationBodyId().equals(acbId))
                .filter(listing -> !activityStatisticsHelper.didListingRemoveAttestationToCriterionDuringTimeInterval(listing.getId(), criterion, curesEffectiveDate, new Date()))
                .collect(Collectors.counting());
    }

    private List<Long> getListOfAcbs(List<CertifiedProductDetailsDTO> certifiedProductDetails) {
        return StreamEx.of(certifiedProductDetails).map(det -> det.getCertificationBodyId()).distinct().toList();
    }

    class CuresCriteriaUpdate {
        private CertificationCriterion originalCriterion;
        private CertificationCriterion curesCriterion;

         CuresCriteriaUpdate(CertificationCriterion originalCriterion, CertificationCriterion curesCriterion) {
            this.originalCriterion = originalCriterion;
            this.curesCriterion = curesCriterion;
        }

        public CertificationCriterion getOriginalCriterion() {
            return originalCriterion;
        }

        public void setOriginalCriterion(CertificationCriterion originalCriterion) {
            this.originalCriterion = originalCriterion;
        }

        public CertificationCriterion getCuresCriterion() {
            return curesCriterion;
        }

        public void setCuresCriterion(CertificationCriterion curesCriterion) {
            this.curesCriterion = curesCriterion;
        }
    }

    class CuresCriteriaUpdateByAcb {
        private CuresCriteriaUpdate curesCriteriaUpdate;
        private CertificationBody certificationBody;
        private Long countOriginalUpgradedToCures = 0L;
        private Long countCuresWhenCreated = 0L;

        public CuresCriteriaUpdate getCuresCriteriaUpdate() {
            return curesCriteriaUpdate;
        }

        public void setCuresCriteriaUpdate(CuresCriteriaUpdate curesCriteriaUpdate) {
            this.curesCriteriaUpdate = curesCriteriaUpdate;
        }

        public CertificationBody getCertificationBody() {
            return certificationBody;
        }

        public void setCertificationBody(CertificationBody certificationBody) {
            this.certificationBody = certificationBody;
        }

        public Long getCountOriginalUpgradedToCures() {
            return countOriginalUpgradedToCures;
        }

        public void setCountOriginalUpgradedToCures(Long countOriginalUpgradedToCures) {
            this.countOriginalUpgradedToCures = countOriginalUpgradedToCures;
        }

        public Long getCountCuresWhenCreated() {
            return countCuresWhenCreated;
        }

        public void setCountCuresWhenCreated(Long countCuresWhenCreated) {
            this.countCuresWhenCreated = countCuresWhenCreated;
        }
    }

}
