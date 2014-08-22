/*
 * PreProcessParameters.java
 *
 * Created on May 1, 2006, 12:15 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package parameters;

import data.discretize.Discretizers;
import util.Arrays;

/**
 * Holds the parameters for scaling, discretization and ChiSq Feature Selection
 * 
 * @author Jonathan Lustgarten
 * @date 5-1-06
 * @version 1.0
 * @since 1.0
 */
public class PreProcessParameters {
	private boolean bDiscretize, bRemoveAttr, bScale, bChiSqTrim,
			bBuildHierarchs, bEqualizeDataset; 
	private boolean bSaveIntermedFiles; // Saves all the intermediate generated files
	private int[] scaleParams;
	private double nChiSqLevel, nDiscMethodValue;
	private int iDiscMethod;
	private boolean bCreateFolds;
	private int iNumFolds;
	private boolean bSplitData;
	private int iPercentSplit;
	private boolean bRRV;
	private int rrvPercent;
	private int iAvStep;
	private boolean combineTechReps;
	private int iRepAtt = -1;

	/** Creates a new instance of PreProcessParameters */
	public PreProcessParameters() {
		scaleParams = new int[0];
		nChiSqLevel = 0;
		nDiscMethodValue = 1;
		iDiscMethod = Discretizers.EBD;
		iNumFolds = -1;
		iPercentSplit = -1;
		rrvPercent = 30;
	}

	public PreProcessParameters(String[] args) {
		this();
		processArgs(args);
	}

