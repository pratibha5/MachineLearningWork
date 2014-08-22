package data.discretize.unsupervised;

import data.discretize.Discretizer;
import util.MathUtil;

public class Gaussian extends Discretizer {

	public Gaussian() {

	}

	@Override
	public double[] discretize(double[] values, double[] classCounts,
			int numClass, int numBins) {
		double[] mmasd = MathUtil.getMinMaxMeanSD(values);
		double[] bins = new double[4];
		bins[0] = mmasd[2] - 2.0 * mmasd[3];
		bins[1] = mmasd[2] - 1.0 * mmasd[3];
		bins[2] = mmasd[2] + 1.0 * mmasd[3];
		bins[3] = mmasd[2] + 2.0 * mmasd[3];
		return bins;
	}

}
