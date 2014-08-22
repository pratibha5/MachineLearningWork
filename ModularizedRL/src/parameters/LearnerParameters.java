package parameters;

import data.discretize.Discretizers;
import data.discretize.supervised.EBD;
import structures.cf.CertaintyFactor;
import structures.cr.ConflictResolver;
import data.dataset.Dataset;
import rule.RuleList; 

/**
 * TODO: remove the trivial getter and setter methods and replace their uses
 * with public fields. --PG2009 
 */

public class LearnerParameters implements Cloneable {
	public static final int RULE_GENERATOR_CLASSIC = 0;
	public static final int RULE_GENERATOR_BAYES_CLOBAL = 1;
	public static final int RULE_GENERATOR_BAYES_LOCAL_STRUCTURE = 2;
	public static final int RULE_GENERATOR_MARKOV_BLANKET_BAYES_GLOBAL = 3;

	// Data and validation parameters
	private boolean doCrossVal = false; // -CV no
	private int numFolds = 10; // -CV #
	private boolean genTestSet = false; // -genTST [n]
	private double percentTestSet = 30; // -genTST #
	public Dataset trainData, testData, sourceData;
	public long cvRandSeed = 1;

	// Running parameters
	private boolean saveCvRules = true; // -svCVR
	public boolean saveCvSets = false; // -svCVS
	private int discMethodIx = Discretizers.EBD;	
	private double discMethodValue = EBD.PRIOR_STRUCTURE;

	// Learner parameters
	private int ruleGenMethod = RULE_GENERATOR_CLASSIC; // -rgm #
	private int ruleGenMethodValue = 0; // -rgm # #
	private boolean specializeGoodRules; // -specialize
	private int minConj = 1; // -minConj #
	private int maxConj = 5; // -maxConj #
	private int cfFunc = 1; // -cfType #
	private double minCF = 0.85; // -minCf #
	private double minCoverage = 4; // PG20090810; // -minCov #		// = 4 by default in RL2005
	private double minTP = 0.05;//1; // -posCov #			// = 0.05 by default in RL2005
	private double maxFP = 0.1;// = 0.999999; // -negCov #
	//private boolean isMaxFPSet;		// Not set by default in RL2005
	private int indStrengthening = 1; // -indStr #
	private int beamWidth = 1000; // -bw #

	// Prior rule and rule transfer parameters
	//private string priorRuleFile;
	public RuleList priorRules;
	private String priorRuleFile; // -priorRule ""
	// Source data for learning prior rules is currently specified as a data parameter
	// TODO: make it a learning parameter
	private int priorBSSFolds = 10;
	private boolean shouldSpecializePriorRules = true; // -noSpecializePrior
	private boolean shouldIndStrPriorRules = true; // -noCoverPrior
	private int transferType = 1; // -transferType #
	public static final int TRANSFER_RULES_UPDATE = 0;
	public static final int TRANSFER_RULES_SEARCH = 1;
	public static final int TRANSFER_RULE_STRUCTURE = 2;

	// Bias space search parameters
	private boolean doBss = false; // -BSS
	private int bssNumFolds = 5; // --F #
	private int bssDepth = 0; // --D #
	private double bssCfInc = 0.20; // -cfInc #

	// Inference parameters
	private int inferenceType = 0; // -inftyp #
	private boolean blsDg = false;

	public int verbosity; // -debug
	
	public LearnerParameters() {
	}

