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

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CriterionProductStatisticsDAO;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CriterionProductStatisticsDTO;
import gov.healthit.chpl.entity.CriterionProductStatisticsEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

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
    public Map<String, Long> getCounts(final List<CertifiedProductFlatSearchResult> listings) {
        Map<String, Long> criterionMap = new HashMap<String, Long>();
        HashSet<String> uniqueProductSet = new HashSet<String>();
        for (CertifiedProductFlatSearchResult listing : listings) {
            if (listing.getCriteriaMet() != null && !listing.getCriteriaMet().isEmpty()) {
                for (String certId : listing.getCriteriaMet().split(CertifiedProductFlatSearchResult.CERTS_SPLIT_CHAR)) {
                    String key = certId + "-" + listing.getDeveloper() + '-' + listing.getProduct();
                    if (!uniqueProductSet.contains(key)) {
                        if (!criterionMap.containsKey(certId)) {
                            criterionMap.put(certId, 0L);
                        }
                        criterionMap.put(certId, criterionMap.get(certId) + 1);
                        uniqueProductSet.add(key);
                    }
                }
            }
        }
        return criterionMap;
    }

    public void logCounts(final Map<String, Long> productCounts) {
        for (Entry<String, Long> entry : productCounts.entrySet()) {
            LOGGER.info("Certification Criteria count: [" + entry.getKey() + " : " + entry.getValue() + "]");
        }
    }

    public void save(final Map<String, Long> productCounts) throws NumberFormatException, EntityRetrievalException {
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
            final Map<String, Long> productCounts) throws NumberFormatException, EntityRetrievalException {
        List<CriterionProductStatisticsEntity> entities = new ArrayList<CriterionProductStatisticsEntity>();
        for (Entry<String, Long> entry : productCounts.entrySet()) {
            CertificationCriterionDTO criterion = certificationCriterionDAO.getById(Long.valueOf(entry.getKey()));
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
