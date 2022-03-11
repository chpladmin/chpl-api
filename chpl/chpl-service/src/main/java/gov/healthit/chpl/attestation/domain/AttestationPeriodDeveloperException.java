package gov.healthit.chpl.attestation.domain;

import java.time.LocalDate;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.attestation.entity.AttestationPeriodDeveloperExceptionEntity;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttestationPeriodDeveloperException {
    private Long id;
    private AttestationPeriod period;
    private Developer developer;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate exceptionEnd;

    public AttestationPeriodDeveloperException(AttestationPeriodDeveloperExceptionEntity entity) {
        this.id = entity.getId();
        this.period = new AttestationPeriod(entity.getPeriod());
        this.developer = new Developer(new DeveloperDTO(entity.getDeveloper()));
        this.exceptionEnd = entity.getExceptionEnd();
    }

}
