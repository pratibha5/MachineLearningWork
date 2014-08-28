/*
 * Discretize.java
 *
 * Created on March 26, 2005, 2:03 PM
 */

package org.probe.data.discretize;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.probe.data.DataModel;
import org.probe.data.discretize.supervised.EBD;
import org.probe.data.discretize.supervised.ErrorBased;
import org.probe.data.discretize.supervised.OneR;
import org.probe.data.discretize.unsupervised.EqualFrequency;
import org.probe.data.discretize.unsupervised.EqualWidth;
import org.probe.stats.structures.data.converters.input.TabCsvDataLoader;

import org.probe.data.dataset.*;

import org.probe.util.MathUtil;

/**
 * @author Jonathan
 */
public class Discretizers {
	public static final int GaussianU = 0;
	public static final int EqualWidthU = 1;
	public static final int EqualFreqU = 2;
	public static final int OneRS = 3;
	public static final int ErrorBasedS = 4;
	public static final int D2S = 5;
	public static final int FayaadMDLS = 6;
	public static final int EBD = 7;
	public static final int MODL = 8;
	private int method;
	private DataModel data;
	private double methodParam;
	private int numBins;

	/**
	 * Log2 conversion factor
	 */
	public final double log2 = Math.log(2);

	public Discretizers() {
		method = EBD;
		methodParam = 1;
		numBins = 5;
	}

	public void setMehtod(int meth, double val) {
		method = meth;
		methodParam = val;
	}

	public void setMethod(String meth, String pMeth) {
		method = Integer.parseInt(meth);
		methodParam = Double.parseDouble(pMeth);
	}

	public void setData(DataModel d) {
		data = d;
	}

	private Discretizer getMethod() {
		Discretizer dm;
		switch (method) {
		case EqualWidthU:
			dm = new EqualWidth();
			numBins = (int) methodParam;
			break;
		case EqualFreqU:
			dm = new EqualFrequency();
			numBins = (int) methodParam;
			break;
		case OneRS:
			dm = new OneR((int) methodParam);
			break;
		case ErrorBasedS:
			dm = new ErrorBased(methodParam);
			break;
/*		case D2S:
			dm = new D2S(data);
			break;
		case FayaadMDLS:
			dm = new FayyadIraniEntropy();
			numBins = (int) methodParam;
			break;
		case EBD:
			dm = new EBD(data, (int) methodParam);
			break;
		case MODL:
			dm = new MODL(data);
			break;*/
		default:
			dm = new EBD(data, 1);
		}
		return dm;
	}

	public double[] discreteizeMDLSingle(int attI) throws AttributeDoesNotExistException {
		Attribute att = null;
		att = data.attribute(attI);
		if (!att.hasContinuousValues()) {
			System.out.println("Cannot discretize the attribute because it is already discrete!");
			return null;
		} else {
			double[] cutPoint = new double[0];
			Discretizer dm = getMethod();
			double[] vals = null;
			double[] classCounts = null;
			vals = data.attributeValues(att.position());
			classCounts = data.getClassValues();
			int[] indOrder = MathUtil.getSortIndex(vals);
			double[] nvs = new double[vals.length];
			double[] ccs = new double[classCounts.length];
			for (int i = 0; i < ccs.length; i++) {
				nvs[i] = vals[indOrder[i]];
				ccs[i] = classCounts[indOrder[i]];
			}
			vals = nvs;
			classCounts = ccs;
			cutPoint = dm.discretize(vals, classCounts, data.numClasses(),
					numBins);
			if (cutPoint == null)
				return new double[0];
			return cutPoint;
		}
	}

	public double[][] discretizeMDL() {
		double[][] cutPoints;
		if (data == null)
			return null;

		int[] doubleAttInds = data.getContinuousAttributeIndexes();
		cutPoints = new double[doubleAttInds.length][];
		//System.out.print("Discretizing " + data.getFileName() + " using "
		//		+ getMethodName(method, paramMethod) + " ");
		System.out.print("Discretizing using "
						+ getMethodName(method, methodParam) + " ");
		//Vector AttsDisc = new Vector(doubleAttInds.length);
		Discretizer dm = getMethod();
		int numClasses = data.numClasses();
		int numAttsWithDisc = 0;
		int numAttsWithCutPoints = 0;
		for (int i = 0; i < doubleAttInds.length; i++) {
			if (doubleAttInds.length < 10
					|| i % (doubleAttInds.length / 10) == 0)
				System.out.print(".");
			double[] vals = null;
			double[] classCounts = null;
			try {
				vals = data.attributeValues(doubleAttInds[i]);
				classCounts = data.getClassValues();
				int[] indOrder = MathUtil.getSortIndex(vals);
				double[] nvs = new double[vals.length];
				double[] ccs = new double[classCounts.length];
				for (int j = 0; j < ccs.length; j++) {
					nvs[j] = vals[indOrder[j]];
					ccs[j] = classCounts[indOrder[j]];
				}
				vals = nvs;
				classCounts = ccs;
			} catch (AttributeDoesNotExistException e) {
				System.err.println(e.getLocalizedMessage());
				e.printStackTrace();
				System.exit(1);
			}

			double[] discPts = dm.discretize(vals, classCounts, numClasses,
					numBins);
			if (discPts == null)
				cutPoints[i] = new double[0];
			else {
				cutPoints[i] = discPts;
				numAttsWithDisc++;
				if (discPts.length > 0) {
					numAttsWithCutPoints++;
				}
			}
		}
		System.out.println(" There are " + numAttsWithCutPoints + " attributes with cut points.");
		return cutPoints;
	}