	public LearnerParameters(String[] options) throws Exception {
		for (int i = 0; i < options.length; i++) {
			if (options[i].equalsIgnoreCase("-DG"))
				blsDg = true;
			else if (options[i].equalsIgnoreCase("-bss")) {
				// Bias space search
				doBss = true;
				while ((i + 1 < options.length) && (options[i + 1].indexOf("--") > -1)) {
					if (options[i + 1].equalsIgnoreCase("--D")
							|| options[i + 1].equalsIgnoreCase("--depth")) {
						// Bias space search depth
						i++;
						try {
							bssDepth = Integer.parseInt(options[i + 1]);
							if (bssDepth < 0 || bssDepth > 3) {
								throw new IncorrectParameterException(options[i], options[i+1], "Integer between 0 (shallowest search) and 3 (deepest search)");
							}
							i++;
						} catch (NumberFormatException nFf) {
							throw new IncorrectParameterException(options[i], options[i+1], "Integer between 2 and the number of training examples");
						}
					} else if (options[i + 1].equalsIgnoreCase("--F")) {
						// Bias space search number of folds
						i++;
							try {
								bssNumFolds = Integer.parseInt(options[i + 1]);
								i++;
							} catch (NumberFormatException nFf) {
								throw new IncorrectParameterException(options[i], options[i+1], "Integer between 2 and the number of training examples");
							}
					}
				}
				switch (bssDepth) {
				case 2:
					beamWidth = 5000;
					bssCfInc = 5;
					//maxConj = 5;
				case 1:
					beamWidth = 2500;
					bssCfInc = 10;
					//maxConj = 5;
					break;
				default:
					bssDepth = 0;
					beamWidth = 1000;
					bssCfInc = 20;
					//maxConj = 5;
				}
				if (ruleGenMethod >= 1)
					maxConj = 7;
			} else if (options[i].equalsIgnoreCase("-inccf")
					|| options[i].equalsIgnoreCase("-cfinc")) {
				if ((i+1 < options.length) && (options[i + 1].indexOf("-") < 0)) {
					try {
						bssCfInc = Double.parseDouble(options[i + 1]);
					} catch (NumberFormatException nfe) {
						//System.err.println("Bad certainty factor increment value specified: " + options[i + 1]);
						//bssCfInc = 20;
						throw new IncorrectParameterException(options[i], options[i+1], "Fraction between 0 and 1");
					}
					i++;
				} else {
					//System.err.println("Not needed. Please specify parameter or remove from the command line");
					throw new IncorrectParameterException(options[i], "", "Fraction between 0 and 1");
				}
			} else if (options[i].equalsIgnoreCase("-cfv")
					|| options[i].equalsIgnoreCase("-cv")) {
				// Cross-validation number of folds
				doCrossVal = true;
				if (i+1 < options.length) {	// There are more parameters
					try {
						numFolds = Integer.parseInt(options[i + 1]);
						i++;
					} catch (NumberFormatException nfe) {
						throw new IncorrectParameterException("-cv", options[i+1], "integer between 1 and the number of training examples");
					}
				} else {
					throw new IncorrectParameterException("-cv", "", "Integer between 1 and the number of training examples");
				}
			} else if (options[i].equalsIgnoreCase("-genTst")
					|| options[i].equalsIgnoreCase("-genTest")) {
				throw new Exception("Cannot generate test set! This fucntionality is not implemented");
			} else if (options[i].equalsIgnoreCase("-rand")) {
				if ((i+1 < options.length) && (!options[i+1].startsWith("-"))) {
						cvRandSeed = Long.parseLong(options[i + 1]);
						i++;
				} else {
					// No seed was specified; use a random number as the seed.
					cvRandSeed = (new java.util.Random()).nextInt();
				}
			} else if (options[i].equalsIgnoreCase("-svCvR")) {
				// Save rules learned in each cross-validation fold?
				saveCvRules = true;
			} else if (options[i].equalsIgnoreCase("-svCvS")
					|| options[i].equalsIgnoreCase("-svCvD")) {
				saveCvSets = true;
			} else if (options[i].equalsIgnoreCase("-rgm")) {
				// Rule generation method
				if ((i+1 < options.length) && (options[i + 1].indexOf('-') < 0)) {
					try {
						int rgm = Integer.parseInt(options[i + 1]);
						switch (rgm) {
						case RULE_GENERATOR_CLASSIC:
						case RULE_GENERATOR_BAYES_CLOBAL:
						case RULE_GENERATOR_BAYES_LOCAL_STRUCTURE:
						case RULE_GENERATOR_MARKOV_BLANKET_BAYES_GLOBAL:
							ruleGenMethod = rgm;
							break;
						default:
							ruleGenMethod = LearnerParameters.RULE_GENERATOR_CLASSIC;
						}
						i++;
						if ((i+1 < options.length) && (options[i + 1].indexOf('-') < 0)) {
							ruleGenMethodValue = Integer.parseInt(options[i + 1]);
							initRuleLearningType();
						}
					} catch (NumberFormatException nfe) {
						throw new IncorrectParameterException(options[1], options[i+1], "integers 0 to 4");
					}

					if (ruleGenMethod >= LearnerParameters.RULE_GENERATOR_BAYES_CLOBAL)
						maxConj = 7;	// TODO: Why? --PG2009
				} else {
					throw new IncorrectParameterException(options[i], "", "integers 0 to 4");
				}
			} else if (options[i].equalsIgnoreCase("-bw")
					|| options[i].equalsIgnoreCase("-beam")) {
				// Beam width
				if ((i + 1 < options.length) && (options[i + 1].indexOf("-") < 0)) {
					try {
						beamWidth = Integer.parseInt(options[i + 1]);
					} catch (NumberFormatException nfe) {
						throw new IncorrectParameterException(options[i], options[i+1], "integer");
					}
					i++;
				} else {
					throw new IncorrectParameterException(options[i], "", "integer");					
				}
			} else if (options[i].equalsIgnoreCase("-mxcj")
					|| options[i].equalsIgnoreCase("-maxconj")) {
				// Max. conjuncts per rule
				if ((i + 1 < options.length) && (options[i + 1].indexOf("-") < 0)) {
					try {
						maxConj = Integer.parseInt(options[i + 1]);
						if (maxConj < 1 || maxConj > 40)
							//throw new NumberOutOfRangeException(maxConj, 1, 40);
							throw new IncorrectParameterException(options[i], options[i+1], "integer between 1 and 40");
					} catch (NumberFormatException nfe) {
					  	throw new IncorrectParameterException(options[i], options[i+1], "integer between 1 and 40");
					}
					i++;
				} else {
					//System.err.println("Not needed. Please specify parameter or remove from the command line");
					throw new IncorrectParameterException(options[i], "", "integer between 1 and 40");	
				}
			} else if (options[i].equalsIgnoreCase("-mncj")
					|| options[i].equalsIgnoreCase("-minconj")) {
				// Min conjuncts per rule
				if ((i + 1 < options.length) && (options[i + 1].indexOf("-") < 0)) {
					try {
						minConj = Integer.parseInt(options[i + 1]);
						if (minConj < 1)
							//throw new NumberOutOfRangeException(minConj, 1, maxConj);
							throw new IncorrectParameterException(options[i], options[i+1], "integer >= 1");	
					} catch (NumberFormatException nfe) {
						throw new IncorrectParameterException(options[i], options[i+1], "integer >= 1");	
					}
					i++;
				} else {
					//System.err.println("Not needed. Please specify parameter or remove from the command line");
					throw new IncorrectParameterException(options[i], "", "integer >= 1");	
				}
			} else if (options[i].equalsIgnoreCase("-cffunc")
					|| options[i].equalsIgnoreCase("-typcf")
					|| options[i].equalsIgnoreCase("-cftype")) {
				// Certainty factor function
				if ((i + 1 < options.length) && (options[i + 1].indexOf("-") < 0)) {
					try {
						cfFunc = Integer.parseInt(options[i + 1]);
						if (cfFunc < 0
								|| cfFunc > CertaintyFactor.getCfArray().length - 1)
							//throw new NumberOutOfRangeException(
							//		cfFunc, 0, CertaintyFactor.getCfArray().length - 1);
							throw new IncorrectParameterException(options[i], options[i+1], 
									"integer between 0 and " + (CertaintyFactor.getCfArray().length - 1));
					} catch (NumberFormatException nfe) {
						throw new IncorrectParameterException(options[i], options[i+1], 
								"integer between 0 and " + (CertaintyFactor.getCfArray().length - 1));
					  }
					i++;
				} else {
					//System.err.println("Not needed. Please specify parameter or remove from the command line");
					throw new IncorrectParameterException(options[i], "", 
							"integer between 0 and " + (CertaintyFactor.getCfArray().length - 1));
				}
			} else if (options[i].equalsIgnoreCase("-mncf")
					|| options[i].equalsIgnoreCase("-mincf")
					|| options[i].equalsIgnoreCase("-cfval")) {
				// Min certainty factor value
				if ((i + 1 < options.length) && (options[i + 1].indexOf("-") < 0)) {
					try {
						minCF = Double.parseDouble(options[i + 1]);
					} catch (NumberFormatException e) {
						//System.err.println("Bad minimum CF specified: " + options[i + 1]);
						//System.err.println("Using default of 0.85");
						//minCF = 0.85;
						throw new IncorrectParameterException(options[i], options[i+1], 
								"number between 0 and 1 or integer between 0 and number of training instances");
					}
					i++;
				} else {
					//System.err.println("Not needed. Please specify parameter or remove from the command line");
					throw new IncorrectParameterException(options[i], "", 
					"number between 0 and 1 or integer between 0 and number of training instances");
				}
			} else if (options[i].equalsIgnoreCase("-fp")
					|| options[i].equalsIgnoreCase("-maxfp")) {
				// Max negative coverage
				if ((i + 1 < options.length)) {
					try {
						maxFP = Double.parseDouble(options[i + 1]);
						//isMaxFPSet = true;
					} catch (NumberFormatException e) {
						throw new IncorrectParameterException(options[i], options[i+1], 
						"number between 0 and 1 or integer between 0 and number of training instances");
					}
					i++;
				} else {
					//System.err.println("Not needed. Please specify parameter or remove from the command line");
					throw new IncorrectParameterException(options[i], "", 
					"number between 0 and 1 or integer between 0 and number of training instances");
				}
			} else if (options[i].equalsIgnoreCase("-tp") 
					|| options[i].equalsIgnoreCase("-mintp")) {
				// Min positive coverage
				if ((i + 1 < options.length) && (options[i + 1].indexOf("-") < 0)) {
					try {
						minTP = Double.parseDouble(options[i + 1]);
					} catch (NumberFormatException e) {
						//System.err.println("Bad min positve coverage specified: " + options[i + 1]);
						throw new IncorrectParameterException(options[i], options[i+1], 
						"number between 0 and 1 or integer between 0 and number of training instances");
					}
					i++;
				} else {
					//System.err.println("Not needed. Please specify parameter or remove from the command line");
					throw new IncorrectParameterException(options[i], "", 
					"number between 0 and 1 or integer between 0 and number of training instances");
				}
			} else if (options[i].equalsIgnoreCase("-mincov") 
					|| options[i].equalsIgnoreCase("-cov") 
					|| options[i].equalsIgnoreCase("-cover")) {
				// Min coverage
				if ((i + 1 < options.length) && (options[i + 1].indexOf("-") < 0)) {
					try {
						minCoverage = Double.parseDouble(options[i + 1]);
					} catch (NumberFormatException e) {
						//System.err.println("Bad minimum coverage parameter specified: " + options[i + 1]);
						throw new IncorrectParameterException(options[i], options[i+1], 
						"number between 0 and 1 or integer between 0 and number of training instances");
					}
					i++;
				} else {
					//System.err.println("Not needed. Please specify parameter or remove from the command line");
					throw new IncorrectParameterException(options[i], "", 
					"number between 0 and 1 or integer between 0 and number of training instances");
				}
			} else if (options[i].equalsIgnoreCase("-indstr")) {
				// Inductive strengthening
				if ((i + 1 < options.length) && (options[i + 1].indexOf("-") < 0)) {
					try {
						indStrengthening = Integer.parseInt(options[i + 1]);
						if (indStrengthening < 1)
							throw new IncorrectParameterException(options[i], options[i+1], 
							"integer between 0 and number of training instances");
					} catch (NumberFormatException nfe) {
						throw new IncorrectParameterException(options[i], options[i+1], 
						"integer between 0 and number of training instances");
					}
					i++;
				} else {
					//System.err.println("Not needed. Please specify parameter or remove from the command line");
					throw new IncorrectParameterException(options[i], "", 
					"integer between 0 and number of training instances");
				}
			} else if (options[i].equalsIgnoreCase("-priorrule")) {
				System.err.println("Cannot parse prior rules. This functionality is not implemented. Continuing...");
			} else if (options[i].equalsIgnoreCase("-priorBSSFolds")) {
				try {
					priorBSSFolds = Integer.parseInt(options[i + 1]);
					if (verbosity > 1) {
						System.out.println("Set BSS cross-val parameter for learning prior rules to " + priorBSSFolds);
					}
					if (priorBSSFolds < 1)
						//throw new NumberOutOfRangeException(priorBSSFolds, 1, 2);
						throw new IncorrectParameterException(options[i], options[i+1], 
						"integer between 0 and number of source data instances");
				} catch (NumberFormatException nfe) {
					throw new IncorrectParameterException(options[i], options[i+1], 
					"integer between 0 and number of source data instances");					
				}
			} else if (options[i].equalsIgnoreCase("-nopriorrulesspecialize")
					|| options[i].equalsIgnoreCase("-nospecializeprior")) {
				shouldSpecializePriorRules = false;
			} else if (options[i].equalsIgnoreCase("-nocoverprior")
					|| options[i].equalsIgnoreCase("-nc")) {
				shouldIndStrPriorRules = false;
			} else if (options[i].equalsIgnoreCase("-transferType")
					|| options[i].equalsIgnoreCase("-transType")
					|| options[i].equalsIgnoreCase("-transfer")
					|| options[i].equalsIgnoreCase("-tr")) {
				if ((i + 1 < options.length) && (options[i + 1].indexOf("-") < 0)) {
					try {
						transferType = Integer.parseInt(options[i + 1]);
						if (transferType < 1 || transferType > 2)
							throw new NumberOutOfRangeException(transferType, 1, 2);
					} catch (NumberFormatException nfe) {
						System.err.println("Bad tranfer type parameter specified: " + options[i + 1]);
						System.err.println("Using default of " + transferType);
					} catch (NumberOutOfRangeException e) {
						System.err.println(e.getLocalizedMessage());
						System.err.println("Using default of 1 (whole rules)");
						transferType = 1;
					}
				} else {
					//System.err.println("Specify an integer after '" + options[i]
					//	+ "' to indicate the type of transfer for prior rules");
					throw new IncorrectParameterException(options[i], "", 
					"integer between 0 and number of source data instances");					
				}
			} else if (options[i].equalsIgnoreCase("-inffunc") 
					|| options[i].equalsIgnoreCase("-inftype")
					|| options[i].equalsIgnoreCase("-infer") 
					|| options[i].equalsIgnoreCase("-inference")
					|| options[i].equalsIgnoreCase("-confres")) {
				// Evidence gathering (conflict resolution or inference) function
				if ((i + 1 < options.length) && (options[i + 1].indexOf("-") < 0)) {
					try {
						inferenceType = Integer.parseInt(options[i + 1]);
						if (inferenceType < 0 || inferenceType > ConflictResolver.getCRArray().length - 1)
							//throw new NumberOutOfRangeException(
							//		inferenceType, 0, ConflictResolver.getCRArray().length - 1);
							throw new IncorrectParameterException(options[i], options[i+1], 
							"integer between 0 and " + (ConflictResolver.getCRArray().length - 1));
					} catch (NumberFormatException nfe) {
					//	System.err.println("Bad conflict resolution parameter specified: " + options[i + 1]);
					//	System.err.println("Using default of "
					//			+ ConflictResolver.getCRArray()[inferenceType].toString()
					//			+ " (index " + inferenceType + ").");
						throw new IncorrectParameterException(options[i], options[i+1], 
								"integer between 0 and " + (ConflictResolver.getCRArray().length - 1));						
					//} catch (NumberOutOfRangeException e) {
					//	System.err.println(e.getLocalizedMessage());
					//	System.err.println("Using default of 0 ("
					//			+ ConflictResolver.getCRArray()[0].toString() 	+ ").");
					//	inferenceType = 0;
					}
					i++;
				} else {
					//System.err.println("Specify an integer after '" + options[i]
					//	+ "' to indicate the type of inference function");
					throw new IncorrectParameterException(options[i], options[i+1], 
							"integer between 0 and " + (ConflictResolver.getCRArray().length - 1));
				}
			} else if (options[i].equalsIgnoreCase("-d")
					|| options[i].equalsIgnoreCase("-disc")) {
				// Discretization method
				if ((i + 1 < options.length) && (options[i + 1].indexOf("-") < 0)) {
					try {
						discMethodIx = Integer.parseInt(options[i + 1]);
						if (discMethodIx < 0
								|| discMethodIx > Discretizers.availableMethods().length - 1)
							//throw new NumberOutOfRangeException(
							//		discMethodIx, 0, Discretizers.availableMethods().length - 1);
							throw new IncorrectParameterException(options[i], options[i+1], 
									"integer between 0 and " + (Discretizers.availableMethods().length - 1));
					} catch (NumberFormatException nfe) {
					//	System.err.println("Bad discretization parameter specified: " + options[i + 1]);
					//	System.err.println("Using default of EBD (index: " + Discretizers.EBD + ").");
					//	discMethodIx = Discretizers.EBD;
						throw new IncorrectParameterException(options[i], options[i+1], 
								"integer between 0 and " + (Discretizers.availableMethods().length - 1));
					//} catch (NumberOutOfRangeException e) {
					//	System.err.println(e.getLocalizedMessage());
					//	System.err.println("Using default of EBD (index: " + Discretizers.EBD + ").");
					//	discMethodIx = Discretizers.EBD;
					}
					i++;
					if ((i + 1 < options.length) && (options[i + 1].indexOf("-") < 0)) {
						// Discretization parameter
						try {
							discMethodValue = Double.parseDouble(options[i + 1]);
						} catch (NumberFormatException e) {
							System.err.println("Bad discretization value parameter specified: " + options[i + 1]);
							switch (discMethodIx) {
							case Discretizers.EBD:
							case Discretizers.MODL:
								discMethodValue = 1;
								break;
							case Discretizers.EqualFreqU:
							case Discretizers.EqualWidthU:
							case Discretizers.FayaadMDLS:
								discMethodValue = 5;
								break;
							case Discretizers.ErrorBasedS:
								discMethodValue = .33;
								break;
							}
							System.err.println("Using discretization method value "
									+ discMethodValue
									+ " for the discretization method "
									+ Discretizers.getMethodName(discMethodIx));
						}
						i++;
					} else {
						System.err.println("Need parameter specifying the value prior method, or number of bins, etc.");
						//System.exit(1);
						throw new IncorrectParameterException(options[i], options[i+1], 
								"integer between 0 and " + (Discretizers.availableMethods().length - 1));
					}
				} else {
					//System.err.println("Not needed. Please specify parameter or remove from the command line");
					throw new IncorrectParameterException(options[i], options[i+1], 
							"integer between 0 and " + (Discretizers.availableMethods().length - 1));
				}
			} else if (options[i].equalsIgnoreCase("-specialize")
					|| options[i].equalsIgnoreCase("-specializeGoodRules")) {
				specializeGoodRules = true;
			} else if (options[i].equalsIgnoreCase("-v")) {
				if ((i + 1 < options.length) && (options[i + 1].indexOf("-") < 0)) {
					// Discretization parameter
					try {
						verbosity = Integer.parseInt(options[i + 1]);
					} catch (NumberFormatException nfe) {
						//System.out.println("Expected an integer specifying verbosity, between 0 and 2");
						throw new IncorrectParameterException(options[i], options[i+1], 
								"integer between 0 and 2");
					}
					i++;
				}
			}
			if (verbosity > 1)
				System.out.println("Learner parameters:\n" + toString() + "\n");
		}
	}

