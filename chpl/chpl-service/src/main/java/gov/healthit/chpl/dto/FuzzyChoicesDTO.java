package gov.healthit.chpl.dto;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.entity.FuzzyType;
import gov.healthit.chpl.entity.FuzzyChoicesEntity;

public class FuzzyChoicesDTO {
    private Long id;
    private FuzzyType fuzzyType;
    private List<String> choices;

    public FuzzyChoicesDTO () {
    }
    public FuzzyChoicesDTO(FuzzyChoicesEntity entity) throws JsonParseException, JsonMappingException, IOException{
    	this.id = entity.getId();
    	this.fuzzyType = entity.getFuzzyType();
    	List<String> choiceList = new ObjectMapper().readValue(entity.getChoices(), List.class);
    	this.choices = choiceList;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public FuzzyType getFuzzyType() {
        return fuzzyType;
    }
    public void setFuzzyType(FuzzyType fuzzyType) {
        this.fuzzyType = fuzzyType;
    }
    public List<String> getChoices() {
        return choices;
    }
    public void setChoices(List<String> choices) {
        this.choices = choices;
    }
}