	public void processArgs(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-chi")) {
				bChiSqTrim = true;
				if (args.length > i + 1 && args[i + 1].indexOf("-") == -1) {
					i++;
					try {
						nChiSqLevel = (new Double(args[i])).doubleValue();
					} catch (Exception e) {
						System.out.println("Incompatible parameter: " + args[i]);
						System.out.println("Using default of 0.");
					}
					if (nChiSqLevel < 0) {
						System.out.println("Can't have threshold < 0");
						System.out.println("Using default of 0");
						nChiSqLevel = 0;
					}
				}
			} else if (args[i].equalsIgnoreCase("-s") ||
					args[i].equalsIgnoreCase("-sc") ||
					args[i].equalsIgnoreCase("-scale")) {
				bScale = true;
				if (args.length > i + 1 && args[i + 1].indexOf('-') == -1) {
					do {
						i++;
						try {
							scaleParams = Arrays.append(scaleParams,
									(new Integer(args[i])).intValue());
						} catch (Exception e) {
							System.out.println("Incompatible parameter: " + args[i]);
						}
					} while (args.length > i + 1
							&& args[i + 1].indexOf('-') == -1);
				} else {
					scaleParams = new int[1];
					scaleParams[0] = 0;
				}
			} else if (args[i].equalsIgnoreCase("-r")) {
				bDiscretize = true;
				iDiscMethod = Discretizers.EBD;
				nDiscMethodValue = 1;
				bRemoveAttr = true;
			} else if (args[i].equalsIgnoreCase("-d")) {
				bDiscretize = true;
				if (args.length >= i + 2) {
					try {
						iDiscMethod = Integer.valueOf(args[i + 1]).intValue();
						i++;
						nDiscMethodValue = Double.valueOf(args[i + 1]);
						i++;
					} catch (Exception e) {
						if (iDiscMethod == -1) {
							System.out.println(
									"-d expects type of discretization.  using MODL Optimized Default");
							iDiscMethod = Discretizers.EBD;
						}
						if (nDiscMethodValue == -1.0) {
							System.out.println(
									"-d expects threshold/number of bins. Using Default for that Method");
							if (iDiscMethod == Discretizers.ErrorBasedS)
								nDiscMethodValue = .33;
							else if (iDiscMethod == Discretizers.EBD)
								nDiscMethodValue = 1;
							else
								nDiscMethodValue = 5;
						}
					}
				} else if (args.length > i + 1) {
					try {
						iDiscMethod = Integer.valueOf(args[i + 1]).intValue();
						i++;
					} catch (Exception e) {
						if (iDiscMethod == -1) {
							System.out.println(
									"-d expects type of discretization.  Using MODL optomized as default");
							iDiscMethod = Discretizers.EBD;
						}
					}
					System.out.println("Using default for the method selected");
					if (iDiscMethod == Discretizers.ErrorBasedS)
						nDiscMethodValue = .25;
					else if (iDiscMethod == Discretizers.EBD)
						nDiscMethodValue = 1;
					else
						nDiscMethodValue = 5;

				} else {
					iDiscMethod = Discretizers.EBD;
					nDiscMethodValue = 1;
				}
			} else if (args[i].equalsIgnoreCase("-dr")) {
				bDiscretize = true;
				bRemoveAttr = true;
				if (args.length >= i + 2) {
					try {
						iDiscMethod = Integer.valueOf(args[i + 1]).intValue();
						i++;
						nDiscMethodValue = Double.valueOf(args[i + 1]);
						i++;
					} catch (Exception e) {
						if (iDiscMethod == -1) {
							System.out.println(
									"-d expects type of discretization.  using MODL optimized default");
							iDiscMethod = Discretizers.EBD;
						}
						if (nDiscMethodValue == -1.0) {
							System.out.println(
									"-d expects threshold/number of bins. Using default for that method");
							if (iDiscMethod == Discretizers.ErrorBasedS)
								nDiscMethodValue = .33;
							else if (iDiscMethod == Discretizers.EBD)
								nDiscMethodValue = 1;
							else
								nDiscMethodValue = 5;
						}
					}
				} else if (args.length > i + 1) {
					try {
						iDiscMethod = Integer.valueOf(args[i + 1]).intValue();
						i++;
					} catch (Exception e) {
						if (iDiscMethod == -1) {
							System.out.println(
									"-d expects type of discretization.  Using MODL optomized as default");
							iDiscMethod = Discretizers.EBD;
						}
					}
					System.out.println("Using default for the method selected");
					if (iDiscMethod == Discretizers.ErrorBasedS)
						nDiscMethodValue = .25;
					else if (iDiscMethod == Discretizers.EBD)
						nDiscMethodValue = 1;
					else
						nDiscMethodValue = 5;

				} else {
					iDiscMethod = Discretizers.EBD;
					nDiscMethodValue = 1;
				}
			} else if (args[i].equalsIgnoreCase("-BVH"))
				bBuildHierarchs = true;
			else if (args[i].equalsIgnoreCase("-EQD"))
				bEqualizeDataset = true;
			else if (args[i].equalsIgnoreCase("-SI"))
				bSaveIntermedFiles = true;
			else if (args[i].equalsIgnoreCase("-CFV")) {
				bCreateFolds = true;
				try {
					iNumFolds = Integer.valueOf(args[i + 1]).intValue();
					i++;
				} catch (Exception e) {
					if (iNumFolds == -1) {
						System.out.println(
								"-CFV expects an integer number of folds.  Using default of 5.");
						iNumFolds = 5;
					}
				}
			} else if (args[i].equalsIgnoreCase("-RRV")) {
				bRRV = true;
				try {
					rrvPercent = Integer.valueOf(args[i + 1]).intValue();
					i++;
				} catch (Exception e) {
					if (rrvPercent == -1) {
						System.out.println(
								"-RRV expects an integer number percent.  Using default of 30");
						rrvPercent = 30;
					}
				}
			} else if (args[i].equalsIgnoreCase("-ITST")) {
				bSplitData = true;
				try {
					iPercentSplit = Integer.valueOf(args[i + 1]).intValue();
					i++;
				} catch (Exception e) {
					if (iPercentSplit == -1) {
						System.out.println(
								"-ITST expects an integer percent.  Using default of 30");
						iPercentSplit = 30;
					}
				}
			} else if (args[i].equalsIgnoreCase("-av")) {
				try {
					iAvStep = Integer.valueOf(args[i + 1]).intValue();
					i++;
				} catch (Exception e) {
					System.out.println("Expected a number representing how many instances to average");
				}
			} else if (args[i].equalsIgnoreCase("-CTR")) {
				combineTechReps = true;
				if ((i+1 < args.length) ) {
					try {
						iRepAtt = Integer.valueOf(args[i +1]).intValue();
						i++;
					} catch (NumberFormatException nfe) {
						//System.out.println("Expected an integer representing the index of the sample ID column");
					}
				}
			}
		}
	}

	public int[] getScaleParams() {
		return scaleParams;
	}

	public boolean shouldScale() {
		return bScale;
	}

	public boolean shouldDiscretize() {
		return bDiscretize;
	}

	public int getDiscMethod() {
		return iDiscMethod;
	}

	public double getDiscMethodVal() {
		return nDiscMethodValue;
	}

	public boolean shouldChiSqTrim() {
		return bChiSqTrim;
	}

	public double getChiSqTrimThreshold() {
		return nChiSqLevel;
	}

	public boolean shouldRemoveAttribute() {
		return 
			bDiscretize ? bRemoveAttr 
					: false;
	}

	public boolean shouldBuildHierarchies() {
		return bBuildHierarchs;
	}

	public boolean shouldEqualizeDatasets() {
		return bEqualizeDataset;
	}

	public boolean shouldWriteInterData() {
		return bSaveIntermedFiles;
	}

	/**
	 * @return the bCreateFolds
	 */
	public boolean shouldCreateFolds() {
		return bCreateFolds;
	}

	/**
	 * @param createFolds
	 *            the bCreateFolds to set
	 */
	public void setShouldCreateFolds(boolean createFolds) {
		bCreateFolds = createFolds;
		bSplitData = false;
	}

	/**
	 * @return the bSplitData
	 */
	public boolean shouldSplitData() {
		return bSplitData;
	}

	/**
	 * @param splitData
	 *            the bSplitData to set
	 */
	public void setShouldSplitData(boolean splitData) {
		bSplitData = splitData;
		bCreateFolds = false;
	}

	/**
	 * @return the iNumFolds
	 */
	public int getNumFolds() {
		return iNumFolds;
	}

	/**
	 * @param numFolds
	 *            the iNumFolds to set
	 */
	public void setNumFolds(int numFolds) {
		iNumFolds = numFolds;
	}

	/**
	 * @return the iPercentSplit
	 */
	public int getPercentSplit() {
		return iPercentSplit;
	}

	/**
	 * @param percentSplit
	 *            the iPercentSplit to set
	 */
	public void setPercentSplit(int percentSplit) {
		iPercentSplit = percentSplit;
	}

	/**
	 * @return the bRRV
	 */
	public boolean shouldDoRRV() {
		return bRRV;
	}

	/**
	 * @param brrv
	 *            the bRRV to set
	 */
	public void setShouldRRV(boolean brrv) {
		bRRV = brrv;
	}

	/**
	 * @return the rrvPercent
	 */
	public int getRRVPercent() {
		return rrvPercent;
	}

	/**
	 * @param rrvPercent
	 *            the rrvPercent to set
	 */
	public void setRRVPercent(int rrvPercent) {
		bRRV = true;
		if (rrvPercent < 1)
			this.rrvPercent = 30;
		else
			this.rrvPercent = rrvPercent;
	}

	public boolean shouldCombineTechReplicates() {
		return combineTechReps;
	}
	
	public void setShouldCombineTechReplicates(boolean b) {
		combineTechReps = b;
	}

	public int getReplicateAttIndex() {
		return iRepAtt;
	}
	
	//public void setReplicateColumnIndex(int iRepCol) {
	//	iRepColumn = iRepCol;
	//}
}