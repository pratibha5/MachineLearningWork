package org.probe.data.discretize.supervised;

import java.io.BufferedReader;
import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
//import java.io.OutputStream;
//import java.io.PrintStream;
import java.text.DecimalFormat;
//import java.text.NumberFormat;
import java.util.ArrayList;
//import java.util.Vector;

import org.probe.data.discretize.Discretizer;
import data.dataset.*;
import org.probe.util.*;
//import corefiles.structures.data.dataset.attribute.*;

/**
 * Uses a K2 Score to calculate the best split There is no maximum bin parameter
 * 
 * @author Jonathan Lustgarten
 * @version 1.0
 */
public class EBD extends Discretizer {
	public static final int PRIOR_NONE = 0;
	public static final int PRIOR_STRUCTURE = 1;
	public static final int PRIOR_LENGTH = 2;

	//private DataModel data;
	private double[][] partitions;
	//private Attribute classAtt;
	private int prior;
	private int cStructPrior;
	private double[] nfacts;
	//private int[] clsVals;
	//private double[] dvals;
	//private double[][] countsAtPts;

	/*
	 * Blank Constructor for EBD Discretization
	 * 
	 */
	/*
	 public EBD() {
		//data = null;
		partitions = new double[0][0];
		prior = 1;
		cStructPrior = 0;
	}
	*/

	/**
	 * @param d
	 *            The dataset to discretize
	 */
	public EBD(DataModel d) {
		//data = d;
		//classAtt = d.classAttribute();
		prior = 1;
		cStructPrior = 1;
		nfacts = new double[d.numInstances() + (int) d.numInstances() / 2];
		nfacts[0] = 0;
		for (int i = 1; i < nfacts.length; i++) {
			nfacts[i] = Math.log10(i) + nfacts[i - 1];
		}
	}

	public EBD(DataModel d, int paramMethod) {
		//data = d;
		//classAtt = d.classAttribute();
		prior = 1;
		if (paramMethod <= 2 && paramMethod >= 0)
			cStructPrior = paramMethod;
		else
			cStructPrior = 0;
		nfacts = new double[d.numInstances() + (int) d.numInstances() / 2];
		nfacts[0] = 0;
		for (int i = 1; i < nfacts.length; i++) {
			nfacts[i] = Math.log10(i) + nfacts[i - 1];
		}
	}

	public EBD(int numSmpls, int numcls, int pr, int meth) {
		nfacts = new double[numSmpls + numSmpls / 2];
		nfacts[0] = 0;
		for (int i = 1; i < nfacts.length; i++) {
			nfacts[i] = Math.log10(i) + nfacts[i - 1];
		}
		prior = pr;
		cStructPrior = meth;
	}

	public void setPrior(int pr) {
		prior = pr;
	}

	/*
	 * Resets the current dataset to d
	 * 
	 * @param d
	 *            The new dataset to discretize
	 */
	/*
	public void setDataModel(DataModel d) {
		//data = d;
		classAtt = d.classAttribute();
		nfacts = new double[d.numInstances() + (int) d.numInstances() / 2];
		nfacts[0] = 0;
		for (int i = 1; i < nfacts.length; i++) {
			nfacts[i] = Math.log10(i) + nfacts[i - 1];
		}
	}
	*/

