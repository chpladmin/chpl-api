package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class IcsFamilyTree implements Serializable{
	
	private static final long serialVersionUID = 9197828368084529919L;
	
	@XmlElement(required = true)
	private List<IcsFamilyTreeNode> icsNodes;
	
	public IcsFamilyTree(){
		icsNodes = new ArrayList<IcsFamilyTreeNode>();
	}

	public List<IcsFamilyTreeNode> getIcsNodes() {
		return icsNodes;
	}

	public void setIcsNodes(List<IcsFamilyTreeNode> icsNodes) {
		this.icsNodes = icsNodes;
	}
	
}
