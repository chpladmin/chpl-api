package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
@AllArgsConstructor
public class CertifiedProductSed implements Serializable {
    private static final long serialVersionUID = -4131156681875211447L;

    @Schema(description = "The user-centered design (UCD) process applied for the corresponding "
            + "certification criteria. This variable is applicable for 2014 and 2015 Edition.")
    @Builder.Default
    private List<CertifiedProductUcdProcess> ucdProcesses = new ArrayList<CertifiedProductUcdProcess>();

    @Schema(description = "Tasks used for SED testing")
    @Builder.Default
    private List<TestTask> testTasks = new ArrayList<TestTask>();

    @JsonIgnore
    @Builder.Default
    private Set<String> unusedTestTaskUniqueIds = new LinkedHashSet<String>();

    @JsonIgnore
    @Builder.Default
    private Set<String> unusedTestParticipantUniqueIds = new LinkedHashSet<String>();

    public CertifiedProductSed() {
        super();
        this.ucdProcesses = new ArrayList<CertifiedProductUcdProcess>();
        this.testTasks = new ArrayList<TestTask>();
    }
}
