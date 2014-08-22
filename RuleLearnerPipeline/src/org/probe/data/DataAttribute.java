package org.probe.data;

public class DataAttribute {

	public DataAttribute(int attributeIndex, String attributeName) {
		this.attributeIndex = attributeIndex;
		this.attributeName = attributeName;
	}

	public String getAttributeName() {
		return attributeName;
	}
	
	public int getAttributeIndex() {
		return attributeIndex;
	}
	
	public boolean isClass() {
		return isClass;
	}
	
	public boolean isInstance() {
		return isInstance;
	}
	
	public void setIsInstance(boolean isInstance) {
		this.isInstance = isInstance;
	}

	public void setIsClass(boolean isClass) {
		this.isClass = isClass;
	} 
	
	public boolean isNominal() {
		return isNominal;
	}

	public void setNominal(boolean isNominal) {
		this.isNominal = isNominal;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + attributeIndex;
		result = prime * result
				+ ((attributeName == null) ? 0 : attributeName.hashCode());
		result = prime * result + (isClass ? 1231 : 1237);
		result = prime * result + (isInstance ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataAttribute other = (DataAttribute) obj;
		return this.attributeIndex == other.attributeIndex;
	}

	private final int attributeIndex;
	private final String attributeName;
	private boolean isInstance = false;
	private boolean isClass = false;
	private boolean isNominal = false;
}
