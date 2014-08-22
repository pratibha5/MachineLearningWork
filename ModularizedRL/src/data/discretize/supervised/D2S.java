package data.discretize.supervised;

import data.discretize.Discretizer;
import data.dataset.*;

/**
 * Implements D2 supervised discretization which is an extension of ID3. It 
 * was proposed by Catlett, 1991.
 * 
 * @author Jonathan Lustgarten
 */
public class D2S extends Discretizer {

	private int numBinsLeft;
	private int maxBins;
	private int minNumInstances;

	public D2S() {
		maxBins = numBinsLeft = 8;
		minNumInstances = 14;
	}

	public D2S(Dataset d) {
		maxBins = numBinsLeft = 8;
		minNumInstances = ((d.numInstances() / 10 < 14) ? (d.numInstances() / 10) : 14);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see preprocess.math.DiscretizeMethods.DiscretizeMethod#discretize(double[],
	 *      int[], int, int)
	 */
	@Override
	public double[] discretize(double[] values, double[] classCounts,
			int numClass, int numBins) {
		// TODO Auto-generated method stub
		return null;
	}

	private double[] runD2(double[] vals, double[] classIs, int[] classCounts) {
		if (vals.length <= minNumInstances || numBinsLeft == 0)
			return null;
		if (sameClass(classCounts))
			return null;
		int ind = -1;
		double minEnt = Double.POSITIVE_INFINITY;
		boolean igSame = true; // Information gain across all splits same
		int[][] currSplit = util.Arrays.init(
				new int[2][classCounts.length], 0);
		int[][] bestSplit = util.Arrays.init(
				new int[2][classCounts.length], 0);
		System.arraycopy(classCounts, 0, currSplit[1], 0, classCounts.length);
		for (int i = 1; i < vals.length - 1; i++) {
			currSplit[0][(int) classIs[i]] += 1;
			currSplit[1][(int) classIs[i]] += 1;
			double currEnt = util.MathUtil.entropy(currSplit);
			if (currEnt < minEnt) {
				ind = i;
				minEnt = currEnt;
				bestSplit = currSplit;
			}
			if (i > 1 && (currEnt != minEnt))
				igSame = false;
		}
		if (igSame)
			return null;

		double[] bins = new double[1];
		bins[0] = (vals[ind] + vals[ind + 1]) / 2.0;
		numBinsLeft -= 1;
		if (numBinsLeft == 0)
			return bins;

		double[] leftSplit, rightSplit;
		// Split Values (first Left then Right)
		// Left
		double[] leftVals = new double[ind + 1];
		double[] leftCIs = new double[ind + 1];
		System.arraycopy(vals, 0, leftVals, 0, leftVals.length);
		System.arraycopy(classIs, 0, leftCIs, 0, leftVals.length);
		// Right
		double[] rightVals = new double[vals.length - leftVals.length];
		double[] rightCIs = new double[vals.length - leftVals.length];
		System.arraycopy(vals, leftVals.length, rightVals, 0, rightVals.length);
		System.arraycopy(classIs, leftVals.length, rightCIs, 0,
				rightVals.length);

		leftSplit = runD2(leftVals, leftCIs, bestSplit[0]);
		if (leftSplit.length + 1 >= maxBins) {
			double[] nsplits = util.Arrays.concatenate(bins, leftSplit);
			if (nsplits.length == maxBins)
				return nsplits;
			double[] msplits = new double[maxBins];
			System.arraycopy(nsplits, 0, msplits, 0, maxBins);
			return msplits;
		}
		bins = util.Arrays.concatenate(bins, leftSplit);
		rightSplit = runD2(rightVals, rightCIs, bestSplit[1]);
		if (rightSplit.length + bins.length >= maxBins) {
			double[] nsplits = util.Arrays.concatenate(bins, rightSplit);
			if (nsplits.length == maxBins)
				return nsplits;
			double[] msplits = new double[maxBins];
			System.arraycopy(nsplits, 0, msplits, 0, maxBins);
			return msplits;
		}
		bins = util.Arrays.concatenate(bins, rightSplit);
		return bins;
	}

	private boolean sameClass(int[] count) {
		boolean same = true;
		boolean gt0 = false; // Greater-than-0 indicator
		for (int i = 0; i < count.length - 1; i++) {
			if (count[i] > 0)
				gt0 = true;
			if (gt0 && count[i + 1] > 0) {
				same = false;
				break;
			}
		}
		return same;
	}
}
