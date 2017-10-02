package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.search.CertifiedProductBasicSearchResultEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
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

	private Date certificationDate;

	private String chplProductNumber;

	private CertificationStatus certificationStatus;

	private List<CertifiedProduct> parents;

	private List<CertifiedProduct> children;

	private Developer developer;

	private ProductVersion version;

	private Product product;

	public IcsFamilyTreeNode(){
		parents = new ArrayList<CertifiedProduct>();
		children = new ArrayList<CertifiedProduct>();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Developer getDeveloper() {
		return developer;
	}

	public void setDeveloper(Developer developer) {
		this.developer = developer;
	}

	public ProductVersion getVersion() {
		return version;
	}

	public void setVersion(ProductVersion version) {
		this.version = version;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public String getChplProductNumber() {
		return chplProductNumber;
	}

	public void setChplProductNumber(String chplProductNumber) {
		this.chplProductNumber = chplProductNumber;
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

	public Date getCertificationDate() {
		return certificationDate;
	}

	public void setCertificationDate(Date certificationDate) {
		this.certificationDate = certificationDate;
	}


}