	private void initRuleLearningType() {
		int val = 0;
		switch (getRuleGeneratorType()) {
		case RULE_GENERATOR_BAYES_CLOBAL:
			val = getRuleGeneratorValue();
			if (val > 2)
				setRuleGenValue(val - 3);
			break;
		case RULE_GENERATOR_BAYES_LOCAL_STRUCTURE:
			val = getRuleGeneratorValue();
			if (val > 2) {
				setDoBlsdg(true);
				setRuleGenValue(val - 3);
			} else {
				setDoBlsdg(false);
				setRuleGenValue(val);
			}
			break;
		default:
			setRuleGenValue(val);
		}
	}
	
	public int getRuleGeneratorType() {
		return ruleGenMethod;
	}

	public static String[] getRuleGenNames() {
		String[] currMeths = new String[2];
		currMeths[0] = "Original RL Rule Generation";
		currMeths[1] = "Bayesian Global Rule Generation";
		return currMeths;
	}

	public int inferenceType() {
		return inferenceType;
	}

	public Object clone() {
		LearnerParameters cln = new LearnerParameters();
		
		// Running parameters
		cln.testData = testData;
		cln.trainData = trainData;
		cln.sourceData = sourceData;
		cln.discMethodIx = discMethodIx;
		cln.discMethodValue = discMethodValue;

		cln.doCrossVal = doCrossVal; // -cfv
		cln.numFolds = numFolds; // -cfv #
		cln.genTestSet = genTestSet; // -genTST
		cln.percentTestSet = percentTestSet; // -genTST #
		cln.saveCvRules = saveCvRules; // -svCVR
		cln.saveCvSets = saveCvSets; // -svCVS

		cln.doBss = doBss; // -bss
		cln.bssNumFolds = bssNumFolds; // --F #
		cln.bssDepth = bssDepth; // --D #

		// Learner parameters
		cln.ruleGenMethod = ruleGenMethod; // -rgm #
		cln.ruleGenMethodValue = ruleGenMethodValue;
		cln.specializeGoodRules = specializeGoodRules; // -noprune TURNS IT OFF
		cln.beamWidth = beamWidth; // -bw #
		cln.maxConj = maxConj; // -mxcj #
		cln.minConj = minConj; // mncj #
		cln.minCF = minCF; // -mncf #
		cln.maxFP = maxFP; // -negcov #
		//cln.isMaxFPSet = isMaxFPSet;
		cln.minTP = minTP; // -poscov #
		cln.minCoverage = minCoverage; // -mncov #
		cln.indStrengthening = indStrengthening; // -indstr #
		cln.cfFunc = cfFunc; // -typcf #
		cln.bssCfInc = bssCfInc; // -inccf #
		// TODO: Why do we have to clone the rules themselves???
		cln.priorRules = (priorRules == null ? null : new RuleList(priorRules.toArrayList()));
		cln.priorRuleFile = priorRuleFile; // -priorRule ""

		// Inference parameters
		cln.inferenceType = inferenceType; // -inftyp #
		cln.blsDg =blsDg;
		
		// Transfer parameters
		cln.transferType = transferType;
		cln.shouldSpecializePriorRules = shouldSpecializePriorRules;
		cln.shouldIndStrPriorRules = shouldIndStrPriorRules;
		cln.verbosity = verbosity;
		return cln;
	}

