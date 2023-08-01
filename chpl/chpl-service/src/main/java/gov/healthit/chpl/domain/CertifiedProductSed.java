package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Safety Enhanced Design data that is relied upon by this Health IT Module to
 * demonstrate its compliance with a certification criterion or criteria. The
 * SED data includes both user-centered design and test tasks.
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
public class CertifiedProductSed implements Serializable {
    private static final long serialVersionUID = -4131156681875211447L;

    /**
     * The user-centered design (UCD) process applied for the corresponding
     * certification criteria. This variable is applicable for 2014 and 2015
     * Edition.
     */
    @Schema(description = "The user-centered design (UCD) process applied for the corresponding "
            + "certification criteria. This variable is applicable for 2014 and 2015 Edition.")
    @XmlElementWrapper(name = "ucdProcesses", nillable = true, required = false)
    @XmlElement(name = "ucdProcess")
    @Builder.Default
    private List<CertifiedProductUcdProcess> ucdProcesses = new ArrayList<CertifiedProductUcdProcess>();

    /**
     * Tasks used for SED testing
     */
    @Schema(description = "Tasks used for SED testing")
    @XmlElementWrapper(name = "testTasks", nillable = true, required = false)
    @XmlElement(name = "testTask")
    @Builder.Default
    private List<TestTask> testTasks = new ArrayList<TestTask>();

    @JsonIgnore
    @XmlTransient
    @Builder.Default
    private Set<String> unusedTestTaskUniqueIds = new LinkedHashSet<String>();

    @JsonIgnore
    @XmlTransient
    @Builder.Default
    private Set<String> unusedTestParticipantUniqueIds = new LinkedHashSet<String>();

    public CertifiedProductSed() {
        super();
        this.ucdProcesses = new ArrayList<CertifiedProductUcdProcess>();
        this.testTasks = new ArrayList<TestTask>();
    }

    public List<CertifiedProductUcdProcess> getUcdProcesses() {
        return ucdProcesses;
    }

    public void setUcdProcesses(List<CertifiedProductUcdProcess> ucdProcesses) {
        this.ucdProcesses = ucdProcesses;
    }

    public List<TestTask> getTestTasks() {
        return testTasks;
    }

    public void setTestTasks(List<TestTask> testTasks) {
        this.testTasks = testTasks;
    }

    public Set<String> getUnusedTestTaskUniqueIds() {
        return unusedTestTaskUniqueIds;
    }

    public void setUnusedTestTaskUniqueIds(Set<String> unusedTestTaskUniqueIds) {
        this.unusedTestTaskUniqueIds = unusedTestTaskUniqueIds;
    }

    public Set<String> getUnusedTestParticipantUniqueIds() {
        return unusedTestParticipantUniqueIds;
    }

    public void setUnusedTestParticipantUniqueIds(Set<String> unusedTestParticipantUniqueIds) {
        this.unusedTestParticipantUniqueIds = unusedTestParticipantUniqueIds;
    }
}
