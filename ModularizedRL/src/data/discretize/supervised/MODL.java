package data.discretize.supervised;

import data.discretize.Discretizer;
import data.dataset.*;
import util.Arrays;
import util.MathUtil;

/**
 * @author Jonathan
 * 
 */
public class MODL extends Discretizer {

	private double[][][] modlScore; // [#cutpts][#samples][0] = best score
	// [#cutpts][#samples[1] = best CutPt from previous line
	// [#cutpts][#samples][2] = num thresholds
	private Attribute nbdca;
	private Dataset data;
	private double[][] attMatrix;
	private double[] clsData;
	private double[] nfacts;

	public MODL(Dataset trainData) {
		data = trainData;
		nbdca = trainData.classAttribute();
		modlScore = new double[trainData.numInstances()][trainData.numInstances()][3];
		for (int i = 0; i < modlScore.length; i++)
			modlScore[i] = Arrays.init(modlScore[i], 0);
		try {
			clsData = trainData.getClassValues();
		} catch (AttributeDoesNotExistException e) {
			System.err.println(e.getLocalizedMessage());
			e.printStackTrace();
			System.exit(1);
		}
		attMatrix = data.getDoubleAttributeValues();
		nfacts = new double[trainData.numInstances() + (int) trainData.numInstances() / 2];
		nfacts[0] = 0;
		for (int i = 1; i < nfacts.length; i++) {
			nfacts[i] = Math.log10(i) + nfacts[i - 1];
		}
	}

	public double[] discretize(double[] values, double[] classCounts,
			int numClass, int numBins) {
		double[] nOrd = classCounts;
		int[] clsCount = Arrays.init(new int[numClass], 0);
		for (int k = 0; k < nOrd.length; k++) {
			for (int i = 0; i < nOrd.length; i++) {
				clsCount[(int) clsData[(int) nOrd[i]]] += 1;
				if (k == 0) {
					modlScore[k][i][0] = k2Score(clsCount, 1);
					modlScore[k][i][1] = i;
					modlScore[k][i][2] = 1;
				} else {
					double pMODLS = modlScore[k - 1][i][0]
							- calcLOGnCr(i + 1 + (int) modlScore[k - 1][i][2]
									- 1, (int) modlScore[k - 1][i][2] - 1);
					double ptMODLS = modlScore[k - 1][i][0];
					double numT = modlScore[k - 1][i][2];
					double val = modlScore[k - 1][i][1];
					int[] tempCts = Arrays.init(new int[nbdca
							.numValues()], 0);
					for (int j = i - 1; j >= 0; j--) {
						tempCts[(int) clsData[(int) nOrd[j + 1]]] += 1;
						double tMODL = k2Score(tempCts, 1);
						tMODL = modlScore[k - 1][j][0] + tMODL;
						double newT = modlScore[k - 1][i][2] + 1;
						double otPenalty = calcLOGnCr(i + 1 + (int) newT - 1,
								(int) newT - 1);
						double nk2s = tMODL - otPenalty;
						if (nk2s > pMODLS) {
							pMODLS = nk2s;
							ptMODLS = tMODL;
							val = j;
							numT = newT;
						}
					}
					modlScore[k][i][0] = ptMODLS;
					modlScore[k][i][1] = val;
					modlScore[k][i][2] = numT;
				}
			}
			clsCount = Arrays.init(new int[nbdca.numValues()], 0);
		}

		double[] cutPts = new double[0];
		int n = nOrd.length - 1;
		int k = nOrd.length - 1;
		while (k >= 0 && n != modlScore[k][n][1]) {
			cutPts = Arrays.append(cutPts,
						(values[(int) nOrd[(int) modlScore[k][n][1]]] + values[(int) nOrd[(int) modlScore[k][n][1] + 1]]) / 2.0);
			n = (int) modlScore[k][n][1];
		}

		return MathUtil.sort(cutPts);
	}

	private double clf(int numer, int denom) {
		int tnumer = numer - 1;
		int tdenom = denom - 1;
		double val = 0;
		if (tnumer == tdenom || tdenom < 0)
			return 0;
		else if (tnumer > tdenom && tnumer > 1) {
			for (int n = tnumer; n > tdenom; n--)
				val += Math.log10(n);
		} else if (tdenom > tnumer && tdenom > 1) {
			for (int n = tdenom; n > tnumer; n--)
				val -= Math.log10(n);
		} else
			return 0;
		return val;
	}

	private double calcLOGFact(int n1, int n2) {
		if (n1 > nfacts.length - 1 || n2 > nfacts.length - 1) {
			return clf(n1, n2);
		} else
			return nfacts[n1 - 1] - nfacts[n2 - 1];
	}

	private double calcLOGnCr(int n, int r) {
		if (n == r || r == 0)
			return 0;
		return calcLOGFact(n + 1, r + 1) - nfacts[n - r];
	}

	/**
	 * @param clsMat
	 *            The Current count array with the row being the att value, and
	 *            the columns being the parent values
	 * @return
	 */
	private double k2Score(int[] currCounts, int prior) {
		double val = 0;
		double sum = 0;
		for (int j = 0; j < currCounts.length; j++) {
			val += calcLOGFact(currCounts[j] + prior, prior);
			sum += currCounts[j];
		}
		val += calcLOGFact(currCounts.length * prior, (int) sum
				+ currCounts.length * prior);
		return val;
	}
}