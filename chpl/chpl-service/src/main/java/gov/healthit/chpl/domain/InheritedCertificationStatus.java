package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class InheritedCertificationStatus implements Serializable {
	private static final long serialVersionUID = 2456763191912903082L;

	/**
	 * Whether or not this listing inherits from another listing.
	 */
	@XmlElement(name = "inherits")
	private Boolean inherits;
	
	/**
	 * The first-level parent listings that this listing inherits from
	 */
	@XmlElementWrapper(name = "parents", nillable = true, required = false)
	@XmlElement(name = "parent")
	private List<CertifiedProduct> parents = new ArrayList<CertifiedProduct>();
	
	/**
	 * The first-level child listings that inherit from this listings
	 */
	@XmlElementWrapper(name = "children", nillable = true, required = false)
	@XmlElement(name = "child")
	private List<CertifiedProduct> children = new ArrayList<CertifiedProduct>();
	
	
	public List<CertifiedProduct> getParents() {
		return parents;
	}
	public void setParents(List<CertifiedProduct> parents) {
		this.parents = parents;
	}
	public List<CertifiedProduct> getChildren() {
		return children;
	}
	public void setChildren(List<CertifiedProduct> children) {
		this.children = children;
	}
	public Boolean getInherits() {
		return inherits;
	}
	public void setInherits(Boolean inherits) {
		this.inherits = inherits;
	}
}
