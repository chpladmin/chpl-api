package gov.healthit.chpl.dto;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.entity.FuzzyChoicesEntity;
import gov.healthit.chpl.entity.FuzzyType;

//TODO: After OCD-4040 and OCD-4041 we should no longer need this class
public class FuzzyChoicesDTO {
    private Long id;
    private FuzzyType fuzzyType;
    private List<String> choices;
    private Long lastModifiedUser;

    public FuzzyChoicesDTO() {}
    public FuzzyChoicesDTO(final FuzzyChoicesEntity entity)
            throws JsonParseException, JsonMappingException, IOException {
        this.id = entity.getId();
        this.fuzzyType = entity.getFuzzyType();
        List<String> choiceList = new ObjectMapper().readValue(entity.getChoices(), List.class);
        this.choices = choiceList;
        this.lastModifiedUser = entity.getLastModifiedUser();
    }
    public Long getId() {
        return id;
    }
    public void setId(final Long id) {
        this.id = id;
    }
    public FuzzyType getFuzzyType() {
        return fuzzyType;
    }
    public void setFuzzyType(final FuzzyType fuzzyType) {
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