	public double getBssCfInc() {
		return bssCfInc;
	}

	public void setMinCf(double mnCF) {
		minCF = mnCF;
	}

	public void setCfMethod(int cf) {
		cfFunc = cf;
	}

	public int getDiscretizerIndex() {
		return discMethodIx;
	}

	public double getDiscretizerValue() {
		return discMethodValue;
	}

	public int getNumFolds() {
		return numFolds;
	}

	public double getPctTestSet() {
		return percentTestSet;
	}

	public boolean shouldGenTestSet() {
		return genTestSet;
	}

	public boolean shouldDoCv() {
		return doCrossVal;
	}

	public int getBeamWidth() {
		return beamWidth;
	}

	public int getInductiveStrengthening() {
		return indStrengthening;
	}

	public boolean shouldSpecializeGoodRules() {
		return specializeGoodRules;
	}

	public int getTransferType() {
		return transferType;
	}

	public boolean shouldSpecializePriorRules() {
		return shouldSpecializePriorRules;
	}
	
	public boolean shouldIndStrPriorRules() {
		return shouldIndStrPriorRules;
	}
	
	public int getCfMethod() {
		return cfFunc;
	}

	public boolean hasPriorRules() {
		return (priorRules != null && priorRules.size() > 0);
	}

	public RuleList getPriorRules() {
		return priorRules;
	}

