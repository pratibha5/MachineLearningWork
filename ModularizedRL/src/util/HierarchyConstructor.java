/*
 * HierarchyConstructor.java
 *
 * Created on May 30, 2006, 1:47 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package util;

import data.dataset.*;
import structures.learner.attribute.*;

//import java.io.*;
//import java.lang.*;
//import java.util.*;

/**
 * This class is for constructing value hierarchies based on different
 * parameters including the number of samples per a bin etc. It is designed to
 * take advantage of the power of Value Hierarchies
 * 
 * @see structures.learner.attribute.VHierarchyNode
 * @see filters.math.Discretizers
 * @author Jonathan Lustgarten
 * @version 1.0
 */
public class HierarchyConstructor {
	private double[][] cutPoints;
	private Dataset trainData;
	private boolean cluster;

	/** Creates a new instance of HierarchyConstructor */
	public HierarchyConstructor(double[][] ctpts, Dataset Train) {
		trainData = Train;
		cutPoints = ctpts;
		cluster = false;
	}

	public HierarchyConstructor(double[][] ctpts, Dataset Train,
			boolean bHierarchy) {
		trainData = Train;
		cutPoints = ctpts;
		cluster = bHierarchy;
	}

	public void setDoCluster(boolean bhierarchy) {
		cluster = bhierarchy;
	}

	public VHierarchyNode[] buildHierachy()
			throws AttributeDoesNotExistException {
		// In here you can do conglomerative clustering to build the hierachry
		double[][] DattVals = trainData.getDoubleAttributeValues();
		VHierarchyNode[] hierarchs = 
				new VHierarchyNode[trainData.numContinuousAttributes()];
		System.out.println("Generating the hierarchy....");
		int[] doubleAttributeIndexes = trainData.getContinuousAttributeIndexes();
		for (int i = 0; i < hierarchs.length; i++) {
			if (hierarchs.length < 11 || i % (hierarchs.length / 10) == 0)
				System.out.print(".");
			if (cluster)
				hierarchs[i] = genHierarchy(cutPoints[i], MathUtil.sort(DattVals[i]), 
						trainData.attribute(doubleAttributeIndexes[i]).name(), false);
			else {
				String AttName = trainData.attribute(doubleAttributeIndexes[i])
						.name();
				IntervalNode[] nVN = getCutPtHierarchy(cutPoints[i], AttName);
				hierarchs[i] = new VHierarchyNode(AttName, "ROOT");
				for (int j = 0; j < nVN.length; j++)
					hierarchs[i].addValue(nVN[j]);
			}
		}
		System.out.println("\nDone generating hierarchies.");
		return hierarchs;
	}

	private static IntervalNode[] getCutPtHierarchy(double[] cutPts,
			String AttName) {
		IntervalNode[] nIN = new IntervalNode[cutPts.length + 1];
		if (cutPts == null || cutPts.length == 0)
			nIN[0] = new IntervalNode(AttName, Double.NEGATIVE_INFINITY,
					Double.POSITIVE_INFINITY);
		else {
			for (int i = 0; i < nIN.length; i++) {
				if (i == 0)
					nIN[i] = new IntervalNode(AttName,
							Double.NEGATIVE_INFINITY, cutPts[i]);
				else if (i == cutPts.length)
					nIN[i] = new IntervalNode(AttName, cutPts[i - 1],
							Double.POSITIVE_INFINITY);
				else if (i < cutPts.length)
					nIN[i] = new IntervalNode(AttName, cutPts[i - 1], cutPts[i]);
			}
		}
		return nIN;
	}

	private static double[] getAvgs(double[] vals, double[] cutPts) {
		double[] nAvgs = new double[cutPts.length + 1];
		double[] tvals;
		int start = 0;
		int ctI = 0;
		while (start < vals.length && ctI < cutPts.length) {
			tvals = new double[0];
			for (int i = start; start < vals.length
					&& vals[start] < cutPts[ctI]; i++) {
				tvals = Arrays.append(tvals, vals[i]);
				start = i + 1;
			}
			if (tvals.length != 0)
				nAvgs[ctI] = MathUtil.getMean(tvals);
			else
				nAvgs[ctI] = Double.NaN;
			ctI++;
		}
		if (ctI == cutPts.length && start < vals.length) {
			tvals = new double[0];
			for (int j = start; j < vals.length; j++)
				tvals = Arrays.append(tvals, vals[j]);
			nAvgs[nAvgs.length - 1] = MathUtil.getMean(tvals);
		} else {
			for (int k = ctI; k < nAvgs.length; k++)
				nAvgs[k] = Double.NaN;
		}
		return nAvgs;
	}

	private static int[] closestAvg(double[] avgs) {
		int[] CAvg = new int[2];
		CAvg[0] = 0;
		CAvg[1] = 1;
		for (int i = 1; i < avgs.length - 1; i++) {
			if (Math.abs(avgs[CAvg[0]] - avgs[CAvg[1]]) > Math.abs(avgs[i]
					- avgs[i + 1])) {
				CAvg[0] = i;
				CAvg[1] = i + 1;
			}
		}
		return CAvg;
	}

