package org.probe.data.discretize.unsupervised;

import org.probe.data.discretize.Discretizer;

public class EqualFrequency extends Discretizer {

	public EqualFrequency() {
	}

	@Override
	public double[] discretize(double[] values, double[] classCounts,
			int numClass, int numBins) {
		int binCount = values.length / numBins;
		double[] bins = new double[numBins - 1];
		for (int i = 0; i < bins.length; i++)
			bins[i] = (values[binCount * (i + 1) - 1] + values[binCount
					* (i + 1)]) / 2.0;
		return bins;
	}

}