	public void setPriorRules(RuleList rules) {
		priorRules = rules;
	}

	public int getPriorBSSNumFolds() {
		return priorBSSFolds;
	}
	
	public int getMinConjuncts() {
		return minConj;
	}

	public int getMaxConjuncts() {
		return maxConj;
	}

	public double getMinCf() {
		return minCF;
	}

	public double getMaxFP() {
		return maxFP;
	}

	public double getMinCoverage() {
		return minCoverage;
	}

	public double getMinTP() {
		return minTP;
	}

	public void setSpecializeGoodRules(boolean b) {
		specializeGoodRules = b;
	}

	public void setMaxFP(double d) {
		maxFP = d;
	}

	public boolean shouldDoBss() {
		return doBss;
	}
	
	public void setDoBss(boolean b) {
		doBss = b;
	}
	
	/**
	 * @return The number of folds to use inside bias space search 
	 */
	public int getNumBssFolds() {
		return bssNumFolds;
	}

	public void setNumBssFolds(int n) {
		bssNumFolds = n;
	}
	
	public void setBssDepth(int i) {
		bssDepth = i;
		switch (bssDepth) {
		case 0:
			beamWidth = 1000;
			bssCfInc = 0.2;
			maxConj = 3;
			break;
		case 1:
			beamWidth = 2500;
			bssCfInc = 0.10;
			maxConj = 4;
			break;
		case 2:
			beamWidth = 5000;
			bssCfInc = 0.05;
			maxConj = 5;
		default:
			bssDepth = 0;
			beamWidth = 1000;
			bssCfInc = 0.20;
			maxConj = 3;
		}
	}

