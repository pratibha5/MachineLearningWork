package org.probe.data;


public class Instance {
	private String sampleName;

	private double[] values;

	private DataModel refDataModel;

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
		return values[refDataModel.classAttIndex()];
	}

	public Attribute attribute(int a) throws AttributeDoesNotExistException {
		return refDataModel.attribute(a);
	}

	public int classIndex() {
		return refDataModel.classAttIndex();
	}
}
