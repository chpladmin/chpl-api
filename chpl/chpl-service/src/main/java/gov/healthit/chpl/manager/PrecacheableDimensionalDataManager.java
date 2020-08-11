package gov.healthit.chpl.manager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.CQMCriterionDAO;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.PracticeTypeDAO;
import gov.healthit.chpl.dao.ProductClassificationTypeDAO;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CriteriaSpecificDescriptiveModel;
import gov.healthit.chpl.domain.DescriptiveModel;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.KeyValueModelStatuses;
import gov.healthit.chpl.dto.CQMCriterionDTO;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.dto.CertificationStatusDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.PracticeTypeDTO;
import gov.healthit.chpl.dto.ProductClassificationTypeDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("precacheableDimensionalDataManager")
public class PrecacheableDimensionalDataManager {
    private ProductClassificationTypeDAO productClassificationTypeDao;
    private CertificationEditionDAO certificationEditionDao;
    private CertificationStatusDAO certificationStatusDao;
    private PracticeTypeDAO practiceTypeDao;
    private CQMCriterionDAO cqmCriterionDao;
    private CertificationCriterionDAO certificationCriterionDao;
    private ProductDAO productDao;
    private DeveloperDAO developerDao;

    @Autowired
    public PrecacheableDimensionalDataManager(
            ProductClassificationTypeDAO productClassificationTypeDao,
            CertificationEditionDAO certificationEditionDao,
            CertificationStatusDAO certificationStatusDao,
            PracticeTypeDAO practiceTypeDao,
            CQMCriterionDAO cqmCriterionDao,
            CertificationCriterionDAO certificationCriterionDao,
            ProductDAO productDao,
            DeveloperDAO developerDao) {
        this.productClassificationTypeDao = productClassificationTypeDao;
        this.certificationEditionDao = certificationEditionDao;
        this.certificationStatusDao = certificationStatusDao;
        this.practiceTypeDao = practiceTypeDao;
        this.cqmCriterionDao = cqmCriterionDao;
        this.certificationCriterionDao = certificationCriterionDao;
        this.productDao = productDao;
        this.developerDao = developerDao;
    }

    @Transactional
    @Cacheable(value = CacheNames.CLASSIFICATION_NAMES)
    public Set<KeyValueModel> getClassificationNames() {
        LOGGER.debug("Getting all classification names from the database (not cached).");
        List<ProductClassificationTypeDTO> classificationTypes = productClassificationTypeDao.findAll();
        Set<KeyValueModel> classificationTypeNames = new HashSet<KeyValueModel>();

        for (ProductClassificationTypeDTO dto : classificationTypes) {
            classificationTypeNames.add(new KeyValueModel(dto.getId(), dto.getName()));
        }

        return classificationTypeNames;
    }

    @Transactional
    @Cacheable(value = CacheNames.EDITION_NAMES)
    public Set<KeyValueModel> getEditionNames(final Boolean simple) {
        LOGGER.debug("Getting all edition names from the database (not cached).");
        List<CertificationEditionDTO> certificationEditions = certificationEditionDao.findAll();
        Set<KeyValueModel> editionNames = new HashSet<KeyValueModel>();

        for (CertificationEditionDTO dto : certificationEditions) {

            if (simple) {
                if (dto.getYear().equals("2011")) {
                    continue;
                }
            }
            editionNames.add(new KeyValueModel(dto.getId(), dto.getYear()));
        }

        return editionNames;
    }

    @Transactional
    @Cacheable(value = CacheNames.CERTIFICATION_STATUSES)
    public Set<KeyValueModel> getCertificationStatuses() {
        LOGGER.debug("Getting all certification statuses from the database (not cached).");
        List<CertificationStatusDTO> certificationStatuses = certificationStatusDao.findAll();
        Set<KeyValueModel> results = new HashSet<KeyValueModel>();

        for (CertificationStatusDTO dto : certificationStatuses) {
            results.add(new KeyValueModel(dto.getId(), dto.getStatus()));
        }

        return results;
    }

    @Transactional
    @Cacheable(value = CacheNames.PRACTICE_TYPE_NAMES)
    public Set<KeyValueModel> getPracticeTypeNames() {
        LOGGER.debug("Getting all practice type names from the database (not cached).");
        List<PracticeTypeDTO> practiceTypeDTOs = practiceTypeDao.findAll();
        Set<KeyValueModel> practiceTypeNames = new HashSet<KeyValueModel>();

        for (PracticeTypeDTO dto : practiceTypeDTOs) {
            practiceTypeNames.add(new KeyValueModel(dto.getId(), dto.getName()));
        }

        return practiceTypeNames;
    }

    @Transactional
    @Cacheable(value = CacheNames.CQM_CRITERION_NUMBERS)
    public Set<DescriptiveModel> getCQMCriterionNumbers(final Boolean simple) {
        LOGGER.debug("Getting all CQM numbers from the database (not cached).");

        List<CQMCriterionDTO> dtos = this.cqmCriterionDao.findAll();
        Set<DescriptiveModel> criterionNames = new HashSet<DescriptiveModel>();

        for (CQMCriterionDTO dto : dtos) {

            String idNumber;

            if (simple) {
                if (dto.getCmsId() != null) {
                    idNumber = dto.getCmsId();
                } else {
                    continue;
                }
            } else {
                if (dto.getCmsId() != null) {
                    idNumber = dto.getCmsId();
                } else {
                    idNumber = dto.getNqfNumber();
                }
            }

            criterionNames.add(new DescriptiveModel(dto.getId(), idNumber, dto.getTitle()));
        }
        return criterionNames;
    }

    @Transactional
    @Cacheable(value = CacheNames.CERTIFICATION_CRITERION_NUMBERS)
    public Set<CriteriaSpecificDescriptiveModel> getCertificationCriterionNumbers() throws EntityRetrievalException {
        LOGGER.debug("Getting all criterion numbers from the database (not cached).");

        List<CertificationCriterionDTO> dtos = this.certificationCriterionDao.findAll();
        Set<CriteriaSpecificDescriptiveModel> criterionNames = new HashSet<CriteriaSpecificDescriptiveModel>();

        for (CertificationCriterionDTO dto : dtos) {
            criterionNames.add(new CriteriaSpecificDescriptiveModel(dto.getId(), dto.getNumber(), dto.getTitle(),
                    new CertificationCriterion(dto)));
        }

        return criterionNames;

    }

    @Transactional
    @Cacheable(CacheNames.PRODUCT_NAMES)
    public Set<KeyValueModelStatuses> getProductsCached() {
        return getProducts();
    }

    @Transactional
    public Set<KeyValueModelStatuses> getProducts() {
        List<ProductDTO> productDTOs = this.productDao.findAll();
        Set<KeyValueModelStatuses> productNames = new HashSet<KeyValueModelStatuses>();
        for (ProductDTO dto : productDTOs) {
            productNames.add(new KeyValueModelStatuses(dto.getId(), dto.getName(), dto.getStatuses()));
        }
        return productNames;
    }

    @Transactional
    @Cacheable(CacheNames.DEVELOPER_NAMES)
    public Set<KeyValueModelStatuses> getDevelopersCached() {
        return getDevelopers();
    }

    @Transactional
    public Set<KeyValueModelStatuses> getDevelopers() {
        List<DeveloperDTO> developerDTOs = this.developerDao.findAll();
        Set<KeyValueModelStatuses> developerNames = new HashSet<KeyValueModelStatuses>();
        for (DeveloperDTO dto : developerDTOs) {
            developerNames.add(new KeyValueModelStatuses(dto.getId(), dto.getName(), dto.getStatuses()));
        }
        return developerNames;
    }
}