	/**
	 * Returns the method of discretization as a string
	 * 
	 * @param meth
	 *            The Method to be selected
	 * @return the string representation
	 */
	//*	@ deprecated PG2009
	public static String getMethodName(int meth) {
		String methS = "";
		switch (meth) {
		case GaussianU:
			methS = "Gaussian";
			break;
		case EqualWidthU:
			methS = "Equal Width";
			break;
		case EqualFreqU:
			methS = "Equal Frequency";
			break;
		case OneRS:
			methS = "1R (Holte 1993)";
			break;
		case ErrorBasedS:
			methS = "Error-Based (Kohavi 1997)";
			break;
		case D2S:
			methS = "D2 (Catlett 1991)";
			break;
		case FayaadMDLS:
			methS = "Fayyad-Irani (1993)";
			break;
		case EBD:
			methS = "EBD (2008)";
			break;
		case MODL:
			methS = "MODL (2006)";
			break;
		default:
			methS = "MODL Optomized (Lustgarten 2007)";
		}
		return methS;
	}



	/**
	 * Returns the method of discretization as a string
	 * 
	 * @param meth
	 *            The Method to be selected
	 * @return the string representation
	 */
	public static String getMethodName(int meth, double pv) {
		String methS = "";
		switch (meth) {
		case GaussianU:
			methS = "Gaussian";
			break;
		case EqualWidthU:
			methS = "Equal Width";
			break;
		case EqualFreqU:
			methS = "Equal Frequency";
			break;
		case OneRS:
			methS = "1R (Holte 1993)";
			break;
		case ErrorBasedS:
			methS = "Error-Based (Kohavi 1997)";
			break;
		case D2S:
			methS = "D2 (Catlett 1991)";
			break;
		case FayaadMDLS:
			methS = "Fayyad-Irani (1993)";
			break;
		case EBD:
			if (pv == 0)
				methS = "EBD with No Prior (2008)";
			else if (pv == 2)
				methS = "EBD with Length Prior (EBDD 2008)";
			else
				methS = "EBD (2008)";
			break;
		case MODL:
			methS = "MODL (2006)";
			break;
		default:
			methS = "MODL Optomized (Lustgarten 2007)";
		}
		return methS;
	}

	public static String getMethod(String methC) {
		String methS = "";
		String m = methC;
		if (m.equalsIgnoreCase("_0"))
			return "No discretization";
		else if (methC.indexOf("_") > -1) {
			m = methC.substring(m.indexOf("_") + 1);
			methS = "Features from ";
		}
		int meth = Integer.parseInt(m);
		switch (meth) {
		case GaussianU:
			methS = methS + "Gaussian";
			break;
		case EqualWidthU:
			methS = methS + "Equal Width";
			break;
		case EqualFreqU:
			methS = methS + "Equal Frequency";
			break;
		case OneRS:
			methS = methS + "1R (Holte 1993)";
			break;
		case ErrorBasedS:
			methS = methS + "Error-Based (Kohavi 1997)";
			break;
		case D2S:
			methS = methS + "D2 (Catlett 1991)";
			break;
		case FayaadMDLS:
			methS = methS + "Fayyad-Irani (1993)";
			break;
		case EBD:
			methS = methS + "EBD (2008)";
			break;
		case MODL:
			methS = methS + "MODL (2006)";
			break;
		default:
			methS = methS + "EBD (2008)";
		}
		return methS;
	}

	public static String getMethod(String methC, String methV) {
		String methS = "";
		String m = methC;
		if (m.equalsIgnoreCase("_0"))
			return "No Discretization";
		else if (methC.indexOf("_") > -1) {
			m = methC.substring(m.indexOf("_") + 1);
			methS = "Features from ";
		}
		int meth = Integer.parseInt(m);
		switch (meth) {
		case GaussianU:
			methS = methS + "Gaussian";
			break;
		case EqualWidthU:
			methS = methS + "Equal Width";
			break;
		case EqualFreqU:
			methS = methS + "Equal Frequency";
			break;
		case OneRS:
			methS = methS + "1R (Holte 1993)";
			break;
		case ErrorBasedS:
			methS = methS + "Error-Based (Kohavi 1997)";
			break;
		case D2S:
			methS = methS + "D2 (Catlett 1991)";
			break;
		case FayaadMDLS:
			methS = methS + "Fayyad-Irani (1993)";
			break;
		case EBD:
			if (methV.equalsIgnoreCase("2"))
				methS = methS + "EBD-Length (2008)";
			else
				methS = methS + "EBD (2008)";
			break;
		case MODL:
			methS = methS + "MODL (2006)";
			break;
		default:
			methS = methS + "MODL Optomized (Lustgarten 2007)";
		}
		return methS;
	}

	public static String[] availableMethods() {
		String[] meths = new String[9];
		meths[GaussianU] = "Gaussian Unsupervised";
		meths[EqualWidthU] = "Equal Width Unsupervised";
		meths[EqualFreqU] = "Equal Frequency Unsupervised";
		meths[OneRS] = "1R (Holte 1993)";
		meths[ErrorBasedS] = "Error-Based (Kohavi 1997)";
		meths[D2S] = "D2 (Catlett 1991)";
		meths[FayaadMDLS] = "Fayyad-Irani (1993)";
		meths[EBD] = "EBD (2008)";
		meths[MODL] = "MODL (2006)";
		return meths;
	}

	public static void main(String[] args) throws Exception {
		TabCsvDataLoader ltcd = new TabCsvDataLoader("tempData.txt");
		DataModel d = ltcd.loadData();
		Discretizers meth = new Discretizers();
		meth.setData(d);
		meth.setMehtod(Discretizers.EBD, 1);
		double[][] discPol = meth.discretizeMDL();
		d.setDiscretization(discPol);
	}
}
