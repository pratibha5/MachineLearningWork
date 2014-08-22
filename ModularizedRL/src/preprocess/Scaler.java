/** The Main Normalization Class.  This class is responsible for taking all the data
 *	dividing it up and preparing it for analysis in RL
 *	@author Jonathan Lustgarten 
 *	@version 2.0 
 *	@siMethode 1.0 
 *	@see java.io.PrintStream
 *	@see java.io.BufferedReader
 **/
package preprocess;

import data.dataset.*;
import util.MathUtil;

public class Scaler {
	public static final int Scale01 = 0;
	public static final int SubLocalMin = 1;
	public static final int SubGlobalMin = 2;
	public static final int Log2Transform = 3;
	public static final int SquareRoot = 4;
	public static final int Exponent2 = 5;
	public static final int Square = 6;
	public static final int NormalDist = 7;

	private Dataset testData;
	private Dataset trainData;
	private Dataset sourceData;
	private double[][] minMax;
	private double globalMin;
	private boolean hasDoubleAtts;

	/**
	 * This is the constructor class for the simulator. It initializes and reads
	 * in the file which constains all of the information about the different
	 * compartments and the affinities for the simulation. Will not allow itself
	 * to run if it does not load the information correctly
	 * @throws AttributeDoesNotExistException 
	 */
	public Scaler(Dataset trnData, Dataset tstData, Dataset srcData) throws AttributeDoesNotExistException {
		trainData = trnData;
		testData = tstData;
		sourceData = srcData;
		hasDoubleAtts = true;
		setMinMax(trainData.getContinuousAttributeIndexes());
	}

	private void minMaxNorm() {
		System.out.print("Processing ");
		double[][] attVals = trainData.getDoubleAttributeValues();
		for (int i = 0; i < attVals.length; i++) {
			if (attVals.length > 9 && i % (attVals.length / 10) == 0)
				System.out.print(".");

			if (minMax[i][0] != minMax[i][1]) {
				for (int r = 0; r < attVals[i].length; r++)
					attVals[i][r] = (attVals[i][r] - minMax[i][0])
							/ (minMax[i][1] - minMax[i][0]);
			}
		}
		trainData.setNewDoubleValues(attVals);
		System.out.println("\nFinished processing training set");
		if (testData != null) {
			System.out.print("Resetting testing vals ");
			double[][] tSampleVals = testData.getDoubleAttributeValues();
			for (int i = 0; i < tSampleVals[0].length; i++) {
				if (tSampleVals[0].length > 9
						&& i % (tSampleVals[0].length / 10) == 0) {
					System.out.print(".");
					// outputSattr(da_minMax);
				}
				if (minMax[i][0] != minMax[i][1]) {
					for (int r = 0; r < tSampleVals.length; r++)
						tSampleVals[r][i] = (tSampleVals[r][i] - minMax[i][0])
								/ (minMax[i][1] - minMax[i][0]);
				}
			}
			testData.setNewDoubleValues(tSampleVals);
		}
		// avg_normData = GetAvg(normData);
		// if(avg_normData.size()>0)
		// avg_normData.remove(0);
	}

	private void subtractLocalMinAddOne() {
		System.out.print("Processing:\t");
		double[][] attVals = trainData.getDoubleAttributeValues();
		for (int i = 0; i < attVals.length; i++) {
			if (attVals.length > 9 && i % (attVals.length / 10) == 0) {
				System.out.print(".");
				// outputSattr(da_minMax);
			}
			for (int r = 0; r < attVals[i].length; r++)
				attVals[i][r] = (attVals[i][r] - minMax[i][0]) + 1.0;
		}
		trainData.setNewDoubleValues(attVals);
		System.out.println("\nFinished the processing training Set");
		if (testData != null) {
			System.out.print("Resetting the testing values ");
			attVals = testData.getDoubleAttributeValues();
			for (int i = 0; i < attVals.length; i++) {
				if (attVals.length > 9 && i % (attVals.length / 10) == 0) {
					System.out.print(".");
					// outputSattr(da_minMax);
				}
				for (int r = 0; r < attVals[i].length; r++)
					attVals[i][r] = (attVals[i][r] - minMax[i][0]) + 1.0;
			}
			testData.setNewDoubleValues(attVals);
		}
		System.out.println("  Done");
	}

