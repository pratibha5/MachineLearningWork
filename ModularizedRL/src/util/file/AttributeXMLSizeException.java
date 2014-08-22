package util.file;

public class AttributeXMLSizeException extends Exception {

	public AttributeXMLSizeException(String from, int expt, int obs) {
		super("Malformed XML file. Expected " + expt + " acutally had " + obs
				+ " with respect to the " + from + " tag");
	}

}
