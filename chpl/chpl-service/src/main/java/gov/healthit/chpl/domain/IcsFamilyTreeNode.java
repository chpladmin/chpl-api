package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

public class IcsFamilyTreeNode implements Serializable{
	
	private static final long serialVersionUID = 4170181178663367311L;

	private Long id;

	private String chplId;
	
	private CertificationStatus certificationStatus;
	
	private List<CertifiedProduct> parents;
	
	private List<CertifiedProduct> children;
	
	public IcsFamilyTreeNode(CertifiedProductSearchDetails cpsd){
		this.id = cpsd.getId();
		this.chplId = cpsd.getChplProductNumber();
		certificationStatus = new CertificationStatus(cpsd.getCertificationStatus());
		parents = new ArrayList<CertifiedProduct>(cpsd.getIcs().getParents());
		children = new ArrayList<CertifiedProduct>(cpsd.getIcs().getChildren());
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getChplId() {
		return chplId;
	}

	public void setChplId(String chplId) {
		this.chplId = chplId;
	}

	public CertificationStatus getCertificationStatus() {
		return certificationStatus;
	}

	public void setCertificationStatus(CertificationStatus certificationStatus) {
		this.certificationStatus = certificationStatus;
	}

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
	
	
}