	private void subtractGlobalMinAddOne() {
		System.out.print("Processing:\t");
		double[][] attVals = trainData.getDoubleAttributeValues();
		for (int i = 0; i < attVals.length; i++) {
			if (attVals.length > 9 && i % (attVals.length / 10) == 0) {
				System.out.print(".");
				// outputSattr(da_minMax);
			}
			for (int r = 0; r < attVals[i].length; r++)
				attVals[i][r] = (attVals[i][r] - globalMin) + 1.0;
		}
		trainData.setNewDoubleValues(attVals);
		System.out.println("\nFinished processing the training data");
		if (testData != null) {
			System.out.print("Resetting the testing values ");
			attVals = testData.getDoubleAttributeValues();
			for (int i = 0; i < attVals.length; i++) {
				if (attVals.length > 9 && i % (attVals.length / 10) == 0) {
					System.out.print(".");
					// outputSattr(da_minMax);
				}
				for (int r = 0; r < attVals[i].length; r++)
					attVals[i][r] = (attVals[i][r] - globalMin) + 1.0;
			}
			testData.setNewDoubleValues(attVals);
		}
		System.out.println("  Done");
	}

	private void log2() {
		if (globalMin > 0) {
			System.out.print("Processing:\t");
			double[][] attVals = trainData.getDoubleAttributeValues();
			for (int i = 0; i < attVals.length; i++) {
				if (attVals.length > 9 && i % (attVals.length / 10) == 0) {
					System.out.print(".");
					// outputSattr(da_minMax);
				}
				for (int r = 0; r < attVals[i].length; r++)
					attVals[i][r] = MathUtil.log2(attVals[i][r]);
			}
			trainData.setNewDoubleValues(attVals);
			System.out.println("\nFinished processing the training data");
			if (testData != null) {
				System.out.print("Resetting the testing values ");
				attVals = testData.getDoubleAttributeValues();
				for (int i = 0; i < attVals.length; i++) {
					if (attVals.length > 9 && i % (attVals.length / 10) == 0) {
						System.out.print(".");
						// outputSattr(da_minMax);
					}
					for (int r = 0; r < attVals[i].length; r++)
						attVals[i][r] = MathUtil.log2(attVals[i][r]);
				}
				testData.setNewDoubleValues(attVals);
				System.out.println("  Done");
			}
		} else
			System.out.println("Cannot perform log2 transform because the min value is <= 0");
	}

	private void sqRoot() {
		if (globalMin >= 0) {
			System.out.print("Processing:\t");
			double[][] attVals = trainData.getDoubleAttributeValues();
			for (int i = 0; i < attVals.length; i++) {
				if (attVals.length > 9 && i % (attVals.length / 10) == 0) {
					System.out.print(".");
					// outputSattr(da_minMax);
				}
				for (int r = 0; r < attVals[i].length; r++)
					attVals[i][r] = MathUtil.log2(attVals[i][r]);
			}
			trainData.setNewDoubleValues(attVals);
			System.out.println("\nFinished processing the training data");
			if (testData != null) {
				System.out.print("Resetting the testing values ");
				attVals = testData.getDoubleAttributeValues();
				for (int i = 0; i < attVals.length; i++) {
					if (attVals.length > 9 && i % (attVals.length / 10) == 0) {
						System.out.print(".");
						// outputSattr(da_minMax);
					}
					for (int r = 0; r < attVals[i].length; r++)
						attVals[i][r] = MathUtil.log2(attVals[i][r]);
				}
				testData.setNewDoubleValues(attVals);
				System.out.println("  Done");
			}
		} else
			System.out
					.println("Cannot perform square root transform because the min value is <= 0");
	}

