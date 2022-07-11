package gov.healthit.chpl.sharedstore.listing;

import java.lang.reflect.Method;
import java.util.List;

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
import lombok.extern.log4j.Log4j2;

@Component
@Aspect
@Log4j2
public class ListingStoreRemoveAspect {
    private ExpressionEvaluator<Long> evaluator = new ExpressionEvaluator<>();
    private SharedListingStoreProvider sharedListingStoreProvider;
    private CertifiedProductDAO certifiedProductDAO;

    @Autowired
    public ListingStoreRemoveAspect(SharedListingStoreProvider sharedListingStoreProvider, CertifiedProductDAO certifiedProductDAO) {
        this.sharedListingStoreProvider = sharedListingStoreProvider;
        this.certifiedProductDAO = certifiedProductDAO;
    }

    @AfterReturning("execution(* *.*(..)) && @annotation(listingStoreRemove)")
    @Transactional
    public void listingStoreRemove(JoinPoint joinPoint, ListingStoreRemove listingStoreRemove) {
      Long id = getValue(joinPoint, listingStoreRemove.id());
      removeListingsFromStore(listingStoreRemove.removeBy(), id);
    }

    private void removeListingsFromStore(RemoveBy removeBy, Long id) {
        switch (removeBy) {
            case DEVELOPER_ID:
                removeListingsFromStoreByDeveloperId(id);
                break;
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
    }

    private void removeListingsFromStoreByDeveloperId(Long developerId) {
        sharedListingStoreProvider.remove(
                getCertifiedProductsForDeveloper(developerId).stream()
                        .map(details -> details.getId())
                        .toList());
    }

    private void removeListingsFromStoreByAcbId(Long acbId) {
        getCertifiedProductsForAcb(acbId).stream()
            .forEach(details -> removeListingFromStoreByListingId(details.getId()));
    }

    private void removeListingsFromStoreByProductId(Long productId) {
        getCertifiedProductsForProduct(productId).stream()
            .forEach(details -> removeListingFromStoreByListingId(details.getId()));
    }

    private void removeListingsFromStoreByVersionId(Long versionId) {
        getCertifiedProductsForVersion(versionId).stream()
            .forEach(details -> removeListingFromStoreByListingId(details.getId()));
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
