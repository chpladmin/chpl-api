package gov.healthit.chpl.manager.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.domain.KeyValueModelStatuses;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.manager.PrecacheableDimensionalDataManager;

@Service("precacheableDimensionalDataManager")
public class PrecacheableDimensionalDataManagerImpl implements PrecacheableDimensionalDataManager {

    @Autowired
    private ProductDAO productDAO;

    @Autowired
    private DeveloperDAO developerDAO;

    @Transactional
    @Override
    @Cacheable(CacheNames.PRODUCT_NAMES)
    public Set<KeyValueModelStatuses> getProductNamesCached() {
        return getProductNames();
    }

    @Transactional
    @Override
    public Set<KeyValueModelStatuses> getProductNames() {
        List<ProductDTO> productDTOs = this.productDAO.findAll();
        Set<KeyValueModelStatuses> productNames = new HashSet<KeyValueModelStatuses>();
        for (ProductDTO dto : productDTOs) {
            productNames.add(new KeyValueModelStatuses(dto.getId(), dto.getName(), dto.getStatuses()));
        }
        return productNames;
    }

    @Transactional
    @Override
    @Cacheable(CacheNames.DEVELOPER_NAMES)
    public Set<KeyValueModelStatuses> getDeveloperNamesCached() {
        return getDeveloperNames();
    }

    @Transactional
    @Override
    public Set<KeyValueModelStatuses> getDeveloperNames() {
        List<DeveloperDTO> developerDTOs = this.developerDAO.findAll();
        Set<KeyValueModelStatuses> developerNames = new HashSet<KeyValueModelStatuses>();
        for (DeveloperDTO dto : developerDTOs) {
            developerNames.add(new KeyValueModelStatuses(dto.getId(), dto.getName(), dto.getStatuses()));
        }
        return developerNames;
    }
}