	/**
	 * Raises every value to the power of 2. This is the inverse of the log2 transform.
	 */
	private void exponent2() {

		System.out.print("Processing ");
		double[][] attVals = trainData.getDoubleAttributeValues();
		for (int i = 0; i < attVals.length; i++) {
			if (attVals.length > 9 && i % (attVals.length / 10) == 0) {
				System.out.print("|");
				// outputSattr(da_minMax);
			}
			for (int r = 0; r < attVals[i].length; r++)
				attVals[i][r] = Math.pow(2, attVals[i][r]);
		}
		trainData.setNewDoubleValues(attVals);
		System.out.println("\nFinished processing the training data");
		if (testData != null) {
			System.out.print("Resetting the testing values ");
			attVals = testData.getDoubleAttributeValues();
			for (int i = 0; i < attVals.length; i++) {
				if (attVals.length > 9 && i % (attVals.length / 10) == 0) {
					System.out.print(".");
					// outputSattr(da_minMax);
				}
				for (int r = 0; r < attVals[i].length; r++)
					attVals[i][r] = Math.pow(2, attVals[i][r]);
			}
			testData.setNewDoubleValues(attVals);
			System.out.println("  Done");
		}
	}

	/** Reverses SquareRoot* */
	private void squareVals() {

		System.out.print("Processing:\t");
		double[][] attVals = trainData.getDoubleAttributeValues();
		for (int i = 0; i < attVals.length; i++) {
			if (attVals.length > 9 && i % (attVals.length / 10) == 0) {
				System.out.print(".");
				// outputSattr(da_minMax);
			}
			for (int r = 0; r < attVals[i].length; r++)
				attVals[i][r] = Math.pow(attVals[i][r], 2);
		}
		trainData.setNewDoubleValues(attVals);
		System.out.println("\nFinished processing the training data");
		if (testData != null) {
			System.out.print("Resetting the testing values");
			attVals = testData.getDoubleAttributeValues();
			for (int i = 0; i < attVals.length; i++) {
				if (attVals.length > 9 && i % (attVals.length / 10) == 0) {
					System.out.print(".");
					// outputSattr(da_minMax);
				}
				for (int r = 0; r < attVals[i].length; r++)
					attVals[i][r] = Math.pow(attVals[i][r], 2);
			}
			testData.setNewDoubleValues(attVals);
			System.out.println("  Done");
		}
	}

	/*
	private void normalDists() {
		 * System.out.print("Processing:\t");
		 * double[][] attVals = nTrnD.getDoubleAttributeValues();
		 * int[] atts = nTrnD.getDoubleAttributeIndexes();
		 * for (int i = 0; i < attVals.length; i++) {
		 * 	double navg = 0;
		 * 	double nstd = 0;
		 * 	if (attVals.length > 9 && i % (attVals.length / 10) == 0)
		 * 		System.out.print(".");
		 * 	Attribute currAtt = nTrnD.attribute(atts[i]);
		 * 	navg += currAtt.average() * currAtt.counts();
		 * 	nstd += Math.pow(currAtt.stdev(), 2);
		 * 	double total = currAtt.counts();
		 * 	if (nTstD != null) {
		 * 		Attribute tstAtt = nTstD.attribute(currAtt.name());
		 * 		navg += tstAtt.average() * tstAtt.counts();
		 * 		nstd += Math.pow(tstAtt.stdev(), 2);
		 * 		total += tstAtt.counts();
		 * 	}
		 * 	nstd = Math.sqrt(nstd);
		 * 	navg = navg / total;
		 * 	for (int r=0; r<attVals[i].length; r++)
		 * 		attVals[i][r] = (attVals[i][r] - navg) / nstd;
		 * 	}
		 * 	nTrnD.setNewDoubleValues(attVals);
		 * 	System.out.println("\nFinished processing the training data");
		 * 	if (nTstD != null) {
		 * 	System.out.print("Resetting the testing values "); 
		 * 	atts = nTstD.getDoubleAttributeKeys(); 
		 * 	attVals = nTstD.getDoubleAttributeMatrix(); 
		 * 	for (int i=0; i<attVals.length; i++) {
		 * 		if (attVals.length > 9 && i % (attVals.length / 10) == 0) {
		 * 		System.out.print("."); //outputSattr(da_minMax); 
		 * 	}
		 * 	for (int r=0; r<attVals[i].length; r++)
		 * 		attVals[i][r] = (attVals[i][r] - atts[i].getAvg()) / atts[i].getSD();
		 * 	}
		 * 	nTstD.setNewDoubleValues(attVals, nTstD);
		 * 	System.out.println("  Done");
		 * }
	}
	 */

