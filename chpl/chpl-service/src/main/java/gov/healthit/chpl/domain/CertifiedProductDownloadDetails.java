package gov.healthit.chpl.domain;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;

@XmlRootElement(name = "certifiedProductDetails")
public class CertifiedProductDownloadDetails {
	private static final Logger logger = LogManager.getLogger(CertifiedProductDownloadDetails.class);

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private Long id;
    private String testingLabName;
    private String chplProductNumber;
    private String reportFileLocation;
    private String acbCertificationId;
    private String classificationType;
    private String otherAcb;
    private String certificationStatus;
	private String developer;
	private String product;
	private String version;
	private String certificationEdition;
	private String practiceType;
	private String certifyingBody;
	private Date certificationDate;
	private Integer countCerts;
	private Integer countCqms;
	private Boolean visibleOnChpl;
	private Boolean privacyAttestation;
	private String termsOfUse;
	private String apiDocumentation;
	private String ics;
	private Boolean sedTesting;
	private Boolean qmsTesting;
	private Boolean transparencyAttestation;
	private Long lastModifiedDate;
	private String additionalSoftware;
	
	private boolean CERT_170_302__a;
	private boolean CERT_170_302__b;
	private boolean CERT_170_302__c;
	private boolean CERT_170_302__d;
	private boolean CERT_170_302__e;
	private boolean CERT_170_302__f__1;
	private boolean CERT_170_302__f__2;
	private boolean CERT_170_302__f__3;
	private boolean CERT_170_302__g;
	private boolean CERT_170_302__h;
	private boolean CERT_170_302__i;
	private boolean CERT_170_302__j;
	private boolean CERT_170_302__k;
	private boolean CERT_170_302__l;
	private boolean CERT_170_302__m;
	private boolean CERT_170_302__n;
	private boolean CERT_170_302__o;
	private boolean CERT_170_302__p;
	private boolean CERT_170_302__q;
	private boolean CERT_170_302__r;
	private boolean CERT_170_302__s;
	private boolean CERT_170_302__t;
	private boolean CERT_170_302__u;
	private boolean CERT_170_302__v;
	private boolean CERT_170_302__w;
	private boolean CERT_170_304__a;
	private boolean CERT_170_304__b;
	private boolean CERT_170_304__c;
	private boolean CERT_170_304__d;
	private boolean CERT_170_304__e;
	private boolean CERT_170_304__f;
	private boolean CERT_170_304__g;
	private boolean CERT_170_304__h;
	private boolean CERT_170_304__i;
	private boolean CERT_170_304__j;
	private boolean CERT_170_306__a;
	private boolean CERT_170_306__b;
	private boolean CERT_170_306__c;
	private boolean CERT_170_306__d__1;
	private boolean CERT_170_306__d__2;
	private boolean CERT_170_306__e;
	private boolean CERT_170_306__f;
	private boolean CERT_170_306__g;
	private boolean CERT_170_306__h;
	private boolean CERT_170_306__i;
	
	private boolean CERT_170_314__a__1;
	private boolean CERT_170_314__a__2;
	private boolean CERT_170_314__a__3;
	private boolean CERT_170_314__a__4;
	private boolean CERT_170_314__a__5;
	private boolean CERT_170_314__a__6;
	private boolean CERT_170_314__a__7;
	private boolean CERT_170_314__a__8;
	private boolean CERT_170_314__a__9;
	private boolean CERT_170_314__a__10;
	private boolean CERT_170_314__a__11;
	private boolean CERT_170_314__a__12;
	private boolean CERT_170_314__a__13;
	private boolean CERT_170_314__a__14;
	private boolean CERT_170_314__a__15;
	private boolean CERT_170_314__a__16;
	private boolean CERT_170_314__a__17;
	private boolean CERT_170_314__a__18;
	private boolean CERT_170_314__a__19;
	private boolean CERT_170_314__a__20;
	private boolean CERT_170_314__b__1;
	private boolean CERT_170_314__b__2;
	private boolean CERT_170_314__b__3;
	private boolean CERT_170_314__b__4;
	private boolean CERT_170_314__b__5__A;
	private boolean CERT_170_314__b__5__B;
	private boolean CERT_170_314__b__6;
	private boolean CERT_170_314__b__7;
	private boolean CERT_170_314__b__8;
	private boolean CERT_170_314__b__9;
	private boolean CERT_170_314__c__1;
	private boolean CERT_170_314__c__2;
	private boolean CERT_170_314__c__3;
	private boolean CERT_170_314__d__1;
	private boolean CERT_170_314__d__2;
	private boolean CERT_170_314__d__3;
	private boolean CERT_170_314__d__4;
	private boolean CERT_170_314__d__5;
	private boolean CERT_170_314__d__6;
	private boolean CERT_170_314__d__7;
	private boolean CERT_170_314__d__8;
	private boolean CERT_170_314__d__9;
	private boolean CERT_170_314__e__1;
	private boolean CERT_170_314__e__2;
	private boolean CERT_170_314__e__3;
	private boolean CERT_170_314__f__1;
	private boolean CERT_170_314__f__2;
	private boolean CERT_170_314__f__3;
	private boolean CERT_170_314__f__4;
	private boolean CERT_170_314__f__5;
	private boolean CERT_170_314__f__6;
	private boolean CERT_170_314__f__7;
	private boolean CERT_170_314__g__1;
	private boolean CERT_170_314__g__2;
	private boolean CERT_170_314__g__3;
	private boolean CERT_170_314__g__4;
	private boolean CERT_170_314__h__1;
	private boolean CERT_170_314__h__2;
	private boolean CERT_170_314__h__3;
	
	private String CMS117 = " ";
	private String CMS122 = " ";
	private String CMS123 = " ";
	private String CMS124 = " ";
	private String CMS125 = " ";
	private String CMS126 = " ";
	private String CMS127 = " ";
	private String CMS128 = " ";
	private String CMS129 = " ";
	private String CMS130 = " ";
	private String CMS131 = " ";
	private String CMS132 = " ";
	private String CMS133 = " ";
	private String CMS134 = " ";
	private String CMS135 = " ";
	private String CMS136 = " ";
	private String CMS137 = " ";
	private String CMS138 = " ";
	private String CMS139 = " ";
	private String CMS140 = " ";
	private String CMS141 = " ";
	private String CMS142 = " ";
	private String CMS143 = " ";
	private String CMS144 = " ";
	private String CMS145 = " ";
	private String CMS146 = " ";
	private String CMS147 = " ";
	private String CMS148 = " ";
	private String CMS149 = " ";
	private String CMS153 = " ";
	private String CMS154 = " ";
	private String CMS155 = " ";
	private String CMS156 = " ";
	private String CMS157 = " ";
	private String CMS158 = " ";
	private String CMS159 = " ";
	private String CMS160 = " ";
	private String CMS161 = " ";
	private String CMS163 = " ";
	private String CMS164 = " ";
	private String CMS165 = " ";
	private String CMS166 = " ";
	private String CMS167 = " ";
	private String CMS169 = " ";
	private String CMS177 = " ";
	private String CMS179 = " ";
	private String CMS182 = " ";
	private String CMS22 = " ";
	private String CMS2 = " ";
	private String CMS50 = " ";
	private String CMS52 = " ";
	private String CMS56 = " ";
	private String CMS61 = " ";
	private String CMS62 = " ";
	private String CMS64 = " ";
	private String CMS65 = " ";
	private String CMS66 = " ";
	private String CMS68 = " ";
	private String CMS69 = " ";
	private String CMS74 = " ";
	private String CMS75 = " ";
	private String CMS77 = " ";
	private String CMS82 = " ";
	private String CMS90 = " ";
	private String CMS26 = " ";
	private String CMS102 = " ";
	private String CMS31 = " ";
	private String CMS100 = " ";
	private String CMS53 = " ";
	private String CMS60 = " ";
	private String CMS108 = " ";
	private String CMS190 = " ";
	private String CMS105 = " ";
	private String CMS73 = " ";
	private String CMS104 = " ";
	private String CMS72 = " ";
	private String CMS113 = " ";
	private String CMS9 = " ";
	private String CMS55 = " ";
	private String CMS111 = " ";
	private String CMS185 = " ";
	private String CMS107 = " ";
	private String CMS109 = " ";
	private String CMS110 = " ";
	private String CMS114 = " ";
	private String CMS71 = " ";
	private String CMS188 = " ";
	private String CMS91 = " ";
	private String CMS178 = " ";
	private String CMS32 = " ";
	private String CMS171 = " ";
	private String CMS172 = " ";
	private String CMS30 = " ";
	
