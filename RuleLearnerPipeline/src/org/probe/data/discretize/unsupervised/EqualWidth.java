package org.probe.data.discretize.unsupervised;

import org.probe.data.discretize.Discretizer;

public class EqualWidth extends Discretizer {

	public EqualWidth() {

	}

	@Override
	public double[] discretize(double[] values, double[] classCounts,
			int numClass, int numBins) {
		double binWidth = (Math.abs(values[0]) + Math
				.abs(values[values.length - 1]))
				/ (double) numBins;
		double[] bins = new double[numBins - 1];
		for (int i = 0; i < bins.length; i++)
			bins[i] = values[0] + binWidth * (i + 1);
		return bins;
	}
}
