package org.probe.data.dataset;

import java.util.Set;

public class AttributeDoesNotExistException extends Exception {

	public AttributeDoesNotExistException(int att, int total) {
		super("There is no attribute at position " + att + ", there are only "
				+ total + " of attributes.");
	}

	public AttributeDoesNotExistException(String attN) {
		super("There is no attribute with the name " + attN);
	}
}