	private boolean NQF_0001;
	private boolean NQF_0002;
	private boolean NQF_0004;
	private boolean NQF_0012;
	private boolean NQF_0013;
	private boolean NQF_0014;
	private boolean NQF_0018;
	private boolean NQF_0024;
	private boolean NQF_0027;
	private boolean NQF_0028;
	private boolean NQF_0031;
	private boolean NQF_0032;
	private boolean NQF_0033;
	private boolean NQF_0034;
	private boolean NQF_0036;
	private boolean NQF_0038;
	private boolean NQF_0041;
	private boolean NQF_0043;
	private boolean NQF_0047;
	private boolean NQF_0052;
	private boolean NQF_0055;
	private boolean NQF_0056;
	private boolean NQF_0059;
	private boolean NQF_0061;
	private boolean NQF_0062;
	private boolean NQF_0064;
	private boolean NQF_0067;
	private boolean NQF_0068;
	private boolean NQF_0070;
	private boolean NQF_0073;
	private boolean NQF_0074;
	private boolean NQF_0075;
	private boolean NQF_0081;
	private boolean NQF_0083;
	private boolean NQF_0084;
	private boolean NQF_0086;
	private boolean NQF_0088;
	private boolean NQF_0089;
	private boolean NQF_0105;
	private boolean NQF_0371;
	private boolean NQF_0372;
	private boolean NQF_0373;
	private boolean NQF_0374;
	private boolean NQF_0375;
	private boolean NQF_0376;
	private boolean NQF_0385;
	private boolean NQF_0387;
	private boolean NQF_0389;
	private boolean NQF_0421;
	private boolean NQF_0435;
	private boolean NQF_0436;
	private boolean NQF_0437;
	private boolean NQF_0438;
	private boolean NQF_0439;
	private boolean NQF_0440;
	private boolean NQF_0441;
	private boolean NQF_0495;
	private boolean NQF_0497;
	private boolean NQF_0575;

	
	public CertifiedProductDownloadDetails() {
	}
	
	public CertifiedProductDownloadDetails(CertifiedProductDetailsDTO dto) {
		this();
		
		this.id = dto.getId();
		this.testingLabName = dto.getTestingLabName();
		if(dto.getYear() != null && 
				(dto.getYear().equals("2011") || dto.getYear().equals("2014"))) {
			this.chplProductNumber = dto.getChplProductNumber();
		} else {
			this.chplProductNumber = dto.getTestingLabCode() + "." + dto.getCertificationBodyCode() + "." + 
					dto.getDeveloperCode() + "." + dto.getProductCode() + "." + dto.getVersionCode() + 
					"." + dto.getIcsCode() + "." + dto.getAdditionalSoftwareCode() + 
					"." + dto.getCertifiedDateCode();
		}
		this.reportFileLocation = dto.getReportFileLocation();
		this.acbCertificationId = dto.getAcbCertificationId();
		this.classificationType = dto.getProductClassificationName();
		this.otherAcb = dto.getOtherAcb();
		this.certificationStatus = dto.getCertificationStatusName();
		this.developer = dto.getDeveloperName();
		this.product = dto.getProductName();
		this.version = dto.getProductVersion();
		this.certificationEdition = dto.getYear();
		this.practiceType = dto.getPracticeTypeName();
		this.certifyingBody = dto.getCertificationBodyName();
		this.certificationDate = dto.getCertificationDate();
		this.countCerts = dto.getCountCertifications();
		this.countCqms = dto.getCountCqms();
		this.visibleOnChpl = dto.getVisibleOnChpl();
		this.privacyAttestation = dto.getPrivacyAttestation();
		this.termsOfUse = dto.getTermsOfUse();
		this.apiDocumentation = dto.getApiDocumentation();
		this.ics = dto.getIcs();
		this.sedTesting = dto.getSedTesting();
		this.qmsTesting = dto.getQmsTesting();
		
		if(dto.getTransparencyAttestation() == null) {
			this.transparencyAttestation = Boolean.FALSE;
		} else {
			this.transparencyAttestation = dto.getTransparencyAttestation();
		}
		if(dto.getLastModifiedDate() != null) {
			this.lastModifiedDate = dto.getLastModifiedDate().getTime();
		}
	}
	
	@XmlTransient
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	public String getChplProductNumber() {
		return chplProductNumber;
	}
	public void setChplProductNumber(String chplProductNumber) {
		this.chplProductNumber = chplProductNumber;
	}
	public String getReportFileLocation() {
		return reportFileLocation;
	}
	public void setReportFileLocation(String reportFileLocation) {
		this.reportFileLocation = reportFileLocation;
	}
	public String getAcbCertificationId() {
		return acbCertificationId;
	}
	public void setAcbCertificationId(String acbCertificationId) {
		this.acbCertificationId = acbCertificationId;
	}
	public String getOtherAcb() {
		return otherAcb;
	}
	public void setOtherAcb(String otherAcb) {
		this.otherAcb = otherAcb;
	}
	
	@XmlTransient
	public Integer getCountCerts() {
		return countCerts;
	}
	public void setCountCerts(Integer countCertsSuccessful) {
		this.countCerts = countCertsSuccessful;
	}
	
	@XmlTransient
	public Integer getCountCqms() {
		return countCqms;
	}
	public void setCountCqms(Integer countCQMsSuccessful) {
		this.countCqms = countCQMsSuccessful;
	}
	
	@XmlTransient
	public Boolean getVisibleOnChpl() {
		return visibleOnChpl;
	}
	public void setVisibleOnChpl(Boolean visibleOnChpl) {
		this.visibleOnChpl = visibleOnChpl;
	}
	
