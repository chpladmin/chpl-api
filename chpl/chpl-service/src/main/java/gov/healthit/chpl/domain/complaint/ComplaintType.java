package gov.healthit.chpl.domain.complaint;

import org.springframework.beans.BeanUtils;

import gov.healthit.chpl.dto.ComplaintTypeDTO;

public class ComplaintType {
    private Long id;
    private String name;
    private String description;

    public ComplaintType() {

    }

    public ComplaintType(ComplaintTypeDTO dto) {
        BeanUtils.copyProperties(dto, this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
