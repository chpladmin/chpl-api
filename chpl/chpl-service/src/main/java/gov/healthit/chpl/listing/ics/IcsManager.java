package gov.healthit.chpl.listing.ics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.util.CertifiedProductUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class IcsManager {

    private CertifiedProductManager cpManager;
    private CertifiedProductUtil cpUtil;
    private IcsDao icsDao;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public IcsManager(CertifiedProductManager cpManager, CertifiedProductUtil cpUtil,
            IcsDao icsDao, ErrorMessageUtil msgUtil) {
        this.cpManager = cpManager;
        this.cpUtil = cpUtil;
        this.icsDao = icsDao;
        this.msgUtil = msgUtil;
    }

    @Transactional
    public List<ListingIcsNode> getIcsFamilyTree(String chplProductNumber) throws EntityRetrievalException {
        CertifiedProduct cp = cpUtil.getListing(chplProductNumber);
        if (cp == null) {
            throw new EntityRetrievalException(msgUtil.getMessage("listing.notFound"));
        }
        return getIcsFamilyTree(cp.getId());
    }

    @Transactional
    public List<ListingIcsNode> getIcsFamilyTree(Long listingId) throws EntityRetrievalException {
        cpManager.getById(listingId); // sends back 404 if bad id

        List<ListingIcsNode> familyTree = new ArrayList<ListingIcsNode>();
        Map<Long, Boolean> processingQueue = new HashMap<Long, Boolean>();

        processingQueue.put(listingId, false);

        while (processingQueue.containsValue(false)) {
            List<Long> idsToAddToProcessingQueue = new ArrayList<Long>();
            for (Entry<Long, Boolean> listingForProcessing : processingQueue.entrySet()) {
                idsToAddToProcessingQueue.addAll(
                        processListingFamilyEntry(listingForProcessing, familyTree, processingQueue));
            }
            if (!CollectionUtils.isEmpty(idsToAddToProcessingQueue)) {
                idsToAddToProcessingQueue.stream()
                    .filter(idToAdd -> !processingQueue.containsKey(idToAdd))
                    .forEach(idToAdd -> processingQueue.put(idToAdd, false));
            }
        }
        return familyTree;
    }

    private List<Long> processListingFamilyEntry(Entry<Long, Boolean> listingForProcessing, List<ListingIcsNode> familyTree,
            Map<Long, Boolean> processingQueue) {
        List<Long> idsToAddToProcessingQueue = new ArrayList<Long>();
        Boolean isProcessed = listingForProcessing.getValue();
        Long listingIdForProcessing = listingForProcessing.getKey();
        if (!isProcessed) {
            ListingIcsNode node = icsDao.getIcsFamilyTree(listingIdForProcessing);

            familyTree.add(node);

            node.getChildren().stream()
                .forEach(child -> idsToAddToProcessingQueue.add(child.getId()));
            node.getParents().stream()
                .forEach(parent -> idsToAddToProcessingQueue.add(parent.getId()));
            processingQueue.put(listingIdForProcessing, true);
        }

        return idsToAddToProcessingQueue;
    }
}
