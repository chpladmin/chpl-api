package gov.healthit.chpl.scheduler.job.subscriptions.types.formatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.ProductDAO;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.subscription.domain.SubscriptionObservation;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "subscriptionObservationsNotificationJobLogger")
public class ProductObservationFormatter extends ObservationTypeFormatter {

    private ProductDAO productDao;

    @Autowired
    public ProductObservationFormatter(ProductDAO productDao,
            Environment env) {
        super(env);
        this.productDao = productDao;
    }

    public String getSubscribedItemHeading(SubscriptionObservation observation) {
        Product product = getProduct(observation.getSubscription().getSubscribedObjectId());
        if (product == null) {
            LOGGER.error("Cannot process subscription observation " + observation.getId());
            return "";
        }
        return product.getName();
    }

    public String getSubscribedItemFooter(SubscriptionObservation observation) {
        Product product = getProduct(observation.getSubscription().getSubscribedObjectId());
        if (product == null) {
            LOGGER.error("Cannot process subscription observation " + observation.getId());
            return "";
        }
        String developerUrl = String.format(getUnformattedDeveloperDetailsUrl(), product.getOwner().getId());
        return String.format(getUnformattedSubscribedItemFooter(), "product", developerUrl, product.getOwner().getName());
    }

    private Product getProduct(Long productId) {
        Product product = null;
        try {
            product = productDao.getById(productId);
        } catch (Exception ex) {
            LOGGER.error(" Product with ID " + productId + " not found.", ex);
            return null;
        }
        return product;
    }
}
