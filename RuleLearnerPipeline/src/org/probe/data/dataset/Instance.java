package org.probe.data.dataset;

import org.probe.data.DataModel;


public class Instance {
	private String sampleName;

	private double[] values;

	private DataModel refDataset;

	private boolean covered;

	public Instance(String sn, double[] vals) {
		sampleName = sn;
		values = vals;
		covered = false;
	}

	public Instance(String sn, double[] vals, boolean cov) {
		sampleName = sn;
		values = vals;
		covered = cov;
	}

	public double classValue() {
		return values[refDataset.classAttIndex()];
	}

	public Attribute attribute(int a) throws AttributeDoesNotExistException {
		return refDataset.attribute(a);
	}

	public int classIndex() {
		return refDataset.classAttIndex();
	}
}
