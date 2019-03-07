package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.List;

public class SplitDeveloperRequest implements Serializable {
    private static final long serialVersionUID = -5814366900559692235L;

    private Developer newDeveloper;
    private List<Product> newProducts;
    private Developer oldDeveloper;
    private List<Product> oldProducts;
    public Developer getNewDeveloper() {
        return newDeveloper;
    }
    public void setNewDeveloper(final Developer newDeveloper) {
        this.newDeveloper = newDeveloper;
    }
    public List<Product> getNewProducts() {
        return newProducts;
    }
    public void setNewProducts(final List<Product> newProducts) {
        this.newProducts = newProducts;
    }
    public Developer getOldDeveloper() {
        return oldDeveloper;
    }
    public void setOldDeveloper(final Developer oldDeveloper) {
        this.oldDeveloper = oldDeveloper;
    }
    public List<Product> getOldProducts() {
        return oldProducts;
    }
    public void setOldProducts(final List<Product> oldProducts) {
        this.oldProducts = oldProducts;
    }
}
