package org.ohdsi.webapi.report;

public class CDMAttribute {
	private String attributeName;
	private String attributeValue;
        private String attributeStatus;
        private String attributeComments;
	
	/**
	 * @return the attributeName
	 */
	public String getAttributeName() {
		return attributeName;
	}
	/**
	 * @param attributeName the attributeName to set
	 */
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	/**
	 * @return the attributeValue
	 */
	public String getAttributeValue() {
		return attributeValue;
	}
	/**
	 * @param attributeValue the attributeValue to set
	 */
	public void setAttributeValue(String attributeValue) {
		this.attributeValue = attributeValue;
	}
        /**
	 * @return the attributeStatus
	 */
	public String getAttributeStatus() {
		return attributeStatus;
	}
	/**
	 * @param attributeStatus the attributeStatus to set
	 */
	public void setAttributeStatus(String attributeStatus) {
		this.attributeStatus = attributeStatus;
	}
	/**
	 * @return the attributeComments
	 */
	public String getAttributeComments() {
		return attributeComments;
	}
	/**
	 * @param attributeComments the attributeComments to set
	 */
	public void setAttributeComments(String attributeComments) {
		this.attributeComments = attributeComments;
	}
	
}
