package gov.healthit.chpl.domain.surveillance;

import java.io.Serializable;
import java.time.LocalDate;

import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.dto.surveillance.SurveillanceBasicDTO;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SurveillanceBasic implements Serializable {
    private static final long serialVersionUID = 3750079664886758825L;

    private Long id;
    private String friendlyId;
    private Long certifiedProductId;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate startDay;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate endDay;
    private Long surveillanceTypeId;
    private SurveillanceType surveillanceType;
    private Integer numRandomizedSites;
    private Integer numOpenNonconformities;
    private Integer numClosedNonconformities;
    private String chplProductNumber;

    public SurveillanceBasic(SurveillanceBasicDTO dto) {
        BeanUtils.copyProperties(dto, this);
        this.startDay = dto.getStartDate();
        this.endDay = dto.getEndDate();
        this.surveillanceType = new SurveillanceType(dto.getSurveillanceType());
    }
}
