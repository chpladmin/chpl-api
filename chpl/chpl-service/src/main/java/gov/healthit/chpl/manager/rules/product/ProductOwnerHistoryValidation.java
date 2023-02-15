package gov.healthit.chpl.manager.rules.product;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductOwner;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class ProductOwnerHistoryValidation extends ValidationRule<ProductValidationContext> {
    private ProductDAO productDao;

    public ProductOwnerHistoryValidation(ProductDAO productDao) {
        this.productDao = productDao;
    }

    @Override
    public boolean isValid(ProductValidationContext context) {
        if (!CollectionUtils.isEmpty(context.getProduct().getOwnerHistory())) {
            long numDaysWithMultipleHistoryEntries = context.getProduct().getOwnerHistory().stream()
                .filter(statusHistoryEntry -> doMutlipleHistoryEntriesExistForDay(context.getProduct().getOwnerHistory(),
                        statusHistoryEntry.getTransferDay()))
                .count();
            if (numDaysWithMultipleHistoryEntries > 0) {
                getMessages().add(context.getErrorMessageUtil().getMessage("product.ownerHistory.notSameDay"));
                return false;
            }

            if (ownerHistoryHasSameOwnerTwiceInARow(context.getProduct().getOwnerHistory())) {
                getMessages().add(context.getErrorMessageUtil().getMessage("product.ownerHistory.sameOwner"));
                return false;
            }

            if (mostRecentPastOwnerHasNoOtherProducts(context.getProduct().getOwnerHistory(), context.getProduct())) {
                ProductOwner mostRecentPastOwner = getMostRecentPastOwner(context.getProduct().getOwnerHistory());
                getMessages().add(context.getErrorMessageUtil().getMessage(
                        "product.ownerHistory.cannotTransferDevelopersOnlyProduct",
                        mostRecentPastOwner.getDeveloper().getName()));
                return false;
            }
        }
        return true;
    }

    private boolean doMutlipleHistoryEntriesExistForDay(List<ProductOwner> ownerHistory, LocalDate day) {
        return ownerHistory.stream()
            .filter(historyEntry -> historyEntry.getTransferDay().equals(day))
            .count() > 1;
    }

    private boolean ownerHistoryHasSameOwnerTwiceInARow(List<ProductOwner> ownerHistory) {
        sortOwnerHistoryByOwnerId(ownerHistory);
        Iterator<ProductOwner> ownershipIter = ownerHistory.iterator();
        while (ownershipIter.hasNext()) {
            ProductOwner currOwnerHistoryEntry = ownershipIter.next();
            if (ownershipIter.hasNext()) {
                ProductOwner nextOwnerHistoryEntry = ownershipIter.next();
                if (currOwnerHistoryEntry.getDeveloper().getId().equals(nextOwnerHistoryEntry.getDeveloper().getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean mostRecentPastOwnerHasNoOtherProducts(List<ProductOwner> ownerHistory, Product product) {
        ProductOwner mostRecentPastOwner = getMostRecentPastOwner(ownerHistory);
        return developerHasNoOtherProducts(mostRecentPastOwner.getDeveloper().getId(), product.getId());
    }

    private boolean developerHasNoOtherProducts(Long developerId, Long productId) {
        List<Product> developerProducts = productDao.getByDeveloper(developerId);
        if (CollectionUtils.isEmpty(developerProducts)) {
            return true;
        } else if (developerProducts.size() == 1) {
            return developerProducts.get(0).getId().equals(productId);
        }
        return false;
    }

    private ProductOwner getMostRecentPastOwner(List<ProductOwner> ownerHistory) {
        sortOwnerHistoryByTransferDay(ownerHistory);
        return ownerHistory.get(0);
    }

    private void sortOwnerHistoryByOwnerId(List<ProductOwner> ownerHistory) {
        ownerHistory.sort(new Comparator<ProductOwner>() {

            @Override
            public int compare(ProductOwner o1, ProductOwner o2) {
                return o1.getDeveloper().getId().compareTo(o2.getDeveloper().getId());
            }
        });
    }

    private void sortOwnerHistoryByTransferDay(List<ProductOwner> ownerHistory) {
        ownerHistory.sort(new Comparator<ProductOwner>() {

            @Override
            public int compare(ProductOwner o1, ProductOwner o2) {
                return o1.getTransferDay().compareTo(o2.getTransferDay()) * -1;
            }
        });
    }
}
