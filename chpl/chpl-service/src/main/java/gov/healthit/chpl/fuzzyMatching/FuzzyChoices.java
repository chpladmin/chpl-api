package gov.healthit.chpl.fuzzyMatching;

import java.io.Serializable;
import java.util.List;

@Deprecated
public class FuzzyChoices implements Serializable {
    private static final long serialVersionUID = -7647761708813529969L;
    private Long id;
    private String fuzzyType;
    private List<String> choices;
    private Long lastModifiedUser;

    public FuzzyChoices() {
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getFuzzyType() {
        return fuzzyType;
    }

    public void setFuzzyType(final String fuzzyType) {
        this.fuzzyType = fuzzyType;
    }

    public List<String> getChoices() {
        return choices;
    }

    public void setChoices(final List<String> choices) {
        this.choices = choices;
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

}
