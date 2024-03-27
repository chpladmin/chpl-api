package gov.healthit.chpl.codeset;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CodeSet implements Serializable {
    private static final long serialVersionUID = 8827071245268602313L;
    public static final String CODE_SET_DATE_FORMAT = "MMM yyyy";

    private Long id;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate requiredDay;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate startDay;

    // Do not include this property if the value is "empty". It will be empty when generating listing details
    // and will be non-empty (this included) when doing CRUD operations on functionality tested
    @JsonInclude(value = Include.NON_EMPTY)
    @Builder.Default
    private List<CertificationCriterion> criteria = new ArrayList<CertificationCriterion>();

    @JsonIgnore
    private String userEnteredName;

    public String getName() {
        String codeSetName = getRequiredDay() != null
                ? getRequiredDay().format(DateTimeFormatter.ofPattern(CODE_SET_DATE_FORMAT)) : null;
        if (codeSetName == null) {
            codeSetName = !StringUtils.isEmpty(getUserEnteredName()) ? getUserEnteredName() : " '?' ";
        }
        return codeSetName;
    }

}