	public static VHierarchyNode genHierarchy(double[] cutpts, double[] avals,
			String AttName, boolean comb) {
		VHierarchyNode nVH = new VHierarchyNode(AttName, "ROOT");
		IntervalNode CurrNode = new IntervalNode(AttName,
				Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		if (cutpts == null || cutpts.length == 0)
			nVH.addValue(new IntervalNode(AttName, Double.NEGATIVE_INFINITY,
					Double.POSITIVE_INFINITY));
		else {
			IntervalNode[] nVN = getCutPtHierarchy(cutpts, AttName);
			double[] NCPts = new double[cutpts.length];
			System.arraycopy(cutpts, 0, NCPts, 0, cutpts.length);
			double[] sortAVals = MathUtil.sort(avals);
			double[] NodeAvgs = getAvgs(sortAVals, cutpts);
			for (int i = 0; i < nVN.length; i++)
				nVH.addValue(nVN[i]);
			if (avals.length / cutpts.length >= 5 && comb) {

				do {
					if (Arrays.contains(NodeAvgs, Double.NaN)) {
						double begin, end;
						int startI = Arrays.indexOf(NodeAvgs, Double.NaN, 0);
						if (startI == NodeAvgs.length - 1) {
							begin = cutpts[startI - 1];
							end = Double.POSITIVE_INFINITY;
							CurrNode = new IntervalNode(AttName, begin, end);
							nVH.addValue(CurrNode);
							nVH.addValue(CurrNode, nVN[startI - 1]);
							nVH.addValue(CurrNode, nVN[startI]);
							IntervalNode[] nVNs = new IntervalNode[nVN.length - 1];
							System.arraycopy(nVN, 0, nVNs, 0, startI - 1);
							nVNs[startI - 1] = CurrNode;
							nVN = nVNs;
						} else if (startI != -1) {
							int[] inArrs = new int[1];
							inArrs[0] = startI;
							if (startI == 0) {
								begin = Double.NEGATIVE_INFINITY;
								end = cutpts[startI + 1];
							} else {
								begin = cutpts[startI - 1];
								end = cutpts[startI];
							}
							int startI2 = Arrays.indexOf(NodeAvgs,
									Double.NaN, startI + 1);
							while (startI2 != -1 && startI2 == startI + 1) {
								if (startI2 == NodeAvgs.length - 1)
									end = Double.POSITIVE_INFINITY;
								else
									end = cutpts[startI2 + 1];
								inArrs = Arrays.append(inArrs, startI2);
								startI = startI2;
								startI2 = Arrays.indexOf(NodeAvgs,
										Double.NaN, startI2 + 1);
							}
							IntervalNode[] nVNs = new IntervalNode[nVN.length
									- inArrs.length];
							CurrNode = new IntervalNode(AttName, begin, end);
							nVH.addValue(CurrNode);
							for (int j = 0; j < inArrs.length; j++)
								nVH.addValue(CurrNode, nVN[inArrs[j]]);
							nVH.addValue(CurrNode,
									nVN[inArrs[inArrs.length - 1] + 1]);
							System.arraycopy(nVN, 0, nVNs, 0, inArrs[0]);
							nVNs[inArrs[0]] = CurrNode;
							if (inArrs[inArrs.length - 1] != NodeAvgs.length - 1)
								System.arraycopy(nVN,
										inArrs[inArrs.length - 1] + 2, nVNs,
										inArrs[0] + 1, nVN.length
												- inArrs.length - 1);
							nVN = nVNs;
						}

					} else if (nVN.length > 2) {
						int[] CloseAvg = closestAvg(NodeAvgs);
						// Creates a new interval node with the children being
						// the two previous node and the parent being the
						// combined range
						CurrNode = new IntervalNode(AttName,
								nVN[CloseAvg[0]].begin, nVN[CloseAvg[1]].end);
						nVH.addValue(CurrNode);
						nVH.addValue(CurrNode, nVN[CloseAvg[0]]);
						nVH.addValue(CurrNode, nVN[CloseAvg[1]]);
						// Sets up the new IntervalNode Array with the combined
						// range
						IntervalNode[] nVNs = new IntervalNode[nVN.length - 1];
						System.arraycopy(nVN, 0, nVNs, 0, CloseAvg[0]);
						nVNs[CloseAvg[0]] = CurrNode;
						System.arraycopy(nVN, CloseAvg[1] + 1, nVNs,
								CloseAvg[0] + 1, nVN.length - CloseAvg[1] - 1);
						nVN = nVNs;
					}
					// Removes the Cut Pts considered
					double[] tCtPts = new double[0];
					for (int j = 0; j < nVN.length - 1; j++)
						tCtPts = Arrays.append(tCtPts, nVN[j].end);

					NCPts = tCtPts;
					// Calculates the new node averages
					NodeAvgs = getAvgs(sortAVals, NCPts);
				} while (nVN.length > 2);
			}
		}
		return nVH;
	}

	public void setCluster(boolean cluster) {
		this.cluster = cluster;
	}
}