	public int getBssDepth() {
		return bssDepth;
	}

	public void setRuleGenMethod(int rgm) {
		ruleGenMethod = rgm;
	}

	public void setDiscMethod(int parseInt) {
		discMethodIx = parseInt;
	}

	public void setDiscMethodValue(double parseDouble) {
		discMethodValue = parseDouble;
	}

	public boolean shouldSaveCvRules() {
		return saveCvRules;
	}

	public int getRuleGeneratorValue() {
		return ruleGenMethodValue;
	}

	public void setRuleGenValue(int i) {
		ruleGenMethodValue = i;

	}

	public void setMaxConjuncts(int i) {
		maxConj = i;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		if (trainData != null) {
			buf.append("Training data: " + trainData.getFileName() + "\n");
		}
		buf.append("Discretization method: "
				+ Discretizers.getMethodName(discMethodIx, discMethodValue) + "\n");
		buf.append("Learning method: " +getLearningMethod() + "\n");
		if (doBss) {
			buf.append("Bias space search: depth " + bssDepth);
			buf.append(", internal folds " + bssNumFolds);
			buf.append(", CF increment " + bssCfInc + "\n");
		}
		buf.append("CF function: "
				+ CertaintyFactor.getCfArray()[cfFunc]+ " (" + cfFunc + ")\n");
		buf.append("Min CF: " + minCF + "\n");
		buf.append("Min conjuncts: " + minConj + "\n");
		buf.append("Max conjuncts: " + maxConj + "\n");
		buf.append("Min coverage: " + minCoverage + "\n");
		//buf.append("Max FP coverage: " + (isMaxFPSet ? maxFP : "--") + "\n");
		buf.append("Max FP coverage: " + (maxFP >=0 ? maxFP : "--") + "\n");
		buf.append("Min TP coverage: " + minTP + "\n");
		buf.append("Inductive strengthening: " + indStrengthening + "\n");
		buf.append("Inference type: "
				+ ConflictResolver.getCRArray()[inferenceType].toString() 
				+ " (" + inferenceType + ")\n");
		buf.append("Beam width: " + beamWidth + "\n");
		buf.append("Specialize satisfactory rules: " + specializeGoodRules + "\n");
		buf.append("Validation method: ");
		if (doCrossVal) {
			buf.append(numFolds + "-fold cross-validation with seed " + cvRandSeed + " \n");
		} else if (genTestSet) {
			buf.append("test set resampled " + percentTestSet + "%\n");
		} else if (testData != null) {
			buf.append("test set " + testData.getFileName() + "\n");
		} else {
			buf.append("training set\n");
		}
		if (verbosity > 0) {
			buf.append("Verbosity: " + verbosity + "\n");
		}
		if (priorRuleFile != null) {
			buf.append("Transfer of prior rules:\n");
			buf.append("    Prior rule file: " + priorRuleFile + "\n");
			buf.append("    Transfer type: " + transferType + "\n");
			buf.append("    Specialize prior rules: " + shouldSpecializePriorRules + "\n");
			buf.append("    Ind. trengthening for prior rules: " + shouldIndStrPriorRules + "\n");			
		} else if (sourceData != null) {
			buf.append("Transfer of prior rules:\n");
			buf.append("    Source data: " + sourceData.getFileName() + "\n");
			buf.append("    Transfer type: " + transferType + "\n");
			buf.append("    Specialize prior rules: " + shouldSpecializePriorRules + "\n");
			buf.append("    Ind. trengthening for prior rules: " + shouldIndStrPriorRules + "\n");			
		}
		return buf.toString();
	}

