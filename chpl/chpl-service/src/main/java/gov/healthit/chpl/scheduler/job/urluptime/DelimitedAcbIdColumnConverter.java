package gov.healthit.chpl.scheduler.job.urluptime;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.IdNamePair;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertificationBodyManager;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Converter
@Component
// @NoArgsConstructor
public class DelimitedAcbIdColumnConverter implements AttributeConverter<List<IdNamePair>, String> {

    @Autowired
    private CertificationBodyManager certificationBodyManager;

    // public DelimitedAcbIdColumnConverter(CertificationBodyManager
    // certificationBodyManager) {
    // this.certificationBodyManager = certificationBodyManager;
    // }
    // public void setCertificationBodyMananger(CertificationBodyManager
    // certificationBodyManager) {
    // DelimitedAcbIdColumnConverter.certificationBodyManager =
    // certificationBodyManager;
    // }

    @Override
    public String convertToDatabaseColumn(List<IdNamePair> attribute) {
        if (CollectionUtils.isEmpty(attribute)) {
            return null;
        }
        var x = attribute.stream()
                .map(acb -> acb.getId().toString())
                .collect(Collectors.joining(","));
        LOGGER.info("Converted ACB IDs to delimited string: " + x);
        return x;
    }

    @Override
    public List<IdNamePair> convertToEntityAttribute(String dbData) {
        LOGGER.info("Converting delimited string to ACB IDs: " + dbData);
        if (StringUtils.isEmpty(dbData)) {
            return List.of();
        }
        return Arrays.asList(dbData.split(",")).stream()
                .map(id -> {
                    CertificationBody acb = getCertificationBodyById(Long.parseLong(id));
                    return IdNamePair.builder()
                            .id(acb.getId())
                            .name(acb.getName())
                            .build();
                })
                .filter(acb -> acb != null)
                .toList();
        // return List.of(IdNamePair.builder().id(1L).name("acb1").build(),
        // IdNamePair.builder().id(2L).name("acb2").build());
    }

    private CertificationBody getCertificationBodyById(Long id) {
        try {
            return certificationBodyManager.getById(id);
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Could not convert the ACB ID to an entity:{} Returning null.", id, ex);
            return null;
        }
    }

}
