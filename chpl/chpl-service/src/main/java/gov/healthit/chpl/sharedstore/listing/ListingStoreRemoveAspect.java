package gov.healthit.chpl.sharedstore.listing;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.expression.EvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.listing.ics.IcsManager;
import gov.healthit.chpl.listing.ics.ListingIcsNode;
import lombok.extern.log4j.Log4j2;

@Component
@Aspect
@Log4j2
public class ListingStoreRemoveAspect {
    private ExpressionEvaluator<Long> evaluator = new ExpressionEvaluator<>();
    private SharedListingStoreProvider sharedListingStoreProvider;
    private IcsManager icsManager;
    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    public ListingStoreRemoveAspect(SharedListingStoreProvider sharedListingStoreProvider, IcsManager icsManager,
            CertifiedProductDAO certifiedProductDAO) {
        this.sharedListingStoreProvider = sharedListingStoreProvider;
        this.icsManager = icsManager;
        this.certifiedProductDAO = certifiedProductDAO;
    }

    @AfterReturning("execution(* *.*(..)) && @annotation(listingStoreRemove)")
    @Transactional
    public void listingStoreRemove(JoinPoint joinPoint, ListingStoreRemove listingStoreRemove) {
        if (listingStoreRemove.removeBy().equals(RemoveBy.ALL)) {
            removeAllListingsFromStore();
        } else {
            Long id = getValue(joinPoint, listingStoreRemove.id());
            removeListingsFromStore(listingStoreRemove.removeBy(), id);
        }
    }

    private void removeAllListingsFromStore() {
        sharedListingStoreProvider.removeAll();
    }

    private void removeListingsFromStore(RemoveBy removeBy, Long id) {
        if (id == null) {
            LOGGER.error("Attempting to remove listing(s) from the shared store by " + removeBy.name()
                + " but the 'id' field passed into the removeListingsFromStore method was null. "
                + "Nothing will be removed from the store.");
        }

        switch (removeBy) {
            case DEVELOPER_ID:
                removeListingsFromStoreByDeveloperId(id);
                break;
//            case DEVELOPERS:
//                removeListingsFromStoreByDevelopers();
//                break;
            case LISTING_ID:
                removeListingFromStoreByListingId(id);
                break;
            case ACB_ID:
                removeListingsFromStoreByAcbId(id);
                break;
            case PRODUCT_ID:
                removeListingsFromStoreByProductId(id);
                break;
            case VERSION_ID:
                removeListingsFromStoreByVersionId(id);
                break;
            default:
        }
    }

    private void removeListingFromStoreByListingId(Long listingId) {
        sharedListingStoreProvider.remove(listingId);
        List<Long> relativeIds = getCertifiedProductRelativeIds(listingId);
        if (!CollectionUtils.isEmpty(relativeIds)) {
            sharedListingStoreProvider.remove(relativeIds);
        }
    }

    private void removeListingsFromStoreByDeveloperId(Long developerId) {
        sharedListingStoreProvider.remove(
                getCertifiedProductsForDeveloper(developerId).stream()
                        .map(details -> details.getId())
                        .toList());
    }

    private void removeListingsFromStoreByAcbId(Long acbId) {
        sharedListingStoreProvider.remove(
                getCertifiedProductsForAcb(acbId).stream()
                        .map(details -> details.getId())
                        .toList());
    }

    private void removeListingsFromStoreByProductId(Long productId) {
        sharedListingStoreProvider.remove(
                getCertifiedProductsForProduct(productId).stream()
                        .map(details -> details.getId())
                        .toList());
    }

    private void removeListingsFromStoreByVersionId(Long versionId) {
        sharedListingStoreProvider.remove(
                getCertifiedProductsForVersion(versionId).stream()
                        .map(details -> details.getId())
                        .toList());
    }

    private List<Long> getCertifiedProductRelativeIds(Long listingId) {
        List<ListingIcsNode> icsRelatives = null;
        try {
            icsRelatives = icsManager.getIcsFamilyTree(listingId);
        } catch (EntityRetrievalException ex) {
            LOGGER.warn("Not deleting any ICS relatives. Listing with ID " + listingId + " was not found.");
        }
        if (!CollectionUtils.isEmpty(icsRelatives)) {
            return icsRelatives.stream()
                    .filter(relative -> !relative.getId().equals(listingId))
                    .map(relative -> relative.getId())
                    .toList();
        }
        return null;
    }

    private List<CertifiedProductDetailsDTO> getCertifiedProductsForDeveloper(Long developerId) {
        return certifiedProductDAO.findByDeveloperId(developerId);
    }

    private List<CertifiedProductDetailsDTO> getCertifiedProductsForAcb(Long acbId) {
        return certifiedProductDAO.getDetailsByAcbIds(List.of(acbId));
    }

    private List<CertifiedProductDetailsDTO> getCertifiedProductsForProduct(Long productId) {
        return certifiedProductDAO.getDetailsByProductId(productId);
    }

    private List<CertifiedProduct> getCertifiedProductsForVersion(Long versionId) {
        return certifiedProductDAO.getDetailsByVersionId(versionId);
    }

    private Long getValue(JoinPoint joinPoint, String condition) {
        return getValue(joinPoint.getTarget(), joinPoint.getArgs(),
                        joinPoint.getTarget().getClass(),
                        ((MethodSignature) joinPoint.getSignature()).getMethod(), condition);
      }

    private Long getValue(Object object, Object[] args, Class clazz, Method method, String condition) {
        if (args == null) {
          return null;
        }
        EvaluationContext evaluationContext = evaluator.createEvaluationContext(object, clazz, method, args);
        AnnotatedElementKey methodKey = new AnnotatedElementKey(method, clazz);
        return evaluator.condition(condition, methodKey, evaluationContext, Long.class);
    }
}
