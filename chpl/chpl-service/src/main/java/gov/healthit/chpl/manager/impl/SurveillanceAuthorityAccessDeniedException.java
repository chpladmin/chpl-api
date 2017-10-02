package gov.healthit.chpl.manager.impl;

public class SurveillanceAuthorityAccessDeniedException extends Exception {
	private static final long serialVersionUID = 1L;

	public SurveillanceAuthorityAccessDeniedException() { super(); }
	public SurveillanceAuthorityAccessDeniedException(String message) { super(message); }
	public SurveillanceAuthorityAccessDeniedException(String message, Throwable cause) { super(message, cause); }
	public SurveillanceAuthorityAccessDeniedException(Throwable cause) { super(cause); }
}