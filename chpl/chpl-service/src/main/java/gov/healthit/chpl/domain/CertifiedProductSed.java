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

    @Schema(description = "The user-centered design (UCD) process applied for the corresponding certification criteria.")
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


    //I would prefer that these two "duplicate" fields were JsonIgnored, but during upload and confirmation of a listing
    //they can ONLY be filled in by parsing the file and looking for duplicate IDs.
    //We cannot reliably determine, after the listing is built from the CSV, whether the file had duplicate IDs.
    //This is because we may ignore some of the IDs if they are unused (we only give a warning about unused IDs)
    //or if a test task/participant has all the same identical fields, we would only have one copy of it in the listing
    //and it would not appear to be duplicated in the listing. So we have to save the duplicates separately
    //in order to reliably generate errors about them if the user goes through multiple rounds of editing listing and clicking
    //confirm - we have to make them "sticky" in the JSON so they are passed back in when clicking "Confirm",
    //otherwise we don't have the duplicates in the JSON and allow confirmation even if the listing had these errors.
    @Builder.Default
    private List<String> duplicateTestTaskIds = new ArrayList<String>();

    @Builder.Default
    private List<String> duplicateTestParticipantIds = new ArrayList<String>();

    public CertifiedProductSed() {
        super();
        this.ucdProcesses = new ArrayList<CertifiedProductUcdProcess>();
        this.testTasks = new ArrayList<TestTask>();
    }
}
