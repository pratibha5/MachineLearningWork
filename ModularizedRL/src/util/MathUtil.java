/*
 * Util.java
 *
 * Created on February 17, 2005, 11:19 PM
 */

package util;

/**
 * @author Jonathan
 */
public class MathUtil {

	public static final double log2 = Math.log(2);

	public static double logFact(int numer, int denom) {
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

	public static double lognCr(int n, int r) {
		if (n == r || r == 0)
			return 0;
		return logFact(n, n - r) - logFact(r, 1);
	}

	/**
	 * Calculates one columns K2 Score in LOG base 10
	 * 
	 * @param clsMat
	 *            The Current count array with the row being the att value, and
	 *            the columns being the parent values
	 * @return
	 */
	public static double k2Score(int[] currCounts, int prior) {
		double val = 0;
		for (int j = 0; j < currCounts.length; j++)
			val += logFact(currCounts[j] + prior, prior);
		val += logFact(currCounts.length * prior, (int) MathUtil
				.sumArray(currCounts)
				+ currCounts.length * prior);
		return val;
	}

	public static final double log2(double a) {
		//return Math.log(a) / log2;	--PG2009
		//return (a > 0) ? (Math.log(a) / log2) : Double.NaN;
		return (a > 0) ? (Math.log(a) / log2) : 0;
	}

	public static final double pLogp(double a) {
		if (a == 1.0 || a == 0.0 || a < 1e-6)
			return 0.0;

		return a * Math.log(a);
	}

	public static final double sumArray(double[] d) {
		double sum = 0.0;
		for (int i = 0; i < d.length; i++)
			sum += d[i];
		return sum;
	}

	public static final int sumArray(int[] d) {
		int sum = 0;
		for (int i = 0; i < d.length; i++)
			sum += d[i];
		return sum;

	}

	private static void swap(int[] a, int i, int j) {
		int old = a[i];
		a[i] = a[j];
		a[j] = old;
	}

	/**
	 * Implements QuickSort
	 * 
	 * @param a
	 *            the array to sort
	 * @return an array arranged in ascending order
	 */
	public static int[] quickSort(int[] a) {
		int[] temp = new int[a.length];
		for (int i = 0; i < a.length; i++)
			temp[i] = i;
		double[] da = Arrays.toDoubleArray(a);
		quickSort(da, temp, 0, a.length - 1);
		int[] na = new int[a.length];
		for (int j = 0; j < a.length; j++)
			na[j] = a[temp[j]];
		return na;
	}

	private static void swap(double[] a, int i, int j) {
		double old = a[i];
		a[i] = a[j];
		a[j] = old;
	}

	private static void quickSort(double[] a) {
		int[] nis = new int[a.length];
		for (int i = 0; i < nis.length; i++)
			nis[i] = i;
		quickSort(a, nis, 0, a.length - 1);
	}

	public static final double[] getMinMaxMeanSD(double[] S) {
		double[] t = new double[4];
		double[] MnMx = getMinMax(S);
		t[0] = MnMx[0];
		t[1] = MnMx[1];
		t[2] = getMean(S);
		t[3] = getSD(S, t[2]);
		return t;
	}

	public static final int indexOfMax(double[] arr) {
		int ind = 0;
		for (int i = 1; i < arr.length; i++) {
			if (arr[ind] < arr[i])
				ind = i;
		}
		return ind;
	}

	public static final double[] getMinMax(double[] S) {
		double[] t = new double[2];
		t[0] = 1e15;
		t[1] = -1e15;
		for (int j = 0; j < S.length; j++) {
			if (t[0] > S[j])
				t[0] = S[j];
			if (t[1] < S[j])
				t[1] = S[j];
		}
		return t;
	}

	public static final boolean equals(double a, double b) {
		return ((a - b < 1e-6) && (b - a < 1e-6));
	}

	public static final int[] subtractArrays(int[] a1, int[] a2) {
		if (a2 == null)
			return a1;
		int[] newA = new int[a2.length];
		if (a1 == null) {
			for (int i = 0; i < newA.length; i++)
				newA[i] = -1 * a2[i];
			return newA;
		}
		if (a1.length != a2.length) {
			System.out
					.println("Cannot subtract arrays because they have different sizes");
			System.exit(1);
		}
		for (int j = 0; j < a1.length; j++)
			newA[j] = a1[j] - a2[j];
		return newA;
	}

	public static final double entropySet(double[] counts) {
		double entrop = 0.0;
		double sum = 0.0;
		for (int i = 0; i < counts.length; i++) {
			sum += counts[i];
			entrop -= MathUtil.pLogp(counts[i]);
		}
		if (sum != 0 && sum > 1e-6)
			return (entrop + MathUtil.pLogp(sum)) / (sum * log2);
		else
			return 0;
	}

	public static final double entropySets(double[][] counts) {
		double totalEntrop = 0, sumRow = 0, total = 0;

		for (int i = 0; i < counts.length; i++) {
			sumRow = 0;
			for (int j = 0; j < counts[0].length; j++) {
				totalEntrop += MathUtil.pLogp(counts[i][j]);
				sumRow += counts[i][j];
			}
			totalEntrop -= MathUtil.pLogp(sumRow);
			total += sumRow;
		}
		if (total == 0 || total < 1e-6) {
			return 0;
		}
		return -totalEntrop / (total * log2);
	}

	public static final double sumMatrix(double[][] matrix) {
		if (matrix == null || matrix.length == 0 || matrix[0].length == 0)
			return 0;
		double sum = 0;
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++)
				sum += matrix[i][j];
		}
		return sum;
	}

	public static final double[] sumMatrixToArray(double[][] matrix) {
		if (matrix == null || matrix.length == 0 || matrix[0].length == 0)
			return null;
		double[] sum = new double[matrix[0].length];
		for (int i = 0; i < sum.length; i++) {
			sum[i] = 0;
			for (int j = 0; j < matrix.length; j++)
				sum[i] += matrix[j][i];
		}
		return sum;

	}

	public static final double getMean(double[] attrVals) {
		double sum = 0;
		
		for (int i = 0; i < attrVals.length; i++)
			sum += attrVals[i];
		return (sum / attrVals.length);
	}

	/**
	 * @param attrVals
	 *            The Attribute Values
	 * @param mean
	 *            The Arithmetic Mean of the Attribute Values
	 * @return
	 */
	public static final double getSD(double[] attrVals, double mean) {
		try {
			double ssquare = 0;
			for (int i = 0; i < attrVals.length; i++) {
				ssquare += Math.pow(attrVals[i] - mean, 2);
			}

			ssquare /= ((double) attrVals.length);
			return Math.sqrt(ssquare);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public static final double getSD(double[] attrVals) {
		double mean = getMean(attrVals);
		double ssquare = 0;

		for (int i = 0; i < attrVals.length; i++) {
			ssquare += Math.pow(attrVals[i] - mean, 2);
		}

		ssquare /= (attrVals.length - 1);
		return Math.sqrt(ssquare);
	}

	/**
	 * Taken from the old version of RL Fixed the calculation such that it
	 * actually calculates for more than two cases
	 */
	public static double calcFisher(int[][] confusionMatrix) {
		if (confusionMatrix.length < 2)
			return Double.NaN;

		double probSum = 0;

		// Copy the confusion martix
		int nMatrix[][] = new int[2][2];
		for (int x = 0; x < nMatrix.length; x++)
			for (int y = 0; y < nMatrix.length; y++)
				nMatrix[x][y] = confusionMatrix[x][y];

		// Compute row and column counts
		final double R1FactLn = factln(nMatrix[0][0] + nMatrix[0][1]);
		final double R2FactLn = factln(nMatrix[1][0] + nMatrix[1][1]);
		final double C1FactLn = factln(nMatrix[0][0] + nMatrix[1][0]);
		final double C2FactLn = factln(nMatrix[0][1] + nMatrix[1][1]);
		final double TFactLn = factln(nMatrix[0][0] + nMatrix[0][1]
				+ nMatrix[1][0] + nMatrix[1][1]);

		// Sum the conditional probabilities of this and more extreme matrices
		while ((nMatrix[0][1] >= 0) && (nMatrix[1][0] >= 0)) {
			// Compute the conditional probability of observing the current
			// matrix given the row and column sums
			probSum += condProb(nMatrix, R1FactLn, R2FactLn, C1FactLn,
					C2FactLn, TFactLn);
			// Consider the next more extreme matrix
			nMatrix[0][0]++;
			nMatrix[1][1]++;
			nMatrix[0][1]--;
			nMatrix[1][0]--;
		}

		return probSum;
	}

	public static double calcChiSq(int[][] matrix) {
		if (matrix == null || matrix.length == 0 || matrix[0].length == 0)
			return Double.NaN;
		int[][] nM = new int[matrix.length + 1][matrix[0].length + 1];
		nM = Arrays.init(nM, 0);
		// Does Matrix Sums
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				nM[i][j] += matrix[i][j];
				nM[i][nM[i].length - 1] += matrix[i][j];
				nM[nM.length - 1][j] += matrix[i][j];
				nM[nM.length - 1][nM[nM.length - 1].length - 1] += matrix[i][j];
			}
		}

		double totalNum = (double) nM[nM.length - 1][nM[nM.length - 1].length - 1];
		double chiSq = 0.0;
		for (int k = 0; k < matrix.length; k++) {
			for (int m = 0; m < matrix[k].length; m++) {
				double expectedVal = nM[k][nM[k].length - 1]
						* nM[nM.length - 1][m] / totalNum;
				if (expectedVal != 0.0)
					chiSq += Math.pow(((double) nM[k][m] - expectedVal), 2.0)
							/ expectedVal;
			}
		}

		return chiSq;
	}

	public static int degreesFreedom(int[][] matrix) {
		if (matrix == null || matrix.length == 0 || matrix[0].length == 0)
			return 0;
		return matrix.length - 1 * matrix[0].length - 1;
	}

	/**
	 * @param a
	 *            the sum of all the counts aka how many predictions were made
	 *            not including abstentions
	 */
	private static double condProb(int[][] m, double R1FactLn, double R2FactLn,
			double C1FactLn, double C2FactLn, double TFactLn) {

		double a = R1FactLn + R2FactLn + C1FactLn + C2FactLn;
		double b = factln(m[0][0]) + factln(m[0][1]) + factln(m[1][0])
				+ factln(m[1][1]) + TFactLn;

		return Math.exp(a - b);
	}

	private static double factln(double n) {
		return gammln(n + 1.0);
	}

	private static double gammln(double xx) {
		double stp = 2.50662827465;
		double x = xx - 1.0;
		double tmp = x + 5.5;
		double tmp1 = (x + .5) * Math.log(tmp) - tmp;
		double ser = 1 + (76.18009173 / (x + 1.0)) + (-86.50532033 / (x + 2.0))
				+ (24.01409822 / (x + 3.0)) + (-1.231739516 / (x + 4.0))
				+ (.0012085803 / (x + 5.0)) + (-0.00000536382 / (x + 6.0));

		return tmp1 + Math.log(stp * ser);
	}

	public static double cubeRoot(double x) {
		return Math.pow(x, 1.0 / 3.0);
	}

	/**
	 * Returns the values in order from least to greatest
	 * 
	 * @param the
	 *            double values to sort
	 * @return The sorted array
	 */
	public static double[] sort(double[] vals) {
		int[] sortInd = getSortIndex(vals);
		double[] nA = new double[vals.length];
		for (int i = 0; i < sortInd.length; i++)
			nA[i] = vals[sortInd[i]];
		return nA;
	}

	/**
	 * Returns the indexes of the values in order from least to greatest
	 * 
	 * @param the
	 *            double values to sort
	 * @return an int[] holding the indexes of each value
	 */
	public static int[] getSortIndex(double[] vals) {
		int[] ixs = new int[vals.length];
		for (int i = 0; i < ixs.length; i++)
			ixs[i] = i;
		double[] toSortVal = new double[vals.length];
		System.arraycopy(vals, 0, toSortVal, 0, vals.length);
		quickSort(toSortVal, ixs, 0, vals.length - 1);
		return ixs;
	}

	/**
	 * Returns the indexes of the values sorted from greatest to least
	 * 
	 * @param val
	 *            the array of double values to be sorted
	 * @return the indexes of the values in the order of greatest to least
	 */
	public static int[] getReverseSortIndex(double[] val) {
		int[] inds = getSortIndex(val);
		int[] rvs = new int[inds.length];
		for (int i = 0; i < inds.length; i++)
			rvs[inds.length - 1 - i] = inds[i];
		return rvs;
	}

	private static void quickSort(double[] a, int[] s, int left, int right) {
		int lo = left;
		int hi = right;
		double mid;
		int help;

		if (right > left) {
			mid = a[s[(left + right) / 2]];

			// Loop through the array until indices cross
			while (lo <= hi) {
				while ((a[s[lo]] < mid) && (lo < right)) {
					++lo;
				}

				// Find an element that is smaller than or equal to
				// the partition element starting from the right Index.
				while ((a[s[hi]] > mid) && (hi > left)) {
					--hi;
				}

				// If the indexes have not crossed, swap
				if (lo <= hi) {
					help = s[lo];
					s[lo] = s[hi];
					s[hi] = help;
					++lo;
					--hi;
				}
			}
			if (left < hi) {
				quickSort(a, s, left, hi);
			}
			if (lo < right) {
				quickSort(a, s, lo, right);
			}
		}
	}

	private static void swap(double[] a, int[] s, int i, int j) {
		double old = a[i];
		int olds = s[i];
		a[i] = a[j];
		s[i] = s[j];
		a[j] = old;
		s[j] = olds;
	}

	public static double[] normalize(double[] arr1) {
		double[] nA = new double[arr1.length];
		double total = MathUtil.sumArray(arr1);
		for (int i = 0; i < arr1.length; i++)
			nA[i] = arr1[i] / total;
		return nA;
	}

	/**
	 * Normalize a double array that consists of logs It accounts that you are
	 * dealing with a reverse scale
	 * 
	 * @param arr1
	 *            The array of log doubles to normalize
	 * @return An array of probabilities on a 0 - 1 scale
	 */
	public static double[] normalizeLogs(double[] arr1) {
		double[] nA = new double[arr1.length];
		double total = MathUtil.sumArray(arr1);
		for (int i = 0; i < arr1.length; i++)
			nA[i] = 1 - arr1[i] / total;
		double nTotal = MathUtil.sumArray(nA);
		for (int j = 0; j < nA.length; j++)
			nA[j] = nA[j] / nTotal;
		return nA;
	}

	private static int[] counts(double[] vals, double ctpt, int[] cvals,
			int start, int numCVals) {
		int[] tcvals = new int[numCVals + 1];
		Arrays.init(tcvals, 0);
		int end;
		for (end = start; end < vals.length; end++) {
			if (vals[end] < ctpt)
				tcvals[cvals[end]] += 1;
			else
				break;
		}
		tcvals[tcvals.length - 1] = end;
		return tcvals;
	}

	private static int[] counts(int[] vals, int[] cvals, int attVal, int nCV) {
		int[] valsinatt = new int[nCV];
		Arrays.init(cvals, 0);
		for (int i = 0; i < cvals.length; i++) {
			if (vals[i] == attVal)
				valsinatt[cvals[i]] += 1;
		}
		return valsinatt;
	}

	private static double calcRemain(int[] counts, int tSamps) {
		double sumCounts = MathUtil.sumArray(counts);
		double total = 0;
		for (int i = 0; i < counts.length; i++)
			total += (sumCounts / ((double) tSamps)) * -1
					* MathUtil.pLogp(counts[i] / ((double) sumCounts));
		return total;
	}

	public static double attributeGain(double[] vals, double[] cutPts,
			int[] classvals, int numCVals) {
		int tSamps = vals.length;
		double[] tvals = new double[vals.length];
		System.arraycopy(vals, 0, tvals, 0, vals.length);
		int[] tcvals = new int[vals.length];
		System.arraycopy(classvals, 0, tcvals, 0, tcvals.length);
		double[] tctpts = new double[cutPts.length];
		System.arraycopy(cutPts, 0, tctpts, 0, cutPts.length);
		MathUtil.quickSort(tvals, tcvals, 0, tvals.length);
		tctpts = MathUtil.sort(tctpts);
		int startI = 0;
		double gain = 0;
		for (int i = 0; i < cutPts.length; i++) {
			int[] counts = counts(tvals, tctpts[i], tcvals, startI, numCVals);
			startI = counts[counts.length - 1];
			int[] countsCV = new int[counts.length - 1];
			System.arraycopy(counts, 0, countsCV, 0, countsCV.length);
			gain += calcRemain(countsCV, tSamps);
		}
		gain = 1.0 - gain;
		return gain;
	}

	public static double attributeGain(int[] vals, int[] classvals,
			int numCVals, int numAttVals) {
		int tSamps = vals.length;
		double gain = 0;
		for (int i = 0; i < numAttVals; i++) {
			int[] counts = counts(vals, classvals, i, numCVals);
			gain += calcRemain(counts, tSamps);
		}
		return 1.0 - gain;
	}

	public static double entropy(int[][] atvals) {
		double gain = 0;
		for (int i = 0; i < atvals.length - 1; i++) {
			double tp = 0;
			for (int j = 0; j < atvals[0].length - 1; j++)
				tp -= MathUtil.pLogp(atvals[i][j]
						/ ((double) atvals[i][atvals[i].length - 1]));
			gain += atvals[i][atvals[i].length - 1]
					/ ((double) atvals[atvals.length - 1][atvals[atvals.length - 1].length - 1])
					* tp;
		}
		return 1.0 - gain;
	}

	public static double mannWhitneyStat(double[][] arr) {
		return 0.0;
	}

	/**
	 * @param info
	 *            An array containing in order all the different statistics each
	 *            row has an array with [threshold,TP,FN,FP,TN]
	 * @return the area under the curve
	 */
	public static double calcAUC(double[][] info) {
		double auc = 0.0;
		if (info == null)
			return auc;
		// Info = double[] classVals
		double c1 = 0;
		double c2 = 0;
		for (int i = 0; i < info.length; i++) {
			if (info[i][1] != 0) {
				c2++;
				continue;
			} else
				c1++;
			for (int j = i - 1; j >= 0; j--) {
				if (info[j][1] == 0)
					continue;
				if (info[j][0] == info[i][0])
					auc += .5;
				else
					auc += 1;
			}
		}
		return auc / (c1 * c2);

		/*
		 * // INFO: 0: threshold, 1-4: TP,FN,FP,TN double xlast = 1; double
		 * ylast = 1; final double fp0 = info[0][3]; final double tp0 =
		 * info[0][1]; for(int i=1;i<info.length;i++){ double tpi = info[i][1];
		 * double fpi = info[i][3]; final double x = info[i][3]/fp0; final
		 * double y = info[i][1]/tp0; final double areaDelta =
		 * (y+ylast)*(xlast-x)/2.0;
		 * 
		 * auc += areaDelta; xlast = x; ylast = y; } //make sure ends at 0,0 if
		 * (xlast > 0.0) { final double areaDelta = ylast * xlast / 2.0;
		 * //System.err.println(" a'=" + areaDelta); auc += areaDelta; } if(auc<.5)
		 * return 1.0-auc;
		 */
	}

	public static double youden(int[][] conMatrix) {
		int tp = conMatrix[0][0];
		int totalN = (int) MathUtil.sumArray(conMatrix[1]);
		int totalP = (int) MathUtil.sumArray(conMatrix[0]);
		int tn = conMatrix[1][1];

		return (tp / ((double) (totalP)) - (1 -tn / ((double) (totalN))));
	}

	public static double kappaScore(int[][] conM) {
		double q = 0;
		double[] sums = new double[0];
		double total = 0;
		double diag = 0;
		for (int r = 0; r < conM.length; r++) {
			double rS = MathUtil
					.sumArray(Arrays.toDoubleArray(conM[0]));
			double cS = 0;
			for (int t = 0; t < conM.length; t++) {
				cS += conM[t][r];
				if (t == r)
					diag += conM[t][r];
			}
			sums = Arrays.append(sums, rS * cS);
			total += rS;
		}
		for (int s = 0; s < sums.length; s++)
			sums[s] = sums[s] / total;
		q = sumArray(sums);
		return (diag - q) / (total - q);
	}

	/**
	 * @param preds
	 *            The predictions with their probability
	 * @return The thresholds including 0 and 1
	 */
	public static double[] getThresholds(double[][] preds) {
		double[] thresh = new double[1];
		thresh[0] = 0;
		for (int i = 1; i < preds.length; i++) {
			if (preds[i][0] != preds[i - 1][0])
				thresh = Arrays.append(thresh,
						(preds[i][0] + preds[i - 1][0]) / 2.0);
		}
		thresh = Arrays.append(thresh, 1.0);
		return thresh;
	}

	/**
	 * @param predictions
	 *            Ordered predctions, where the first column is the predicted value
	 * @return
	 */
	public static double calculateAUCMW(double[][] predictions) {
		double sumRanks = 0;
		double[] rank = new double[predictions.length];
		int startRank = 0;
		int numNeg = 0;

		// Assign the ranks
		boolean sameRank = false;
		double[] negRanks = new double[0];
		for (int i = 0; i < rank.length - 1; i++) {
			if (predictions[i][0] == predictions[i + 1][0]) {
				if (!sameRank) {
					startRank = i + 1;
					sameRank = true;
				}
			} else if (sameRank) {
				double avgRank = ((i + 1) * (i + 2) / 2.0)
						- (startRank * (startRank + 1) / 2.0);
				for (int j = startRank - 1; j < i + 1; j++) {
					rank[j] = avgRank;
					if (predictions[j][1] > 0) {
						sumRanks += rank[j];
						negRanks = Arrays.append(negRanks, rank[j]);
					}
				}
				sameRank = false;
			} else {
				rank[i] = i + 1;
				if (predictions[i][1] > 0) {
					sumRanks += rank[i];
					negRanks = Arrays.append(negRanks, rank[i]);
				}
			}
			if (predictions[i][1] > 0)
				numNeg++;
		}
		if (sameRank) {
			double avgRank = ((rank.length) * (rank.length + 1) / 2.0)
					- (startRank * (startRank + 1) / 2.0);
			for (int j = startRank - 1; j < rank.length; j++) {
				rank[j] = avgRank;
				if (predictions[j][1] > 0) {
					sumRanks += rank[j];
					negRanks = Arrays.append(negRanks, rank[j]);
				}
			}
		} else {
			rank[rank.length - 1] = rank.length;
			if (predictions[rank.length - 1][1] > 0) {
				sumRanks += rank[rank.length - 1];
				negRanks = Arrays.append(negRanks, rank[rank.length - 1]);
			}
		}
		//double sumNegArray = MathUtil.sumArray(negRanks);
		double U = sumRanks - negRanks.length * (negRanks.length + 1) / 2.0;
		double auc = U
				/ (negRanks.length * (predictions.length - negRanks.length));
		if (auc < .5)
			auc = 1 - auc;
		/*
		 * double auc = 0; double[][] roc = generateThresholdROC(predictions);
		 * final double tp0 = roc[0][1]; final double fp0 = roc[0][3]; //starts
		 * at high values and goes down double xlast = 1.0; double ylast = 1.0;
		 * for (int i = 1; i < roc.length; i++) { final double x = roc[i][3] /
		 * fp0; final double y = roc[i][1] / tp0; final double areaDelta = (y +
		 * ylast) * (xlast - x) / 2.0; /* System.err.println("[" + i + "]" + "
		 * x=" + x + " y'=" + y + " xl=" + xlast + " yl=" + ylast + " a'=" +
		 * areaDelta);
		 */
		/*
		 * auc += areaDelta; xlast = x; ylast = y; }
		 * 
		 * //make sure ends at 0,0 if (xlast > 0.0) { final double areaDelta =
		 * ylast * xlast / 2.0; //System.err.println(" a'=" + areaDelta); auc +=
		 * areaDelta; }
		 */
		return auc;
	}

	public static double[][] generateThresholdROC(double[][] predictions) {
		double[] thresh = getThresholds(predictions);
		double[][] roc = Arrays.init(new double[thresh.length][5],
				0);
		for (int i = 0; i < thresh.length; i++) {
			roc[i][0] = thresh[i];
			for (int j = 0; j < predictions.length; j++) {
				if (predictions[j][0] > thresh[i]) {
					if (predictions[j][1] > 0)
						roc[i][3]++;
					else
						roc[i][1]++;
				} else {
					if (predictions[j][1] > 0)
						roc[i][4]++;
					else
						roc[i][2]++;
				}
			}
		}
		return roc;
	}

	/**
	 * @return The index of the most probable class
	 */
	/*
	private static int getMaxIndex(double[] cpv, double threshold) {
		if (cpv.length > 2 || threshold == -1) {
			int mc = 0;
			for (int i = 1; i < cpv.length; i++) {
				if (cpv[i] > cpv[mc])
					mc = i;
			}
			return mc;
		} else {
			if (cpv[0] > threshold)
				return 0;
			else
				return 1;
		}
	}
	*/
	
	public static int ceiling(double d) {
		double fl = Math.floor(d);
		if (fl < d)
			return (int) fl + 1;
		return (int) d;
	}

	private static double logFact(double[] facts, double numer, double denom) {
		if (numer > facts.length - 1 || denom > facts.length - 1) {
			double val = 0;
			if (numer > denom) {
				for (int i = (int) (denom + 1); i <= numer; i++)
					val += Math.log10(i);
				return val;
			} else if (numer < denom) {
				for (int i = (int) (numer + 1); i <= denom; i++)
					val -= Math.log10(i);
				return val;
			} else
				return val;
		} else {
			return facts[(int) numer] - facts[(int) denom];
		}
	}

	/**
	 * Calculates the K2Score with the hyper-parameter of 1 using the algorithm
	 * developed by Cooper and Herskovits [1992] The CPT has the following form:
	 * <code>    P1   P2   P3...PN  Row Totals
	 * 		C1   v1   v2   v3   vn   SUM(v{1-n})
	 * 		C2   vn+1 ...      v2n   SUM(v{n+1 - 2n})
	 * 		...	...	...	...	...	...	...
	 * 	Col Sum	SUM(v{1,n+1,(m-1)*n+1}...... Sum(v{1-m*n}) 
	 * </code>
	 * where N refers to the number of parent states and M refers to the number
	 * of children states
	 * 
	 * @param cpt
	 *            The conditional probability table with all the sums on both
	 *            the column and rows
	 * @param prior
	 *            A value that is an integer prior (unable to handle non-integer
	 *            priors... yet
	 * @return the K2Score as a logarithm base 10
	 */
	public static double k2Score(double[][] cpt, double prior) {
		double[] facts = new double[(int) cpt[cpt.length - 1][cpt[cpt.length - 1].length - 1] * 2];
		facts[0] = 0;
		facts[1] = 0;
		for (int i = 2; i < facts.length; i++)
			facts[i] = facts[i - 1] + Math.log10(i);
		double val = 0;
		for (int i = 0; i < cpt[0].length - 1; i++) {
			// This is the product of alpha (I,J,K) = prior and cpt[j][i] =
			// Sijk+ in the algorithm
			for (int j = 0; j < cpt.length - 1; j++)
				val += logFact(facts, cpt[j][i] + prior - 1, prior - 1);
			// This is the parent calc where Nij = (cpt.length-1)*prior,
			val += logFact(facts, (cpt.length - 1) * prior - 1,
					(cpt.length - 1) * prior + cpt[cpt.length - 1][i] - 1);
		}
		return val;
	}

	public static double chiSq(double[][] cpt) {
		double totalNum = cpt[cpt.length - 1][cpt[cpt.length - 1].length - 1];
		double chiSq = 0.0;
		for (int k = 0; k < cpt.length - 1; k++) {
			for (int m = 0; m < cpt[k].length - 1; m++) {
				double expectedVal = cpt[k][cpt[k].length - 1]
						* cpt[cpt.length - 1][m] / totalNum;
				if (expectedVal != 0.0)
					chiSq += Math.pow((cpt[k][m] - expectedVal), 2.0)
							/ expectedVal;
			}
		}
		return chiSq;
	}

	public static double[] avgAndSE(double[] nums) {
		double[] vals = new double[2];
		double mean = 0;
		double s = 0;
		int x = Arrays.count(nums, Double.NaN);
		int n = nums.length - x;
		int j = 0;
		for (int i = 0; i < nums.length; i++ ) {
			if (!Double.valueOf(nums[i]).equals(Double.valueOf(Double.NaN))) {
				mean += nums[i];
				j++;
			}
		}
		mean /= j;
		vals[0] = mean;
		
		double stdev = 0;
		int sum = 0;
		for (int i = 0; i < nums.length; i++) {
			if (! Double.valueOf(nums[i]).equals(Double.valueOf(Double.NaN))) {
				double delta = nums[i] - mean;
				sum += delta * delta;
			}
		}
		stdev = (n > 1) ? Math.sqrt(((double) sum) / (j - 1)) : 0;

		//for (int i = 0; i < nums.length; i++) {
		//	if (!Double.valueOf(nums[i]).equals(Double.NaN)) {
		//		System.out.print(", " + nums[i]);
		//		double delta = nums[i] - mean;
		//		mean += delta / ((double) (i + 1));
		//		s += delta * (nums[i] - mean);
		//	}
		//}

		//double stddev = 
		//	nums.length > 1 
		//	? Math.sqrt(s / ((double) (nums.length - 1)))
		//	: 0;
		//vals[1] = stddev / Math.sqrt(nums.length);
		
		//double stdev =
		//	n > 1 ? Math.sqrt(s / ((double) (n - 1))) : 0;

		vals[1] = stdev / Math.sqrt(n);
		
		return vals;
	}

	public static void sortValsAndClass(double[] vals, int[] cvals) {
		int[] order = MathUtil.getSortIndex(vals);
		double[] na = new double[vals.length];
		int[] ca = new int[cvals.length];
		for (int i = 0; i < order.length; i++) {
			na[i] = vals[order[i]];
			ca[i] = cvals[order[i]];
		}

		for (int c = 0; c < na.length; c++) {
			vals[c] = na[c];
			cvals[c] = ca[c];
		}
	}

	public static int indexOfMax(int[] arr) {
		int mi = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] > arr[mi] || arr[i] == Integer.MAX_VALUE
					&& arr[mi] != Integer.MAX_VALUE)
				mi = i;
		}
		return mi;
	}

	public static double max(double[][] arr) {
		double max = 0;
		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				if (max < arr[i][j]) {
					max = arr[i][j];
				}
			}
		}
		return max;
	}
	
	public static int[] getReverseSortIndex(int[] arr) {
		int[] inds = getSortIndex(Arrays.toDoubleArray(arr));
		int[] rinds = new int[inds.length];
		for (int i = 0; i < inds.length; i++)
			rinds[i] = inds[inds.length - 1 - i];
		return rinds;
	}

	public static void main(String[] args) {
		System.out.println("Testing the sorting function\ndouble vals:");
		double[] vals = new double[5];
		for (int i = 0; i < vals.length; i++) {
			vals[i] = Math.random();
			System.out.print("Val " + i + ": " + vals[i] + "\t");
		}
		System.out.println();
		int[] sVal = getSortIndex(vals);
		System.out.println("Sorted indexes: ");
		for (int j = 0; j < sVal.length; j++)
			System.out.print("" + sVal[j] + "\t");
		System.out.println();
		for (int k = 0; k < sVal.length; k++) {
			System.out.print("" + vals[sVal[k]] + "\t");
		}
	}
}