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
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.PracticeTypeDAO;
import gov.healthit.chpl.dao.ProductClassificationTypeDAO;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.DescriptiveModel;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.PracticeType;
import gov.healthit.chpl.dto.ProductClassificationTypeDTO;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service("CacheableDimensionalDataManager")
public class CacheableDimensionalDataManager {
    private ProductClassificationTypeDAO productClassificationTypeDao;
    private CertificationEditionDAO certificationEditionDao;
    private CertificationStatusDAO certificationStatusDao;
    private PracticeTypeDAO practiceTypeDao;
    private CQMCriterionDAO cqmCriterionDao;
    private ProductDAO productDao;
    private DeveloperDAO developerDao;

    @Autowired
    public CacheableDimensionalDataManager(
            ProductClassificationTypeDAO productClassificationTypeDao,
            CertificationEditionDAO certificationEditionDao,
            CertificationStatusDAO certificationStatusDao,
            PracticeTypeDAO practiceTypeDao,
            CQMCriterionDAO cqmCriterionDao,
            ProductDAO productDao,
            DeveloperDAO developerDao) {
        this.productClassificationTypeDao = productClassificationTypeDao;
        this.certificationEditionDao = certificationEditionDao;
        this.certificationStatusDao = certificationStatusDao;
        this.practiceTypeDao = practiceTypeDao;
        this.cqmCriterionDao = cqmCriterionDao;
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
        List<CertificationEdition> certificationEditions = certificationEditionDao.findAll();
        Set<KeyValueModel> editionNames = new HashSet<KeyValueModel>();

        for (CertificationEdition edition : certificationEditions) {

            if (simple) {
                if (edition.getName().equals("2011")) {
                    continue;
                }
            }
            editionNames.add(new KeyValueModel(edition.getId(), edition.getName()));
        }

        return editionNames;
    }

    @Transactional
    @Cacheable(value = CacheNames.CERTIFICATION_STATUSES)
    public Set<KeyValueModel> getCertificationStatuses() {
        LOGGER.debug("Getting all certification statuses from the database (not cached).");
        List<CertificationStatus> certificationStatuses = certificationStatusDao.findAll();
        Set<KeyValueModel> results = new HashSet<KeyValueModel>();

        for (CertificationStatus cs : certificationStatuses) {
            results.add(new KeyValueModel(cs.getId(), cs.getName()));
        }

        return results;
    }

    @Transactional
    @Cacheable(value = CacheNames.PRACTICE_TYPE_NAMES)
    public Set<KeyValueModel> getPracticeTypeNames() {
        LOGGER.debug("Getting all practice type names from the database (not cached).");
        List<PracticeType> practiceTypes = practiceTypeDao.findAll();
        Set<KeyValueModel> practiceTypeNames = new HashSet<KeyValueModel>();

        for (PracticeType practiceType : practiceTypes) {
            practiceTypeNames.add(new KeyValueModel(practiceType.getId(), practiceType.getName()));
        }

        return practiceTypeNames;
    }

    @Transactional
    @Cacheable(value = CacheNames.CQM_CRITERION_NUMBERS)
    public Set<DescriptiveModel> getCQMCriterionNumbers(final Boolean simple) {
        LOGGER.debug("Getting all CQM numbers from the database (not cached).");

        List<CQMCriterion> cqms = this.cqmCriterionDao.findAll();
        Set<DescriptiveModel> criterionNames = new HashSet<DescriptiveModel>();

        for (CQMCriterion cqm : cqms) {
            String idNumber;

            if (simple) {
                if (cqm.getCmsId() != null) {
                    idNumber = cqm.getCmsId();
                } else {
                    continue;
                }
            } else {
                if (cqm.getCmsId() != null) {
                    idNumber = cqm.getCmsId();
                } else {
                    idNumber = cqm.getNqfNumber();
                }
            }

            criterionNames.add(new DescriptiveModel(cqm.getCriterionId(), idNumber, cqm.getTitle()));
        }
        return criterionNames;
    }
}
