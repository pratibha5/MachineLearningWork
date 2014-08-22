package data.dataset;

public class ValueNotFoundException extends Exception {

	/**
	 * @param SeavalSearchrch
	 *            The Value to find
	 * @param Attribute
	 *            The attribute being looked in
	 */
	public ValueNotFoundException(String searchVal, String Attribute) {
		super(searchVal + " is not found in Attribute " + Attribute);
	}

	public ValueNotFoundException(double val, String Attribute) {
		super(val + " is not found in Attribute " + Attribute);
	}
}
