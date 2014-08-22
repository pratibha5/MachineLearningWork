package org.probe.data.discretize.supervised;

import org.probe.data.discretize.Discretizer;

public class ErrorBased extends Discretizer {

	private int numClasses;
	private int maxNumBins;
	private int numBinsLeft;
	private double paramMeth;

	public ErrorBased() {
		paramMeth = .33;
		maxNumBins = 5;
	}

	public ErrorBased(double param) {
		paramMeth = param;
		maxNumBins = 5;
	}

	@Override
	public double[] discretize(double[] values, double[] classCounts,
			int numClass, int numBins) {
		numClasses = numClass;
		maxNumBins = numBins;
		numBinsLeft = maxNumBins;
		return runError(values, classCounts, numBins);
	}

	private boolean errorMDL(double[][] m_counts) {
		double sumS1 = org.probe.util.MathUtil.sumArray(m_counts[0]);
		double sumS2 = org.probe.util.MathUtil.sumArray(m_counts[1]);
		return (Math.min(m_counts[0][0] / sumS1, m_counts[0][1] / sumS1)
				+ Math.min(m_counts[1][0] / sumS2, m_counts[1][1] / sumS2) > paramMeth);
	}

	private double[] runError(double[] attVals, double[] classCounts,
			int binsLeft) {
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
			priorEntrop = org.probe.util.MathUtil.entropySet(initCount);
			bestEntrop = priorEntrop;
			// System.out.println("\n\nPrior entropy is: " + priorEntrop);
			bestCount = new double[2][numClasses];
			for (int i = 0; i < classCounts.length - 1; i++) {
				int iClassVal = (int) classCounts[i];
				int i1ClassVal = (int) classCounts[i + 1];
				counts[0][(int) classCounts[i]] += 1;
				counts[1][(int) classCounts[i]] -= 1;
				if (attVals[i] < attVals[i + 1]) {
					numCutPts++;
					// Need to fix this using the fact that PossCut now encodes both
					// the cut point and the far side of the cut point.

					if (iClassVal != i1ClassVal) {
						currCutPt = (attVals[i] + attVals[i + 1]) / 2.0;
						currEntrop = org.probe.util.MathUtil.entropySets(counts);
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

			// System.out.println("Entropy of group: " + cutEntrop + "mdlR of:
			// " + mdlR);
			if (errorMDL(bestCount))
				return newCut;
			else {
				left = runError(org.probe.util.Arrays.subArray(attVals, 0, bestI + 1),
						org.probe.util.Arrays.subArray(classCounts, 0, bestI + 1),
						binsLeft);
				if (left != null)
					numBinsLeft = maxNumBins - left.length;
				right = runError(org.probe.util.Arrays.subArray(attVals, bestI + 1,
						attVals.length), org.probe.util.Arrays.subArray(classCounts,
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
						System.arraycopy(right, 0, newCut, left.length, right.length);
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
