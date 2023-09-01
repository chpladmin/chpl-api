package gov.healthit.chpl.scheduler.job.chartdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CriterionProductStatisticsDAO;
import gov.healthit.chpl.dto.CriterionProductStatisticsDTO;
import gov.healthit.chpl.entity.statistics.CriterionProductStatisticsEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.search.domain.ListingSearchResult.CertificationCriterionSearchResult;

/**
 * Populates the criterion_product_statistics table with summarized count
 * information.
 *
 * @author alarned
 *
 */
public class CriterionProductStatisticsCalculator {
    private static final Logger LOGGER = LogManager.getLogger("chartDataCreatorJobLogger");

    @Autowired
    private CertificationCriterionDAO certificationCriterionDAO;
    @Autowired
    private CriterionProductStatisticsDAO criterionProductStatisticsDAO;

    public CriterionProductStatisticsCalculator() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    /**
     * criterionMap maps the certification criterion id to the count of unique
     * Products that certify to that criterion.
     *
     * uniqueProductSet contains strings of the form
     * "<CriterionId>-<DeveloperName>-<ProductName>" iff that combination of
     * criterion and product have already been counted in the criterion map
     *
     * @param listings
     *            listings to parse
     * @return map of criteria to counts
     */
    public Map<Long, Long> getCounts(List<ListingSearchResult> listings) {
        Map<Long, Long> criterionMap = new HashMap<Long, Long>();
        HashSet<String> uniqueProductSet = new HashSet<String>();
        for (ListingSearchResult listing : listings) {
            if (listing.getCriteriaMet() != null && !listing.getCriteriaMet().isEmpty()) {
                for (CertificationCriterionSearchResult cert : listing.getCriteriaMet()) {
                    String key = cert.getId() + "-" + listing.getDeveloper().getName() + '-' + listing.getProduct().getName();
                    if (!uniqueProductSet.contains(key)) {
                        if (!criterionMap.containsKey(cert.getId())) {
                            criterionMap.put(cert.getId(), 0L);
                        }
                        criterionMap.put(cert.getId(), criterionMap.get(cert.getId()) + 1);
                        uniqueProductSet.add(key);
                    }
                }
            }
        }
        return criterionMap;
    }

    public void logCounts(Map<Long, Long> productCounts) {
        for (Entry<Long, Long> entry : productCounts.entrySet()) {
            LOGGER.info("Certification Criteria count: [" + entry.getKey() + " : " + entry.getValue() + "]");
        }
    }

    public void save(Map<Long, Long> productCounts) throws NumberFormatException, EntityRetrievalException {
        List<CriterionProductStatisticsEntity> entities =
        convertProductCountMapToListOfCriterionProductStatistics(productCounts);
        try {
            deleteExistingCriterionProductStatistics();
        } catch (EntityRetrievalException e) {
            LOGGER.error("Error occured while deleting existing CriterionProductStatistics.", e);
            return;
        }

        for (CriterionProductStatisticsEntity entity : entities) {
            saveCriterionProductStatistic(entity);
        }
    }

    private List<CriterionProductStatisticsEntity> convertProductCountMapToListOfCriterionProductStatistics(
            Map<Long, Long> productCounts) throws NumberFormatException, EntityRetrievalException {
        List<CriterionProductStatisticsEntity> entities = new ArrayList<CriterionProductStatisticsEntity>();
        for (Entry<Long, Long> entry : productCounts.entrySet()) {
            CertificationCriterion criterion = certificationCriterionDAO.getById(entry.getKey());
            if (!criterion.getRemoved()) {
                CriterionProductStatisticsEntity entity = new CriterionProductStatisticsEntity();
                entity.setProductCount(entry.getValue());
                entity.setCertificationCriterionId(criterion.getId());
                entities.add(entity);
            }
        }
        return entities;
    }

    private void deleteExistingCriterionProductStatistics() throws EntityRetrievalException {
        List<CriterionProductStatisticsDTO> dtos = criterionProductStatisticsDAO.findAll();
        for (CriterionProductStatisticsDTO dto : dtos) {
            criterionProductStatisticsDAO.delete(dto.getId());
            LOGGER.info("Deleted: " + dto.getId());
        }
    }

    private void saveCriterionProductStatistic(final CriterionProductStatisticsEntity entity) {
        try {
            CriterionProductStatisticsDTO dto = new CriterionProductStatisticsDTO(entity);
            criterionProductStatisticsDAO.create(dto);
            LOGGER.info("Saved CriterionProductStatisticsDTO [Certification Criteria Id: "
                    + dto.getCertificationCriterionId() + ", Count:" + dto.getProductCount() + "]");
        } catch (EntityCreationException | EntityRetrievalException e) {
            LOGGER.error("Error occured while inserting counts.", e);
            return;
        }
    }
}
