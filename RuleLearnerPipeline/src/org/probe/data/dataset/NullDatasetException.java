package org.probe.data.dataset;

public class NullDataModelException extends Exception {

	public NullDataModelException() {
		super("Request results in a null dataset or an empty dataset.");
	}

}