	@XmlTransient
	public Long getLastModifiedDate() {
		return lastModifiedDate;
	}
	public void setLastModifiedDate(Long lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	
	@XmlElement(name = "lastModifiedDate") 
	public String getFormattedLastModifiedDate() {
		if(lastModifiedDate != null) {
			Date d = new Date(lastModifiedDate);
			return timestampFormat.format(d);
		}
		return null;
	}
	
	public Boolean getPrivacyAttestation() {
		return privacyAttestation;
	}
	public void setPrivacyAttestation(Boolean privacyAttestation) {
		this.privacyAttestation = privacyAttestation;
	}

	public String getClassificationType() {
		return classificationType;
	}

	public void setClassificationType(String classificationType) {
		this.classificationType = classificationType;
	}

	public String getCertificationStatus() {
		return certificationStatus;
	}

	public void setCertificationStatus(String certificationStatus) {
		this.certificationStatus = certificationStatus;
	}

	public String getDeveloper() {
		return developer;
	}

	public void setDeveloper(String developer) {
		this.developer = developer;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getCertificationEdition() {
		return certificationEdition;
	}

	public void setCertificationEdition(String certificationEdition) {
		this.certificationEdition = certificationEdition;
	}

	public String getPracticeType() {
		return practiceType;
	}

	public void setPracticeType(String practiceType) {
		this.practiceType = practiceType;
	}

	public String getCertifyingBody() {
		return certifyingBody;
	}

	public void setCertifyingBody(String certifyingBody) {
		this.certifyingBody = certifyingBody;
	}

	@XmlTransient
	public Date getCertificationDate() {
		return certificationDate;
	}

	public void setCertificationDate(Date certificationDate) {
		this.certificationDate = certificationDate;
	}

	@XmlElement(name = "certificationDate")
	public String getFormattedCertificationDate() {
		if(certificationDate != null) {
			return dateFormat.format(certificationDate);
		}
		return null;
	}
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getAdditionalSoftware() {
		return additionalSoftware;
	}

	public void setAdditionalSoftware(String additionalSoftware) {
		this.additionalSoftware = additionalSoftware;
	}

	public boolean isCERT_170_302__a() {
		return CERT_170_302__a;
	}

	public void setCERT_170_302__a(boolean cERT_170_302__a) {
		CERT_170_302__a = cERT_170_302__a;
	}

	public boolean isCERT_170_302__b() {
		return CERT_170_302__b;
	}

	public void setCERT_170_302__b(boolean cERT_170_302__b) {
		CERT_170_302__b = cERT_170_302__b;
	}

	public boolean isCERT_170_302__c() {
		return CERT_170_302__c;
	}

	public void setCERT_170_302__c(boolean cERT_170_302__c) {
		CERT_170_302__c = cERT_170_302__c;
	}

	public boolean isCERT_170_302__d() {
		return CERT_170_302__d;
	}

	public void setCERT_170_302__d(boolean cERT_170_302__d) {
		CERT_170_302__d = cERT_170_302__d;
	}

	public boolean isCERT_170_302__e() {
		return CERT_170_302__e;
	}

	public void setCERT_170_302__e(boolean cERT_170_302__e) {
		CERT_170_302__e = cERT_170_302__e;
	}

	public boolean isCERT_170_302__f__1() {
		return CERT_170_302__f__1;
	}

	public void setCERT_170_302__f__1(boolean cERT_170_302__f__1) {
		CERT_170_302__f__1 = cERT_170_302__f__1;
	}

	public boolean isCERT_170_302__f__2() {
		return CERT_170_302__f__2;
	}

	public void setCERT_170_302__f__2(boolean cERT_170_302__f__2) {
		CERT_170_302__f__2 = cERT_170_302__f__2;
	}

	public boolean isCERT_170_302__f__3() {
		return CERT_170_302__f__3;
	}

	public void setCERT_170_302__f__3(boolean cERT_170_302__f__3) {
		CERT_170_302__f__3 = cERT_170_302__f__3;
	}

	public boolean isCERT_170_302__g() {
		return CERT_170_302__g;
	}

	public void setCERT_170_302__g(boolean cERT_170_302__g) {
		CERT_170_302__g = cERT_170_302__g;
	}

	public boolean isCERT_170_302__h() {
		return CERT_170_302__h;
	}

	public void setCERT_170_302__h(boolean cERT_170_302__h) {
		CERT_170_302__h = cERT_170_302__h;
	}

	public boolean isCERT_170_302__i() {
		return CERT_170_302__i;
	}

	public void setCERT_170_302__i(boolean cERT_170_302__i) {
		CERT_170_302__i = cERT_170_302__i;
	}

	public boolean isCERT_170_302__j() {
		return CERT_170_302__j;
	}

	public void setCERT_170_302__j(boolean cERT_170_302__j) {
		CERT_170_302__j = cERT_170_302__j;
	}

	public boolean isCERT_170_302__k() {
		return CERT_170_302__k;
	}

	public void setCERT_170_302__k(boolean cERT_170_302__k) {
		CERT_170_302__k = cERT_170_302__k;
	}

	public boolean isCERT_170_302__l() {
		return CERT_170_302__l;
	}

	public void setCERT_170_302__l(boolean cERT_170_302__l) {
		CERT_170_302__l = cERT_170_302__l;
	}

	public boolean isCERT_170_302__m() {
		return CERT_170_302__m;
	}

	public void setCERT_170_302__m(boolean cERT_170_302__m) {
		CERT_170_302__m = cERT_170_302__m;
	}

	public boolean isCERT_170_302__n() {
		return CERT_170_302__n;
	}

	public void setCERT_170_302__n(boolean cERT_170_302__n) {
		CERT_170_302__n = cERT_170_302__n;
	}

	public boolean isCERT_170_302__o() {
		return CERT_170_302__o;
	}

	public void setCERT_170_302__o(boolean cERT_170_302__o) {
		CERT_170_302__o = cERT_170_302__o;
	}

	public boolean isCERT_170_302__p() {
		return CERT_170_302__p;
	}

	public void setCERT_170_302__p(boolean cERT_170_302__p) {
		CERT_170_302__p = cERT_170_302__p;
	}

	public boolean isCERT_170_302__q() {
		return CERT_170_302__q;
	}

	public void setCERT_170_302__q(boolean cERT_170_302__q) {
		CERT_170_302__q = cERT_170_302__q;
	}

	public boolean isCERT_170_302__r() {
		return CERT_170_302__r;
	}

	public void setCERT_170_302__r(boolean cERT_170_302__r) {
		CERT_170_302__r = cERT_170_302__r;
	}

	public boolean isCERT_170_302__s() {
		return CERT_170_302__s;
	}

	public void setCERT_170_302__s(boolean cERT_170_302__s) {
		CERT_170_302__s = cERT_170_302__s;
	}

	public boolean isCERT_170_302__t() {
		return CERT_170_302__t;
	}

	public void setCERT_170_302__t(boolean cERT_170_302__t) {
		CERT_170_302__t = cERT_170_302__t;
	}

	public boolean isCERT_170_302__u() {
		return CERT_170_302__u;
	}

	public void setCERT_170_302__u(boolean cERT_170_302__u) {
		CERT_170_302__u = cERT_170_302__u;
	}

	public boolean isCERT_170_302__v() {
		return CERT_170_302__v;
	}

	public void setCERT_170_302__v(boolean cERT_170_302__v) {
		CERT_170_302__v = cERT_170_302__v;
	}

	public boolean isCERT_170_302__w() {
		return CERT_170_302__w;
	}

	public void setCERT_170_302__w(boolean cERT_170_302__w) {
		CERT_170_302__w = cERT_170_302__w;
	}

	public boolean isCERT_170_304__a() {
		return CERT_170_304__a;
	}

	public void setCERT_170_304__a(boolean cERT_170_304__a) {
		CERT_170_304__a = cERT_170_304__a;
	}

	public boolean isCERT_170_304__b() {
		return CERT_170_304__b;
	}

	public void setCERT_170_304__b(boolean cERT_170_304__b) {
		CERT_170_304__b = cERT_170_304__b;
	}

	public boolean isCERT_170_304__c() {
		return CERT_170_304__c;
	}

	public void setCERT_170_304__c(boolean cERT_170_304__c) {
		CERT_170_304__c = cERT_170_304__c;
	}

	public boolean isCERT_170_304__d() {
		return CERT_170_304__d;
	}

	public void setCERT_170_304__d(boolean cERT_170_304__d) {
		CERT_170_304__d = cERT_170_304__d;
	}

	public boolean isCERT_170_304__e() {
		return CERT_170_304__e;
	}

	public void setCERT_170_304__e(boolean cERT_170_304__e) {
		CERT_170_304__e = cERT_170_304__e;
	}

	public boolean isCERT_170_304__f() {
		return CERT_170_304__f;
	}

	public void setCERT_170_304__f(boolean cERT_170_304__f) {
		CERT_170_304__f = cERT_170_304__f;
	}

	public boolean isCERT_170_304__g() {
		return CERT_170_304__g;
	}

	public void setCERT_170_304__g(boolean cERT_170_304__g) {
		CERT_170_304__g = cERT_170_304__g;
	}

	public boolean isCERT_170_304__h() {
		return CERT_170_304__h;
	}

	public void setCERT_170_304__h(boolean cERT_170_304__h) {
		CERT_170_304__h = cERT_170_304__h;
	}

	public boolean isCERT_170_304__i() {
		return CERT_170_304__i;
	}

	public void setCERT_170_304__i(boolean cERT_170_304__i) {
		CERT_170_304__i = cERT_170_304__i;
	}

	public boolean isCERT_170_304__j() {
		return CERT_170_304__j;
	}

	public void setCERT_170_304__j(boolean cERT_170_304__j) {
		CERT_170_304__j = cERT_170_304__j;
	}

	public boolean isCERT_170_306__a() {
		return CERT_170_306__a;
	}

	public void setCERT_170_306__a(boolean cERT_170_306__a) {
		CERT_170_306__a = cERT_170_306__a;
	}

	public boolean isCERT_170_306__b() {
		return CERT_170_306__b;
	}

	public void setCERT_170_306__b(boolean cERT_170_306__b) {
		CERT_170_306__b = cERT_170_306__b;
	}

	public boolean isCERT_170_306__c() {
		return CERT_170_306__c;
	}

	public void setCERT_170_306__c(boolean cERT_170_306__c) {
		CERT_170_306__c = cERT_170_306__c;
	}

	public boolean isCERT_170_306__d__1() {
		return CERT_170_306__d__1;
	}

	public void setCERT_170_306__d__1(boolean cERT_170_306__d__1) {
		CERT_170_306__d__1 = cERT_170_306__d__1;
	}

	public boolean isCERT_170_306__d__2() {
		return CERT_170_306__d__2;
	}

	public void setCERT_170_306__d__2(boolean cERT_170_306__d__2) {
		CERT_170_306__d__2 = cERT_170_306__d__2;
	}

	public boolean isCERT_170_306__e() {
		return CERT_170_306__e;
	}

	public void setCERT_170_306__e(boolean cERT_170_306__e) {
		CERT_170_306__e = cERT_170_306__e;
	}

	public boolean isCERT_170_306__f() {
		return CERT_170_306__f;
	}

	public void setCERT_170_306__f(boolean cERT_170_306__f) {
		CERT_170_306__f = cERT_170_306__f;
	}

	public boolean isCERT_170_306__g() {
		return CERT_170_306__g;
	}

	public void setCERT_170_306__g(boolean cERT_170_306__g) {
		CERT_170_306__g = cERT_170_306__g;
	}

	public boolean isCERT_170_306__h() {
		return CERT_170_306__h;
	}

	public void setCERT_170_306__h(boolean cERT_170_306__h) {
		CERT_170_306__h = cERT_170_306__h;
	}

	public boolean isCERT_170_306__i() {
		return CERT_170_306__i;
	}

	public void setCERT_170_306__i(boolean cERT_170_306__i) {
		CERT_170_306__i = cERT_170_306__i;
	}

	public boolean isCERT_170_314__a__1() {
		return CERT_170_314__a__1;
	}

	public void setCERT_170_314__a__1(boolean cERT_170_314__a__1) {
		CERT_170_314__a__1 = cERT_170_314__a__1;
	}

	public boolean isCERT_170_314__a__2() {
		return CERT_170_314__a__2;
	}

	public void setCERT_170_314__a__2(boolean cERT_170_314__a__2) {
		CERT_170_314__a__2 = cERT_170_314__a__2;
	}

	public boolean isCERT_170_314__a__3() {
		return CERT_170_314__a__3;
	}

	public void setCERT_170_314__a__3(boolean cERT_170_314__a__3) {
		CERT_170_314__a__3 = cERT_170_314__a__3;
	}

	public boolean isCERT_170_314__a__4() {
		return CERT_170_314__a__4;
	}

	public void setCERT_170_314__a__4(boolean cERT_170_314__a__4) {
		CERT_170_314__a__4 = cERT_170_314__a__4;
	}

	public boolean isCERT_170_314__a__5() {
		return CERT_170_314__a__5;
	}

	public void setCERT_170_314__a__5(boolean cERT_170_314__a__5) {
		CERT_170_314__a__5 = cERT_170_314__a__5;
	}

	public boolean isCERT_170_314__a__6() {
		return CERT_170_314__a__6;
	}

	public void setCERT_170_314__a__6(boolean cERT_170_314__a__6) {
		CERT_170_314__a__6 = cERT_170_314__a__6;
	}

	public boolean isCERT_170_314__a__7() {
		return CERT_170_314__a__7;
	}

	public void setCERT_170_314__a__7(boolean cERT_170_314__a__7) {
		CERT_170_314__a__7 = cERT_170_314__a__7;
	}

	public boolean isCERT_170_314__a__8() {
		return CERT_170_314__a__8;
	}

	public void setCERT_170_314__a__8(boolean cERT_170_314__a__8) {
		CERT_170_314__a__8 = cERT_170_314__a__8;
	}

	public boolean isCERT_170_314__a__9() {
		return CERT_170_314__a__9;
	}

	public void setCERT_170_314__a__9(boolean cERT_170_314__a__9) {
		CERT_170_314__a__9 = cERT_170_314__a__9;
	}

	public boolean isCERT_170_314__a__10() {
		return CERT_170_314__a__10;
	}

	public void setCERT_170_314__a__10(boolean cERT_170_314__a__10) {
		CERT_170_314__a__10 = cERT_170_314__a__10;
	}

	public boolean isCERT_170_314__a__11() {
		return CERT_170_314__a__11;
	}

	public void setCERT_170_314__a__11(boolean cERT_170_314__a__11) {
		CERT_170_314__a__11 = cERT_170_314__a__11;
	}

	public boolean isCERT_170_314__a__12() {
		return CERT_170_314__a__12;
	}

	public void setCERT_170_314__a__12(boolean cERT_170_314__a__12) {
		CERT_170_314__a__12 = cERT_170_314__a__12;
	}

	public boolean isCERT_170_314__a__13() {
		return CERT_170_314__a__13;
	}

	public void setCERT_170_314__a__13(boolean cERT_170_314__a__13) {
		CERT_170_314__a__13 = cERT_170_314__a__13;
	}

	public boolean isCERT_170_314__a__14() {
		return CERT_170_314__a__14;
	}

	public void setCERT_170_314__a__14(boolean cERT_170_314__a__14) {
		CERT_170_314__a__14 = cERT_170_314__a__14;
	}

	public boolean isCERT_170_314__a__15() {
		return CERT_170_314__a__15;
	}

	public void setCERT_170_314__a__15(boolean cERT_170_314__a__15) {
		CERT_170_314__a__15 = cERT_170_314__a__15;
	}

	public boolean isCERT_170_314__a__16() {
		return CERT_170_314__a__16;
	}

	public void setCERT_170_314__a__16(boolean cERT_170_314__a__16) {
		CERT_170_314__a__16 = cERT_170_314__a__16;
	}

	public boolean isCERT_170_314__a__17() {
		return CERT_170_314__a__17;
	}

	public void setCERT_170_314__a__17(boolean cERT_170_314__a__17) {
		CERT_170_314__a__17 = cERT_170_314__a__17;
	}

	public boolean isCERT_170_314__a__18() {
		return CERT_170_314__a__18;
	}

	public void setCERT_170_314__a__18(boolean cERT_170_314__a__18) {
		CERT_170_314__a__18 = cERT_170_314__a__18;
	}

	public boolean isCERT_170_314__a__19() {
		return CERT_170_314__a__19;
	}

	public void setCERT_170_314__a__19(boolean cERT_170_314__a__19) {
		CERT_170_314__a__19 = cERT_170_314__a__19;
	}

	public boolean isCERT_170_314__a__20() {
		return CERT_170_314__a__20;
	}

	public void setCERT_170_314__a__20(boolean cERT_170_314__a__20) {
		CERT_170_314__a__20 = cERT_170_314__a__20;
	}

	public boolean isCERT_170_314__b__1() {
		return CERT_170_314__b__1;
	}

	public void setCERT_170_314__b__1(boolean cERT_170_314__b__1) {
		CERT_170_314__b__1 = cERT_170_314__b__1;
	}

	public boolean isCERT_170_314__b__2() {
		return CERT_170_314__b__2;
	}

	public void setCERT_170_314__b__2(boolean cERT_170_314__b__2) {
		CERT_170_314__b__2 = cERT_170_314__b__2;
	}

	public boolean isCERT_170_314__b__3() {
		return CERT_170_314__b__3;
	}

	public void setCERT_170_314__b__3(boolean cERT_170_314__b__3) {
		CERT_170_314__b__3 = cERT_170_314__b__3;
	}

	public boolean isCERT_170_314__b__4() {
		return CERT_170_314__b__4;
	}

	public void setCERT_170_314__b__4(boolean cERT_170_314__b__4) {
		CERT_170_314__b__4 = cERT_170_314__b__4;
	}

	public boolean isCERT_170_314__b__5__A() {
		return CERT_170_314__b__5__A;
	}

	public void setCERT_170_314__b__5__A(boolean cERT_170_314__b__5__A) {
		CERT_170_314__b__5__A = cERT_170_314__b__5__A;
	}

	public boolean isCERT_170_314__b__5__B() {
		return CERT_170_314__b__5__B;
	}

	public void setCERT_170_314__b__5__B(boolean cERT_170_314__b__5__B) {
		CERT_170_314__b__5__B = cERT_170_314__b__5__B;
	}

	public boolean isCERT_170_314__b__6() {
		return CERT_170_314__b__6;
	}

	public void setCERT_170_314__b__6(boolean cERT_170_314__b__6) {
		CERT_170_314__b__6 = cERT_170_314__b__6;
	}

	public boolean isCERT_170_314__b__7() {
		return CERT_170_314__b__7;
	}

	public void setCERT_170_314__b__7(boolean cERT_170_314__b__7) {
		CERT_170_314__b__7 = cERT_170_314__b__7;
	}

	public boolean isCERT_170_314__b__8() {
		return CERT_170_314__b__8;
	}

	public void setCERT_170_314__b__8(boolean cERT_170_314__b__8) {
		CERT_170_314__b__8 = cERT_170_314__b__8;
	}

	public boolean isCERT_170_314__b__9() {
		return CERT_170_314__b__9;
	}

	public void setCERT_170_314__b__9(boolean cERT_170_314__b__9) {
		CERT_170_314__b__9 = cERT_170_314__b__9;
	}

	public boolean isCERT_170_314__c__1() {
		return CERT_170_314__c__1;
	}

	public void setCERT_170_314__c__1(boolean cERT_170_314__c__1) {
		CERT_170_314__c__1 = cERT_170_314__c__1;
	}

	public boolean isCERT_170_314__c__2() {
		return CERT_170_314__c__2;
	}

	public void setCERT_170_314__c__2(boolean cERT_170_314__c__2) {
		CERT_170_314__c__2 = cERT_170_314__c__2;
	}

	public boolean isCERT_170_314__c__3() {
		return CERT_170_314__c__3;
	}

	public void setCERT_170_314__c__3(boolean cERT_170_314__c__3) {
		CERT_170_314__c__3 = cERT_170_314__c__3;
	}

	public boolean isCERT_170_314__d__1() {
		return CERT_170_314__d__1;
	}

	public void setCERT_170_314__d__1(boolean cERT_170_314__d__1) {
		CERT_170_314__d__1 = cERT_170_314__d__1;
	}

	public boolean isCERT_170_314__d__2() {
		return CERT_170_314__d__2;
	}

	public void setCERT_170_314__d__2(boolean cERT_170_314__d__2) {
		CERT_170_314__d__2 = cERT_170_314__d__2;
	}

	public boolean isCERT_170_314__d__3() {
		return CERT_170_314__d__3;
	}

	public void setCERT_170_314__d__3(boolean cERT_170_314__d__3) {
		CERT_170_314__d__3 = cERT_170_314__d__3;
	}

	public boolean isCERT_170_314__d__4() {
		return CERT_170_314__d__4;
	}

	public void setCERT_170_314__d__4(boolean cERT_170_314__d__4) {
		CERT_170_314__d__4 = cERT_170_314__d__4;
	}

	public boolean isCERT_170_314__d__5() {
		return CERT_170_314__d__5;
	}

	public void setCERT_170_314__d__5(boolean cERT_170_314__d__5) {
		CERT_170_314__d__5 = cERT_170_314__d__5;
	}

	public boolean isCERT_170_314__d__6() {
		return CERT_170_314__d__6;
	}

	public void setCERT_170_314__d__6(boolean cERT_170_314__d__6) {
		CERT_170_314__d__6 = cERT_170_314__d__6;
	}

	public boolean isCERT_170_314__d__7() {
		return CERT_170_314__d__7;
	}

	public void setCERT_170_314__d__7(boolean cERT_170_314__d__7) {
		CERT_170_314__d__7 = cERT_170_314__d__7;
	}

	public boolean isCERT_170_314__d__8() {
		return CERT_170_314__d__8;
	}

	public void setCERT_170_314__d__8(boolean cERT_170_314__d__8) {
		CERT_170_314__d__8 = cERT_170_314__d__8;
	}

	public boolean isCERT_170_314__d__9() {
		return CERT_170_314__d__9;
	}

	public void setCERT_170_314__d__9(boolean cERT_170_314__d__9) {
		CERT_170_314__d__9 = cERT_170_314__d__9;
	}

	public boolean isCERT_170_314__e__1() {
		return CERT_170_314__e__1;
	}

	public void setCERT_170_314__e__1(boolean cERT_170_314__e__1) {
		CERT_170_314__e__1 = cERT_170_314__e__1;
	}

	public boolean isCERT_170_314__e__2() {
		return CERT_170_314__e__2;
	}

	public void setCERT_170_314__e__2(boolean cERT_170_314__e__2) {
		CERT_170_314__e__2 = cERT_170_314__e__2;
	}

	public boolean isCERT_170_314__e__3() {
		return CERT_170_314__e__3;
	}

	public void setCERT_170_314__e__3(boolean cERT_170_314__e__3) {
		CERT_170_314__e__3 = cERT_170_314__e__3;
	}

	public boolean isCERT_170_314__f__1() {
		return CERT_170_314__f__1;
	}

	public void setCERT_170_314__f__1(boolean cERT_170_314__f__1) {
		CERT_170_314__f__1 = cERT_170_314__f__1;
	}

	public boolean isCERT_170_314__f__2() {
		return CERT_170_314__f__2;
	}

	public void setCERT_170_314__f__2(boolean cERT_170_314__f__2) {
		CERT_170_314__f__2 = cERT_170_314__f__2;
	}

	public boolean isCERT_170_314__f__3() {
		return CERT_170_314__f__3;
	}

	public void setCERT_170_314__f__3(boolean cERT_170_314__f__3) {
		CERT_170_314__f__3 = cERT_170_314__f__3;
	}

	public boolean isCERT_170_314__f__4() {
		return CERT_170_314__f__4;
	}

	public void setCERT_170_314__f__4(boolean cERT_170_314__f__4) {
		CERT_170_314__f__4 = cERT_170_314__f__4;
	}

	public boolean isCERT_170_314__f__5() {
		return CERT_170_314__f__5;
	}

	public void setCERT_170_314__f__5(boolean cERT_170_314__f__5) {
		CERT_170_314__f__5 = cERT_170_314__f__5;
	}

	public boolean isCERT_170_314__f__6() {
		return CERT_170_314__f__6;
	}

	public void setCERT_170_314__f__6(boolean cERT_170_314__f__6) {
		CERT_170_314__f__6 = cERT_170_314__f__6;
	}

	public boolean isCERT_170_314__f__7() {
		return CERT_170_314__f__7;
	}

	public void setCERT_170_314__f__7(boolean cERT_170_314__f__7) {
		CERT_170_314__f__7 = cERT_170_314__f__7;
	}

	public boolean isCERT_170_314__g__1() {
		return CERT_170_314__g__1;
	}

	public void setCERT_170_314__g__1(boolean cERT_170_314__g__1) {
		CERT_170_314__g__1 = cERT_170_314__g__1;
	}

	public boolean isCERT_170_314__g__2() {
		return CERT_170_314__g__2;
	}

	public void setCERT_170_314__g__2(boolean cERT_170_314__g__2) {
		CERT_170_314__g__2 = cERT_170_314__g__2;
	}

	public boolean isCERT_170_314__g__3() {
		return CERT_170_314__g__3;
	}

	public void setCERT_170_314__g__3(boolean cERT_170_314__g__3) {
		CERT_170_314__g__3 = cERT_170_314__g__3;
	}

	public boolean isCERT_170_314__g__4() {
		return CERT_170_314__g__4;
	}

	public void setCERT_170_314__g__4(boolean cERT_170_314__g__4) {
		CERT_170_314__g__4 = cERT_170_314__g__4;
	}

	public boolean isCERT_170_314__h__1() {
		return CERT_170_314__h__1;
	}

	public void setCERT_170_314__h__1(boolean cERT_170_314__h__1) {
		CERT_170_314__h__1 = cERT_170_314__h__1;
	}

	public boolean isCERT_170_314__h__2() {
		return CERT_170_314__h__2;
	}

	public void setCERT_170_314__h__2(boolean cERT_170_314__h__2) {
		CERT_170_314__h__2 = cERT_170_314__h__2;
	}

	public boolean isCERT_170_314__h__3() {
		return CERT_170_314__h__3;
	}

	public void setCERT_170_314__h__3(boolean cERT_170_314__h__3) {
		CERT_170_314__h__3 = cERT_170_314__h__3;
	}

	public String getCMS117() {
		return CMS117;
	}

	public void setCMS117(String cMS117) {
		CMS117 = cMS117;
	}

	public String getCMS122() {
		return CMS122;
	}

	public void setCMS122(String cMS122) {
		CMS122 = cMS122;
	}

	public String getCMS123() {
		return CMS123;
	}

	public void setCMS123(String cMS123) {
		CMS123 = cMS123;
	}

	public String getCMS124() {
		return CMS124;
	}

	public void setCMS124(String cMS124) {
		CMS124 = cMS124;
	}

	public String getCMS125() {
		return CMS125;
	}

	public void setCMS125(String cMS125) {
		CMS125 = cMS125;
	}

	public String getCMS126() {
		return CMS126;
	}

	public void setCMS126(String cMS126) {
		CMS126 = cMS126;
	}

	public String getCMS127() {
		return CMS127;
	}

	public void setCMS127(String cMS127) {
		CMS127 = cMS127;
	}

	public String getCMS128() {
		return CMS128;
	}

	public void setCMS128(String cMS128) {
		CMS128 = cMS128;
	}

	public String getCMS129() {
		return CMS129;
	}

	public void setCMS129(String cMS129) {
		CMS129 = cMS129;
	}

	public String getCMS130() {
		return CMS130;
	}

	public void setCMS130(String cMS130) {
		CMS130 = cMS130;
	}

	public String getCMS131() {
		return CMS131;
	}

	public void setCMS131(String cMS131) {
		CMS131 = cMS131;
	}

	public String getCMS132() {
		return CMS132;
	}

	public void setCMS132(String cMS132) {
		CMS132 = cMS132;
	}

	public String getCMS133() {
		return CMS133;
	}

	public void setCMS133(String cMS133) {
		CMS133 = cMS133;
	}

	public String getCMS134() {
		return CMS134;
	}

	public void setCMS134(String cMS134) {
		CMS134 = cMS134;
	}

	public String getCMS135() {
		return CMS135;
	}

	public void setCMS135(String cMS135) {
		CMS135 = cMS135;
	}

	public String getCMS136() {
		return CMS136;
	}

	public void setCMS136(String cMS136) {
		CMS136 = cMS136;
	}

	public String getCMS137() {
		return CMS137;
	}

	public void setCMS137(String cMS137) {
		CMS137 = cMS137;
	}

	public String getCMS138() {
		return CMS138;
	}

	public void setCMS138(String cMS138) {
		CMS138 = cMS138;
	}

	public String getCMS139() {
		return CMS139;
	}

	public void setCMS139(String cMS139) {
		CMS139 = cMS139;
	}

	public String getCMS140() {
		return CMS140;
	}

	public void setCMS140(String cMS140) {
		CMS140 = cMS140;
	}

	public String getCMS141() {
		return CMS141;
	}

	public void setCMS141(String cMS141) {
		CMS141 = cMS141;
	}

	public String getCMS142() {
		return CMS142;
	}

	public void setCMS142(String cMS142) {
		CMS142 = cMS142;
	}

	public String getCMS143() {
		return CMS143;
	}

	public void setCMS143(String cMS143) {
		CMS143 = cMS143;
	}

	public String getCMS144() {
		return CMS144;
	}

	public void setCMS144(String cMS144) {
		CMS144 = cMS144;
	}

	public String getCMS145() {
		return CMS145;
	}

	public void setCMS145(String cMS145) {
		CMS145 = cMS145;
	}

	public String getCMS146() {
		return CMS146;
	}

	public void setCMS146(String cMS146) {
		CMS146 = cMS146;
	}

	public String getCMS147() {
		return CMS147;
	}

	public void setCMS147(String cMS147) {
		CMS147 = cMS147;
	}

	public String getCMS148() {
		return CMS148;
	}

	public void setCMS148(String cMS148) {
		CMS148 = cMS148;
	}

	public String getCMS149() {
		return CMS149;
	}

	public void setCMS149(String cMS149) {
		CMS149 = cMS149;
	}

	public String getCMS153() {
		return CMS153;
	}

	public void setCMS153(String cMS153) {
		CMS153 = cMS153;
	}

	public String getCMS154() {
		return CMS154;
	}

	public void setCMS154(String cMS154) {
		CMS154 = cMS154;
	}

	public String getCMS155() {
		return CMS155;
	}

	public void setCMS155(String cMS155) {
		CMS155 = cMS155;
	}

	public String getCMS156() {
		return CMS156;
	}

	public void setCMS156(String cMS156) {
		CMS156 = cMS156;
	}

	public String getCMS157() {
		return CMS157;
	}

	public void setCMS157(String cMS157) {
		CMS157 = cMS157;
	}

	public String getCMS158() {
		return CMS158;
	}

	public void setCMS158(String cMS158) {
		CMS158 = cMS158;
	}

	public String getCMS159() {
		return CMS159;
	}

	public void setCMS159(String cMS159) {
		CMS159 = cMS159;
	}

	public String getCMS160() {
		return CMS160;
	}

	public void setCMS160(String cMS160) {
		CMS160 = cMS160;
	}

	public String getCMS161() {
		return CMS161;
	}

	public void setCMS161(String cMS161) {
		CMS161 = cMS161;
	}

	public String getCMS163() {
		return CMS163;
	}

	public void setCMS163(String cMS163) {
		CMS163 = cMS163;
	}

	public String getCMS164() {
		return CMS164;
	}

	public void setCMS164(String cMS164) {
		CMS164 = cMS164;
	}

	public String getCMS165() {
		return CMS165;
	}

	public void setCMS165(String cMS165) {
		CMS165 = cMS165;
	}

	public String getCMS166() {
		return CMS166;
	}

	public void setCMS166(String cMS166) {
		CMS166 = cMS166;
	}

	public String getCMS167() {
		return CMS167;
	}

	public void setCMS167(String cMS167) {
		CMS167 = cMS167;
	}

	public String getCMS169() {
		return CMS169;
	}

	public void setCMS169(String cMS169) {
		CMS169 = cMS169;
	}

	public String getCMS177() {
		return CMS177;
	}

	public void setCMS177(String cMS177) {
		CMS177 = cMS177;
	}

	public String getCMS179() {
		return CMS179;
	}

	public void setCMS179(String cMS179) {
		CMS179 = cMS179;
	}

	public String getCMS182() {
		return CMS182;
	}

	public void setCMS182(String cMS182) {
		CMS182 = cMS182;
	}

	public String getCMS22() {
		return CMS22;
	}

	public void setCMS22(String cMS22) {
		CMS22 = cMS22;
	}

	public String getCMS2() {
		return CMS2;
	}

	public void setCMS2(String cMS2) {
		CMS2 = cMS2;
	}

	public String getCMS50() {
		return CMS50;
	}

	public void setCMS50(String cMS50) {
		CMS50 = cMS50;
	}

	public String getCMS52() {
		return CMS52;
	}

	public void setCMS52(String cMS52) {
		CMS52 = cMS52;
	}

	public String getCMS56() {
		return CMS56;
	}

	public void setCMS56(String cMS56) {
		CMS56 = cMS56;
	}

	public String getCMS61() {
		return CMS61;
	}

	public void setCMS61(String cMS61) {
		CMS61 = cMS61;
	}

	public String getCMS62() {
		return CMS62;
	}

	public void setCMS62(String cMS62) {
		CMS62 = cMS62;
	}

	public String getCMS64() {
		return CMS64;
	}

	public void setCMS64(String cMS64) {
		CMS64 = cMS64;
	}

	public String getCMS65() {
		return CMS65;
	}

	public void setCMS65(String cMS65) {
		CMS65 = cMS65;
	}

	public String getCMS66() {
		return CMS66;
	}

	public void setCMS66(String cMS66) {
		CMS66 = cMS66;
	}

	public String getCMS68() {
		return CMS68;
	}

	public void setCMS68(String cMS68) {
		CMS68 = cMS68;
	}

	public String getCMS69() {
		return CMS69;
	}

	public void setCMS69(String cMS69) {
		CMS69 = cMS69;
	}

	public String getCMS74() {
		return CMS74;
	}

	public void setCMS74(String cMS74) {
		CMS74 = cMS74;
	}

	public String getCMS75() {
		return CMS75;
	}

	public void setCMS75(String cMS75) {
		CMS75 = cMS75;
	}

	public String getCMS77() {
		return CMS77;
	}

	public void setCMS77(String cMS77) {
		CMS77 = cMS77;
	}

	public String getCMS82() {
		return CMS82;
	}

	public void setCMS82(String cMS82) {
		CMS82 = cMS82;
	}

	public String getCMS90() {
		return CMS90;
	}

	public void setCMS90(String cMS90) {
		CMS90 = cMS90;
	}

	public String getCMS26() {
		return CMS26;
	}

	public void setCMS26(String cMS26) {
		CMS26 = cMS26;
	}

	public String getCMS102() {
		return CMS102;
	}

	public void setCMS102(String cMS102) {
		CMS102 = cMS102;
	}

	public String getCMS31() {
		return CMS31;
	}

	public void setCMS31(String cMS31) {
		CMS31 = cMS31;
	}

	public String getCMS100() {
		return CMS100;
	}

	public void setCMS100(String cMS100) {
		CMS100 = cMS100;
	}

	public String getCMS53() {
		return CMS53;
	}

	public void setCMS53(String cMS53) {
		CMS53 = cMS53;
	}

	public String getCMS60() {
		return CMS60;
	}

	public void setCMS60(String cMS60) {
		CMS60 = cMS60;
	}

	public String getCMS108() {
		return CMS108;
	}

	public void setCMS108(String cMS108) {
		CMS108 = cMS108;
	}

	public String getCMS190() {
		return CMS190;
	}

	public void setCMS190(String cMS190) {
		CMS190 = cMS190;
	}

	public String getCMS105() {
		return CMS105;
	}

	public void setCMS105(String cMS105) {
		CMS105 = cMS105;
	}

	public String getCMS73() {
		return CMS73;
	}

	public void setCMS73(String cMS73) {
		CMS73 = cMS73;
	}

	public String getCMS104() {
		return CMS104;
	}

	public void setCMS104(String cMS104) {
		CMS104 = cMS104;
	}

	public String getCMS72() {
		return CMS72;
	}

	public void setCMS72(String cMS72) {
		CMS72 = cMS72;
	}

	public String getCMS113() {
		return CMS113;
	}

	public void setCMS113(String cMS113) {
		CMS113 = cMS113;
	}

	public String getCMS9() {
		return CMS9;
	}

	public void setCMS9(String cMS9) {
		CMS9 = cMS9;
	}

	public String getCMS55() {
		return CMS55;
	}

	public void setCMS55(String cMS55) {
		CMS55 = cMS55;
	}

	public String getCMS111() {
		return CMS111;
	}

	public void setCMS111(String cMS111) {
		CMS111 = cMS111;
	}

	public String getCMS185() {
		return CMS185;
	}

	public void setCMS185(String cMS185) {
		CMS185 = cMS185;
	}

	public String getCMS107() {
		return CMS107;
	}

	public void setCMS107(String cMS107) {
		CMS107 = cMS107;
	}

	public String getCMS109() {
		return CMS109;
	}

	public void setCMS109(String cMS109) {
		CMS109 = cMS109;
	}

	public String getCMS110() {
		return CMS110;
	}

	public void setCMS110(String cMS110) {
		CMS110 = cMS110;
	}

	public String getCMS114() {
		return CMS114;
	}

	public void setCMS114(String cMS114) {
		CMS114 = cMS114;
	}

	public String getCMS71() {
		return CMS71;
	}

	public void setCMS71(String cMS71) {
		CMS71 = cMS71;
	}

	public String getCMS188() {
		return CMS188;
	}

	public void setCMS188(String cMS188) {
		CMS188 = cMS188;
	}

	public String getCMS91() {
		return CMS91;
	}

	public void setCMS91(String cMS91) {
		CMS91 = cMS91;
	}

	public String getCMS178() {
		return CMS178;
	}

	public void setCMS178(String cMS178) {
		CMS178 = cMS178;
	}

	public String getCMS32() {
		return CMS32;
	}

	public void setCMS32(String cMS32) {
		CMS32 = cMS32;
	}

	public String getCMS171() {
		return CMS171;
	}

	public void setCMS171(String cMS171) {
		CMS171 = cMS171;
	}

	public String getCMS172() {
		return CMS172;
	}

	public void setCMS172(String cMS172) {
		CMS172 = cMS172;
	}

	public String getCMS30() {
		return CMS30;
	}

	public void setCMS30(String cMS30) {
		CMS30 = cMS30;
	}

	public boolean isNQF_0001() {
		return NQF_0001;
	}

	public void setNQF_0001(boolean nQF_0001) {
		NQF_0001 = nQF_0001;
	}

	public boolean isNQF_0002() {
		return NQF_0002;
	}

	public void setNQF_0002(boolean nQF_0002) {
		NQF_0002 = nQF_0002;
	}

	public boolean isNQF_0004() {
		return NQF_0004;
	}

	public void setNQF_0004(boolean nQF_0004) {
		NQF_0004 = nQF_0004;
	}

	public boolean isNQF_0012() {
		return NQF_0012;
	}

	public void setNQF_0012(boolean nQF_0012) {
		NQF_0012 = nQF_0012;
	}

	public boolean isNQF_0013() {
		return NQF_0013;
	}

	public void setNQF_0013(boolean nQF_0013) {
		NQF_0013 = nQF_0013;
	}

	public boolean isNQF_0014() {
		return NQF_0014;
	}

	public void setNQF_0014(boolean nQF_0014) {
		NQF_0014 = nQF_0014;
	}

	public boolean isNQF_0018() {
		return NQF_0018;
	}

	public void setNQF_0018(boolean nQF_0018) {
		NQF_0018 = nQF_0018;
	}

	public boolean isNQF_0024() {
		return NQF_0024;
	}

	public void setNQF_0024(boolean nQF_0024) {
		NQF_0024 = nQF_0024;
	}

	public boolean isNQF_0027() {
		return NQF_0027;
	}

	public void setNQF_0027(boolean nQF_0027) {
		NQF_0027 = nQF_0027;
	}

	public boolean isNQF_0028() {
		return NQF_0028;
	}

	public void setNQF_0028(boolean nQF_0028) {
		NQF_0028 = nQF_0028;
	}

	public boolean isNQF_0031() {
		return NQF_0031;
	}

	public void setNQF_0031(boolean nQF_0031) {
		NQF_0031 = nQF_0031;
	}

	public boolean isNQF_0032() {
		return NQF_0032;
	}

	public void setNQF_0032(boolean nQF_0032) {
		NQF_0032 = nQF_0032;
	}

	public boolean isNQF_0033() {
		return NQF_0033;
	}

	public void setNQF_0033(boolean nQF_0033) {
		NQF_0033 = nQF_0033;
	}

	public boolean isNQF_0034() {
		return NQF_0034;
	}

	public void setNQF_0034(boolean nQF_0034) {
		NQF_0034 = nQF_0034;
	}

	public boolean isNQF_0036() {
		return NQF_0036;
	}

	public void setNQF_0036(boolean nQF_0036) {
		NQF_0036 = nQF_0036;
	}

	public boolean isNQF_0038() {
		return NQF_0038;
	}

	public void setNQF_0038(boolean nQF_0038) {
		NQF_0038 = nQF_0038;
	}

	public boolean isNQF_0041() {
		return NQF_0041;
	}

	public void setNQF_0041(boolean nQF_0041) {
		NQF_0041 = nQF_0041;
	}

	public boolean isNQF_0043() {
		return NQF_0043;
	}

	public void setNQF_0043(boolean nQF_0043) {
		NQF_0043 = nQF_0043;
	}

	public boolean isNQF_0047() {
		return NQF_0047;
	}

	public void setNQF_0047(boolean nQF_0047) {
		NQF_0047 = nQF_0047;
	}

	public boolean isNQF_0052() {
		return NQF_0052;
	}

	public void setNQF_0052(boolean nQF_0052) {
		NQF_0052 = nQF_0052;
	}

	public boolean isNQF_0055() {
		return NQF_0055;
	}

	public void setNQF_0055(boolean nQF_0055) {
		NQF_0055 = nQF_0055;
	}

	public boolean isNQF_0056() {
		return NQF_0056;
	}

	public void setNQF_0056(boolean nQF_0056) {
		NQF_0056 = nQF_0056;
	}

	public boolean isNQF_0059() {
		return NQF_0059;
	}

	public void setNQF_0059(boolean nQF_0059) {
		NQF_0059 = nQF_0059;
	}

	public boolean isNQF_0061() {
		return NQF_0061;
	}

	public void setNQF_0061(boolean nQF_0061) {
		NQF_0061 = nQF_0061;
	}

	public boolean isNQF_0062() {
		return NQF_0062;
	}

	public void setNQF_0062(boolean nQF_0062) {
		NQF_0062 = nQF_0062;
	}

	public boolean isNQF_0064() {
		return NQF_0064;
	}

	public void setNQF_0064(boolean nQF_0064) {
		NQF_0064 = nQF_0064;
	}

	public boolean isNQF_0067() {
		return NQF_0067;
	}

	public void setNQF_0067(boolean nQF_0067) {
		NQF_0067 = nQF_0067;
	}

	public boolean isNQF_0068() {
		return NQF_0068;
	}

	public void setNQF_0068(boolean nQF_0068) {
		NQF_0068 = nQF_0068;
	}

	public boolean isNQF_0070() {
		return NQF_0070;
	}

	public void setNQF_0070(boolean nQF_0070) {
		NQF_0070 = nQF_0070;
	}

	public boolean isNQF_0073() {
		return NQF_0073;
	}

	public void setNQF_0073(boolean nQF_0073) {
		NQF_0073 = nQF_0073;
	}

	public boolean isNQF_0074() {
		return NQF_0074;
	}

	public void setNQF_0074(boolean nQF_0074) {
		NQF_0074 = nQF_0074;
	}

	public boolean isNQF_0075() {
		return NQF_0075;
	}

	public void setNQF_0075(boolean nQF_0075) {
		NQF_0075 = nQF_0075;
	}

	public boolean isNQF_0081() {
		return NQF_0081;
	}

	public void setNQF_0081(boolean nQF_0081) {
		NQF_0081 = nQF_0081;
	}

	public boolean isNQF_0083() {
		return NQF_0083;
	}

	public void setNQF_0083(boolean nQF_0083) {
		NQF_0083 = nQF_0083;
	}

	public boolean isNQF_0084() {
		return NQF_0084;
	}

	public void setNQF_0084(boolean nQF_0084) {
		NQF_0084 = nQF_0084;
	}

	public boolean isNQF_0086() {
		return NQF_0086;
	}

	public void setNQF_0086(boolean nQF_0086) {
		NQF_0086 = nQF_0086;
	}

	public boolean isNQF_0088() {
		return NQF_0088;
	}

	public void setNQF_0088(boolean nQF_0088) {
		NQF_0088 = nQF_0088;
	}

	public boolean isNQF_0089() {
		return NQF_0089;
	}

	public void setNQF_0089(boolean nQF_0089) {
		NQF_0089 = nQF_0089;
	}

	public boolean isNQF_0105() {
		return NQF_0105;
	}

	public void setNQF_0105(boolean nQF_0105) {
		NQF_0105 = nQF_0105;
	}

	public boolean isNQF_0371() {
		return NQF_0371;
	}

	public void setNQF_0371(boolean nQF_0371) {
		NQF_0371 = nQF_0371;
	}

	public boolean isNQF_0372() {
		return NQF_0372;
	}

	public void setNQF_0372(boolean nQF_0372) {
		NQF_0372 = nQF_0372;
	}

	public boolean isNQF_0373() {
		return NQF_0373;
	}

	public void setNQF_0373(boolean nQF_0373) {
		NQF_0373 = nQF_0373;
	}

	public boolean isNQF_0374() {
		return NQF_0374;
	}

	public void setNQF_0374(boolean nQF_0374) {
		NQF_0374 = nQF_0374;
	}

	public boolean isNQF_0375() {
		return NQF_0375;
	}

	public void setNQF_0375(boolean nQF_0375) {
		NQF_0375 = nQF_0375;
	}

	public boolean isNQF_0376() {
		return NQF_0376;
	}

	public void setNQF_0376(boolean nQF_0376) {
		NQF_0376 = nQF_0376;
	}

	public boolean isNQF_0385() {
		return NQF_0385;
	}

	public void setNQF_0385(boolean nQF_0385) {
		NQF_0385 = nQF_0385;
	}

	public boolean isNQF_0387() {
		return NQF_0387;
	}

	public void setNQF_0387(boolean nQF_0387) {
		NQF_0387 = nQF_0387;
	}

	public boolean isNQF_0389() {
		return NQF_0389;
	}

	public void setNQF_0389(boolean nQF_0389) {
		NQF_0389 = nQF_0389;
	}

	public boolean isNQF_0421() {
		return NQF_0421;
	}

	public void setNQF_0421(boolean nQF_0421) {
		NQF_0421 = nQF_0421;
	}

	public boolean isNQF_0435() {
		return NQF_0435;
	}

	public void setNQF_0435(boolean nQF_0435) {
		NQF_0435 = nQF_0435;
	}

	public boolean isNQF_0436() {
		return NQF_0436;
	}

	public void setNQF_0436(boolean nQF_0436) {
		NQF_0436 = nQF_0436;
	}

	public boolean isNQF_0437() {
		return NQF_0437;
	}

	public void setNQF_0437(boolean nQF_0437) {
		NQF_0437 = nQF_0437;
	}

	public boolean isNQF_0438() {
		return NQF_0438;
	}

	public void setNQF_0438(boolean nQF_0438) {
		NQF_0438 = nQF_0438;
	}

	public boolean isNQF_0439() {
		return NQF_0439;
	}

	public void setNQF_0439(boolean nQF_0439) {
		NQF_0439 = nQF_0439;
	}

	public boolean isNQF_0440() {
		return NQF_0440;
	}

	public void setNQF_0440(boolean nQF_0440) {
		NQF_0440 = nQF_0440;
	}

	public boolean isNQF_0441() {
		return NQF_0441;
	}

	public void setNQF_0441(boolean nQF_0441) {
		NQF_0441 = nQF_0441;
	}

	public boolean isNQF_0495() {
		return NQF_0495;
	}

	public void setNQF_0495(boolean nQF_0495) {
		NQF_0495 = nQF_0495;
	}

	public boolean isNQF_0497() {
		return NQF_0497;
	}

	public void setNQF_0497(boolean nQF_0497) {
		NQF_0497 = nQF_0497;
	}

	public boolean isNQF_0575() {
		return NQF_0575;
	}

	public void setNQF_0575(boolean nQF_0575) {
		NQF_0575 = nQF_0575;
	}
	
	public void setCertificationSuccess(String certNumber, boolean success) {
		String methodName = certNumber;
		methodName = methodName.replace(".", "_");
		methodName = methodName.replace("(", "_");
		methodName = methodName.replace(")", "_");
		methodName = methodName.replace(" ", "_");
		methodName = methodName.substring(0, methodName.length()-1);
		methodName = "setCERT_" + methodName;
		
		Method method = null;
		try {
			  method = this.getClass().getMethod(methodName, boolean.class);
		} catch (SecurityException e) {
			logger.error("Security exception calling method " + methodName, e);
		} catch (NoSuchMethodException e) {
			logger.error("No such method " + methodName, e);
		}
		
		if(method != null) {
			try {
				  method.invoke(this, success);
			} catch (IllegalArgumentException e) {
				logger.error("IllegalArgumentException " + methodName, e);
			} catch (IllegalAccessException e) {
				logger.error("IllegalAccessException " + methodName, e);
			} catch (InvocationTargetException e) {
				logger.error("InvocationTargetException " + methodName, e);
			}
		}	
	}
	
	public void setNqfSuccess(String nqfNumber, boolean success) {
		String methodName = "setNQF_" + nqfNumber;
		Method method = null;
		try {
			  method = this.getClass().getMethod(methodName, boolean.class);
		} catch (SecurityException e) {
			logger.error("Security exception calling method " + methodName, e);
		} catch (NoSuchMethodException e) {
			logger.error("No such method " + methodName, e);
		}
		
		if(method != null) {
			try {
				  method.invoke(this, success);
			} catch (IllegalArgumentException e) {
				logger.error("IllegalArgumentException " + methodName, e);
			} catch (IllegalAccessException e) {
				logger.error("IllegalAccessException " + methodName, e);
			} catch (InvocationTargetException e) {
				logger.error("InvocationTargetException " + methodName, e);
			}
		}	
	}
	
	public void addCmsVersion(String cmsId, String versionToAdd) {
		String currVersions = "";
		String methodName = "get" + cmsId;
		Method method = null;
		try {
			  method = this.getClass().getMethod(methodName);
		} catch (SecurityException e) {
			logger.error("Security exception calling method " + methodName, e);
		} catch (NoSuchMethodException e) {
			logger.error("No such method " + methodName, e);
		}
		
		if(method != null) {
			try {
				 Object methodResult = method.invoke(this);
				 currVersions = methodResult.toString();
			} catch (IllegalArgumentException e) {
				logger.error("IllegalArgumentException " + methodName, e);
			} catch (IllegalAccessException e) {
				logger.error("IllegalAccessException " + methodName, e);
			} catch (InvocationTargetException e) {
				logger.error("InvocationTargetException " + methodName, e);
			}
		}
		
		String newVersions = versionToAdd;
		if(currVersions != null && !StringUtils.isEmpty(currVersions.trim())) {
			newVersions = currVersions + "," + versionToAdd;
		} 
		
		methodName = "set" + cmsId;
		method = null;
		try {
			  method = this.getClass().getMethod(methodName, String.class);
		} catch (SecurityException e) {
			logger.error("Security exception calling method " + methodName, e);
		} catch (NoSuchMethodException e) {
			logger.error("No such method " + methodName, e);
		}
		
		if(method != null) {
			try {
				 method.invoke(this, newVersions);
			} catch (IllegalArgumentException e) {
				logger.error("IllegalArgumentException " + methodName, e);
			} catch (IllegalAccessException e) {
				logger.error("IllegalAccessException " + methodName, e);
			} catch (InvocationTargetException e) {
				logger.error("InvocationTargetException " + methodName, e);
			}
		}
	}

	public String getTermsOfUse() {
		return termsOfUse;
	}

	public void setTermsOfUse(String termsOfUse) {
		this.termsOfUse = termsOfUse;
	}

	public String getApiDocumentation() {
		return apiDocumentation;
	}

	public void setApiDocumentation(String apiDocumentation) {
		this.apiDocumentation = apiDocumentation;
	}

	public Boolean getTransparencyAttestation() {
		return transparencyAttestation;
	}

	public void setTransparencyAttestation(Boolean transparencyAttestation) {
		this.transparencyAttestation = transparencyAttestation;
	}

	public String getTestingLabName() {
		return testingLabName;
	}

	public void setTestingLabName(String testingLabName) {
		this.testingLabName = testingLabName;
	}

	public String getIcs() {
		return ics;
	}

	public void setIcs(String ics) {
		this.ics = ics;
	}

	public Boolean getSedTesting() {
		return sedTesting;
	}

	public void setSedTesting(Boolean sedTesting) {
		this.sedTesting = sedTesting;
	}

	public Boolean getQmsTesting() {
		return qmsTesting;
	}

	public void setQmsTesting(Boolean qmsTesting) {
		this.qmsTesting = qmsTesting;
	}
}