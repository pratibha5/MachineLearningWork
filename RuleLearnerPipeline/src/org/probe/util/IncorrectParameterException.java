package org.probe.util;

public class IncorrectParameterException extends Exception {
	//private String algorithm;
	private String parameter;
	private String enteredValue;
	private String validValues;
	//private String defaultValue;
	
	/**
	 * @param algorithm
	 * @param parameter
	 * @param validValues
	 * @param defaultValue
	 */
	public IncorrectParameterException(String parameter,
			String enteredValue, String validValues) {
		super();
		//this.algorithm = algorithm;
		this.parameter = parameter;
		this.enteredValue = enteredValue;
		this.validValues = validValues;
		//this.defaultValue = defaultValue;
	}

	public IncorrectParameterException(String message,
			String parameter, String enteredValue, String validValues) {
		super(message);
		//this.algorithm = algorithm;
		this.parameter = parameter;
		this.enteredValue = enteredValue;
		this.validValues = validValues;
		//this.defaultValue = defaultValue;
		// TODO Auto-generated constructor stub
	}

	public IncorrectParameterException(Throwable cause,
			String parameter, String enteredValue, String validValues) {
		super(cause);
		//this.algorithm = algorithm;
		this.parameter = parameter;
		this.enteredValue = enteredValue;
		this.validValues = validValues;
		//this.defaultValue = defaultValue;
	}

	public IncorrectParameterException(String message, Throwable cause,
			String parameter, String enteredValue,
			String validValues) {
		super(message, cause);
		//this.algorithm = algorithm;
		this.parameter = parameter;
		this.enteredValue = enteredValue;
		this.validValues = validValues;
		//this.setDefault = defaultValue;
	}

	public String getLocalizedMessage() {
		if (getMessage() != null)
			return getMessage();
		StringBuffer sb = new StringBuffer();
		//sb.append("Error occured using algorithm: " + algorithm + "\n");
		sb.append("Parameter: '" + parameter + "'. ");
		sb.append("entered value: '" + enteredValue + "'. ");
		sb.append("valid values: " + validValues + "\n");
		//sb.append("Using default value: " + setDefault + "\n");
		return sb.toString();
	}
}