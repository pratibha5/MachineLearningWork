package data.discretize.supervised;

import data.discretize.Discretizer;

/**
 * @author Jonathan
 */
public class FayyadIraniEntropy extends Discretizer {
	private int numClasses;
	private int maxNumBins;
	private int numBinsLeft;

	/*
	 * (non-Javadoc)
	 * 
	 * @see preprocess.math.DiscretizeMethods.DiscretizeMethod#discretize(double[],
	 *      int[], int, int)
	 */
	@Override
	public double[] discretize(double[] values, double[] classCounts,
			int numClass, int numBins) {
		numClasses = numClass;
		maxNumBins = numBins;
		numBinsLeft = maxNumBins;

		return runMDL(values, classCounts, numBins);
	}

	private double[] runMDL(double[] attVals, double[] classCounts, int binsLeft) {
		double[] newCut = null, initCount, left, right;
		double[][] counts, bestCount;
		double priorEntrop, currCutPt = -Double.MAX_VALUE, bestCutPt = -1, currEntrop = Double.MAX_VALUE, bestEntrop = 0;
		int bestI = -1, numCutPts = 0;
		if (numBinsLeft == 0)
			return newCut;
		if (classCounts.length == 0)
			return newCut;

		// Init counts
		counts = new double[2][numClasses];
		for (int i = 0; i < classCounts.length; i++)
			counts[1][(int) classCounts[i]] += 1;

		initCount = new double[numClasses];
		System.arraycopy(counts[1], 0, initCount, 0, counts[1].length);

		if (examSameClass(initCount))
			return newCut;
		else {
			priorEntrop = util.MathUtil.entropySet(initCount);
			bestEntrop = priorEntrop;
			// System.out.println("\n\nPrior Entropy is: "+priorEntrop);
			bestCount = new double[2][numClasses];
			for (int i = 0; i < classCounts.length - 1; i++) {
				int iClassVal = (int) classCounts[i];
				int i1ClassVal = (int) classCounts[i + 1];
				counts[0][(int) classCounts[i]] += 1;
				counts[1][(int) classCounts[i]] -= 1;
				if (attVals[i] < attVals[i + 1]) {
					numCutPts++;
					// Need to fix this using the fact that PossCut now has both
					// the cut point and the far side of the Cut pt encoded into
					// it

					if (iClassVal != i1ClassVal) {
						currCutPt = (attVals[i] + attVals[i + 1]) / 2.0;
						currEntrop = util.MathUtil.entropySets(counts);
					}
					if (currEntrop < bestEntrop) {
						bestCutPt = currCutPt;
						bestEntrop = currEntrop;
						bestI = i;
						System.arraycopy(counts[0], 0, bestCount[0], 0,
								numClasses);
						System.arraycopy(counts[1], 0, bestCount[1], 0,
								numClasses);
					}
				}
			}

			// System.out.println("Entropy of group: "+cutEntrop+"mdlR of:
			// "+mdlR);
			if (!FayaadIranniMDL(bestCount, initCount, numCutPts)
					|| (priorEntrop - bestEntrop <= 0))
				return newCut;
			else {
				left = runMDL(util.Arrays.subArray(attVals, 0, bestI + 1),
						util.Arrays.subArray(classCounts, 0, bestI + 1),
						binsLeft);
				if (left != null)
					numBinsLeft = maxNumBins - left.length;
				right = runMDL(util.Arrays.subArray(attVals, bestI + 1,
						attVals.length), util.Arrays.subArray(classCounts,
						bestI + 1, classCounts.length), binsLeft);
				if (numBinsLeft == 0) {
					if (left == null && right == null)
						return newCut;
					else if (left == null)
						return right;
					else if (right == null)
						return left;
					else {
						newCut = new double[left.length + right.length];
						System.arraycopy(left, 0, newCut, 0, left.length);
						System.arraycopy(right, 0, newCut, left.length,
								right.length);
						return newCut;
					}
				}
				if (left == null && right == null) {
					newCut = new double[1];
					newCut[0] = bestCutPt;
				} else if (left == null) {
					newCut = new double[right.length + 1];
					System.arraycopy(right, 0, newCut, 1, right.length);
					newCut[0] = bestCutPt;
				} else if (right == null) {
					newCut = new double[left.length + 1];
					System.arraycopy(left, 0, newCut, 0, left.length);
					newCut[left.length] = bestCutPt;
				} else {
					newCut = new double[left.length + right.length + 1];
					System.arraycopy(left, 0, newCut, 0, left.length);
					System.arraycopy(right, 0, newCut, 1 + left.length,
							right.length);
					newCut[left.length] = bestCutPt;
				}
				return newCut;
			}
		}
	}

	private double delta(double[][] m_counts, double[] initCount,
			double priorEnt) {
		double nTotalClass = 0;
		for (int i = 0; i < initCount.length; i++) {
			if (initCount[i] != 0)
				nTotalClass++;
		}
		double nClassLeft = 0;
		double nClassRight = 0;
		for (int j = 0; j < m_counts[0].length; j++) {
			if (m_counts[0][j] != 0)
				nClassLeft++;
		}
		for (int c = 0; c < m_counts[1].length; c++) {
			if (m_counts[1][c] != 0)
				nClassRight++;
		}
		double LeftEntrop = util.MathUtil.entropySet(m_counts[0]);
		double RightEntrop = util.MathUtil.entropySet(m_counts[1]);

		return util.MathUtil.log2(Math.pow(3, nTotalClass) - 2)
				- ((nTotalClass * priorEnt) - (nClassLeft * LeftEntrop) - (nClassRight * RightEntrop));
	}

	private boolean FayaadIranniMDL(double[][] m_counts, double[] initCount,
			int numCutPts) {
		double priorEnt = util.MathUtil.entropySet(initCount);
		double currEnt = util.MathUtil.entropySets(m_counts);
		double IGain = priorEnt - currEnt;

		return (IGain > (util.MathUtil.log2(numCutPts) + delta(m_counts,
				initCount, priorEnt))
				/ (util.MathUtil.sumArray(initCount)));
	}

	private boolean examSameClass(double[] iCount) {
		boolean singleClass = false;
		for (int i = 0; i < iCount.length; i++) {
			if (singleClass && iCount[i] != 0)
				return false;
			if (!singleClass && iCount[i] != 0)
				singleClass = true;
		}
		return singleClass;
	}
}