	private double clf(int numer, int denom) {
		int tnumer = numer;
		int tdenom = denom;
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

	private double calcLogFact(int n1, int n2) {
		if (n1 > nfacts.length - 1 || n2 > nfacts.length - 1) {
			return clf(n1, n2);
		} else
			return nfacts[n1] - nfacts[n2];
	}

	private double calcLognCr(int n, int r) {
		if (n == r || r == 0)
			return 0;
		return calcLogFact(n, r) - nfacts[n - r];
	}

	/**
	 * @param clsMat
	 *            The current count array with the row being the att value, and
	 *            the columns being the parent values
	 * @return
	 */
	private double k2Score(int[] currCounts, int prior) {
		double val = 0;
		double sum = 0;
		for (int j = 0; j < currCounts.length; j++) {
			val += calcLogFact(currCounts[j] + prior - 1, prior - 1);
			sum += currCounts[j];
		}
		val += calcLogFact(currCounts.length * prior - 1, (int) sum
				+ currCounts.length * prior - 1);
		return val;
	}

	private double[] getUniqueValues(double[] values) {
		double[] nvals = new double[values.length];
		int currInd = 0;
		for (int i = 0; i < values.length; i++) {
			if (i == 0) {
				nvals[i] = values[i];
				currInd += 1;
			} else {
				if (values[i] == nvals[currInd - 1])
					continue;
				else {
					nvals[currInd] = values[i];
					currInd += 1;
				}
			}
		}
		double[] trimVals = new double[currInd];
		System.arraycopy(nvals, 0, trimVals, 0, trimVals.length);
		return trimVals;
	}

	private int addCounts(double nval, int start, boolean forward,
			double[] vals, double[] classCnts, int[] cnt) {
		int endInd = start;
		if (forward) {
			for (int i = start; i < vals.length; i++) {
				if (vals[i] <= nval) {
					cnt[(int) classCnts[i]] += 1;
					endInd += 1;
				} else
					break;
			}
		} else {
			for (int i = start; i >= 0; i--) {
				if (vals[i] >= nval) {
					cnt[(int) classCnts[i]] += 1;
					endInd -= 1;
				} else
					break;
			}
		}
		return endInd;
	}

	public double[] discretize(double[] values, double[] classCounts,
			int numClass, int numBins, boolean outProgress) {
		double[] discPts;
		double[] nvals = getUniqueValues(values);
		partitions = new double[nvals.length][5 + numClass]; // pt,score,#thresh,total,prior,ClassDists
		// WithinBin
		int[] counts = new int[numClass];
		int[] rightOfSplit = new int[numClass];
		double[] lengths = Arrays.init(
				new double[nvals.length - 1], 0);
		for (int c = 0; c < numClass; c++) {
			counts[c] = 0;
			rightOfSplit[c] = 0;
		}
		if (cStructPrior == EBD.PRIOR_LENGTH) {
			double totalLength = nvals[nvals.length - 1] - nvals[0];
			for (int i = 0; i < lengths.length; i++)
				lengths[i] = ((nvals[i + 1] - nvals[i])) / totalLength;
		}

		double val, tempVal, pt, structPrior, oSP;
		int nBins;
		if (outProgress)
			System.out.print("Progress: ");
		int valInd = 0;
		for (int i = 0; i < nvals.length; i++) {
			if (outProgress && i % (values.length / 20) == 0)
				System.out.print(".");
			val = tempVal = 0; // The K2 Score
			pt = i; // The split point index
			nBins = 1; // Number of bins
			valInd = addCounts(nvals[i], valInd, true, values, classCounts, counts);
			oSP = 0;
			val = k2Score(counts, prior);
			double sumcs = MathUtil.sumArray(counts);
			rightOfSplit = Arrays.init(rightOfSplit, 0);
			int splitInd = valInd - 1;
			for (int j = i - 1; j >= 0; j--) {
				splitInd = addCounts(nvals[j + 1], splitInd, false, values,
						classCounts, rightOfSplit);
				tempVal = k2Score(rightOfSplit, prior);
				if (cStructPrior == EBD.PRIOR_STRUCTURE)
					structPrior = calcLognCr((int) sumcs
							+ (int) partitions[j][2], (int) partitions[j][2]);
				else if (cStructPrior == EBD.PRIOR_LENGTH)
					structPrior = -1 * Math.log10(lengths[j])
							+ partitions[j][4];
				else
					structPrior = 0;
				tempVal += partitions[j][1];
				double nVal = tempVal - structPrior;
				if (nVal > val - oSP) {
					val = tempVal;
					pt = j;
					nBins = (int) partitions[j][2] + 1;
					oSP = structPrior;
				}
			}
			partitions[i][0] = pt;
			partitions[i][1] = val;
			partitions[i][2] = nBins;
			partitions[i][3] = val - oSP;
			partitions[i][4] = oSP;
			for (int c = 0; c < numClass; c++) {
				if (pt == i)
					partitions[i][c + 5] = counts[c];
				else {
					partitions[i][c + 5] = rightOfSplit[c];
				}
			}
			System.out.print("");
		}
		if (outProgress)
			System.out.println("Done");
		// Calculate thel cut-points using a simple mid-pt heuristic
		discPts = new double[0];
		int p = partitions.length - 1;
		while (partitions[p][0] != p && p != 0) {
			discPts = Arrays.append(
							discPts,
							(nvals[(int) partitions[p][0]] + nvals[(int) partitions[p][0] + 1]) / 2.0);
			p = (int) partitions[p][0];
		}
		if (discPts.length == 0)
			return discPts;
		else
			return MathUtil.sort(discPts);
	}

	@Override
	public double[] discretize(double[] values, double[] classCounts,
			int numClass, int numBins) {
		return discretize(values, classCounts, numClass, numBins, false);
	}

	public static void main(String[] args) {
		BufferedReader br;
		try {
			System.out.println("Reading file.... " + args[0]);
			br = new BufferedReader(new FileReader(args[0]));
			ArrayList<String> ins = new ArrayList<String>(20);
			String inp = "";
			while ((inp = br.readLine()) != null) {
				ins.add(inp);
			}
			ins.trimToSize();
			String[] nA = new String[ins.size()];
			nA = ins.toArray(nA);
			String[] initVals = nA[0].split(",");
			int numSmps = Integer.parseInt(initVals[0]);
			int numCls = Integer.parseInt(initVals[1]);
			int prior = Integer.parseInt(initVals[2]);
			double[] vals = new double[numSmps];
			double[] clsvs = new double[numSmps];
			System.out.println("Parsing...");
			for (int i = 0; i < numSmps; i++) {
				String[] vls = nA[i + 1].split(",");
				vals[i] = Double.parseDouble(vls[0]);
				clsvs[i] = Double.parseDouble(vls[1]);
			}
			System.out.println("Discretizing...");
			EBD ndisc = new EBD(numSmps, numCls, prior, 1);
			double[] discpts = ndisc.discretize(vals, clsvs, numCls, 10, true);
			System.out.println("Computing statistics...");
			double[][] ncnts = new double[discpts.length + 2][numCls + 1];
			for (int i = 0; i < ncnts.length; i++) {
				for (int j = 0; j < ncnts[i].length; j++)
					ncnts[i][j] = 0;
			}
			int currInd = 0;
			for (int i = 0; i < vals.length; i++) {
				if (currInd < discpts.length && vals[i] >= discpts[currInd])
					currInd += 1;
				ncnts[currInd][(int) clsvs[i]] += 1;
				ncnts[ncnts.length - 1][(int) clsvs[i]] += 1;
				ncnts[currInd][ncnts[currInd].length - 1] += 1;
				ncnts[ncnts.length - 1][ncnts[ncnts.length - 1].length - 1] += 1;
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter("EBD-"
					+ args[0].substring(0, args[0].lastIndexOf("."))
					+ "-bins.txt"));
			StringBuffer sb = new StringBuffer();

			sb.append("Overall results: \n");
			sb.append("We found: " + (discpts.length + 1)
					+ " bins and for each bin:\n");
			System.out.print(sb.toString());
			System.out.println();
			bw.write(sb.toString());
			DecimalFormat df = new DecimalFormat("#.##");
			DecimalFormat df2 = new DecimalFormat("#.####");
			sb = new StringBuffer();
			for (int i = 0; i < discpts.length; i++) {
				if (i % 10 == 1) {
					bw.write(sb.toString());
					sb = new StringBuffer();
				}
				sb.append("Bin " + (i + 1) + ":\n");
				if (i == 0)
					sb.append("\t Point: NegInf - " + df.format(discpts[i]) + "\n");
				else
					sb.append("\t" + df.format(discpts[i - 1]) + " - "
							+ df.format(discpts[i]) + "\n");
				int bstCls = 0;
				for (int j = 1; j < ncnts[i].length - 1; j++) {
					if (ncnts[i][bstCls] < ncnts[i][j])
						bstCls = j;
				}
				double prob = ncnts[i][bstCls] / ncnts[i][ncnts[i].length - 1];
				if (prob == 1)
					sb.append("\t Log 10 LR: infinity\n");
				else
					sb.append("\t Log 10 LR: "
							+ df2.format(Math.log10(prob / (1.0 - prob)))
							+ "\n");
				sb.append("Predicted class: " + (bstCls + 1));
				sb.append("\n");
			}
			sb.append("Bin " + (discpts.length + 1) + ":\n");
			sb.append("\t" + df.format(discpts[discpts.length - 1])
					+ " - inf");
			int bstCls = 0;
			for (int j = 1; j < ncnts[discpts.length].length - 1; j++) {
				if (ncnts[discpts.length][bstCls] < ncnts[discpts.length][j])
					bstCls = j;
			}
			double prob = ncnts[discpts.length][bstCls]
					/ ncnts[discpts.length][ncnts[discpts.length].length - 1];
			if (prob == 1)
				sb.append("\t Log 10 LR: infinity\n");
			else
				sb.append("\t Log 10 LR: "
						+ df2.format(Math.log10(prob / (1.0 - prob))) + "\n");
			sb.append("Predicted class: " + (bstCls + 1));
			String output = sb.toString();
			if (discpts.length > 9)
				System.out.println("Too Many bins to output... see the EBD File.");
			else
				System.out.println(output);
			bw.write(output);
			bw.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("Goodbye!!");
	}
}
