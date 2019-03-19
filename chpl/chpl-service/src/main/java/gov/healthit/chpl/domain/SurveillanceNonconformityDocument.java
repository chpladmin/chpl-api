package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class SurveillanceNonconformityDocument implements Serializable {
    private static final long serialVersionUID = -7456509117016763596L;

    /**
     * Document internal ID
     */
    @XmlElement(required = true)
    private Long id;

    /**
     * Document file name
     */
    @XmlElement(required = true)
    private String fileName;

    /**
     * Document file type (XML, PDF, etc)
     */
    @XmlElement(required = true)
    private String fileType;

    @XmlTransient
    private byte[] fileContents;

    /**
     * Determines if this document matches another document.
     * Expects ID and/or document name to be filled in.
     * @param anotherDocument
     * @return whether the two document objects are the same
     */
    public boolean matches(final SurveillanceNonconformityDocument anotherDocument) {
        if (this.id != null && anotherDocument.id != null
                && this.id.longValue() == anotherDocument.id.longValue()) {
            return true;
        } else if (!StringUtils.isEmpty(this.fileName) && !StringUtils.isEmpty(anotherDocument.fileName)
                && this.fileName.equalsIgnoreCase(anotherDocument.fileName)) {
            return true;
        }
        return false;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(final String fileType) {
        this.fileType = fileType;
    }

    public byte[] getFileContents() {
        return fileContents;
    }

    public void setFileContents(final byte[] fileContents) {
        this.fileContents = fileContents;
    }
}