	/**
	 * Normalize each attribute by subtracting its mean and dividing by its 
	 * standard deviation.
	 * @throws AttributeDoesNotExistException 
	 */
	private void normMeanStdev(Dataset data) throws AttributeDoesNotExistException {
		if (data != null) {
			data.recalculateAttributeInfo();
			for (int i = 0; i < data.numAttributes(); i++) {
				Attribute att = data.attribute(i);
				data.normAtt(att.name(), att.average(), att.stdev());
			}
		}		
	}
	
	private void setMinMax(int[] dTrn) throws AttributeDoesNotExistException {
		if (dTrn != null && dTrn.length != 0) {
			hasDoubleAtts = true;
			minMax = new double[dTrn.length][2];
			globalMin = Double.POSITIVE_INFINITY;
			for (int i = 0; i < dTrn.length; i++) {
				minMax[i][0] = trainData.attribute(dTrn[i]).minimumValue();
				minMax[i][1] = trainData.attribute(dTrn[i]).maximumValue();
				if (testData != null) {
					double tstMin = Double.POSITIVE_INFINITY;
					double tstMax = Double.NEGATIVE_INFINITY;
					try {
						tstMin = testData.attribute(
								trainData.attribute(dTrn[i]).name()).minimumValue();
						tstMax = testData.attribute(
								trainData.attribute(dTrn[i]).name()).maximumValue();
					} catch (AttributeDoesNotExistException e) {
						System.err.println(e.getLocalizedMessage());
					}
					if (minMax[i][0] > tstMin)
						minMax[i][0] = tstMin;
					if (minMax[i][1] < tstMax)
						minMax[i][1] = tstMax;
				}
				if (minMax[i][0] < globalMin)
					globalMin = minMax[i][0];
			}
		} else
			hasDoubleAtts = false;
	}

	/*
	public void setNewMethod(int method) {
		this.method = method;
	}
	*/

	/*
	public void setDatasets(Dataset train, Dataset test) throws AttributeDoesNotExistException {
		trainData = train;
		testData = test;
		setMinMax(trainData.getContinuousAttributeIndexes());
	}
	 */

	public void normalize(int method) throws AttributeDoesNotExistException {
		if (trainData.getContinuousAttributeIndexes().length > 0) {
			switch (method) {
			case Scale01:
				System.out.println("\nScaling to mean 0, stdev 1");
				System.out.flush();
				minMaxNorm();
				break;
			case SubGlobalMin:
				System.out.println("\nSubtracting global min + 1 ");
				subtractGlobalMinAddOne();
				break;
			case SubLocalMin:
				System.out.println("\nSubtracting local min + 1");
				subtractLocalMinAddOne();
				break;
			case Log2Transform:
				System.out.println("\nTransforming by log2");
				log2();
				break;
			case SquareRoot:
				System.out.println("\nTransforming by square root");
				sqRoot();
				break;
			case Exponent2:
				System.out.println("\nTransforming by exponent 2");
				exponent2();
				break;
			case Square:
				System.out.println("\nTransforming by power 2");
				squareVals();
				break;
			case NormalDist:
				System.out.println("\nEqualizing the means and variances");
				//normalDists();
				normMeanStdev(trainData);
				normMeanStdev(testData);
				normMeanStdev(sourceData);
				break;
			default:
				System.err.println("\nMethod: " + method
						+ " is invlaid."); //\nUsing 0-1 Scaling Method");
				//minMaxNorm();
			}
		} else
			System.out.println("Can't perform scaling on datasets which have no numeric attributes.");
	}

	public void run(int[] meths) throws AttributeDoesNotExistException {
		for (int i = 0; i < meths.length; i++) {
			normalize(meths[i]);
			setMinMax(trainData.getContinuousAttributeIndexes());
		}
	}
}