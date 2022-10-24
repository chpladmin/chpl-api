package gov.healthit.chpl.manager.rules.product;

import java.time.LocalDate;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import gov.healthit.chpl.domain.ProductOwner;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class ProductOwnerHistoryValidation extends ValidationRule<ProductValidationContext> {

    @Override
    public boolean isValid(ProductValidationContext context) {
        if (!CollectionUtils.isEmpty(context.getProduct().getOwnerHistory())) {
            long numDaysWithMultipleHistoryEntries = context.getProduct().getOwnerHistory().stream()
                .filter(statusHistoryEntry -> doMutlipleHistoryEntriesExistForDay(context.getProduct().getOwnerHistory(),
                        statusHistoryEntry.getTransferDay()))
                .count();
            if (numDaysWithMultipleHistoryEntries > 0) {
                getMessages().add(context.getErrorMessageUtil().getMessage("product.ownerStatusHistory.notSameDay"));
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
}
