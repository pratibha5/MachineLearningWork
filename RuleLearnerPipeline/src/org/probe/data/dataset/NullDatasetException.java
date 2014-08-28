package org.probe.data.dataset;

public class NullDatasetException extends Exception {

	public NullDatasetException() {
		super("Request results in a null dataset or an empty dataset.");
	}

}
