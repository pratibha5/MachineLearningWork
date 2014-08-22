package data.discretize.supervised;

import data.discretize.Discretizer;
import util.Arrays;
import util.MathUtil;

/**
 * Adapted from Holte 1993 1R Discretization Scheme
 */

public class OneR extends Discretizer {

	private int minNumInstances;

	public OneR() {
		minNumInstances = 6;
	}

	public OneR(int mNI) {
		minNumInstances = mNI;
	}

	@Override
	public double[] discretize(double[] values, double[] classCounts,
			int numClass, int numBins) {
		int[][] counts = Arrays.init(new int[values.length
				/ minNumInstances][numClass + 1], 0);
		int num = 0;
		int bin = 0;
		for (int i = 0; i < classCounts.length; i++) {
			if (num >= minNumInstances) {
				boolean oneMaxClass = false;
				int max = 0;
				for (int j = 1; j < counts[bin].length - 1; j++) {
					if (counts[bin][j - 1] != counts[bin][j - 1]) {
						oneMaxClass = true;
						if (counts[bin][j - 1] > counts[bin][j - 1])
							max = j - 1;
						else
							max = j;
					}
				}
				if (oneMaxClass) {
					counts[bin][counts[bin].length - 1] = max;
					num = 0;
					bin += 1;
				}
			}
			if (bin >= counts.length)
				bin = counts.length - 1;
			counts[bin][(int) classCounts[i]] += 1;
			num++;
		}
		int lenCount = counts[counts.length - 1].length - 1;
		do {
			int ind1 = -1;
			int ind2 = -2;
			for (int c = 1; c < counts.length; c++) {
				if (counts[c - 1][lenCount] == counts[c][lenCount]) {
					ind1 = c - 1;
					ind2 = c;
				}
			}
			if (ind1 == -1 || ind1 > ind2)
				break;

			int[][] nc = new int[counts.length - 1][counts[counts.length - 1].length];
			int ni = 0;
			for (int r = 0; r < counts.length; r++) {
				for (int rc = 0; rc < counts[r].length - 1; r++)
					nc[ni][rc] += counts[r][rc];
				nc[ni][nc[ni].length - 1] = counts[r][lenCount];
				if (r != ind1)
					ni++;
			}
		} while (counts.length > 1);
		if (counts.length == 1)
			return new double[0];
		double[] bins = new double[0];
		int currInd = 0;
		for (int c = 0; c < counts.length; c++) {
			double sum = MathUtil.sumArray(counts[c]) - counts[c][lenCount];
			currInd += sum;
			Arrays.append(bins,
					(values[currInd] + values[currInd - 1]) / 2.0);
		}
		return MathUtil.sort(bins);
	}

}