	public void setBeamWidth(int i) {
		beamWidth = i;
	}

	public boolean shouldDoBlsdg() {
		return blsDg;
	}

	public void setDoBlsdg(boolean blsDg) {
		this.blsDg = blsDg;
		System.out.println("I am in here!!!!!!!!!!!!!!!           "+blsDg);
	}

	public String getLearningMethod() {
		StringBuffer sb = new StringBuffer();
		switch (ruleGenMethod) {
		case RULE_GENERATOR_BAYES_CLOBAL:
			sb.append("Constrained Bayesian Global Network Rule Learner - ");
			break;
		case RULE_GENERATOR_BAYES_LOCAL_STRUCTURE:
			sb.append("Constrained Bayesian Global Network Rule Learner - ");
			if (blsDg)
				sb.append("Decision Graph - ");
			else
				sb.append("Decision Tree - ");
			break;
		case RULE_GENERATOR_CLASSIC:
			sb.append("Rule Learner");
			break;
		case RULE_GENERATOR_MARKOV_BLANKET_BAYES_GLOBAL:
			sb.append("Greedy Markov Blanket Global Network Rule Learner");
			break;
		default:

		}
		/*if (ruleGenMethod == RULE_GENERATOR_BAYES_CLOBAL) {
			sb.append(ProbRuleGenerator.getSearchMethod(ruleGenMethodValue));
		} else if (ruleGenMethod == RULE_GENERATOR_BAYES_LOCAL_STRUCTURE) {
			sb.append(BLSRuleGenerator.getSearchMethod(ruleGenMethodValue));
		}*/
		return sb.toString();
	}

	public void setNumFolds(int n) {
		this.numFolds = n;
		this.doCrossVal = true;
	}

	public void setInferenceType(int inferenceTyp) {
		this.inferenceType = inferenceTyp;
	}

	//public boolean isMaxFPSet() {
	//	return isMaxFPSet;
	//}
}
